package com.example.memberservice.controller;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.service.PrivilegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/privileges")
@RequiredArgsConstructor
public class PrivilegeController {

    private final PrivilegeService privilegeService;

    @PostMapping
    public ResponseEntity<PrivilegeDto> createPrivilege(@Valid @RequestBody PrivilegeDto privilegeDto) {
        PrivilegeDto createdPrivilege = privilegeService.createPrivilege(privilegeDto);
        return new ResponseEntity<>(createdPrivilege, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PrivilegeDto> updatePrivilege(@PathVariable Long id, @Valid @RequestBody PrivilegeDto privilegeDto) {
        PrivilegeDto updatedPrivilege = privilegeService.updatePrivilege(id, privilegeDto);
        return ResponseEntity.ok(updatedPrivilege);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrivilege(@PathVariable Long id) {
        privilegeService.deletePrivilege(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PrivilegeDto> getPrivilege(@PathVariable Long id) {
        PrivilegeDto privilegeDto = privilegeService.getPrivilege(id);
        return ResponseEntity.ok(privilegeDto);
    }

    @GetMapping
    public ResponseEntity<Page<PrivilegeDto>> getAllPrivileges(Pageable pageable) {
        Page<PrivilegeDto> privileges = privilegeService.getAllPrivileges(pageable);
        return ResponseEntity.ok(privileges);
    }
}
