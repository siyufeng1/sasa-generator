package com.siyufeng.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.siyufeng.maker.generator.JarGenerator;
import com.siyufeng.maker.generator.ScriptGenerator;
import com.siyufeng.maker.generator.file.DynamicFileGenerator;
import com.siyufeng.maker.meta.Meta;
import com.siyufeng.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * @author 司雨枫
 */
public abstract class GenerateTemplate {
    public void doGenerate() throws IOException, TemplateException, InterruptedException {
        Meta meta = MetaManager.getMeta();
        System.out.println(meta);

        //输出的根路径
        String projectsPath = System.getProperty("user.dir");
        String outputPath = projectsPath + File.separator + "generated" + File.separator + "acm-template-pro-generator";
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }

        //1.复制原始文件
        String sourceCopyDestPath = copySource(meta, outputPath);

        //2.代码生成
        generateCode(meta, outputPath);

        //3.构建 jar 包
        String jarPath = buildJar(outputPath, meta);

        //4.封装脚本
        String shellOutputFilePath = buildScript(outputPath, jarPath);

        //5.生成精简版的程序（产物包）
        buildDist(outputPath, sourceCopyDestPath, shellOutputFilePath, jarPath);
    }

    protected String buildScript(String outputPath, String jarPath) throws IOException {
        String shellOutputFilePath = outputPath + File.separator + "generator";
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
        return shellOutputFilePath;
    }

    protected void buildDist(String outputPath, String sourceCopyDestPath, String shellOutputFilePath, String jarPath) {
        String distOutputPath = outputPath + "-dist";
        //  - 拷贝jar包
        String targetAbsolutePath = distOutputPath + File.separator + "target";
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath = outputPath + File.separator + jarPath;
        FileUtil.copy(jarAbsolutePath, targetAbsolutePath, true);
        //  - 拷贝脚本文件
        FileUtil.copy(shellOutputFilePath, distOutputPath, true);
        FileUtil.copy(shellOutputFilePath + ".bat", distOutputPath, true);
        //  - 拷贝模板文件
        FileUtil.copy(sourceCopyDestPath, distOutputPath, true);
    }

    protected String  buildJar(String outputPath,Meta meta) throws InterruptedException, IOException {
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        return jarPath;
    }

    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        //读取resources目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        //java包基础路径
        //com.siyufeng
        String outputBasePackage = meta.getBasePackage();
        //com/siyufeng
        String outputBasePackagePath = StrUtil.join("/", StrUtil.split(outputBasePackage, "."));
        //src/main/java/com/siyufeng
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java" + File.separator + outputBasePackagePath;

        String inputFilePath;
        String outputFilePath;

        //model.DataModel
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "/model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GeneratorCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/GeneratorCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.StaticFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.DynamicFileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicFileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicFileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // generator.FileGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/FileGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/FileGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);

        //生成READMD.md
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath + File.separator + "README.md";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    protected String copySource(Meta meta, String outputPath) {
        //从原始模板文件路径复制到生成的代码包中
        String sourceRootPath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyDestPath = outputPath + File.separator + ".source";
        FileUtil.copy(sourceRootPath, sourceCopyDestPath, true);
        return sourceCopyDestPath;
    }
}
