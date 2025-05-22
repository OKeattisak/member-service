package com.example.memberservice.service;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.MemberLevel;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.exception.DuplicatePrivilegeNameException;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PrivilegeNotActiveException;
import com.example.memberservice.exception.PrivilegeNotFoundException;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PrivilegeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivilegeServiceTests {

    @Mock
    private PrivilegeRepository privilegeRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PrivilegeService privilegeService;

    private Privilege privilege1;
    private PrivilegeDto privilegeDto1;
    private Member member;
    private MemberLevel memberLevel;


    @BeforeEach
    void setUp() {
        privilege1 = new Privilege();
        privilege1.setId(1L);
        privilege1.setName("TEST_PRIVILEGE");
        privilege1.setDescription("Test Description");
        privilege1.setMinMemberLevel(MemberLevel.Level.BRONZE);
        privilege1.setActive(true);
        privilege1.setBenefitPointAmount(100);
        privilege1.setBenefitActionType("EARN_POINTS");

        privilegeDto1 = new PrivilegeDto();
        privilegeDto1.setId(1L);
        privilegeDto1.setName("TEST_PRIVILEGE");
        privilegeDto1.setDescription("Test Description");
        privilegeDto1.setMinMemberLevel("BRONZE");
        privilegeDto1.setActive(true);
        privilegeDto1.setBenefitPointAmount(100);
        privilegeDto1.setBenefitActionType("EARN_POINTS");
        
        memberLevel = new MemberLevel();
        memberLevel.setId(1L);
        memberLevel.setLevel(MemberLevel.Level.GOLD);
        memberLevel.setThresholdPoints(0);


        member = new Member();
        member.setId(1L);
        member.setName("Test User");
        member.setMemberLevel(memberLevel);
    }

    // --- createPrivilege Tests ---
    @Test
    void createPrivilege_Success() {
        when(privilegeRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(privilegeRepository.save(any(Privilege.class))).thenReturn(privilege1);

        PrivilegeDto result = privilegeService.createPrivilege(privilegeDto1);

        assertNotNull(result);
        assertEquals(privilegeDto1.getName(), result.getName());
        verify(privilegeRepository, times(1)).findByName(privilegeDto1.getName());
        verify(privilegeRepository, times(1)).save(any(Privilege.class));
    }

    @Test
    void createPrivilege_DuplicateName_ThrowsDuplicatePrivilegeNameException() {
        when(privilegeRepository.findByName(anyString())).thenReturn(Optional.of(privilege1));

        assertThrows(DuplicatePrivilegeNameException.class, () -> {
            privilegeService.createPrivilege(privilegeDto1);
        });

        verify(privilegeRepository, times(1)).findByName(privilegeDto1.getName());
        verify(privilegeRepository, never()).save(any(Privilege.class));
    }

    @Test
    void createPrivilege_InvalidMinMemberLevel_ThrowsIllegalArgumentException() {
        privilegeDto1.setMinMemberLevel("INVALID_LEVEL");
        // This exception is thrown by MemberLevel.Level.valueOf()
        assertThrows(IllegalArgumentException.class, () -> {
            privilegeService.createPrivilege(privilegeDto1);
        });
         verify(privilegeRepository, times(1)).findByName(privilegeDto1.getName()); // It will check name first
    }

    // --- updatePrivilege Tests ---
    @Test
    void updatePrivilege_Success() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        when(privilegeRepository.findByName(anyString())).thenReturn(Optional.empty()); // Assuming name is changed to a new unique name
        when(privilegeRepository.save(any(Privilege.class))).thenReturn(privilege1);
        
        PrivilegeDto updatedDto = new PrivilegeDto();
        updatedDto.setName("NEW_NAME");
        updatedDto.setDescription("New Desc");
        updatedDto.setMinMemberLevel("SILVER");
        updatedDto.setActive(false);
        updatedDto.setBenefitPointAmount(50);
        updatedDto.setBenefitActionType("DISCOUNT");


        PrivilegeDto result = privilegeService.updatePrivilege(1L, updatedDto);

        assertNotNull(result);
        assertEquals("NEW_NAME", result.getName());
        assertEquals("SILVER", result.getMinMemberLevel());
        assertFalse(result.isActive());
        assertEquals(50, result.getBenefitPointAmount());
        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, times(1)).save(any(Privilege.class));
    }
    
    @Test
    void updatePrivilege_Success_SameName() {
        // Test case where the name is not changed
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        // privilegeRepository.findByName should not be called if name is not changed
        when(privilegeRepository.save(any(Privilege.class))).thenReturn(privilege1);
        
        privilegeDto1.setDescription("Updated Description Same Name");

        PrivilegeDto result = privilegeService.updatePrivilege(1L, privilegeDto1);

        assertNotNull(result);
        assertEquals(privilege1.getName(), result.getName());
        assertEquals("Updated Description Same Name", result.getDescription());
        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, never()).findByName(anyString()); // Not called if name is same
        verify(privilegeRepository, times(1)).save(any(Privilege.class));
    }


    @Test
    void updatePrivilege_NotFound_ThrowsPrivilegeNotFoundException() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeService.updatePrivilege(1L, privilegeDto1);
        });
        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, never()).save(any(Privilege.class));
    }

    @Test
    void updatePrivilege_DuplicateName_ThrowsDuplicatePrivilegeNameException() {
        Privilege existingPrivilegeWithSameName = new Privilege();
        existingPrivilegeWithSameName.setId(2L); // Different ID
        existingPrivilegeWithSameName.setName("NEW_NAME");

        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        // privilege1.setName("OLD_NAME"); // Original name
        privilegeDto1.setName("NEW_NAME"); // Attempting to change to "NEW_NAME"
        when(privilegeRepository.findByName("NEW_NAME")).thenReturn(Optional.of(existingPrivilegeWithSameName));


        assertThrows(DuplicatePrivilegeNameException.class, () -> {
            privilegeService.updatePrivilege(1L, privilegeDto1);
        });

        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, times(1)).findByName("NEW_NAME");
        verify(privilegeRepository, never()).save(any(Privilege.class));
    }

    // --- deletePrivilege Tests ---
    @Test
    void deletePrivilege_Success() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        doNothing().when(privilegeRepository).delete(privilege1);

        privilegeService.deletePrivilege(1L);

        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, times(1)).delete(privilege1);
    }

    @Test
    void deletePrivilege_NotFound_ThrowsPrivilegeNotFoundException() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeService.deletePrivilege(1L);
        });
        verify(privilegeRepository, times(1)).findById(1L);
        verify(privilegeRepository, never()).delete(any(Privilege.class));
    }

    // --- getPrivilege Tests ---
    @Test
    void getPrivilege_Success() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        PrivilegeDto result = privilegeService.getPrivilege(1L);

        assertNotNull(result);
        assertEquals(privilege1.getName(), result.getName());
        verify(privilegeRepository, times(1)).findById(1L);
    }

    @Test
    void getPrivilege_NotFound_ThrowsPrivilegeNotFoundException() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeService.getPrivilege(1L);
        });
        verify(privilegeRepository, times(1)).findById(1L);
    }

    // --- getAllPrivileges Tests ---
    @Test
    void getAllPrivileges_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Privilege> privileges = Collections.singletonList(privilege1);
        Page<Privilege> page = new PageImpl<>(privileges, pageable, privileges.size());

        when(privilegeRepository.findAll(pageable)).thenReturn(page);

        Page<PrivilegeDto> result = privilegeService.getAllPrivileges(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(privilege1.getName(), result.getContent().get(0).getName());
        verify(privilegeRepository, times(1)).findAll(pageable);
    }

    // --- canMemberUsePrivilege Tests ---
    @Test
    void canMemberUsePrivilege_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1)); // BRONZE required, member is GOLD

        boolean canUse = privilegeService.canMemberUsePrivilege(1L, 1L);
        assertTrue(canUse);
    }

    @Test
    void canMemberUsePrivilege_PrivilegeNotActive() {
        privilege1.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));

        boolean canUse = privilegeService.canMemberUsePrivilege(1L, 1L);
        assertFalse(canUse);
    }

    @Test
    void canMemberUsePrivilege_MemberLevelTooLow() {
        memberLevel.setLevel(MemberLevel.Level.BRONZE); // Member is BRONZE
        member.setMemberLevel(memberLevel);
        privilege1.setMinMemberLevel(MemberLevel.Level.GOLD); // Privilege requires GOLD

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));

        boolean canUse = privilegeService.canMemberUsePrivilege(1L, 1L);
        assertFalse(canUse);
    }
    
    @Test
    void canMemberUsePrivilege_MemberLevelSufficient_ExactMatch() {
        memberLevel.setLevel(MemberLevel.Level.BRONZE); // Member is BRONZE
        member.setMemberLevel(memberLevel);
        privilege1.setMinMemberLevel(MemberLevel.Level.BRONZE); // Privilege requires BRONZE

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));

        boolean canUse = privilegeService.canMemberUsePrivilege(1L, 1L);
        assertTrue(canUse);
    }


    @Test
    void canMemberUsePrivilege_MemberNotFound_ThrowsMemberNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> {
            privilegeService.canMemberUsePrivilege(1L, 1L);
        });
    }

    @Test
    void canMemberUsePrivilege_PrivilegeNotFound_ThrowsPrivilegeNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeService.canMemberUsePrivilege(1L, 1L);
        });
    }

    // --- verifyAndGetPrivilege Tests ---
    @Test
    void verifyAndGetPrivilege_Success() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        Privilege result = privilegeService.verifyAndGetPrivilege(1L);

        assertNotNull(result);
        assertEquals(privilege1.getName(), result.getName());
        assertTrue(result.isActive());
    }

    @Test
    void verifyAndGetPrivilege_NotFound_ThrowsPrivilegeNotFoundException() {
        when(privilegeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(PrivilegeNotFoundException.class, () -> {
            privilegeService.verifyAndGetPrivilege(1L);
        });
    }

    @Test
    void verifyAndGetPrivilege_NotActive_ThrowsPrivilegeNotActiveException() {
        privilege1.setActive(false);
        when(privilegeRepository.findById(1L)).thenReturn(Optional.of(privilege1));
        assertThrows(PrivilegeNotActiveException.class, () -> {
            privilegeService.verifyAndGetPrivilege(1L);
        });
    }

    // --- getAvailablePrivilegesForMember Tests ---
    @Test
    void getAvailablePrivilegesForMember_Success() {
        Privilege bronzePrivilege = new Privilege();
        bronzePrivilege.setId(1L);
        bronzePrivilege.setName("BRONZE_PRIV");
        bronzePrivilege.setMinMemberLevel(MemberLevel.Level.BRONZE);
        bronzePrivilege.setActive(true);

        Privilege goldPrivilegeActive = new Privilege();
        goldPrivilegeActive.setId(2L);
        goldPrivilegeActive.setName("GOLD_PRIV_ACTIVE");
        goldPrivilegeActive.setMinMemberLevel(MemberLevel.Level.GOLD);
        goldPrivilegeActive.setActive(true);
        
        Privilege goldPrivilegeInactive = new Privilege();
        goldPrivilegeInactive.setId(3L);
        goldPrivilegeInactive.setName("GOLD_PRIV_INACTIVE");
        goldPrivilegeInactive.setMinMemberLevel(MemberLevel.Level.GOLD);
        goldPrivilegeInactive.setActive(false);


        Privilege platinumPrivilege = new Privilege();
        platinumPrivilege.setId(4L);
        platinumPrivilege.setName("PLATINUM_PRIV");
        platinumPrivilege.setMinMemberLevel(MemberLevel.Level.PLATINUM);
        platinumPrivilege.setActive(true);

        member.getMemberLevel().setLevel(MemberLevel.Level.GOLD); // Member is GOLD

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findAll()).thenReturn(Arrays.asList(bronzePrivilege, goldPrivilegeActive, goldPrivilegeInactive, platinumPrivilege));

        List<PrivilegeDto> result = privilegeService.getAvailablePrivilegesForMember(1L);

        assertNotNull(result);
        assertEquals(2, result.size()); // BRONZE_PRIV and GOLD_PRIV_ACTIVE
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("BRONZE_PRIV")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("GOLD_PRIV_ACTIVE")));
    }

    @Test
    void getAvailablePrivilegesForMember_NoSuitablePrivileges() {
        Privilege platinumPrivilege = new Privilege();
        platinumPrivilege.setId(1L);
        platinumPrivilege.setName("PLATINUM_PRIV");
        platinumPrivilege.setMinMemberLevel(MemberLevel.Level.PLATINUM);
        platinumPrivilege.setActive(true);
        
        member.getMemberLevel().setLevel(MemberLevel.Level.BRONZE); // Member is BRONZE

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findAll()).thenReturn(Collections.singletonList(platinumPrivilege));

        List<PrivilegeDto> result = privilegeService.getAvailablePrivilegesForMember(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void getAvailablePrivilegesForMember_AllPrivilegesActiveAndSufficientLevel() {
        Privilege bronzePrivilege = new Privilege();
        bronzePrivilege.setId(1L);
        bronzePrivilege.setName("BRONZE_PRIV");
        bronzePrivilege.setMinMemberLevel(MemberLevel.Level.BRONZE);
        bronzePrivilege.setActive(true);

        Privilege silverPrivilege = new Privilege();
        silverPrivilege.setId(2L);
        silverPrivilege.setName("SILVER_PRIV");
        silverPrivilege.setMinMemberLevel(MemberLevel.Level.SILVER);
        silverPrivilege.setActive(true);
        
        member.getMemberLevel().setLevel(MemberLevel.Level.GOLD); // Member is GOLD

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeRepository.findAll()).thenReturn(Arrays.asList(bronzePrivilege, silverPrivilege));

        List<PrivilegeDto> result = privilegeService.getAvailablePrivilegesForMember(1L);

        assertNotNull(result);
        assertEquals(2, result.size()); 
    }


    @Test
    void getAvailablePrivilegesForMember_MemberNotFound_ThrowsMemberNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class, () -> {
            privilegeService.getAvailablePrivilegesForMember(1L);
        });
    }
}
