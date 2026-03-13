package com.abc.ordersystem.product.domain;

import com.abc.ordersystem.common.domain.BaseTimeEntity;
import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.ordering.domain.OrderingDetail;
import com.abc.ordersystem.product.dto.ProductUpdateDto;
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

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderingDetail> orderingDetailsList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

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
