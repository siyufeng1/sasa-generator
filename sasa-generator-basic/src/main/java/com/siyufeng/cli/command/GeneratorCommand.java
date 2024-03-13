package com.siyufeng.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.siyufeng.generator.MainGenerator;
import com.siyufeng.model.MainTemplateConfig;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * @author 司雨枫
 */
@CommandLine.Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GeneratorCommand implements Callable {
    /**
     * 是否生成循环
     */
    @CommandLine.Option(names = {"-l", "--loop"}, arity = "0..1", description = "是否循环", interactive = true, echo = true)
    private boolean loop;
    /**
     * 作者注释
     */
    @CommandLine.Option(names = {"-a", "--author"}, arity = "0..1", description = "作者名称", interactive = true, echo = true)
    private String author = "syf";
    /**
     * 输出信息
     */
    @CommandLine.Option(names = {"-o", "--outputText"}, arity = "0..1", description = "输出文本", interactive = true, echo = true)
    private String outputText = "sum = ";


    @Override
    public Object call() throws Exception {
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        BeanUtil.copyProperties(this, mainTemplateConfig);
        MainGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}
