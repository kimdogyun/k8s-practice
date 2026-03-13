package com.commerce.ordersystem.product.controller;

import com.commerce.ordersystem.product.dto.*;
import com.commerce.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public Long create(@ModelAttribute ProductCreateDto dto, @RequestHeader("X-User-Email")String email){
        return productService.save(dto, email);
    }

    @GetMapping("/detail/{id}")
    public ProductResDto productDetailDto(@PathVariable Long id){
        return productService.findById(id);
    }

    @GetMapping("/list")
    public Page<ProductResDto> productListDtoList(@PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC)Pageable pageable, ProductSearchDto searchDto){
        return productService.findAll(pageable, searchDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, ProductUpdateDto dto){
        productService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductStockUpdateDto dto){
        productService.updateStock(dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
