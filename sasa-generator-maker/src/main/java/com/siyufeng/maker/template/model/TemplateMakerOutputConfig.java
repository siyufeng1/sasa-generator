package com.siyufeng.maker.template.model;

import lombok.Data;

/**
 * @author 司雨枫
 */
@Data
public class TemplateMakerOutputConfig {
    // 从未分组文件中移除组内的同名文件
    private boolean removeGroupFilesFromRoot = true;
}
