package com.suhakopan.springexamples.productservice.repository;

import com.suhakopan.springexamples.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IProductRepository extends MongoRepository<Product, String> {
}
