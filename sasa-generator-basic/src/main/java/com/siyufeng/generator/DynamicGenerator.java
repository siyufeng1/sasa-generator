package com.siyufeng.generator;

import com.siyufeng.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态文件生成器
 *
 * @author 司雨枫
 */
public class DynamicGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        String projectPath = System.getProperty("user.dir") + File.separator + "sasa-generator-basic";
        String inputPath = projectPath + File.separator + "src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath = projectPath + File.separator + "MainTemplate1.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        // 创建 Map 对象，作为模板数据模型
        mainTemplateConfig.setAuthor("syfnb");
        mainTemplateConfig.setLoop(true);
        mainTemplateConfig.setOutputText("输出结果");
        //调用process方法，处理并生成文件
        doGenerate(inputPath, outputPath, mainTemplateConfig);
    }


    /**
     * @param inputPath  模板文件输入路径
     * @param outputPath 生成代码的输出路径
     * @param model      参数配置
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);

        // 指定模板文件所在的路径，模板文件的父级目录
        File templateDir = new File(inputPath).getParentFile();
        configuration.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");
        // *修改数字格式
        configuration.setNumberFormat("0.######");

        //创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);

        //指定生成的文件
        Writer out = new FileWriter(outputPath);
        //调用process方法，处理并生成文件
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }
}
