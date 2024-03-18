package com.siyufeng.maker.template.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * @author 司雨枫
 * 文件过滤规则枚举
 */
@Getter
public enum FileFilterRuleEnum {

    CONTAINS("包含","contains"),
    STARTS_WITH("前缀匹配","starts_with"),
    END_WITHS("后缀匹配","end_withs"),
    REGEX("正则","regex"),
    EQUALS("相等","equals");

    private final String text;
    private final String value;

    FileFilterRuleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static FileFilterRuleEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        for (FileFilterRuleEnum fileFilterRuleEnum : FileFilterRuleEnum.values()) {
            if (fileFilterRuleEnum.value.equals(value)) {
                return fileFilterRuleEnum;
            }
        }
        return null;
    }
}
