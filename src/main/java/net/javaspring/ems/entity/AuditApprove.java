package net.javaspring.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "audit_approve")
public class AuditApprove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 默认为"" 说明此人还未批准
     * approved 审批通过
     * refused 审批拒绝
     */
    @Column(name = "approved", nullable = true)
    private String approved;

    @Column(name = "approval_time", nullable = true)
    private LocalDateTime approvalTime;

    /**
     * 等级1最低 等级9最高 审批类型决定等级高低 某些情况可以自己批准自己
     */
    @Column(name = "audit_level_order")
    private int auditLevelOrder;


    @Column(name = "is_last_level")
    private boolean isLastLevel;

    /**
     * status: waiting(前面节点还在审批)， process(当前节点正在审批)， complete(审批完成)， cancel(前面节点未通过 或者其他人已经允许)
     */
    @Column(name = "approved_status")
    private String approvedStatus;
}
