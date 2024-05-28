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

    /**
     * 默认为"" 说明此人还未批准
     * approved 审批通过
     * refused 审批拒绝
     */
    private String approved;
    private int auditLevelOrder;
    private boolean isLastLevel;
    private String approvedStatus;
}
