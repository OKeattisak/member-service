package com.example.memberservice.entity;

import com.example.memberservice.common.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class Privilege extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberLevel.Level minMemberLevel;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = true)
    private Integer benefitPointAmount;

    @Column(nullable = true)
    private String benefitActionType;
}
