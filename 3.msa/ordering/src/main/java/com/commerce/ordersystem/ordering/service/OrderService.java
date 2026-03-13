package com.commerce.ordersystem.ordering.service;

import com.commerce.ordersystem.common.configs.RestTemplateConfig;
import com.commerce.ordersystem.common.service.SseAlarmService;
import com.commerce.ordersystem.ordering.domain.Ordering;
import com.commerce.ordersystem.ordering.domain.OrderingDetail;
import com.commerce.ordersystem.ordering.dto.OrderCreateDto;
import com.commerce.ordersystem.ordering.dto.OrderListDto;
import com.commerce.ordersystem.ordering.dto.OrderMyDto;
import com.commerce.ordersystem.ordering.dto.ProductDto;
import com.commerce.ordersystem.ordering.feinclients.ProductFeignClient;
import com.commerce.ordersystem.ordering.repository.OrderRepository;
import com.commerce.ordersystem.ordering.repository.OrderingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final SseAlarmService sseAlarmService;
    private final OrderingDetailRepository orderingDetailRepository;
    private final RestTemplate restTemplate;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository, SseAlarmService sseAlarmService, OrderingDetailRepository orderingDetailRepository, RestTemplate restTemplate, ProductFeignClient productFeignClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.sseAlarmService = sseAlarmService;
        this.orderingDetailRepository = orderingDetailRepository;
        this.restTemplate = restTemplate;
        this.productFeignClient = productFeignClient;

        this.kafkaTemplate = kafkaTemplate;
    }

    public Long create(List<OrderCreateDto> dtoList, String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        Ordering OrderingDb = orderRepository.save(ordering);
        for (OrderCreateDto dto : dtoList){
//            1. 재고조회(동기요청-http요청)
//            http://localhost:8080/product-service : apigateway를 통한 호출
//            http:product-service : eureka에게 질의 후 product-service 직접 호출
            String endPoint1 = "http://product-service/product/detail/" + dto.getProductId();
            HttpHeaders headers = new HttpHeaders();
//            HttpEntity : header + body
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
//            아래 코드에서 직렬화가 자동으로 됨.
            ResponseEntity<ProductDto> responseEntity = restTemplate.exchange(endPoint1, HttpMethod.GET, httpEntity, ProductDto.class);
            ProductDto product = responseEntity.getBody();
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
//            2. 주문 발생
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderingDetail);
//            3. 재고감소요청(동기-http요청/비동기-이벤트기반 모두 가능)
            String endPoint2 = "http://product-service/product/updatestock";
            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OrderCreateDto> httpEntity2 = new HttpEntity<>(dto, headers2);
            restTemplate.exchange(endPoint2, HttpMethod.PUT, httpEntity2, Void.class);
        }
        return OrderingDb.getId();
    }

    public Long createFeign(List<OrderCreateDto> dtoList, String email){
        Ordering ordering = Ordering.builder()
                .memberEmail(email)
                .build();
        Ordering OrderingDb = orderRepository.save(ordering);
        for (OrderCreateDto dto : dtoList){
            ProductDto product = productFeignClient.getProductById(dto.getProductId());
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
//            2. 주문 발생
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .productName(product.getName())
                    .productId(dto.getProductId())
                    .quantity(dto.getProductCount())
                    .build();
            orderingDetailRepository.save(orderingDetail);
//            feign을 사용한 동기적 재고 감소 요청
//            productFeignClient.updateStockQuantity(dto);
//            kafka를 활용한 비동기적 재고감소 요청
            kafkaTemplate.send("stock-update-topic", dto);

            
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

    public List<OrderMyDto> findMyOrder(String email){
        List<OrderMyDto> orderMyDtoList = new ArrayList<>();
        List<Ordering> orderingList = orderRepository.findAllByMemberEmail(email);
        for (Ordering o : orderingList){
            orderMyDtoList.add(OrderMyDto.fromEntity(o));
        }
        return orderMyDtoList;
    }
}
