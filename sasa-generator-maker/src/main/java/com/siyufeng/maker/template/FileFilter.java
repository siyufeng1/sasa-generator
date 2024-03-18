package com.siyufeng.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.siyufeng.maker.template.enums.FileFilterRangeEnum;
import com.siyufeng.maker.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;

/**
 * @author 司雨枫
 */
public class FileFilter {

    /**
     * 单个文件过滤
     * @param fileFilterConfigList
     * @param file
     * @return
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file){
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        //所有过滤器校验结束后的结果
        boolean result = true;

        if(CollUtil.isEmpty(fileFilterConfigList)){
            return true;
        }

        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            FileFilterRangeEnum fileFilterRangeEnum = FileFilterRangeEnum.getEnumByValue(value);
            if(fileFilterRangeEnum == null){
                continue;
            }
            FileFilterRangeEnum enumByValue = FileFilterRangeEnum.getEnumByValue(rule);

        }
    }
}
