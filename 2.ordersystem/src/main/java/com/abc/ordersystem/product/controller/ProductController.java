package com.abc.ordersystem.product.controller;

import com.abc.ordersystem.product.dto.ProductCreateDto;
import com.abc.ordersystem.product.dto.ProductResDto;
import com.abc.ordersystem.product.dto.ProductSearchDto;
import com.abc.ordersystem.product.dto.ProductUpdateDto;
import com.abc.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public Long create(@ModelAttribute ProductCreateDto dto){
        return productService.save(dto);
    }

    @GetMapping("/detail/{id}")
    public ProductResDto productDetailDto(@PathVariable Long id){
        return productService.findById(id);
    }

    @GetMapping("/list")
//    페이징처리를 위한 데이터 요청 형식 : localhost:8080/posts?page=0&size=5&sort=title,asc
//    검색 + 페이징처리를 위한 데이터 요청 형식 : localhost:8080/posts?page=0&size=5&sort=title,asc&title=hello&category=경제
//    아래 줄에 @ModelAttribute 안 써도 됨.

    public Page<ProductResDto> productListDtoList(@PageableDefault(size = 3, sort = "id", direction = Sort.Direction.DESC)Pageable pageable, ProductSearchDto searchDto){
        return productService.findAll(pageable, searchDto);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, ProductUpdateDto dto){
        productService.update(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
