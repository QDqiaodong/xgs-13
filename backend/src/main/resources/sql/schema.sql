-- 工业设备维保工单资产台账系统 - 数据库初始化脚本

-- 设备分类表
CREATE TABLE IF NOT EXISTS equipment_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
    name VARCHAR(100) NOT NULL COMMENT '分类名称',
    maintenance_interval_days INT NOT NULL DEFAULT 90 COMMENT '标准维保间隔天数',
    description VARCHAR(500) COMMENT '分类描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分类表';

-- 客户表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '客户单位名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(500) COMMENT '地址',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name (name),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 设备档案表
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    category_id BIGINT NOT NULL COMMENT '设备分类ID',
    equipment_model VARCHAR(100) NOT NULL COMMENT '设备型号',
    serial_number VARCHAR(100) COMMENT '出厂编号',
    install_address VARCHAR(500) NOT NULL COMMENT '安装地址',
    install_date DATE COMMENT '安装日期',
    last_maintenance_date DATE COMMENT '上次维保日期',
    next_maintenance_date DATE COMMENT '下次维保日期',
    status VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '设备状态：NORMAL-正常，FAULT-故障，MAINTENANCE-维保中',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_serial_number (serial_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_category_id (category_id),
    INDEX idx_next_maintenance_date (next_maintenance_date),
    INDEX idx_status (status),
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT,
    FOREIGN KEY (category_id) REFERENCES equipment_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备档案表';

-- 备件品类表
CREATE TABLE IF NOT EXISTS spare_part_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '品类编码',
    name VARCHAR(100) NOT NULL COMMENT '品类名称',
    unit VARCHAR(20) COMMENT '单位',
    description VARCHAR(500) COMMENT '描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件品类表';

-- 备件库存表
CREATE TABLE IF NOT EXISTS spare_part (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '备件品类ID',
    part_model VARCHAR(100) NOT NULL COMMENT '备件型号',
    part_name VARCHAR(100) NOT NULL COMMENT '备件名称',
    spec VARCHAR(200) COMMENT '规格参数',
    unit VARCHAR(20) COMMENT '单位',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    safety_stock INT NOT NULL DEFAULT 10 COMMENT '安全库存',
    unit_price DECIMAL(12, 2) DEFAULT 0.00 COMMENT '单价',
    supplier VARCHAR(200) COMMENT '供应商',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_part_model (part_model),
    INDEX idx_category_id (category_id),
    INDEX idx_part_model (part_model),
    INDEX idx_stock_quantity (stock_quantity),
    FOREIGN KEY (category_id) REFERENCES spare_part_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备件库存表';

-- 维保工序表
CREATE TABLE IF NOT EXISTS maintenance_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL COMMENT '设备分类ID',
    process_code VARCHAR(50) NOT NULL UNIQUE COMMENT '工序编码',
    process_name VARCHAR(100) NOT NULL COMMENT '工序名称',
    process_content TEXT COMMENT '工序内容',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    FOREIGN KEY (category_id) REFERENCES equipment_category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维保工序表';

-- 维保工单表
CREATE TABLE IF NOT EXISTS maintenance_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '工单编号',
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    order_type VARCHAR(30) NOT NULL DEFAULT 'PERIODIC' COMMENT '工单类型：PERIODIC-定期维保，FAULT-故障维修，CLEANING-整机清洁',
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '工单状态：PENDING-待执行，IN_PROGRESS-执行中，COMPLETED-已完成，CANCELLED-已取消',
    plan_date DATE NOT NULL COMMENT '计划执行日期',
    actual_date DATE COMMENT '实际执行日期',
    engineer VARCHAR(50) COMMENT '执行工程师',
    work_hours DECIMAL(5, 1) COMMENT '工时(小时)',
    work_content TEXT COMMENT '工作内容',
    fault_description TEXT COMMENT '故障描述',
    solution TEXT COMMENT '解决方案',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (order_no),
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_status (order_status),
    INDEX idx_plan_date (plan_date),
    INDEX idx_actual_date (actual_date),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='维保工单表';

-- 工单备件消耗表
CREATE TABLE IF NOT EXISTS order_part_consumption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '工单ID',
    part_id BIGINT NOT NULL COMMENT '备件ID',
    part_name VARCHAR(100) COMMENT '备件名称(快照)',
    part_model VARCHAR(100) COMMENT '备件型号(快照)',
    spec VARCHAR(200) COMMENT '规格参数(快照)',
    unit VARCHAR(20) COMMENT '单位(快照)',
    quantity INT NOT NULL COMMENT '消耗数量',
    unit_price DECIMAL(12, 2) DEFAULT 0.00 COMMENT '单价(快照)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_part (order_id, part_id),
    INDEX idx_order_id (order_id),
    INDEX idx_part_id (part_id),
    FOREIGN KEY (order_id) REFERENCES maintenance_order(id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES spare_part(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单备件消耗表';

-- 维保计划表
CREATE TABLE IF NOT EXISTS maintenance_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL COMMENT '设备ID',
    plan_date DATE NOT NULL COMMENT '计划执行日期',
    plan_type VARCHAR(30) NOT NULL DEFAULT 'PERIODIC' COMMENT '计划类型：PERIODIC-定期维保，INSPECTION-巡检',
    cycle_months INT NOT NULL DEFAULT 3 COMMENT '周期(月)：3-季度，6-半年度，12-年度',
    plan_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待执行，COMPLETED-已完成',
    order_id BIGINT COMMENT '关联工单ID',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_equipment_date_type (equipment_id, plan_date, plan_type),
    INDEX idx_equipment_id (equipment_id),
    INDEX idx_plan_date (plan_date),
    INDEX idx_plan_status (plan_status),
    FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT,
    FOREIGN KEY (order_id) REFERENCES maintenance_order(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4_unicode_ci COMMENT='维保计划表';
