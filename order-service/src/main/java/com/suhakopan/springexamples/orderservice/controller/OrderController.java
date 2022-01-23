package com.suhakopan.springexamples.orderservice.controller;

import com.suhakopan.springexamples.orderservice.client.InventoryClient;
import com.suhakopan.springexamples.orderservice.dto.OrderDto;
import com.suhakopan.springexamples.orderservice.model.Order;
import com.suhakopan.springexamples.orderservice.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/order")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final IOrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;
    private final StreamBridge streamBridge;
    private final ExecutorService traceableExecutorService;

    @PostMapping
    public String placeOrder(@RequestBody OrderDto orderDto) {
        circuitBreakerFactory.configureExecutorService(traceableExecutorService);
        Resilience4JCircuitBreaker circuitBreaker = circuitBreakerFactory.create("inventory");
        Supplier<Boolean> booleanSupplier = () -> orderDto.getOrderLineItemsList().stream()
                .allMatch(orderLineItem -> {
                    log.info("Making Call to Inventory Service for SkuCode {}", orderLineItem.getSkuCode());
                    return inventoryClient.checkStock(orderLineItem.getSkuCode()); });
        boolean productsInStock = circuitBreaker.run(booleanSupplier, throwable -> handleErrorCase());
        if (productsInStock) {
            Order order = new Order();
            order.setOrderLineItems(orderDto.getOrderLineItemsList());
            order.setOrderNumber(UUID.randomUUID().toString());
            orderRepository.save(order);
            log.info("Sending Order Details with Order Id {} to Notification Service", order.getId());
            streamBridge.send("notificationEventSupplier-out-0", MessageBuilder.withPayload(order.getId()).build());
            return "Order Place Successfully";
        } else {
            return "Order Failed, One of the products in the order is not in stock";
        }
    }

    private Boolean handleErrorCase() {
        return false;
    }
}
