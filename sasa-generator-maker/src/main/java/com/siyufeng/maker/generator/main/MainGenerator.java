package com.siyufeng.maker.generator.main;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author 司雨枫
 */
@Slf4j
public class MainGenerator extends GenerateTemplate{

    @Override
    protected void buildDist(String outputPath, String sourceCopyDestPath, String shellOutputFilePath, String jarPath) {
        System.out.println("不生成精简版");
    }




    public static void main(String[] args) throws IOException, TemplateException, InterruptedException {
        MainGenerator mainGenerator = new MainGenerator();
        mainGenerator.doGenerate();
    }
}
