package com.siyufeng.maker.meta;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.siyufeng.maker.meta.Meta.FileConfig;
import com.siyufeng.maker.meta.Meta.ModelConfig;
import com.siyufeng.maker.meta.enums.FileGeneratorEnum;
import com.siyufeng.maker.meta.enums.FileTypeEnum;
import com.siyufeng.maker.meta.enums.ModelTypeEnum;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author 司雨枫
 */
public class MetaValidator {

    public static void validateAndFill(Meta meta){
        validAndFillMetaRoot(meta);


        
        validAndFillFileConfig(meta);

        validAndFillModelConfig(meta);

    }

    private static void validAndFillModelConfig(Meta meta) {
        //modelConfig校验和默认值
        ModelConfig modelConfig = meta.getModelConfig();
        if (modelConfig == null) {
            return;
        }
        List<ModelConfig.ModelInfo> modelInfoList = modelConfig.getModels();
        if (CollectionUtil.isNotEmpty(modelInfoList)) {
            for (ModelConfig.ModelInfo modelInfo : modelInfoList) {
                // 输出路径默认值
                String fieldName = modelInfo.getFieldName();
                if (StrUtil.isBlank(fieldName)) {
                    throw new MetaException("未填写 fieldName");
                }

                String modelInfoType = modelInfo.getType();
                if (StrUtil.isEmpty(modelInfoType)) {
                    modelInfo.setType(ModelTypeEnum.STRING.getValue());
                }
            }
        }
    }

    private static void validAndFillFileConfig(Meta meta) {
        //fileConfig校验和默认值
        FileConfig fileConfig = meta.getFileConfig();
        if (fileConfig == null) {
            return;
        }
        String sourceRootPath = fileConfig.getSourceRootPath();
        if(sourceRootPath == null){
            throw new MetaException("未填写sourceRootPath");
        }

        //inputRootPath =.source+sourceRootPath的最后一个层级路径
        String inputRootPath = fileConfig.getInputRootPath();
        String defaultInputRootPath = ".source/" +
                FileUtil.getLastPathEle(Paths.get(sourceRootPath)).toString();
        if(StrUtil.isBlank(inputRootPath)){
            fileConfig.setInputRootPath(defaultInputRootPath);
        }

        //outputRootPath = 当前目录下的generated
        String outputRootPath = fileConfig.getOutputRootPath();
        String defaultOutputRootPath = System.getProperty("user.dir") + File.separator + "generated";
        if(StrUtil.isBlank(outputRootPath)){
            fileConfig.setOutputRootPath(defaultOutputRootPath);
        }

        //fileConfigType = dir
        String fileConfigType = fileConfig.getType();
        String defaultFileConfigType = FileTypeEnum.DIR.getValue();
        if(StrUtil.isBlank(fileConfigType)){
            fileConfig.setType(defaultFileConfigType);
        }


        List<FileConfig.FileInfo> fileInfoList = fileConfig.getFiles();
        if (CollectionUtil.isEmpty(fileInfoList)) {
            return;
        }
        for (FileConfig.FileInfo fileInfo : fileInfoList) {
            //inputPath必填
            String inputPath = fileInfo.getInputPath();
            if(StrUtil.isBlank(inputPath)){
                throw new MetaException("未填写inputPath");
            }
            //outputPath默认等于inputPath
            String outputPath = fileInfo.getOutputPath();
            String defaultOutputPath = inputRootPath;
            if(StrUtil.isBlank(outputPath)){
                fileInfo.setOutputPath(defaultOutputPath);
            }
            // type: 默认 inputPath 有文件后缀（如.java）为 file，否则为 dir
            String type = fileInfo.getType();
            if (StrUtil.isBlank(type)) {
                // 无文件后缀
                if (StrUtil.isBlank(FileUtil.getSuffix(inputPath))) {
                    fileInfo.setType(FileTypeEnum.DIR.getValue());
                } else {
                    fileInfo.setType(FileTypeEnum.FILE.getValue());
                }
            }

            // generateType: 如果文件结尾不为 .ftl，默认为static，否则为 dynamic
            String generateType = fileInfo.getGenerateType();
            if (StrUtil.isBlank(generateType)) {
                // 动态模板
                if (inputPath.endsWith(".ftl")) {
                    fileInfo.setGenerateType(FileGeneratorEnum.DYNAMIC.getValue());
                } else {
                    fileInfo.setGenerateType(FileGeneratorEnum.STATIC.getValue());
                }
            }
        }

    }

    private static void validAndFillMetaRoot(Meta meta) {
        //基础信息校验
        // 基础信息校验和默认值
        String name = StrUtil.blankToDefault(meta.getName(),"my-generator");
        String description = StrUtil.emptyToDefault(meta.getDescription(),"我的模板代码生成器");
        String basePackage = StrUtil.blankToDefault(meta.getBasePackage(),"com.siyufeng");
        String version = StrUtil.emptyToDefault(meta.getVersion(),"1.0");
        String author = StrUtil.emptyToDefault(meta.getAuthor(),"syf");
        String createTime = StrUtil.emptyToDefault(meta.getCreateTime(),DateUtil.now());

        meta.setName(name);
        meta.setDescription(description);
        meta.setBasePackage(basePackage);
        meta.setVersion(version);
        meta.setAuthor(author);
        meta.setCreateTime(createTime);
    }
}
