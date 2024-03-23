package com.siyufeng.maker;

//import com.siyufeng.maker.cli.CommandExecutor;

import cn.hutool.core.io.FileUtil;
import com.siyufeng.maker.generator.main.GenerateTemplate;
import com.siyufeng.maker.generator.main.MainGenerator;
import com.siyufeng.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author 司雨枫
 */
public class Main {
   public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
      GenerateTemplate generateTemplate = new ZipGenerator();
      generateTemplate.doGenerate();
   }
}
