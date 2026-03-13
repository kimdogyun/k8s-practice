package com.abc.ordersystem.ordering.controller;

import com.abc.ordersystem.ordering.dto.OrderCreateDto;
import com.abc.ordersystem.ordering.dto.OrderListDto;
import com.abc.ordersystem.ordering.dto.OrderMyDto;
import com.abc.ordersystem.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public Long create(@RequestBody List<OrderCreateDto> dtoList){
        return orderService.create(dtoList);

    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderListDto> findAll(){
        return orderService.findAll();
    }

    @GetMapping("/myorders")
    public List<OrderMyDto> orderMyDto(){
        List<OrderMyDto> dto = orderService.findMyOrder();
        System.out.println(dto);
        return dto;
    }

}
