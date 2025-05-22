package com.example.memberservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.PointBalance;
import com.example.memberservice.entity.PointRecord;
import com.example.memberservice.entity.PointTransaction;
import com.example.memberservice.dto.PrivilegeUseRequestDto;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.exception.*;
import com.example.memberservice.repository.*;
import lombok.RequiredArgsConstructor; // Added for constructor injection

@Service
@RequiredArgsConstructor // Added for constructor injection
public class PointService {
    private final PointRecordRepository pointRecordRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointBalanceRepository pointBalanceRepository;
    private final MemberRepository memberRepository; // Added
    private final PrivilegeService privilegeService; // Added
    private final PrivilegeUsageLogService privilegeUsageLogService; // Added

    // Constructor removed as @RequiredArgsConstructor will handle it

    @Transactional
    public void redeemPoints(Member member, Integer pointsToRedeem, String description) {
        List<PointRecord> availableRecords = pointRecordRepository.findAvailablePoints(member, LocalDateTime.now());

        int remaining = pointsToRedeem;

        for (PointRecord record : availableRecords) {
            if (record.getRemainingPoints() == 0) continue;

            int used = Math.min(remaining, record.getRemainingPoints());
            record.setRemainingPoints(record.getRemainingPoints() - used);
            remaining -= used;

            if (remaining <= 0) break;
        }

        if (remaining > 0) {
            throw new InsufficientPointBalanceException("Not enough points to redeem.");
        }

        pointRecordRepository.saveAll(availableRecords);

        PointTransaction txn = new PointTransaction();
        txn.setMember(member);
        txn.setPoints(-pointsToRedeem);
        txn.setType("REDEEM");
        txn.setTransactionDate(LocalDateTime.now());
        txn.setDescription(description);
        pointTransactionRepository.save(txn);

        PointBalance balance = pointBalanceRepository.findByMember(member).orElseThrow(() -> new PointBalanceNotFoundException(member.getId()));
        balance.setTotalPoints(balance.getTotalPoints() - pointsToRedeem);
        balance.setUsedPoints(balance.getUsedPoints() + pointsToRedeem);
        balance.setLastUpdated(LocalDateTime.now());
        pointBalanceRepository.save(balance);
    }

    @Transactional
    public void earnPoints(Member member, Integer pointsEarned, String source, String description, LocalDateTime expireAt) {
        PointRecord pointRecord = new PointRecord();
        pointRecord.setMember(member);
        pointRecord.setPoints(pointsEarned);
        pointRecord.setRemainingPoints(pointsEarned);
        pointRecord.setReceivedDate(LocalDateTime.now());
        pointRecord.setExpireDate(expireAt);
        pointRecord.setSource(source);
        pointRecordRepository.save(pointRecord);

        PointTransaction txn = new PointTransaction();
        txn.setMember(member);
        txn.setPoints(pointsEarned);
        txn.setType("EARN");
        txn.setTransactionDate(LocalDateTime.now());
        txn.setDescription(description);
        pointTransactionRepository.save(txn);

        PointBalance balance = pointBalanceRepository.findByMember(member)
            .orElseGet(() -> {
                PointBalance b = new PointBalance();
                b.setMember(member);
                b.setTotalPoints(0);
                b.setUsedPoints(0);
                return b;
            });
        balance.setTotalPoints(balance.getTotalPoints() + pointsEarned);
        balance.setLastUpdated(LocalDateTime.now());
        pointBalanceRepository.save(balance);
    }

    @Transactional
    public void applyPrivilegeForPointBenefit(Long memberId, PrivilegeUseRequestDto request) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("Member not found with id: " + memberId));

        Privilege privilege = privilegeService.verifyAndGetPrivilege(request.getPrivilegeId());

        if (!privilegeService.canMemberUsePrivilege(memberId, request.getPrivilegeId())) {
            throw new PrivilegeNotAuthorizedException("Member is not authorized to use this privilege.");
        }

        String actionType = privilege.getBenefitActionType();
        if (actionType == null) {
            // Fallback to name if actionType is null, or handle as an error
            // For now, let's assume if actionType is null, it's an unsupported privilege for direct point benefits
            throw new UnsupportedOperationException("Privilege action type is null and cannot be processed for point benefits.");
        }

        switch (actionType) {
            case "EARN_POINTS":
                if (privilege.getBenefitPointAmount() != null && privilege.getBenefitPointAmount() > 0) {
                    this.earnPoints(member, privilege.getBenefitPointAmount(), "PRIVILEGE_BENEFIT",
                        "Earned points via privilege: " + privilege.getName(), LocalDateTime.now().plusYears(1));
                    privilegeUsageLogService.logPrivilegeUsage(memberId, privilege.getId(),
                        "Used privilege: " + privilege.getName() + " to earn " + privilege.getBenefitPointAmount() + " points.");
                } else {
                    throw new IllegalStateException("Privilege " + privilege.getName() + " is of type EARN_POINTS but has no valid point amount.");
                }
                break;
            case "EXAMPLE_DISCOUNT":
                // Assuming params contains originalCost
                Object originalCostObj = request.getParams() != null ? request.getParams().get("originalCost") : null;
                if (!(originalCostObj instanceof Number)) {
                    throw new IllegalArgumentException("Missing or invalid 'originalCost' in request parameters for EXAMPLE_DISCOUNT privilege.");
                }
                double originalCost = ((Number) originalCostObj).doubleValue();

                if (privilege.getBenefitPointAmount() == null || privilege.getBenefitPointAmount() <= 0 || privilege.getBenefitPointAmount() > 100) {
                     throw new IllegalStateException("Privilege " + privilege.getName() + " is of type EXAMPLE_DISCOUNT but has no valid discount percentage (1-100).");
                }
                int discountPercentage = privilege.getBenefitPointAmount();
                int discountAmountInPoints = (int) (originalCost * (discountPercentage / 100.0)); // Example: treat discount as points

                if (discountAmountInPoints > 0) {
                    this.earnPoints(member, discountAmountInPoints, "PRIVILEGE_DISCOUNT",
                        "Discount via privilege: " + privilege.getName(), LocalDateTime.now().plusYears(1));
                    privilegeUsageLogService.logPrivilegeUsage(memberId, privilege.getId(),
                        "Used privilege: " + privilege.getName() + " for a discount equivalent to " + discountAmountInPoints + " points on an item costing " + originalCost + ".");
                } else {
                     privilegeUsageLogService.logPrivilegeUsage(memberId, privilege.getId(),
                        "Used privilege: " + privilege.getName() + " for a discount on an item costing " + originalCost + ", but discount amount was zero points.");
                }
                break;
            default:
                throw new UnsupportedOperationException("Privilege action type '" + privilege.getBenefitActionType() + "' is not supported yet.");
        }
    }
}
