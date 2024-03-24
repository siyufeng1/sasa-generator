package com.siyufeng.web;

import cn.hutool.core.io.FileUtil;
import com.siyufeng.web.config.CosClientConfig;
import com.siyufeng.web.manager.CosManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * 主类测试
 *

 */
@SpringBootTest
class MainApplicationTests {

    @Test
    public void test(){
        String projectPath = System.getProperty("user.dir");
        String tempDirPath = String.format("%s/.temp/use/%s", projectPath, 1);
        String zipFilePath = tempDirPath + "/dist.zip";

        System.out.println(zipFilePath);
    }
}
