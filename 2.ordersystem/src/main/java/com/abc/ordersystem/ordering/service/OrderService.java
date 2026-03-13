package com.abc.ordersystem.ordering.service;

import com.abc.ordersystem.common.service.SseAlarmService;
import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.member.repository.MemberRepository;
import com.abc.ordersystem.ordering.domain.Ordering;
import com.abc.ordersystem.ordering.domain.OrderingDetail;
import com.abc.ordersystem.ordering.dto.OrderCreateDto;
import com.abc.ordersystem.ordering.dto.OrderListDto;
import com.abc.ordersystem.ordering.dto.OrderMyDto;
import com.abc.ordersystem.ordering.repository.OrderRepository;
import com.abc.ordersystem.ordering.repository.OrderingDetailRepository;
import com.abc.ordersystem.product.domain.Product;
import com.abc.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;
    private final OrderingDetailRepository orderingDetailRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate, OrderingDetailRepository orderingDetailRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
        this.orderingDetailRepository = orderingDetailRepository;
    }

    public Long create(List<OrderCreateDto> dtoList){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).get();
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        Ordering OrderingDb = orderRepository.save(ordering);
        for (OrderCreateDto dto : dtoList){
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new EntityNotFoundException("상품 없음"));
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            product.updateStockQuantity(dto.getProductCount());
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderingDetail);
        }
        return OrderingDb.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderListDto> findAll(){
        List<Ordering> orderList = orderRepository.findAll();
        List<OrderListDto> orderListDtoList = new ArrayList<>();
        for (Ordering o : orderList){
            orderListDtoList.add(OrderListDto.fromEntity(o)) ;
        }
        return orderListDtoList;
    }

    public List<OrderMyDto> findMyOrder(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).get();
        List<OrderMyDto> orderMyDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderRepository.findAllByMember(member);
        for (Ordering o : orderingList){
            orderMyDtoList.add(OrderMyDto.fromEntity(o));
        }
        return orderMyDtoList;
    }
}
