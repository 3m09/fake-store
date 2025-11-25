package com.example.fake_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/products"})
    public String products() {
        return "products";
    }

    @GetMapping("/products/detail/{id}")
    public String productDetail() {
        return "product-detail";
    }

    @GetMapping("/products/add")
    public String addProduct() {
        return "add-product";
    }

    @GetMapping("/products/update/{id}")
    public String updateProduct() {
        return "update-product";
    }
}