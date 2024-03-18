package com.siyufeng.maker.template.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

/**
 * @author 司雨枫
 */
@Data
public class TemplateFileConfig {

    private List<FileInfoConfig> files;

    @Data
    @NoArgsConstructor
    static class FileInfoConfig {
        private String path;
        private List<TemplateFileConfig> filterConfigList;
    }

}
