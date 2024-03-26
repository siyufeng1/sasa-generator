package com.siyufeng.web.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 缓存代码生成器的请求
 *
 * @author 司雨枫
 */
@Data
public class GeneratorCacheRequest implements Serializable {
    /**
     * 生成器id
     */
    private Long id;

    private static final long serialVersionUID = 1L;

}
