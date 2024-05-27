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

    /** 审批方式 （True: Any 只需一人允许, False: All 所有人都要同意） */
    @Column(name = "require_all_approval_passing", nullable = false)
    private boolean requireAllApprovalPassing;

    /** 同级是否需要审批 */
    @Column(name = "require_peer_review")
    private boolean requirePeerReview;

    /** 是否允许越级审批 */
    @Column(name = "is_allowed_to_leapfrog")
    private boolean isAllowedToLeapfrog;

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

    /** 审批状态 0代表自批 1代表审批中 2代表审批成功 3代表审批失败 */
    @Column(name = "status", columnDefinition = "TINYINT(4)")
    private Integer status;

    /** 添加审批时间 */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 审批更新上传时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;



}
