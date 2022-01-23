package com.suhakopan.springexamples.inventoryservice.controller;

import com.suhakopan.springexamples.inventoryservice.model.Inventory;
import com.suhakopan.springexamples.inventoryservice.repository.IInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryRestController {

    private final IInventoryRepository inventoryRepository;

    @GetMapping("/{skuCode}")
    Boolean isInStock(@PathVariable String skuCode) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Cannot Find Product by sku code " + skuCode));
        return inventory.getStock() > 0;
    }
}
