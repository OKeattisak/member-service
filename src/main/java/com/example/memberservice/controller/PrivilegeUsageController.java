package com.example.memberservice.controller;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.dto.PrivilegeUseRequestDto;
import com.example.memberservice.security.CustomUserDetails;
import com.example.memberservice.service.PointService;
import com.example.memberservice.service.PrivilegeService; // Added
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping; // Added
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List; // Added

@RestController
@RequestMapping("/api/me/privileges")
@RequiredArgsConstructor
public class PrivilegeUsageController {

    private final PointService pointService;
    private final PrivilegeService privilegeService; // Added

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMemberId();
        }
        // Handle cases where the user is not authenticated or principal is not CustomUserDetails
        // This might involve throwing an exception or returning a specific error response
        throw new IllegalStateException("User not authenticated or member ID not found in token.");
    }

    @PostMapping("/use")
    public ResponseEntity<String> usePrivilege(@Valid @RequestBody PrivilegeUseRequestDto request) {
        Long memberId = getCurrentMemberId();
        pointService.applyPrivilegeForPointBenefit(memberId, request);
        return ResponseEntity.ok("Privilege used successfully.");
    }

    @GetMapping
    public ResponseEntity<List<PrivilegeDto>> getAvailablePrivileges() {
        Long memberId = getCurrentMemberId();
        List<PrivilegeDto> availablePrivileges = privilegeService.getAvailablePrivilegesForMember(memberId);
        return ResponseEntity.ok(availablePrivileges);
    }
}
