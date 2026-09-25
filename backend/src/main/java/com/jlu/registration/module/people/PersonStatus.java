package com.jlu.registration.module.people;

/**
 * 人员档案状态。学生和教师共用这一组简单状态，避免为了课程作业引入两套重复枚举。
 */
public enum PersonStatus {
    ACTIVE,
    INACTIVE,
    GRADUATED,
    ON_LEAVE
}
