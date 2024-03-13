package com.siyufeng.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * 静态文件生成器
 *
 * @author 司雨枫
 */
public class StaticGenerator {

    public static void main(String[] args) throws IOException {
        String projectPath = System.getProperty("user.dir");
        //输入路径
        String inputPath = projectPath + File.separatorChar + "sasa-generator-demo-projects" + File.separatorChar + "acm-template";
        //输出路径
        String outputPath = projectPath;
//        copyFilesByRecursive(inputPath, outputPath);
    }

    /**
     * 拷贝文件（实现方式1：Hutool工具类实现）
     *
     * @param inputPath  输入目录
     * @param outputPath 输出目录
     */
    public static void copyFilesByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, false);
        int[] a = new int[6];

    }


    /**
     * 拷贝文件（实现方式2：递归复制）
     *
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesByRecursive(String inputPath, String outputPath) {
        File inputFile = new File(inputPath);
        File outputFile = new File(outputPath);
        try {
            copyFilesByRecursive(inputFile, outputFile);
        } catch (Exception e) {
            System.err.println("文件复制失败");
            e.printStackTrace();
        }
    }

    /**
     * 文件 A => 目录 B，则文件 A 放在目录 B 下
     * 文件 A => 文件 B，则文件 A 覆盖文件 B
     * 目录 A => 目录 B，则目录 A 放在目录 B 下
     *
     * @param inputFile
     * @param outputFile
     * @throws IOException
     */
    private static void copyFilesByRecursive(File inputFile, File outputFile) throws IOException {
        // 如果要复制的是目录
        if (inputFile.isDirectory()) {
            System.out.println(inputFile.getName());
            File destOutputFile = new File(outputFile, inputFile.getName());
            // 如果目的地还是目录，先创建
            if (!destOutputFile.exists()) {
                destOutputFile.mkdirs();
            }
            // 获取输入目录下的所有文件和子目录
            File[] files = inputFile.listFiles();
            // 无子文件，直接结束
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File file : files) {
                // 递归拷贝下一层文件
                copyFilesByRecursive(file, destOutputFile);
            }
        } else {
            // 要复制的是文件
            Path destPath = outputFile.toPath().resolve(inputFile.getName());
            Files.copy(inputFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }


}
