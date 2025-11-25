package com.example.fake_store.service;
import com.example.fake_store.DTO.ProductDTO;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String EXTERNAL_API_URL = "https://fakestoreapi.com/products";

    public List<ProductDTO> getAllProducts() {
        ProductDTO[] response = restTemplate.getForObject(
                EXTERNAL_API_URL, 
                ProductDTO[].class
        );

        return Arrays.asList(response);
    }

    public ProductDTO getProductById(Long id) {
        String url = EXTERNAL_API_URL + "/" + id;
        return restTemplate.getForObject(url, ProductDTO.class);
    }

    public boolean deleteProductById(Long id) {
        String url = EXTERNAL_API_URL + "/" + id;
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE,  null, Void.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    public ProductDTO addProduct(ProductDTO productDTO) {
        return restTemplate.postForObject(EXTERNAL_API_URL, productDTO, ProductDTO.class);
    }

    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        String url = EXTERNAL_API_URL + "/" + id;
        return restTemplate.postForObject(url, productDTO, ProductDTO.class);
    }

}
