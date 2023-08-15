package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    @GetMapping("/v1/members")
    public ResponseEntity<List<MemberTeamDto>> searchMemberV1(@ModelAttribute MemberSearchCondition condition) {
        return ResponseEntity.ok(memberJpaRepository.search(condition));
    }

    @GetMapping("/v2/members")
    public ResponseEntity<Page<MemberTeamDto>> searchMemberV2(@ModelAttribute MemberSearchCondition condition, Pageable pageable) {
        return ResponseEntity.ok(memberRepository.searchPageSimple(condition, pageable));

    }


    @GetMapping("/v3/members")
    public ResponseEntity<Page<MemberTeamDto>> searchMemberV3(@ModelAttribute MemberSearchCondition condition, @ModelAttribute Pageable pageable) {
        return ResponseEntity.ok(memberRepository.searchPageComplex(condition, pageable));
    }
}
