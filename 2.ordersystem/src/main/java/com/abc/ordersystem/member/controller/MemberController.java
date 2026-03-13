package com.abc.ordersystem.member.controller;

import com.abc.ordersystem.common.auth.JwtTokenProvider;
import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.member.dto.*;
import com.abc.ordersystem.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;



    @Autowired
    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/create")
    @Operation(
            summary = "회원가입", description = "이메일, 비밀번호를 통한 회원가입"
    )
    public ResponseEntity<?> create(@RequestBody MemberCreateDto dto){
        Long id = memberService.save(dto);
        return ResponseEntity
                .status(HttpStatus.OK).body(id);

    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody MemberLoginDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh 토큰 생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);

        MemberTokenDto tokenDto = MemberTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK).body(tokenDto);
    }

    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){ // rt 갖고와서 db에 있는지 검증
//        rt검증(1. 토큰 자체 검증 2. redis 조회 검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at신규 생성
        String accessToken = jwtTokenProvider.createToken(member);

        MemberTokenDto tokenDto = MemberTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        return ResponseEntity
                .status(HttpStatus.OK).body(tokenDto);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResDto> findAll(){
        return memberService.findAll();
    }

    @GetMapping("/myinfo")
    public ResponseEntity<?> myInfo(@AuthenticationPrincipal String email){
        MemberResDto dto = memberService.myInfo(email);
        return ResponseEntity
                .status(HttpStatus.OK).body(dto);
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id){
        return ResponseEntity
                .status(HttpStatus.OK).body(memberService.findById(id));
    }

    @PatchMapping("/update/password")
    public void updatePw(@RequestBody MemberUpdatePwDto dto){
        memberService.updatePw(dto);
    }

}
