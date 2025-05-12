package com.example.memberservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberWithPointsDto {
    private Long memberId;
    private String firstName;
    private String lastName;
    private String email;
    private Integer totalPoints;
    private Integer availablePoints;
    private String memberLevel;
}
