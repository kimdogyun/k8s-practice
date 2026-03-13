package com.abc.ordersystem.member.service;

import com.abc.ordersystem.member.domain.Member;
import com.abc.ordersystem.member.dto.*;
import com.abc.ordersystem.member.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Long save(MemberCreateDto dto){

        if (memberRepository.findByEmail(dto.getEmail()).isPresent()){
            throw new IllegalArgumentException("중복되는 이메일이 있습니다.");
        }
        Member member = dto.toEntity(passwordEncoder.encode(dto.getPassword()));

//        Member memberDB = memberRepository.save(member);
//        return memberDB.getId();
        memberRepository.save(member); // 트랜잭션
        return member.getId();
    }

    public Member login(MemberLoginDto dto){
        Optional<Member> optMember = memberRepository.findByEmail(dto.getEmail());
        boolean check = true;
        if (optMember.isEmpty()) {
            check = false;
        } else {
            if (!passwordEncoder.matches(dto.getPassword(), optMember.get().getPassword())){
                check = false;
            }
        }
        if (!check){
            throw new IllegalArgumentException("입력하신 정보가 일치하지 않습니다.");
        }

        return optMember.get();
    }

    @Transactional(readOnly = true)
    public List<MemberResDto> findAll(){
        List<Member> memberList = memberRepository.findAll();
        List<MemberResDto> memberListDtoList = new ArrayList<>();
        for (Member m : memberList){
            memberListDtoList.add(MemberResDto.fromEntity(m));
        }
        return memberListDtoList;
    }

    @Transactional(readOnly = true)
    public MemberResDto myInfo(String email){
//        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Optional<Member> optAuthor = memberRepository.findByEmail(email);
        Member member = optAuthor.orElseThrow(()-> new NoSuchElementException("entity is not found"));
        MemberResDto dto = MemberResDto.fromEntity(member);
        return dto;
    }


    public MemberResDto findById(Long id){
        Member member = memberRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("없는 이메일 입니다."));
        return MemberResDto.fromEntity(member);
    }

    public void updatePw(MemberUpdatePwDto dto){
        Member member = memberRepository.findByEmail(dto.getEmail())
                .orElseThrow(()->new EntityNotFoundException("없는 이메일 입니다."));
        member.updatePw(passwordEncoder.encode(dto.getPassword()));

    }
}
