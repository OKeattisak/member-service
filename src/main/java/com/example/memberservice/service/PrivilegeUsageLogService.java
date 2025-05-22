package com.example.memberservice.service;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.entity.PrivilegeUsageLog;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PrivilegeNotFoundException;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PrivilegeRepository;
import com.example.memberservice.repository.PrivilegeUsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrivilegeUsageLogService {

    private final PrivilegeUsageLogRepository privilegeUsageLogRepository;
    private final MemberRepository memberRepository;
    private final PrivilegeRepository privilegeRepository;

    @Transactional
    public void logPrivilegeUsage(Long memberId, Long privilegeId, String notes) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Privilege privilege = privilegeRepository.findById(privilegeId)
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + privilegeId));

        PrivilegeUsageLog logEntry = new PrivilegeUsageLog();
        logEntry.setMember(member);
        logEntry.setPrivilege(privilege);
        logEntry.setUsageTimestamp(LocalDateTime.now());
        logEntry.setNotes(notes);

        privilegeUsageLogRepository.save(logEntry);
    }
}
