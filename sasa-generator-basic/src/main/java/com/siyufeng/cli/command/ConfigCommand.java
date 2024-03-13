package com.siyufeng.cli.command;

import cn.hutool.core.util.ReflectUtil;
import com.siyufeng.model.MainTemplateConfig;
import picocli.CommandLine;

import java.lang.reflect.Field;

/**
 * @author 司雨枫
 */
@CommandLine.Command(name = "config", description = "查看参数信息", mixinStandardHelpOptions = true)
public class ConfigCommand implements Runnable{

    @Override
    public void run() {
//        Class<MainTemplateConfig> mainTemplateConfigClass = MainTemplateConfig.class;
//        Field[] fields = mainTemplateConfigClass.getDeclaredFields();
        Field[] fields = ReflectUtil.getFields(MainTemplateConfig.class);
        for (Field field : fields) {
            System.out.println("字段名称：" + field.getName());
            System.out.println("字段类型：" + field.getType());
            System.out.println("-----");
        }
    }
}
