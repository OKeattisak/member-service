package com.example.memberservice.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.memberservice.dto.MemberWithPointsDto;
import com.example.memberservice.entity.PointBalance;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PointBalanceNotFoundException;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PointBalanceRepository;
import com.example.memberservice.repository.PointRecordRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PointRecordRepository pointRecordRepository;
    private final PointBalanceRepository pointBalanceRepository;

    public MemberService(MemberRepository memberRepository, PointRecordRepository pointRecordRepository, PointBalanceRepository pointBalanceRepository) {
        this.memberRepository = memberRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.pointBalanceRepository = pointBalanceRepository;
    }

    public Page<MemberWithPointsDto> getAllMembersWithPoints(Pageable pageable) {
        return memberRepository.findAll(pageable).map(member -> {
            Integer availablePoints = pointRecordRepository.sumAvailablePoints(member, LocalDateTime.now());
            PointBalance pointBalance = pointBalanceRepository.findByMember(member).orElseThrow(() -> new PointBalanceNotFoundException(member.getId()));
            return new MemberWithPointsDto(
                    member.getId(),
                    member.getFirstName(),
                    member.getLastName(),
                    member.getEmail(),
                    pointBalance.getTotalPoints(),
                    availablePoints,
                    member.getMemberLevel().getLevel().name());
        });
    }

    public MemberWithPointsDto getMemberWithPoints(Long id) {
        var member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        Integer availablePoints = pointRecordRepository.sumAvailablePoints(member, LocalDateTime.now());
        PointBalance pointBalance = pointBalanceRepository.findByMember(member).orElseThrow(() -> new PointBalanceNotFoundException(id));
        return new MemberWithPointsDto(
            member.getId(),
            member.getFirstName(),
            member.getLastName(),
            member.getEmail(),
            pointBalance.getTotalPoints(),
            availablePoints,
            member.getMemberLevel().getLevel().name()
        );
    }
}
