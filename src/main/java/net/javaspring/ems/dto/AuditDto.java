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
    private Long id;
    private String title;
    private String content;
    private Long amountMoney;
    private Integer numOfApprovalRequests;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    /* private List<AuditApprove> approvals; */
}
