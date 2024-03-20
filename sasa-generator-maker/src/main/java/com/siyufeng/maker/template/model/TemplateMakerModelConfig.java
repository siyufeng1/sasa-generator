package com.siyufeng.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模板制作模型配置
 */
@Data
public class TemplateMakerModelConfig {

    private List<ModelInfoConfig> models;

    private ModelGroupConfig modelGroupConfig;

    @NoArgsConstructor
    @Data
    public static class ModelInfoConfig {

        // 替换成哪些文本
        private String fieldName;

        private String type;

        private String description;

        private Object defaultValue;

        private String abbr;

        //用于替换的参数名称
        private String replaceText;
    }

    @Data
    public static class ModelGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;

        private String type;

        private String description;
    }
}