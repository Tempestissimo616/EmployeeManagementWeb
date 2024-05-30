package net.javaspring.ems.service;

import net.javaspring.ems.dto.AuditApproveDto;
import net.javaspring.ems.dto.AuditDto;
import net.javaspring.ems.entity.Audit;
import net.javaspring.ems.entity.AuditApprove;

import java.util.List;

public interface AuditService {

    AuditDto createAudit(AuditDto auditDto, String username);

    AuditDto getAuditById(Long auditId3);

    List<AuditDto> getAllAudit();

    List<AuditApprove> getAllApprovalsById(Long auditId);

    AuditDto userApproveAudit(AuditApproveDto auditApproveDto, String username);
}
