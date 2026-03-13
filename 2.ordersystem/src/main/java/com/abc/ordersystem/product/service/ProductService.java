package com.abc.ordersystem.product.service;

import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.member.repository.MemberRepository;
import com.abc.ordersystem.product.domain.Product;
import com.abc.ordersystem.product.dto.ProductCreateDto;
import com.abc.ordersystem.product.dto.ProductResDto;
import com.abc.ordersystem.product.dto.ProductSearchDto;
import com.abc.ordersystem.product.dto.ProductUpdateDto;
import com.abc.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }

    @Value("${aws.s3.bucket1}")
    private String bucket1;

    public Long save(ProductCreateDto dto){ // 사진이 리스트로 들어오는 경우는 나중에 고려해보기
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(()->new EntityNotFoundException("email is not found"));
        Product product = dto.toEntity(member);
        Product productDb = productRepository.save(product);
        if (dto.getProductImage()!= null) {
            String fileName = "product-" + product.getId() + "profileImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket1)
                    .key(fileName) // 파일명
                    .contentType(dto.getProductImage().getContentType()) // image/jpeg, video/mp4, ...인지 정보
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
//            이미지 올리다가 안됐으면 안됐다고 얘기해줘야 하기 때문.
                throw new RuntimeException(e); // 롤백의 기준이 되기 때문에 필요.
            }
            String imgUrl = s3Client.utilities().getUrl(a-> a.bucket(bucket1).key(fileName)).toExternalForm();
            product.updateProfileImageUrl(imgUrl);
        }

//        동시성문제 해결을 위해 상품등록 시 redis에 재고 세팅
        redisTemplate.opsForValue().set(String.valueOf(product.getId()), String.valueOf(product.getStockQuantity()));

        return productDb.getId();
    }

    @Transactional(readOnly = true)
    public ProductResDto findById(Long id){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException());
        return ProductResDto.fromEntity(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto searchDto){

        Specification<Product> specification = new Specification<Product>() {
                @Override
                public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicateList = new ArrayList<>();
                    if (searchDto.getProductName() != null){
                        predicateList.add(criteriaBuilder.like(root.get("name"),"%" + searchDto.getProductName() + "%"));
                    }
                    if (searchDto.getCategory() != null){
                        predicateList.add(criteriaBuilder.equal(root.get("category"), searchDto.getCategory()));
                    }
                    Predicate[] predicateArr = new Predicate[predicateList.size()];
                    for (int i = 0; i < predicateArr.length; i++) {
                        predicateArr[i] = predicateList.get(i);
                    }
//                Predicate에는 검색조건들이 담길 것이고, 이 Predicate list를 한 줄의 predicate 조립.
                    Predicate predicate = criteriaBuilder.and(predicateArr);
                    return predicate;
                }
            };
            Page<Product> productList = productRepository.findAll(specification, pageable);

        return productList.map(p-> ProductResDto.fromEntity(p));
    }

    public void update(Long id, ProductUpdateDto dto){
        Product product = productRepository.findById(id).orElseThrow(()->new EntityNotFoundException());
        product.updateProduct(dto);

        if (dto.getProductImage() != null){
//            이미지를 수정하는 경우 : 삭제 후 추가
//            기존 이미지를 파일명으로 삭제
//            기존 이미지가 null이 아닌경우(기존 이미지가 있었던 경우)에만 이미지 삭제
            if (product.getImagePath() != null){
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                s3Client.deleteObject(a->a.bucket(bucket1).key(fileName));
            }
            
//            신규이미지를 등록
            String newFileName = "product-" + product.getId() + "profileImage-" + dto.getProductImage().getOriginalFilename();
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket1)
                    .key(newFileName) // 파일명
                    .contentType(dto.getProductImage().getContentType()) // image/jpeg, video/mp4, ...인지 정보
                    .build();
            try {
                s3Client.putObject(request, RequestBody.fromBytes(dto.getProductImage().getBytes()));
            } catch (IOException e) {
//            이미지 올리다가 안됐으면 안됐다고 얘기해줘야 하기 때문.
                throw new RuntimeException(e); // 롤백의 기준이 되기 때문에 필요.
            }
            String newImgUrl = s3Client.utilities().getUrl(a-> a.bucket(bucket1).key(newFileName)).toExternalForm();
            product.updateProfileImageUrl(newImgUrl);

        } else {
//            이미지를 삭제하고자 하는 경우
//            기존 이미지가 null이 아닌경우(기존 이미지가 있었던 경우)에만 이미지 삭제
            if (product.getImagePath() != null){
                String imgUrl = product.getImagePath();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/")+1);
                s3Client.deleteObject(a->a.bucket(bucket1).key(fileName));
            }
        }
    }
}
