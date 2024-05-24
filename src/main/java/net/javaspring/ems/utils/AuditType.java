package net.javaspring.ems.utils;

/**
 * 审批类型
 *
 * 命名以 ** 如 11 *** 如 111. 个位数表示审批等级（数字1为最低等级） 其余数字为审批类型
 *
 * 1x : 请假审批
 * 2x : 项目审批
 * 3x : 资金审批
 */
public enum AuditType {
    LEAVING_FORM_LESS_THAN_3_DAYS(12),
    LEAVING_FORM_LESS_THAN_7_DAYS(13),
    LEAVING_FORM_MORE_THAN_7_DAYS(14),
    BAISC_PROJECT_APPLYING(22),
    MEDIUM_PROJECT_APPLYING(23);


    private final int value;

    AuditType(int i) {
        value = i;
    }

    public int getValue() {
        return value;
    }
}
