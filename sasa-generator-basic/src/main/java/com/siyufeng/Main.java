package com.siyufeng;

import com.siyufeng.cli.CommandExecutor;
import picocli.CommandLine;

/**
 * @author 司雨枫
 */
public class Main {
   public static void main(String[] args) {
//      args = new String[]{"generate", "-l", "-a", "-o"};
//      args = new String[]{"config"};
//      args = new String[]{"list"};
      CommandExecutor commandExecutor = new CommandExecutor();
      commandExecutor.doExecute(args);
   }
}
