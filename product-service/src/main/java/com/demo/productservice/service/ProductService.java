package com.demo.productservice.service;

import com.demo.productservice.dto.ProductRequest;
import com.demo.productservice.dto.ProductResponse;
import com.demo.productservice.entity.Product;
import com.demo.productservice.exception.ResourceNotFoundException;
import com.demo.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }


    public Product getProductById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        return repository.save(product);
    }

    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return repository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        repository.delete(product);
    }
}
