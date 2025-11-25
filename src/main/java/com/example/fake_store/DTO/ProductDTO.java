package com.example.fake_store.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    Long id;
    String title;
    double price;
    String description;
    String category;
    String image;
}
