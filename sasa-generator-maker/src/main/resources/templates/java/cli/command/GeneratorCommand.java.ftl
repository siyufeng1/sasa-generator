package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.FileGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.concurrent.Callable;

<#-- 生成选项 -->
<#macro generateOption indent modelInfo>
${indent}@Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}", </#if>"--${modelInfo.fieldName}"}, arity = "0..1", <#if modelInfo.description??>description = "${modelInfo.description}", </#if>interactive = true, echo = true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;
</#macro>
<#-- 生成命令调用 -->
<#macro generateCommand indent modelInfo>
${indent}System.out.println("输入${modelInfo.groupName}配置：");
${indent}CommandLine commandLine = new CommandLine(${modelInfo.type}.class);
${indent}commandLine.execute(${modelInfo.allArgsStr});
</#macro>

@Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GeneratorCommand implements Callable<Integer> {

    <#list modelConfig.models as modelInfo>

        <#if modelInfo.groupKey??>
            /**
             * ${modelInfo.groupName}
             */
            static DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

            @Command(name = "${modelInfo.groupKey}", description = "${modelInfo.description}")
            @Data
            public static class ${modelInfo.type} implements Runnable{
                <#list modelInfo.models as submodelInfo>
                    <@generateOption indent="        " modelInfo=submodelInfo />
                </#list>

                @Override
                public void run() {
                    <#list modelInfo.models as submodelInfo>
                    ${modelInfo.groupKey}.${submodelInfo.fieldName} = ${submodelInfo.fieldName};
                    </#list>
                }
            }
        <#else>
            <@generateOption indent="    " modelInfo=modelInfo />
        </#if>


    </#list>

    <#-- 生成调用方法 -->
    public Integer call() throws Exception {
    <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
            <#if modelInfo.condition??>
                if (${modelInfo.condition}) {
                <@generateCommand indent="            " modelInfo = modelInfo />
                }
            <#else>
                <@generateCommand indent="        " modelInfo = modelInfo />
            </#if>
        </#if>
    </#list>
    <#-- 填充数据模型对象 -->
    DataModel dataModel = new DataModel();
    BeanUtil.copyProperties(this, dataModel);
    <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
            dataModel.${modelInfo.groupKey} = ${modelInfo.groupKey};
        </#if>
    </#list>
    FileGenerator.doGenerate(dataModel);
    return 0;
    }
}