package com.siyufeng.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.siyufeng.maker.generator.file.FileGenerator;
import com.siyufeng.maker.meta.Meta;
import com.siyufeng.maker.meta.enums.FileGeneratorEnum;
import com.siyufeng.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.distinct;

/**
 * @author 司雨枫
 * 模板制作工具
 */
public class TemplateMaker {

    /**
     * 制作模板
     *
     * @param newMeta
     * @param originProjectPath
     * @param fileInputPathList
     * @param modelInfo
     * @param searchStr
     * @param id
     * @return
     */
    private static Long makeTemplate(Meta newMeta, String originProjectPath, List<String> fileInputPathList, Meta.ModelConfig.ModelInfo modelInfo, String searchStr, Long id) {
        //没有id则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        //指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        //复制目录
        String tempDirPath = projectPath + File.separator + ".temp";
        String templatePath = tempDirPath + File.separator + id;
        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originProjectPath, templatePath, true);
        }

        //一、输入信息
        //2、输入文件信息
        //要挖坑项目的根目录
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");


        //遍历所有文件
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();
        for(String fileInputPath : fileInputPathList){
            String inputFileAbsolutePath = sourceRootPath + File.separator + fileInputPath;
            if (FileUtil.isDirectory(inputFileAbsolutePath)) {
                List<File> filelist = FileUtil.loopFiles(inputFileAbsolutePath);
                for (File file : filelist) {
                    Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(file, modelInfo, searchStr, sourceRootPath);
                    newFileInfoList.add(fileInfo);
                }
            } else {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(new File(inputFileAbsolutePath), modelInfo, searchStr, sourceRootPath);
                newFileInfoList.add(fileInfo);
            }
        }

        // 三、生成配置文件
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";

        //已有meta文件，不是第一次制作了，则在meta基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            //1、追加配置
            List<Meta.FileConfig.FileInfo> fileInfoList = oldMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = oldMeta.getModelConfig().getModels();
            modelInfoList.add(modelInfo);


            //配置去重
            oldMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            oldMeta.getModelConfig().setModels(distinctModels(modelInfoList));

            //2、输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaOutputPath);
        } else {
            //1、构造配置参数

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            newMeta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            newMeta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.add(modelInfo);


            //2、输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        }

        return id;
    }

    /**
     * 制作模板文件
     *
     * @param inputFile
     * @param modelInfo
     * @param searchStr
     * @param sourceRootPath
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(File inputFile, Meta.ModelConfig.ModelInfo modelInfo, String searchStr, String sourceRootPath) {
        //要挖坑的文件(一定要是相对路径)
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");

        String fileInputPath = fileInputAbsolutePath.replaceAll(sourceRootPath + "/", "");

        String fileOutputPath = fileInputPath + ".ftl";

        // 二、使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = inputFile.getAbsolutePath() + ".ftl";

        String fileContent;
        //如果已有模板文件，表示不是第一次制作，则在原有模板的基础上再挖坑
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        String replacement = String.format("${%s}", modelInfo.getFieldName());
        String newFileContent = StrUtil.replace(fileContent, searchStr, replacement);

        //文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());


        //和原文件内容一致，没有挖坑，静态生成
        if (newFileContent.equals(fileContent)) {
            //不需要加.ftl，与原文件后缀一致
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGeneratorEnum.STATIC.getValue());
        } else {
            fileInfo.setOutputPath(fileOutputPath);
            fileInfo.setGenerateType(FileGeneratorEnum.DYNAMIC.getValue());
            //输出文件到指定目录
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }

        return fileInfo;
    }


    public static void main(String[] args) {
        //1、项目基本信息
        Meta meta = new Meta();
        meta.setName("acm-template-pro-generator");
        meta.setDescription("ACM 示例模板生成器");

        //指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        //要挖坑项目的根目录
        String originProjectPath = new File(projectPath).getParent() + File.separator + "sasa-generator-demo-projects/springboot-init";
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "src/main/java/com/yupi/springbootinit/controller";
        List<String> fileInputPathList = Arrays.asList(fileInputPath1, fileInputPath2);

        //3、模型参数信息
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setType("String");
//        modelInfo.setDefaultValue("sum = ");

        //模型参数信息(第二次)
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        modelInfo.setDefaultValue("BaseResponse");


        String searchStr = "Sum: ";

        searchStr = "BaseResponse";

        Long id = TemplateMaker.makeTemplate(meta, originProjectPath, fileInputPathList, modelInfo, searchStr, null);
        System.out.println(id);
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        Collection<Meta.FileConfig.FileInfo> values = fileInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (oldValue, newValue) -> newValue)
                ).values();
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(values);
        return newFileInfoList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        Collection<Meta.ModelConfig.ModelInfo> values = modelInfoList.stream()
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (oldValue, newValue) -> newValue)
                ).values();
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(values);
        return newModelInfoList;
    }

}

