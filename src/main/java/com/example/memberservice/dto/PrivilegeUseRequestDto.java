package com.example.memberservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class PrivilegeUseRequestDto {

    @NotNull(message = "Privilege ID cannot be null")
    private Long privilegeId;

    private String benefitType; // Optional

    private Map<String, Object> params; // Optional
}
