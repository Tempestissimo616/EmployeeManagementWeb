package net.javaspring.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditApproveDto {
    private Long userId;
    private Long auditId;
    private String approvedStatus;
    private String approved;
    private Integer auditLevelOrder;
    private boolean isLastLevel;
}
