package com.maintenance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MAINTENANCE_INTERVAL_KEY = "maintenance:interval:";
    private static final String SPARE_PART_TEMPLATE_KEY = "maintenance:sparepart:template:";

    public void setMaintenanceInterval(String categoryCode, Integer days) {
        String key = MAINTENANCE_INTERVAL_KEY + categoryCode;
        redisTemplate.opsForValue().set(key, days, 30, TimeUnit.DAYS);
        log.debug("缓存设备分类维保间隔: {} -> {}天", categoryCode, days);
    }

    public Integer getMaintenanceInterval(String categoryCode) {
        String key = MAINTENANCE_INTERVAL_KEY + categoryCode;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("从缓存获取设备分类维保间隔: {} -> {}天", categoryCode, value);
            return (Integer) value;
        }
        return null;
    }

    public void setSparePartTemplate(String categoryCode, Map<String, Object> template) {
        String key = SPARE_PART_TEMPLATE_KEY + categoryCode;
        redisTemplate.opsForValue().set(key, template, 30, TimeUnit.DAYS);
        log.debug("缓存备件规格模板: {}", categoryCode);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getSparePartTemplate(String categoryCode) {
        String key = SPARE_PART_TEMPLATE_KEY + categoryCode;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            return (Map<String, Object>) value;
        }
        return null;
    }

    public void evictMaintenanceInterval(String categoryCode) {
        String key = MAINTENANCE_INTERVAL_KEY + categoryCode;
        redisTemplate.delete(key);
    }

    public void evictAll() {
        redisTemplate.delete(redisTemplate.keys("maintenance:*"));
        log.info("已清除所有维保相关缓存");
    }
}
