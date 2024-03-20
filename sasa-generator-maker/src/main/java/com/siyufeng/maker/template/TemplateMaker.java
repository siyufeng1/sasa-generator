package com.siyufeng.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
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
import com.siyufeng.maker.template.model.*;

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
     * @param templateMakeConfig
     * @return
     */
    public static Long makeTemplate(TemplateMakeConfig templateMakeConfig) {
        Long id = templateMakeConfig.getId();
        Meta meta = templateMakeConfig.getMeta();
        String originProjectPath = templateMakeConfig.getOriginProjectPath();
        TemplateMakerFileConfig templateMakerFileConfig = templateMakeConfig.getFileConfig();
        TemplateMakerModelConfig templateMakerModelConfig = templateMakeConfig.getModelConfig();
        TemplateMakerOutputConfig templateMakerOutputConfig = templateMakeConfig.getTemplateMakerOutputConfig();
        return makeTemplate(meta, originProjectPath, templateMakerFileConfig, templateMakerModelConfig, id, templateMakerOutputConfig);
    }


    /**
     * 制作模板
     *
     * @param meta
     * @param originProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param id
     * @param templateMakerOutputConfig
     * @return
     */
    public static Long makeTemplate(Meta meta, String originProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, Long id, TemplateMakerOutputConfig templateMakerOutputConfig) {
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

        //一、输入文件信息
        //要挖坑项目的根目录
        String sourceRootPath = FileUtil.loopFiles(new File(templatePath), 1, null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();


        // 注意 win 系统需要对路径进行转义
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

        //二、处理文件信息
        List<Meta.FileConfig.FileInfo> newFileInfoList = getFileInfoList(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);


        //三、处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = getModelInfoList(templateMakerModelConfig);


        // 四、生成配置文件
        String metaOutputPath = templatePath + File.separator + "meta.json";

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

        //2、额外的输出配置
        if (templateMakerOutputConfig != null) {
            //文件外层和分组去重
            if (templateMakerOutputConfig.isRemoveGroupFilesFromRoot()) {
                List<Meta.FileConfig.FileInfo> fileInfoList = TemplateMakerUtils.removeGroupFilesFromRoot(meta.getFileConfig().getFiles());
                meta.getFileConfig().setFiles(fileInfoList);
            }
        }

        //3、输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaOutputPath);

        return id;
    }

    /**
     * 生成模型列表
     *
     * @param templateMakerModelConfig
     * @return
     */

    private static List<Meta.ModelConfig.ModelInfo> getModelInfoList(TemplateMakerModelConfig templateMakerModelConfig) {
        // - 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();

        if (templateMakerModelConfig == null) {
            return newModelInfoList;
        }
        //转换为配置文件接受的modelInfo对象
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();

        if (CollectionUtil.isEmpty(models)) {
            return newModelInfoList;
        }

        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // - 如果是模型组
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            //变量复制
            Meta.ModelConfig.ModelInfo groupModelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, groupModelInfo);
            // 模型全放到一个分组内
            groupModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(groupModelInfo);
        } else {
            // 不分组，添加所有的模型信息到列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }


    /**
     * 生成文件列表
     *
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> getFileInfoList(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        List<Meta.FileConfig.FileInfo> newFileInfoList = new ArrayList<>();

        //非空校验
        if (templateMakerFileConfig == null) {
            return newFileInfoList;
        }

        List<TemplateMakerFileConfig.FileInfoConfig> fileConfigInfoList = templateMakerFileConfig.getFiles();
        if (CollectionUtil.isEmpty(fileConfigInfoList)) {
            return newFileInfoList;
        }

        //遍历所有文件
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : templateMakerFileConfig.getFiles()) {
            String inputFilePath = fileInfoConfig.getPath();

            //传入绝对路径
            String inputFileAbsolutePath = sourceRootPath + File.separator + inputFilePath;

            //得到过滤后的文件列表
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileInfoConfig.getFileFilterConfigList());

            //不处理已经生成的ftl文件
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith("ftl"))
                    .collect(Collectors.toList());

            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(file, templateMakerModelConfig, sourceRootPath, fileInfoConfig);
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
        return newFileInfoList;
    }

    /**
     * 制作模板文件
     *
     * @param inputFile
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @param fileInfoConfig
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(File inputFile, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath, TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        //要挖坑的文件(一定要是相对路径)
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");

        String fileInputPath = fileInputAbsolutePath.replaceAll(sourceRootPath + "/", "");

        String fileOutputPath = fileInputPath + ".ftl";

        //使用字符串替换，生成模板文件
        String fileOutputAbsolutePath = inputFile.getAbsolutePath() + ".ftl";

        String fileContent;
        //如果已有模板文件，表示不是第一次制作，则在原有模板的基础上再挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
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
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getValue());
        fileInfo.setGenerateType(FileGeneratorEnum.DYNAMIC.getValue());
        fileInfo.setCondition(fileInfoConfig.getCondition());

        //是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);

        //之前不存在模板文件,并且这次替换没有修改文件内容，则是静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                //不需要加.ftl，与原文件后缀一致
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGeneratorEnum.STATIC.getValue());
            } else {
                //输出文件到指定目录
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            //有模板文件，并且增加了新坑，更新模板文件
            //输出文件到指定目录
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
        }


        return fileInfo;
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
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (oldValue, newValue) -> newValue)
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
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, o -> o, (oldValue, newValue) -> newValue)
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

