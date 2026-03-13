package com.commerce.ordersystem.ordering.dto;

import com.commerce.ordersystem.ordering.domain.OrderStatus;
import com.commerce.ordersystem.ordering.domain.Ordering;
import com.commerce.ordersystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderListDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailsListDto> orderDetails;

    public static OrderListDto fromEntity(Ordering ordering){
        List<OrderingDetail> orderingDetailsList = ordering.getOrderingDetailsList();
        List<OrderDetailsListDto> dtoList = new ArrayList<>();

        for (OrderingDetail od : orderingDetailsList){
            dtoList.add(OrderDetailsListDto.fromEntity(od));
        }
        return OrderListDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMemberEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(dtoList)
                .build();
    }
}
