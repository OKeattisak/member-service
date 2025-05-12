package com.example.memberservice.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.PointTransaction;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    List<PointTransaction> findByMemberOrderByTransactionDateDesc(Member member);
    List<PointTransaction> findByMemberAndTransactionDateBetween(Member member, LocalDateTime start, LocalDateTime end);
}
