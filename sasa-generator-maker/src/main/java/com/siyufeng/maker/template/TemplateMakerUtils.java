package com.siyufeng.maker.template;

import cn.hutool.core.util.StrUtil;
import com.siyufeng.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 司雨枫
 * 模板制作工具类
 */
public class TemplateMakerUtils {

    /**
     * 从未分组的文件中移除组内的同名文件
     *
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfig.FileInfo> removeGroupFilesFromRoot(List<Meta.FileConfig.FileInfo> fileInfoList) {
        //先获取到所有分组
        List<Meta.FileConfig.FileInfo> groupFileInfoList = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(Collectors.toList());

        //获取到所有分组内的文件列表
        List<Meta.FileConfig.FileInfo> groupInnerFileInfoList = groupFileInfoList.stream()
                .flatMap(fileInfo -> fileInfo.getFiles().stream())
                .collect(Collectors.toList());


        //获取所有分组内文件的输入路径集合
        Set<String> fileInputPathSet = groupInnerFileInfoList.stream()
                .map(Meta.FileConfig.FileInfo::getInputPath)
                .collect(Collectors.toSet());

        //移除在集合内的外层文件
        List<Meta.FileConfig.FileInfo> finalList = fileInfoList.stream()
                .filter(fileInfo ->
                        //如果不在集合内才保留
                        !fileInputPathSet.contains(fileInfo.getInputPath())
                )
                .collect(Collectors.toList());

        return finalList;
    }
}
