package com.abc.ordersystem.ordering.dto;

import com.abc.ordersystem.ordering.domain.OrderStatus;
import com.abc.ordersystem.ordering.domain.Ordering;
import com.abc.ordersystem.ordering.domain.OrderingDetail;
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
public class OrderMyDto {
    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderDetailsListDto> orderDetails;
//    private List<OrderingDetails> orderingDetailsList;

    public static OrderMyDto fromEntity(Ordering ordering){
        List<OrderingDetail> orderingDetailsList = ordering.getOrderingDetailsList();
        List<OrderDetailsListDto> dtoList = new ArrayList<>();

        for (OrderingDetail od : orderingDetailsList){
            dtoList.add(OrderDetailsListDto.fromEntity(od));
        }
        return OrderMyDto.builder()
                .id(ordering.getId())
                .memberEmail(ordering.getMember().getEmail())
                .orderStatus(ordering.getOrderStatus())
                .orderDetails(dtoList)
                .build();
    }

}

