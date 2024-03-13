package com.siyufeng.generator;

import com.siyufeng.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * @author 司雨枫
 */
public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        // 创建 Map 对象，作为模板数据模型
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("syfnb");
        mainTemplateConfig.setLoop(false);
        mainTemplateConfig.setOutputText("输出结果");

        doGenerate(mainTemplateConfig);
    }

    public static void doGenerate(Object model) throws TemplateException, IOException {
        //静态文件生成
        String projectPath = System.getProperty("user.dir");
        //输入路径
        String inputPath = projectPath + File.separatorChar + "sasa-generator-demo-projects" + File.separatorChar + "acm-template";
        //输出路径
        String outputPath = projectPath;
        //复制
        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);

        //动态文件生成
        String dynamicInputPath = projectPath + File.separator + "sasa-generator-basic" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";
        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
