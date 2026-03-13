package com.commerce.ordersystem.ordering.controller;

import com.commerce.ordersystem.ordering.dto.OrderCreateDto;
import com.commerce.ordersystem.ordering.dto.OrderListDto;
import com.commerce.ordersystem.ordering.dto.OrderMyDto;
import com.commerce.ordersystem.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Long create(@RequestBody List<OrderCreateDto> dtoList, @RequestHeader("X-User-Email")String email){
        return orderService.createFeign(dtoList, email);

    }

    @GetMapping("/list")
    public List<OrderListDto> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/myorders")
    public List<OrderMyDto> orderMyDto(@RequestHeader("X-User-Email")String email){
        List<OrderMyDto> dto = orderService.findMyOrder(email);
        System.out.println(dto);
        return dto;
    }
}
