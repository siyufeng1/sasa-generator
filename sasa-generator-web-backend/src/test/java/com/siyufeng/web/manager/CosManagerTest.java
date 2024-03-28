package com.siyufeng.web.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Cos 操作测试
 *

 */
@SpringBootTest
@ActiveProfiles("local")
class CosManagerTest {

    @Resource
    private CosManager cosManager;


    @Test
    void deleteObject() {
        cosManager.deleteObject("/test/815fc046e829d5851dd.jpeg");
    }

    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("test/IMG_20210609_200655.jpg","test/IMG_20210904_015235.jpg"));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("/test/");
    }
}