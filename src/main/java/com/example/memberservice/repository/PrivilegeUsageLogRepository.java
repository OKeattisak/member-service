package com.example.memberservice.repository;

import com.example.memberservice.entity.PrivilegeUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegeUsageLogRepository extends JpaRepository<PrivilegeUsageLog, Long> {
}
