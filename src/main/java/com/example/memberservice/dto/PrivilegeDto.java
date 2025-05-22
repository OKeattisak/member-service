package com.example.memberservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PrivilegeDto {

    private Long id;

    @NotBlank(message = "Privilege name cannot be blank")
    private String name;

    private String description;

    @NotNull(message = "Minimum member level cannot be null")
    private String minMemberLevel; // Representing MemberLevel.Level enum name

    private boolean active = true;

    private Integer benefitPointAmount;

    private String benefitActionType;
}
