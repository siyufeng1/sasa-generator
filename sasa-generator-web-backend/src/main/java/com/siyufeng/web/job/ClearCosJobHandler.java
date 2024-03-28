package com.siyufeng.web.job;

import cn.hutool.core.util.StrUtil;
import com.siyufeng.web.manager.CosManager;
import com.siyufeng.web.mapper.GeneratorMapper;
import com.siyufeng.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 示例任务
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class ClearCosJobHandler {

    @Resource
    private CosManager cosManager;

    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 每天执行
     * @throws Exception
     */
    @XxlJob("clearCosJobHandler")
    public void demoJobHandler() throws Exception {
        log.info("ClearCosJobHandler start");
        //编写业务逻辑
        //每天清理所有无用的文件
        //包括用户上传的模板制作文件(generator_make_template)
        cosManager.deleteDir("/generator_name_template/");


        //已删除的代码生成器对应的产物包文件(generator_dist)。
        List<Generator> generatorList = generatorMapper.listDeletedGenerator();
        List<String> keyList = generatorList.stream()
                .map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                //移除'/'前缀
                .map(disPath -> StrUtil.removePrefix(disPath, "/"))
                .collect(Collectors.toList());



        cosManager.deleteObjects(keyList);

        log.info("ClearCosJobHandler end");
    }




}
