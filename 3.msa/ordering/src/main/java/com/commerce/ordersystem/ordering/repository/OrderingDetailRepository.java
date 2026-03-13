package com.commerce.ordersystem.ordering.repository;

import com.commerce.ordersystem.ordering.domain.OrderingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderingDetailRepository extends JpaRepository<OrderingDetail, Long> {
}
