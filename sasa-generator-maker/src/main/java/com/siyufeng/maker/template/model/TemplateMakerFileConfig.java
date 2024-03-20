package com.siyufeng.maker.template.model;

import com.siyufeng.maker.meta.Meta;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 司雨枫
 */
@Data
public class TemplateMakerFileConfig {

    private List<FileInfoConfig> files;

    private FileGroupConfig fileGroupConfig;

    @Data
    @NoArgsConstructor
    public static class FileInfoConfig {
        /**
         * 文件（目录）路径
         */
        private String path;

        /**
         * 控制单个文件是否生成
         */
        private String condition;

        /**
         * 文件过滤配置
         */
        private List<FileFilterConfig> fileFilterConfigList;
    }


    @Data
    public static class FileGroupConfig {

        private String condition;

        private String groupKey;

        private String groupName;
    }

}
