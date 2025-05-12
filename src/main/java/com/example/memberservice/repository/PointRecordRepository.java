package com.example.memberservice.repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.PointRecord;

@Repository
public interface PointRecordRepository extends JpaRepository<PointRecord, Long> {
    @Query("SELECT pr FROM PointRecord pr WHERE pr.member = :member AND pr.remainingPoints > 0 AND pr.expireDate > :now ORDER BY pr.receivedDate ASC")
    List<PointRecord> findAvailablePoints(Member member, LocalDateTime now);

    List<PointRecord> findByExpireDateBefore(LocalDateTime now);

    @Query("SELECT COALESCE(SUM(pr.remainingPoints), 0) FROM PointRecord pr WHERE pr.member = :member AND pr.expireDate > :now")
    Integer sumAvailablePoints(Member member, LocalDateTime now);
}
