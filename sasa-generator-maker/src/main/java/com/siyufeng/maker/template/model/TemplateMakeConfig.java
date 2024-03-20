package com.siyufeng.maker.template.model;

import com.siyufeng.maker.meta.Meta;
import lombok.Data;

/**
 * @author 司雨枫
 * 模板制作配置
 */
@Data
public class TemplateMakeConfig {

    private Long id;

    private Meta meta = new Meta();

    private String originProjectPath;

    private TemplateMakerFileConfig fileConfig = new TemplateMakerFileConfig();

    private TemplateMakerModelConfig modelConfig = new TemplateMakerModelConfig();

    private TemplateMakerOutputConfig templateMakerOutputConfig = new TemplateMakerOutputConfig();

}
