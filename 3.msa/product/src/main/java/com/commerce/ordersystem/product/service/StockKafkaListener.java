package com.commerce.ordersystem.product.service;

import com.commerce.ordersystem.product.dto.ProductStockUpdateDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StockKafkaListener {
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public StockKafkaListener(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "stock-update-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message) throws JsonProcessingException {
        System.out.println("=====kafka listener start====");
        ProductStockUpdateDto dto = objectMapper.readValue(message, ProductStockUpdateDto.class);
        productService.updateStock(dto);
    }
}
