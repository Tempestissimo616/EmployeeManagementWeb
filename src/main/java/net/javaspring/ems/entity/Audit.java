package net.javaspring.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import net.javaspring.ems.utils.AuditType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批表 audit
 *
 * @author Phoenix
 */


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit")
public class Audit {

    /** 审批ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** 审配类型 */
    @Column(name = "audit_type", nullable = false)
    private AuditType auditType;

    /** 审批方式 （Any 只需一人允许， All 所有人都要同意） */
    @Column(name = "is_any_approval_allowed", nullable = false)
    private boolean isAnyApprovalAllowed;

    /** 审批标题 */
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    /** 审批内容 */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 审批金额 */
    @Column(name = "amountMoney")
    private Long amountMoney;

    /** 审批和批准的关系 */
    @ToString.Exclude
    @OneToMany(mappedBy = "audit", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH}, fetch = FetchType.EAGER)
    private List<AuditApprove> approvals;

    @Column(name = "num_approval_requests", nullable = false)
    private Integer numOfApprovalRequests;

    /** 审批状态 0代表草稿 1代表审批中 2代表审批成功 3代表审批失败 */
    @Column(name = "status", columnDefinition = "TINYINT(4)")
    private Integer status;

    /** 添加审批时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 审批结束上传时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;



}
