package com.siyufeng.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

import java.io.File;

/**
 * @author 司雨枫
 */
public class MetaManager {

    private static volatile Meta meta;

    public static Meta getMeta() {
        if(meta == null){
            synchronized (MetaManager.class){
                if(meta == null) {
                    meta = initMeta();
                }
            }
        }
        return meta;
    }

    private static Meta initMeta() {
//        String metaJson = ResourceUtil.readUtf8Str("meta.json");
        String metaJson = ResourceUtil.readUtf8Str("springboot-init-meta.json");
        Meta meta = JSONUtil.toBean(metaJson, Meta.class);
        MetaValidator.validateAndFill(meta);
        return meta;
    }

    public static void main(String[] args) {

        System.out.println(System.getProperty("user.dir") + File.separator + "generated");
    }
}
