package com.siyufeng.model;

import lombok.Data;

/**
 * @author 司雨枫
 * 数据模型
 */
@Data
public class MainTemplateConfig {
    /**
     * 是否生成循环
     */
    private boolean loop;
    /**
     * 作者注释
     */
    private String author;
    /**
     * 输出信息
     */
    private String outputText;
}
