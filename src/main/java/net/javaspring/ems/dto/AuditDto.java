package net.javaspring.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.javaspring.ems.entity.AuditApprove;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditDto {
    private Long userId;
    private Integer auditType;
    private boolean requireAllApprovalPassing;
    private boolean requirePeerReview;
    private boolean allowedToLeapfrog;
    private String title;
    private String content;
    private Long amountMoney;
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<AuditApproveDto> approvals;
}
