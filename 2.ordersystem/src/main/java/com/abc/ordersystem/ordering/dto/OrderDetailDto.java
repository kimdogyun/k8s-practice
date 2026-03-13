package com.abc.ordersystem.ordering.dto;

import com.abc.ordersystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailDto {
    private Long id;
    private String name;
    private String category;
    private Long price;
    private Integer stockQuantity;
    private String imagePath;

    public static OrderDetailDto fromEntity(OrderingDetail orderingDetail){
        return OrderDetailDto.builder()
                .id(orderingDetail.getId())
                .name(orderingDetail.getProduct().getName())
                .category(orderingDetail.getProduct().getCategory())
                .price(orderingDetail.getProduct().getPrice())
                .stockQuantity(orderingDetail.getProduct().getStockQuantity())
                .imagePath(orderingDetail.getProduct().getImagePath())
                .build();
    }
}
