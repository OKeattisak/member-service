package com.example.memberservice.entity;

import java.time.LocalDateTime;

import com.example.memberservice.common.Auditable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class PointBalance extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private Integer totalPoints;
    private Integer usedPoints;
    private Integer expiredPoints;
    private LocalDateTime lastUpdated;
}
