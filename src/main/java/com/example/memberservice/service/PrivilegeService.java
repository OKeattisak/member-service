package com.example.memberservice.service;

import com.example.memberservice.dto.PrivilegeDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.entity.MemberLevel;
import com.example.memberservice.exception.DuplicatePrivilegeNameException;
import com.example.memberservice.exception.MemberNotFoundException;
import com.example.memberservice.exception.PrivilegeNotActiveException;
import com.example.memberservice.exception.PrivilegeNotFoundException;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PrivilegeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrivilegeService {

    private final PrivilegeRepository privilegeRepository;
    private final MemberRepository memberRepository;

    // Helper method to map Entity to DTO
    private PrivilegeDto toDto(Privilege privilege) {
        PrivilegeDto dto = new PrivilegeDto();
        dto.setId(privilege.getId());
        dto.setName(privilege.getName());
        dto.setDescription(privilege.getDescription());
        dto.setMinMemberLevel(privilege.getMinMemberLevel().name());
        dto.setActive(privilege.isActive());
        dto.setBenefitPointAmount(privilege.getBenefitPointAmount());
        dto.setBenefitActionType(privilege.getBenefitActionType());
        return dto;
    }

    // Helper method to map DTO to Entity
    private Privilege toEntity(PrivilegeDto privilegeDto) {
        Privilege privilege = new Privilege();
        privilege.setName(privilegeDto.getName());
        privilege.setDescription(privilegeDto.getDescription());
        try {
            privilege.setMinMemberLevel(MemberLevel.Level.valueOf(privilegeDto.getMinMemberLevel().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid member level: " + privilegeDto.getMinMemberLevel());
        }
        privilege.setActive(privilegeDto.isActive());
        privilege.setBenefitPointAmount(privilegeDto.getBenefitPointAmount());
        privilege.setBenefitActionType(privilegeDto.getBenefitActionType());
        return privilege;
    }

    @Transactional
    public PrivilegeDto createPrivilege(PrivilegeDto privilegeDto) {
        privilegeRepository.findByName(privilegeDto.getName())
            .ifPresent(p -> {
                throw new DuplicatePrivilegeNameException("Privilege with name '" + privilegeDto.getName() + "' already exists.");
            });

        Privilege privilege = toEntity(privilegeDto);
        // Ensure minMemberLevel is valid (already handled in toEntity, but good to be aware)
        // MemberLevel.Level.valueOf(privilegeDto.getMinMemberLevel().toUpperCase()); 

        Privilege savedPrivilege = privilegeRepository.save(privilege);
        return toDto(savedPrivilege);
    }

    @Transactional
    public PrivilegeDto updatePrivilege(Long id, PrivilegeDto privilegeDto) {
        Privilege existingPrivilege = privilegeRepository.findById(id)
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + id));

        // Check for name duplication if the name is being changed
        if (!existingPrivilege.getName().equals(privilegeDto.getName())) {
            privilegeRepository.findByName(privilegeDto.getName())
                .ifPresent(p -> {
                    throw new DuplicatePrivilegeNameException("Privilege with name '" + privilegeDto.getName() + "' already exists.");
                });
        }

        existingPrivilege.setName(privilegeDto.getName());
        existingPrivilege.setDescription(privilegeDto.getDescription());
        try {
            existingPrivilege.setMinMemberLevel(MemberLevel.Level.valueOf(privilegeDto.getMinMemberLevel().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid member level: " + privilegeDto.getMinMemberLevel());
        }
        existingPrivilege.setActive(privilegeDto.isActive());
        existingPrivilege.setBenefitPointAmount(privilegeDto.getBenefitPointAmount());
        existingPrivilege.setBenefitActionType(privilegeDto.getBenefitActionType());

        Privilege updatedPrivilege = privilegeRepository.save(existingPrivilege);
        return toDto(updatedPrivilege);
    }

    @Transactional
    public void deletePrivilege(Long id) {
        Privilege privilege = privilegeRepository.findById(id)
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + id));
        privilegeRepository.delete(privilege);
    }

    @Transactional(readOnly = true)
    public PrivilegeDto getPrivilege(Long id) {
        Privilege privilege = privilegeRepository.findById(id)
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + id));
        return toDto(privilege);
    }

    @Transactional(readOnly = true)
    public Page<PrivilegeDto> getAllPrivileges(Pageable pageable) {
        return privilegeRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Privilege verifyAndGetPrivilege(Long privilegeId) {
        Privilege privilege = privilegeRepository.findById(privilegeId)
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + privilegeId));

        if (!privilege.isActive()) {
            throw new PrivilegeNotActiveException("Privilege with ID " + privilegeId + " is not active.");
        }
        return privilege;
    }

    @Transactional(readOnly = true)
    public boolean canMemberUsePrivilege(Long memberId, Long privilegeId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Privilege privilege = privilegeRepository.findById(privilegeId) // Re-fetch or use verifyAndGetPrivilege
            .orElseThrow(() -> new PrivilegeNotFoundException("Privilege not found with id: " + privilegeId));

        if (!privilege.isActive()) {
            return false; // Or throw PrivilegeNotActiveException depending on desired behavior in this specific method
        }

        MemberLevel.Level memberLevel = member.getMemberLevel().getLevel();
        MemberLevel.Level requiredLevel = privilege.getMinMemberLevel();

        return memberLevel.ordinal() >= requiredLevel.ordinal();
    }

    @Transactional(readOnly = true)
    public List<PrivilegeDto> getAvailablePrivilegesForMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        MemberLevel.Level memberLevel = member.getMemberLevel().getLevel();

        List<Privilege> availablePrivileges = privilegeRepository.findAll()
            .stream()
            .filter(Privilege::isActive)
            .filter(privilege -> memberLevel.ordinal() >= privilege.getMinMemberLevel().ordinal())
            .collect(Collectors.toList());

        return availablePrivileges.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
}
