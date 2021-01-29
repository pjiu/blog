package com.testblog.redis;


public interface RedisService {

    /**
     * 判断key是否存在
     */
    Boolean hasKey(String key);

}
