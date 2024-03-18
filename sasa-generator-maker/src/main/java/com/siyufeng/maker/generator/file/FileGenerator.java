package com.siyufeng.maker.generator.file;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * @author 司雨枫
 */
public class FileGenerator {

    public static void doGenerate(Object model) throws TemplateException, IOException {
        //静态文件生成
        String projectPath = System.getProperty("user.dir");
        //输入路径
        String inputPath = projectPath + File.separatorChar + "sasa-generator-demo-projects" + File.separatorChar + "acm-template";
        //输出路径
        String outputPath = projectPath;
        //复制
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);

        //动态文件生成
        String dynamicInputPath = projectPath + File.separator + "sasa-generator-maker" + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separator + "acm-template/src/com/yupi/acm/MainTemplate.java";
        DynamicFileGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
