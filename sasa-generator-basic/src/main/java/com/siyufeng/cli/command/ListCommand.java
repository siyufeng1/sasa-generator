package com.siyufeng.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

/**
 * @author 司雨枫
 */
@CommandLine.Command(name = "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable{
    @Override
    public void run() {
        // 当前打开的窗口
        String projectPath = System.getProperty("user.dir");
        // 父级目录，项目根目录
        File parentFile = new File(projectPath);
        // 输入路径
        String inputPath = new File(parentFile, "sasa-generator-demo-projects/acm-template").getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath);
        for (File file : files) {
            // 打印文件信息
            System.out.println(file);
        }
    }
}
