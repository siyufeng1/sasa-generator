package com.siyufeng.maker;

//import com.siyufeng.maker.cli.CommandExecutor;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author 司雨枫
 */
public class Main {
   public static void main(String[] args) {
//      args = new String[]{"generate", "-l", "-a", "-o"};
//      args = new String[]{"config"};
//      args = new String[]{"list"};
//      CommandExecutor commandExecutor = new CommandExecutor();
//      commandExecutor.doExecute(args);

      String defaultInputRootPath = ".source" + File.separator +
              Paths.get("D:/sasa-generator/sasa-generator-maker").getFileName().toString();
      System.out.println(defaultInputRootPath);
   }
}
