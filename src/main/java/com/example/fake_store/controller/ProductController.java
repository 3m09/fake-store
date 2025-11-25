package com.example.fake_store.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.fake_store.DTO.ProductDTO;
import com.example.fake_store.response.ApiResponse;
import com.example.fake_store.service.ApiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ApiService apiService;

    @GetMapping("/all")
    ResponseEntity<ApiResponse> getAllProducts(@RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductDTO> allProducts = apiService.getAllProducts();
            allProducts = allProducts.stream().skip(offset).limit(limit).toList();
            return ResponseEntity.ok(new ApiResponse("Request for all products successful", allProducts));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while fetching products: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(new ApiResponse("Request for product by ID successful", apiService.getProductById(id)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while fetching the product: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse> deleteProductById(@PathVariable Long id) {
        try {
            boolean deleted = apiService.deleteProductById(id);
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse("Product deleted successfully", null));
            } else {
                return ResponseEntity.status(404).body(new ApiResponse("Product not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while deleting the product: " + e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    ResponseEntity<ApiResponse> addProduct(@RequestBody ProductDTO productDTO) {
        try {
            ProductDTO createdProduct = apiService.addProduct(productDTO);
            return ResponseEntity.ok(new ApiResponse("Product added successfully", createdProduct));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while adding the product: " + e.getMessage(), null));
        }
    }

    @PostMapping("/update/{id}")
    ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        try {
            ProductDTO updatedProduct = apiService.updateProduct(id, productDTO);
            return ResponseEntity.ok(new ApiResponse("Product updated successfully", updatedProduct));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while updating the product: " + e.getMessage(), null));
        }
    }

    @GetMapping("/filter")
    ResponseEntity<ApiResponse> filterByFields(@RequestParam String field, @RequestParam String value, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "10") int limit) {
        try {
            List<ProductDTO> allProducts = apiService.getAllProducts();
            List<ProductDTO> filteredProducts = allProducts.stream().filter(product -> {
                switch (field.toLowerCase()) {
                    case "title":
                        return product.getTitle().equalsIgnoreCase(value);
                    case "category":
                        return product.getCategory().equalsIgnoreCase(value);
                    case "price":
                        {
                            String[] priceRange = value.split("-");
                            if (priceRange.length == 2) {
                                double minPrice = Double.parseDouble(priceRange[0]);
                                double maxPrice = Double.parseDouble(priceRange[1]);
                                return product.getPrice() >= minPrice && product.getPrice() <= maxPrice;
                            } else {
                                double price = Double.parseDouble(value);
                                return product.getPrice() == price;
                            }
                        }
                    default:
                        return false;
                }
            }).toList();
            filteredProducts = filteredProducts.stream().skip(offset).limit(limit).toList();
            return ResponseEntity.ok(new ApiResponse("Filtering successful", filteredProducts));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(new ApiResponse("An error occurred while filtering products: " + e.getMessage(), null));
        }
    }
}