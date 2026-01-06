package com.demo.productservice.controller;

import com.demo.productservice.dto.ProductRequest;
import com.demo.productservice.entity.Product;
import com.demo.productservice.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    // GET all products (Accessible to USER & ADMIN)
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(service.getAllProducts());
    }

    // GET product by id (Accessible to USER & ADMIN)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    // POST new product (Only ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.createProduct(request));
    }

    // PUT update product (Only ADMIN)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.updateProduct(id, request));
    }

    // DELETE product (Only ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        service.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}
