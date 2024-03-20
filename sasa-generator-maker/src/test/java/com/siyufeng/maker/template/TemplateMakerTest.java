package com.siyufeng.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.siyufeng.maker.meta.Meta;
import com.siyufeng.maker.template.enums.FileFilterRangeEnum;
import com.siyufeng.maker.template.enums.FileFilterRuleEnum;
import com.siyufeng.maker.template.model.FileFilterConfig;
import com.siyufeng.maker.template.model.TemplateMakeConfig;
import com.siyufeng.maker.template.model.TemplateMakerFileConfig;
import com.siyufeng.maker.template.model.TemplateMakerModelConfig;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author 司雨枫
 */
public class TemplateMakerTest {

    /**
     * 测试 Bug 修复1：同配置多次生成，强制变为静态生成
     */
    @Test
    public void testMakeTemplateBug1() {
        Meta meta = new Meta();
        meta.setName("spring-boot-init-generator");
        meta.setDescription("Spring Boot 初始化模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "sasa-generator-demo-projects/springboot-init";

        // 文件参数配置
        String inputFilePath1 = "src/main/resources/application.yml";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();

        // - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));

        Long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1L,null);
        System.out.println(id);
    }


    /**
     * 测试 Bug 修复2：同目录多次生成时，会扫描新的 .ftl 文件
     */
    @Test
    public void testMakeTemplateBug2() {
        Meta meta = new Meta();
        meta.setName("spring-boot-init-generator");
        meta.setDescription("Spring Boot 初始化模板生成器");

        String projectPath = System.getProperty("user.dir");
        String originProjectPath = new File(projectPath).getParent() + File.separator + "sasa-generator-demo-projects/springboot-init";

        // 文件参数配置
        String inputFilePath1 = "src/main/java/com/yupi/springbootinit/common";
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(inputFilePath1);
        templateMakerFileConfig.setFiles(Arrays.asList(fileInfoConfig1));

        // 模型参数配置
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("className");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setReplaceText("BaseResponse");
        templateMakerModelConfig.setModels(Arrays.asList(modelInfoConfig1));

        Long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, 1L,null);
        System.out.println(id);
    }

    /**
     * 使用 JSON 制作模板
     */
    @Test
    public void testMakeTemplateWithJSON(){
        String configStr = ResourceUtil.readUtf8Str("templateMaker.json");
        TemplateMakeConfig templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        Long id = TemplateMaker.makeTemplate(templateMakeConfig);
        System.out.println(id);
    }


    @Test
    public void makeSpringBootTemplate(){
        String rootPath = "examples/springboot-init/";
        String configStr = ResourceUtil.readUtf8Str(rootPath + "templateMaker.json");
        TemplateMakeConfig templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        Long id = TemplateMaker.makeTemplate(templateMakeConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker1.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);


        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker2.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);


        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker3.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker4.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker5.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker6.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);

        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker7.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);


        configStr = ResourceUtil.readUtf8Str(rootPath + File.separator +"templateMaker8.json");
        templateMakeConfig = JSONUtil.toBean(configStr, TemplateMakeConfig.class);
        TemplateMaker.makeTemplate(templateMakeConfig);
        System.out.println(id);


    }

}