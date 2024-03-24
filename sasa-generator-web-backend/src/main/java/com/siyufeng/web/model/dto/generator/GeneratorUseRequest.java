package com.siyufeng.web.model.dto.generator;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 使用代码生成器的请求
 *
 * @author 司雨枫
 */
@Data
public class GeneratorUseRequest implements Serializable {
    /**
     * 生成器id
     */
    private Long id;

    /**
     * 数据模型
     */
    private Map<String, Object> dataModel;

    private static final long serialVersionUID = 1L;

}
