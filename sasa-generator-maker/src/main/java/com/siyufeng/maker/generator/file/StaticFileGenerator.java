package com.siyufeng.maker.generator.file;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成器
 *
 * @author 司雨枫
 */
public class StaticFileGenerator {

    /**
     * 拷贝文件（实现方式1：Hutool工具类实现）
     *
     * @param inputPath  输入目录
     * @param outputPath 输出目录
     */
    public static void copyFilesByHutool(String inputPath, String outputPath) {
        FileUtil.copy(inputPath, outputPath, false);
    }

}
