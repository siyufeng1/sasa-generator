package com.siyufeng.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siyufeng.web.model.entity.Generator;

import java.util.List;

/**
 * @author siyufeng
 * @description 针对表【generator(代码生成器)】的数据库操作Mapper
 * @createDate 2024-03-21 17:29:01
 * @Entity com.siyufeng.web.model.entity.Generator
 */
public interface GeneratorMapper extends BaseMapper<Generator> {
      List<Generator> listDeletedGenerator();
}




