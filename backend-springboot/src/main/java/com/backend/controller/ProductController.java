package com.backend.controller;

import com.backend.entity.Product;
import com.backend.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<Product> getProducts(){
        return this.productService.getAllProducts();
    }

    @PostMapping
    public void addProduct(@RequestBody Product product) {
        this.productService.addProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return productService.updateProduct(id, product);
    }

    @PatchMapping("/{id}/lock")
    public Product toggleLock(@PathVariable Long id) {
        return productService.toggleLock(id);
    }


}
