package com.example.memberservice.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.PointBalance;
import com.example.memberservice.entity.PointRecord;
import com.example.memberservice.entity.PointTransaction;
import com.example.memberservice.exception.InsufficientPointBalanceException;
import com.example.memberservice.exception.PointBalanceNotFoundException;
import com.example.memberservice.repository.PointBalanceRepository;
import com.example.memberservice.repository.PointRecordRepository;
import com.example.memberservice.repository.PointTransactionRepository;

@Service
public class PointService {
    private final PointRecordRepository pointRecordRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointBalanceRepository pointBalanceRepository;

    public PointService(PointRecordRepository pointRecordRepository, PointTransactionRepository pointTransactionRepository, PointBalanceRepository pointBalanceRepository) {
        this.pointRecordRepository = pointRecordRepository;
        this.pointTransactionRepository = pointTransactionRepository;
        this.pointBalanceRepository = pointBalanceRepository;
    }
    
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
}
