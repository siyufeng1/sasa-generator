package com.siyufeng.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.stream.CollectorUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.siyufeng.maker.generator.file.FileGenerator;
import com.siyufeng.maker.meta.Meta;
import com.siyufeng.maker.meta.enums.FileGeneratorEnum;
import com.siyufeng.maker.meta.enums.FileTypeEnum;
import com.siyufeng.maker.template.enums.FileFilterRangeEnum;
import com.siyufeng.maker.template.enums.FileFilterRuleEnum;
import com.siyufeng.maker.template.model.FileFilterConfig;
import com.siyufeng.maker.template.model.TemplateMakerFileConfig;
import com.siyufeng.maker.template.model.TemplateMakerModelConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
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
     * @param meta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param id
     * @return
     */
    private static Long makeTemplate(Meta meta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, Long id) {
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

        //1、处理模型信息
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        //转换为配置文件接受的modelInfo对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // - 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        // - 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKey();
            String groupName = modelGroupConfig.getGroupName();
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            groupModelInfo.setGroupKey(groupKey);
            groupModelInfo.setGroupName(groupName);
            groupModelInfo.setCondition(condition);
            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }


        //2、输入文件信息
        //要挖坑项目的根目录
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originProjectPath)).toString();
        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();

        //遍历所有文件
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String inputFilePath = fileInfoConfig.getPath();
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;
            //传入绝对路径
            //得到过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileFilterConfigList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(file, templateMakerModelConfig, sourceRootPath);
                newFileInfoList.add(fileInfo);
            }
        }


        //如果是文件组
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (fileGroupConfig != null) {
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            Meta.FileConfig.FileInfo groupInfo = new Meta.FileConfig.FileInfo();
            groupInfo.setCondition(condition);
            groupInfo.setGroupKey(groupKey);
            groupInfo.setGroupName(groupName);

            //文件全放入一个分组内
            groupInfo.setFiles(newFileInfoList);
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(groupInfo);
        }


        // 三、生成配置文件
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";

        //已有meta文件，不是第一次制作了，则在meta基础上进行修改
        if (FileUtil.exist(metaOutputPath)) {
            meta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            //1、追加配置
            List<Meta.FileConfig.FileInfo> fileInfoList = meta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = meta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);

            //配置去重
            meta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            meta.getModelConfig().setModels(distinctModels(modelInfoList));
        } else {
            //1、构造配置参数
            Meta.FileConfig fileConfig = new Meta.FileConfig();
            meta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();
            fileConfig.setFiles(fileInfoList);
            fileInfoList.addAll(newFileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            meta.setModelConfig(modelConfig);
            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelConfig.setModels(modelInfoList);
            modelInfoList.addAll(newModelInfoList);
        }

        //2、输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaOutputPath);

        return id;
    }

    /**
     * 制作模板文件
     *
     * @param inputFile
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(File inputFile, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        //要挖坑的文件(一定要是相对路径)
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");

        String fileInputPath = fileInputAbsolutePath.replaceAll(sourceRootPath + "/", "");

        String fileOutputPath = fileInputPath + ".ftl";

        //使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = inputFile.getAbsolutePath() + ".ftl";

        String fileContent;
        //如果已有模板文件，表示不是第一次制作，则在原有模板的基础上再挖坑
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        // 支持多个模型：对于同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();

        //最新替换后的内容
        String newFileContent = fileContent;
        String replacement = null;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            String fieldName = modelInfoConfig.getFieldName();
            //模型配置
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", fieldName);
            } else {
                String groupKey = modelGroupConfig.getGroupKey();
                replacement = String.format("${%s.%s}", groupKey, fieldName);
            }
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

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
        String fileInputPath2 = "src/main/resources/application.yml";
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


        //替换模型
        String searchStr = "Sum: ";

        searchStr = "BaseResponse";


        //文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = new ArrayList<>();
        fileInfoConfigList.add(fileInfoConfig1);
        fileInfoConfigList.add(fileInfoConfig2);


        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = new TemplateMakerFileConfig.FileGroupConfig();
        fileGroupConfig.setCondition("outputText");
        fileGroupConfig.setGroupKey("test");
        fileGroupConfig.setGroupName("测试分组syf");


        // 模型分组
        TemplateMakerModelConfig templateMakerModelConfig = new TemplateMakerModelConfig();
        // - 模型组配置
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = new TemplateMakerModelConfig.ModelGroupConfig();
        modelGroupConfig.setGroupKey("mysql");
        modelGroupConfig.setGroupName("数据库配置");


        // - 模型配置
        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig1 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig1.setFieldName("url");
        modelInfoConfig1.setType("String");
        modelInfoConfig1.setDefaultValue("jdbc:mysql://localhost:3306/my_db");
        modelInfoConfig1.setReplaceText("jdbc:mysql://localhost:3306/my_db");

        TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig2 = new TemplateMakerModelConfig.ModelInfoConfig();
        modelInfoConfig2.setFieldName("username");
        modelInfoConfig2.setType("String");
        modelInfoConfig2.setDefaultValue("root");
        modelInfoConfig2.setReplaceText("root");


        List<TemplateMakerModelConfig.ModelInfoConfig> modelInfoConfigList = Arrays.asList(modelInfoConfig1, modelInfoConfig2);
        templateMakerModelConfig.setModelGroupConfig(modelGroupConfig);
        templateMakerModelConfig.setModels(modelInfoConfigList);



        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);
        templateMakerFileConfig.setFileGroupConfig(fileGroupConfig);





        Long id = TemplateMaker.makeTemplate(meta, originProjectPath, templateMakerFileConfig,templateMakerModelConfig, 1769660470582337536L);
        System.out.println(id);
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        // 策略：同分组内文件 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        // {"groupKey": "a", "files": [1, 2]}, {"groupKey": "a", "files": [2, 3]}, {"groupKey": "b", "files": [4, 5]}
        // {"groupKey": "a", "files": [[1, 2], [2, 3]]}, {"groupKey": "b", "files": [[4, 5]]}
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );

        // 2. 同组内的文件配置合并
        // {"groupKey": "a", "files": [[1, 2], [2, 3]]}
        // {"groupKey": "a", "files": [1, 2, 2, 3]}
        // {"groupKey": "a", "files": [1, 2, 3]}
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream()
                    .flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (oldValue, newValue) -> newValue)
                    ).values());
            //使用新的group配置(如组名更改则修改组名)
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            newFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newFileInfo);
        }
        // 3. 将文件分组添加到结果列表
        List<Meta.FileConfig.FileInfo> resultList = new ArrayList<>();
        resultList.addAll(groupKeyMergedFileInfoMap.values());

        // 4. 将未分组的文件添加到结果列表
        resultList.addAll(new ArrayList<>(fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, o -> o, (oldValue, newValue) -> newValue)
                ).values()));

        return resultList;
    }

    /**
     * 模型去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        // 策略：同分组内模型 merge，不同分组保留

        // 1. 有分组的，以组为单位划分
        // {"groupKey": "a", "models": [1, 2]}, {"groupKey": "a", "models": [2, 3]}, {"groupKey": "b", "models": [4, 5]}
        // {"groupKey": "a", "models": [[1, 2], [2, 3]]}, {"groupKey": "b", "models": [[4, 5]]}
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );

        // 2. 同组内的模型配置合并
        // {"groupKey": "a", "models": [[1, 2], [2, 3]]}
        // {"groupKey": "a", "models": [1, 2, 2, 3]}
        // {"groupKey": "a", "models": [1, 2, 3]}
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempModelInfoList = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream()
                    .flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (oldValue, newValue) -> newValue)
                    ).values());
            //使用新的group配置(如组名更改则修改组名)
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempModelInfoList);
            newModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newModelInfo);
        }
        // 3. 将模型分组添加到结果列表
        List<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>();
        resultList.addAll(groupKeyMergedModelInfoMap.values());

        // 4. 将未分组的模型添加到结果列表
        resultList.addAll(new ArrayList<>(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, o -> o, (oldValue, newValue) -> newValue)
                ).values()));

        return resultList;
    }

}

