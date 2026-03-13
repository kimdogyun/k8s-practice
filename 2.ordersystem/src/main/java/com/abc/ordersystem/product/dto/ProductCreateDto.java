package com.abc.ordersystem.product.dto;

import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.product.domain.Product;
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

    public Product toEntity(Member member){
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .member(member)
                .stockQuantity(this.stockQuantity)
//                .imagePath(this.productImage)
                .build();
    }
}
