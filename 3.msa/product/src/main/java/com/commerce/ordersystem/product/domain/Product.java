package com.commerce.ordersystem.product.domain;

import com.commerce.ordersystem.common.domain.BaseTimeEntity;
import com.commerce.ordersystem.product.dto.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long price;
    private String category;
    @Column(nullable = false)
    private Integer stockQuantity;
    private String imagePath;

    private String memberEmail;

    public void updateProfileImageUrl(String imagePath){
        this.imagePath = imagePath;
    }

    public void updateStockQuantity(Integer orderQuantity){
        this.stockQuantity = this.stockQuantity - orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto){
        this.name = dto.getName();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
        this.price = dto.getPrice();
    }



}
