package net.javaspring.ems.repository;

import net.javaspring.ems.entity.AuditApprove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditApproveRepository extends JpaRepository<AuditApprove, Long> {
    List<AuditApprove> findByAuditId(Long auditId);
}
