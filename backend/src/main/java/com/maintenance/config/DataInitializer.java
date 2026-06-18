package com.maintenance.config;

import com.maintenance.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CacheService cacheService;

    @Override
    public void run(String... args) {
        log.info("开始初始化维保标准数据缓存...");
        initMaintenanceIntervals();
        initSparePartTemplates();
        log.info("维保标准数据缓存初始化完成");
    }

    private void initMaintenanceIntervals() {
        log.info("缓存设备标准维保间隔...");
        cacheService.setMaintenanceInterval("AUTO", 90);
        cacheService.setMaintenanceInterval("COMPRESSOR", 90);
        cacheService.setMaintenanceInterval("REFRIGERATION", 180);
        cacheService.setMaintenanceInterval("PUMP", 90);
        cacheService.setMaintenanceInterval("FAN", 180);
        cacheService.setMaintenanceInterval("BOILER", 90);
        cacheService.setMaintenanceInterval("CONVEYOR", 90);
        cacheService.setMaintenanceInterval("MACHINE", 180);
        log.info("设备标准维保间隔缓存完成，共8种设备分类");
    }

    private void initSparePartTemplates() {
        log.info("缓存常用备件规格模板...");

        Map<String, Object> autoTemplate = new HashMap<>();
        autoTemplate.put("category", "AUTO");
        autoTemplate.put("categoryName", "自动化设备");
        autoTemplate.put("commonParts", new String[]{
                "6205-2RS 深沟球轴承",
                "608-2RS 深沟球轴承",
                "TC-25*42*7 骨架油封",
                "TC-40*62*8 骨架油封",
                "O型圈套装",
                "L-HM46抗磨液压油",
                "CJX2-1810 交流接触器"
        });
        cacheService.setSparePartTemplate("AUTO", autoTemplate);

        Map<String, Object> compressorTemplate = new HashMap<>();
        compressorTemplate.put("category", "COMPRESSOR");
        compressorTemplate.put("categoryName", "空压机");
        compressorTemplate.put("commonParts", new String[]{
                "C1176 空气滤芯",
                "C23632 机油滤芯",
                "DD17 精密滤芯",
                "PD17 精密滤芯",
                "压缩机专用润滑油",
                "电磁阀",
                "最小压力阀"
        });
        cacheService.setSparePartTemplate("COMPRESSOR", compressorTemplate);

        Map<String, Object> refrigerationTemplate = new HashMap<>();
        refrigerationTemplate.put("category", "REFRIGERATION");
        refrigerationTemplate.put("categoryName", "制冷设备");
        refrigerationTemplate.put("commonParts", new String[]{
                "R134a 制冷剂",
                "R410A 制冷剂",
                "干燥过滤器",
                "热力膨胀阀",
                "电磁阀",
                "水过滤器",
                "温控器"
        });
        cacheService.setSparePartTemplate("REFRIGERATION", refrigerationTemplate);

        log.info("常用备件规格模板缓存完成");
    }
}
