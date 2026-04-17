package com.pet.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("开始插入填充...");
        // 使用fillStrategy方法进行填充，更安全可靠
        this.fillStrategy(metaObject, "createTime", LocalDateTime.now());
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
        this.fillStrategy(metaObject, "deleted", 0);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("开始更新填充...");
        // 使用fillStrategy方法进行填充，更安全可靠
        this.fillStrategy(metaObject, "updateTime", LocalDateTime.now());
    }
}
