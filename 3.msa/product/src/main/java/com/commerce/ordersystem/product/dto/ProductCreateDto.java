package com.commerce.ordersystem.product.dto;

import com.commerce.ordersystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCreateDto {
    private String name;
    private Long price;
    private String category;
    private Integer stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(String email){
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .memberEmail(email)
                .stockQuantity(this.stockQuantity)
//                .imagePath(this.productImage)
                .build();
    }
}
