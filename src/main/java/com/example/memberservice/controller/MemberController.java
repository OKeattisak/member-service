package com.example.memberservice.controller;

import com.example.memberservice.dto.RegisterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.memberservice.dto.ApiResponse;
import com.example.memberservice.dto.MemberWithPointsDto;
import com.example.memberservice.service.MemberService;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberWithPointsDto>>> getAllMembersWithPoints(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getAllMembersWithPoints(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberWithPointsDto>> getMemberWithPoints(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMemberWithPoints(id)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberWithPointsDto>> registerMember(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(ApiResponse.ok(memberService.registerMember(registerRequest)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
