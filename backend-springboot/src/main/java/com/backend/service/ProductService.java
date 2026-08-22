package com.backend.service;

import com.backend.entity.Product;
import com.backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts()
    {
        return this.productRepository.findAll();
    }

    public void addProduct(Product product){
       this.productRepository.save(product);
    }

    public Product updateProduct(Long id, Product updatedProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (Boolean.TRUE.equals(product.getLocked())) {
            throw new RuntimeException("This product is locked.");
        }

        product.setName(updatedProduct.getName());
        product.setPrice(updatedProduct.getPrice());

        return productRepository.save(product);
    }

    public Product toggleLock(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setLocked(!product.getLocked());

        return productRepository.save(product);
    }






}
