package com.example.memberservice.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.memberservice.dto.RegisterRequest;

import com.example.memberservice.entity.Account;
import com.example.memberservice.repository.*;
import com.example.memberservice.utils.PhoneNumberUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.memberservice.dto.MemberWithPointsDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.MemberLevel;
import com.example.memberservice.entity.PointBalance;
import com.example.memberservice.exception.DuplicateEmailException;
import com.example.memberservice.exception.MemberLevelNotFoundException;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PointBalanceNotFoundException;

@Service
public class MemberService {
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final PointRecordRepository pointRecordRepository;
    private final PointBalanceRepository pointBalanceRepository;
    private final MemberLevelRepository memberLevelRepository;

    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository, AccountRepository accountRepository, PointRecordRepository pointRecordRepository, PointBalanceRepository pointBalanceRepository, MemberLevelRepository memberLevelRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.pointRecordRepository = pointRecordRepository;
        this.pointBalanceRepository = pointBalanceRepository;
        this.memberLevelRepository = memberLevelRepository;
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

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        memberRepository.delete(member);
    }

    public MemberWithPointsDto getMemberWithPoints(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
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

    @Transactional
    public MemberWithPointsDto registerMember(RegisterRequest registerRequest) {
        MemberLevel defaultLevel = memberLevelRepository.findByLevel(MemberLevel.Level.BRONZE).orElseThrow(() -> new MemberLevelNotFoundException("Default member level not found"));

        Member member = new Member();
        member.setFirstName(registerRequest.getFirstName());
        member.setLastName(registerRequest.getLastName());
        member.setPhoneNumber(PhoneNumberUtils.normalize(registerRequest.getPhoneNumber()));
        member.setEmail(registerRequest.getEmail());
        member.setDateOfBirth(registerRequest.getDateOfBirth());
        member.setMemberLevel(defaultLevel);

        Optional<Member> existing = memberRepository.findByEmail(member.getEmail());
        if (existing.isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }

        Member savedMember = memberRepository.save(member);

        Account account = new Account();
        account.setUsername(member.getEmail());
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        account.setPassword(encodedPassword);
        account.setMember(savedMember);
        accountRepository.save(account);

        PointBalance pointBalance = new PointBalance();
        pointBalance.setMember(savedMember);
        pointBalance.setTotalPoints(0);
        pointBalance.setUsedPoints(0);
        pointBalance.setExpiredPoints(0);
        pointBalance.setLastUpdated(LocalDateTime.now());
        pointBalanceRepository.save(pointBalance);

        return new MemberWithPointsDto(
                savedMember.getId(),
                savedMember.getFirstName(),
                savedMember.getLastName(),
                savedMember.getEmail(),
                0,
                0,
                savedMember.getMemberLevel().getLevel().name()
        );
    }
}
