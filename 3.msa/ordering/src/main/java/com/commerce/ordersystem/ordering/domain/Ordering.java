package com.commerce.ordersystem.ordering.domain;

import com.commerce.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
@Entity
public class Ordering extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

//    msa모듈 간의 관계성 제거
    private String memberEmail;

    @OneToMany(mappedBy = "ordering", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default // 캐스케이딩
    private List<OrderingDetail> orderingDetailsList = new ArrayList<>();

    
}
