package net.javaspring.ems.repository;

import net.javaspring.ems.entity.Audit;
import net.javaspring.ems.entity.AuditApprove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditApproveRepository extends JpaRepository<AuditApprove, Long> {
    List<AuditApprove> findByAuditId(Long auditId);

    @Query("SELECT aa FROM AuditApprove aa WHERE aa.audit = :audit AND aa.auditLevelOrder = :auditLevelOrder")
    List<AuditApprove> findAllCurrentLevelApprovals(@Param("audit") Audit audit, @Param("auditLevelOrder") int auditLevelOrder);

}
