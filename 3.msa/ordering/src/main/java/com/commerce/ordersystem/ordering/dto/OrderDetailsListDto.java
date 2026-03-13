package com.commerce.ordersystem.ordering.dto;

import com.commerce.ordersystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailsListDto {
    private Long detailId;
    private String productName;
    private Integer productCount;

    public static OrderDetailsListDto fromEntity(OrderingDetail orderingDetails){
        return OrderDetailsListDto.builder()
//                .detailId(orderingDetails.getProduct().getId())
//                .productName(orderingDetails.getProduct().getName())
                .productCount(orderingDetails.getQuantity())
                .build();
    }
}
