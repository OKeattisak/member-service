package com.example.memberservice.service;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.entity.PrivilegeUsageLog;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PrivilegeNotFoundException;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PrivilegeRepository;
import com.example.memberservice.repository.PrivilegeUsageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivilegeUsageLogServiceTests {

    @Mock
    private PrivilegeUsageLogRepository privilegeUsageLogRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private PrivilegeUsageLogService privilegeUsageLogService;

    private Member member;
    private Privilege privilege;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setId(1L);
        member.setName("Test Member");

        privilege = new Privilege();
        privilege.setId(1L);
        privilege.setName("Test Privilege");
    }

    @Test
    void logPrivilegeUsage_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege));
        when(privilegeUsageLogRepository.save(any(PrivilegeUsageLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String notes = "Test usage notes";
        privilegeUsageLogService.logPrivilegeUsage(1L, 1L, notes);

        ArgumentCaptor<PrivilegeUsageLog> logCaptor = ArgumentCaptor.forClass(PrivilegeUsageLog.class);
        verify(privilegeUsageLogRepository, times(1)).save(logCaptor.capture());

        PrivilegeUsageLog savedLog = logCaptor.getValue();
        assertNotNull(savedLog);
        assertEquals(member, savedLog.getMember());
        assertEquals(privilege, savedLog.getPrivilege());
        assertEquals(notes, savedLog.getNotes());
        assertNotNull(savedLog.getUsageTimestamp());
        assertTrue(savedLog.getUsageTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                   savedLog.getUsageTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void logPrivilegeUsage_MemberNotFound_ThrowsMemberNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        String notes = "Test usage notes";
        assertThrows(MemberNotFoundException.class, () -> {
            privilegeUsageLogService.logPrivilegeUsage(1L, 1L, notes);
        });

        verify(privilegeRepository, never()).findById(anyLong());
        verify(privilegeUsageLogRepository, never()).save(any(PrivilegeUsageLog.class));
    }

    @Test
    void logPrivilegeUsage_PrivilegeNotFound_ThrowsPrivilegeNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());

        String notes = "Test usage notes";
        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeUsageLogService.logPrivilegeUsage(1L, 1L, notes);
        });

        verify(privilegeUsageLogRepository, never()).save(any(PrivilegeUsageLog.class));
    }
}
