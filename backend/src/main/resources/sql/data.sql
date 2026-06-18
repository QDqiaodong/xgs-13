-- 初始化设备分类数据（工业设备维保行业标准）
INSERT INTO equipment_category (code, name, maintenance_interval_days, description) VALUES
('AUTO', '自动化设备', 90, '工业自动化生产线、机器人、PLC控制系统等，每季度维保一次'),
('COMPRESSOR', '空压机', 90, '螺杆空压机、活塞空压机、离心空压机等，每季度维保一次'),
('REFRIGERATION', '制冷设备', 180, '工业冷水机、中央空调、冷库制冷机组等，每半年维保一次'),
('PUMP', '泵类设备', 90, '水泵、油泵、真空泵等，每季度维保一次'),
('FAN', '风机设备', 180, '工业风机、排风机、冷却塔风机等，每半年维保一次'),
('BOILER', '锅炉设备', 90, '蒸汽锅炉、热水锅炉等，每季度维保一次'),
('CONVEYOR', '输送设备', 90, '皮带输送机、链条输送机、提升机等，每季度维保一次'),
('MACHINE', '机床设备', 180, '数控车床、铣床、磨床、加工中心等，每半年维保一次')
ON DUPLICATE KEY UPDATE name = VALUES(name), maintenance_interval_days = VALUES(maintenance_interval_days);

-- 初始化备件品类数据
INSERT INTO spare_part_category (code, name, unit, description) VALUES
('BEARING', '轴承', '个', '各类滚动轴承、滑动轴承'),
('FILTER', '滤芯', '个', '空气滤芯、机油滤芯、油分芯、水滤芯'),
('SEAL', '密封件', '套', 'O型圈、油封、机械密封、密封垫'),
('BELT', '传动带', '条', 'V带、同步带、平皮带'),
('LUBRICANT', '润滑油', '桶', '机油、齿轮油、液压油、润滑脂'),
('COOLANT', '制冷剂', 'kg', 'R22、R134a、R410A等制冷剂'),
('CONTACTOR', '接触器', '个', '交流接触器、直流接触器'),
('RELAY', '继电器', '个', '中间继电器、热继电器、时间继电器'),
('SENSOR', '传感器', '个', '温度传感器、压力传感器、流量传感器'),
('VALVE', '阀门', '个', '电磁阀、球阀、闸阀、止回阀')
ON DUPLICATE KEY UPDATE name = VALUES(name), unit = VALUES(unit);

-- 初始化维保工序数据（自动化设备）
INSERT INTO maintenance_process (category_id, process_code, process_name, process_content, sort_order) VALUES
(1, 'AUTO-001', '设备外观清洁', '清洁设备表面、工作台面、防护罩，去除油污、灰尘、杂物', 1),
(1, 'AUTO-002', '电气系统检查', '检查接线端子紧固情况、PLC程序运行状态、传感器信号、急停按钮功能', 2),
(1, 'AUTO-003', '气动系统检查', '检查气源压力、过滤器状态、气管是否老化漏气、气缸运行是否平稳', 3),
(1, 'AUTO-004', '传动系统润滑', '检查导轨、丝杠、齿轮、链条润滑情况，按需添加润滑油/脂', 4),
(1, 'AUTO-005', '安全装置检查', '检查安全门开关、光栅、防护罩、报警装置功能是否正常', 5)
ON DUPLICATE KEY UPDATE process_name = VALUES(process_name), process_content = VALUES(process_content);

-- 初始化维保工序数据（空压机）
INSERT INTO maintenance_process (category_id, process_code, process_name, process_content, sort_order) VALUES
(2, 'COMP-001', '空气滤芯检查更换', '检查空气滤芯压差，超出规定值时更换，清洁滤芯外壳', 1),
(2, 'COMP-002', '机油检查更换', '检查油位，检测油品质量，按需添加或更换润滑油', 2),
(2, 'COMP-003', '油滤芯更换', '更换机油滤芯，检查密封圈，确保无渗漏', 3),
(2, 'COMP-004', '油气分离器检查', '检查油分芯压差，超出规定值时更换，检查回油管路', 4),
(2, 'COMP-005', '冷却系统检查', '检查冷却器散热片，清洁积尘，检查冷却风扇运行状态', 5),
(2, 'COMP-006', '安全阀校验', '检查安全阀起跳压力，必要时送检校验', 6)
ON DUPLICATE KEY UPDATE process_name = VALUES(process_name), process_content = VALUES(process_content);

-- 初始化维保工序数据（制冷设备）
INSERT INTO maintenance_process (category_id, process_code, process_name, process_content, sort_order) VALUES
(3, 'REF-001', '冷凝器清洁', '清洗冷凝器散热翅片，去除灰尘油污，保证换热效果', 1),
(3, 'REF-002', '蒸发器检查', '检查蒸发器结霜情况，必要时进行融霜和清洗', 2),
(3, 'REF-003', '制冷系统检漏', '用检漏仪检查系统管路是否有制冷剂泄漏', 3),
(3, 'REF-004', '压缩机检查', '检查压缩机运行声音、温度、油位，测量运行电流', 4),
(3, 'REF-005', '电气控制检查', '检查温控器、压力开关、接触器、继电器工作状态', 5),
(3, 'REF-006', '水系统检查', '检查水泵运行状态、过滤器堵塞情况、水质情况', 6)
ON DUPLICATE KEY UPDATE process_name = VALUES(process_name), process_content = VALUES(process_content);

-- 初始化示例客户数据
INSERT INTO customer (name, contact_person, contact_phone, address, remark) VALUES
('华盛机械制造有限公司', '张经理', '13800138001', '上海市浦东新区张江路888号', '重点客户'),
('恒顺电子科技有限公司', '李工', '13800138002', '苏州市工业园区金鸡湖大道168号', '月度巡检客户'),
('瑞丰食品加工厂', '王主任', '13800138003', '杭州市余杭区良渚街道工业园', '冷库设备重点维护'),
('鑫达纺织印染有限公司', '赵总', '13800138004', '无锡市惠山区堰桥街道工业园区', '空压机群集中管理'),
('明辉制药股份有限公司', '陈工', '13800138005', '南京市江宁区科学园芝兰路', 'GMP要求严格')
ON DUPLICATE KEY UPDATE contact_person = VALUES(contact_person), contact_phone = VALUES(contact_phone);

-- 初始化示例备件数据
INSERT INTO spare_part (category_id, part_model, part_name, spec, unit, stock_quantity, safety_stock, unit_price, supplier) VALUES
(1, '6205-2RS', '深沟球轴承', '内径25mm外径52mm宽15mm', '个', 50, 20, 45.00, 'SKF授权经销商'),
(1, '6308-2RS', '深沟球轴承', '内径40mm外径90mm宽23mm', '个', 30, 15, 128.00, 'SKF授权经销商'),
(2, 'C1176', '空压机空气滤芯', '适用75kW螺杆机', '个', 20, 10, 185.00, '阿特拉斯配件'),
(2, 'C23632', '空压机机油滤芯', '适用37-90kW机型', '个', 25, 12, 95.00, '阿特拉斯配件'),
(3, 'TC-40*62*8', '骨架油封', '内径40mm外径62mm厚8mm', '个', 100, 30, 12.00, 'NOK授权经销商'),
(3, 'O型圈-50', 'O型密封圈套件', '丁腈橡胶70度', '套', 40, 20, 85.00, '国产优质'),
(4, 'SPA-1250', '窄V带', 'SPA型长度1250mm', '条', 60, 25, 38.00, '盖茨皮带'),
(5, '46#抗磨液压油', 'L-HM46抗磨液压油', '18L/桶', '桶', 10, 5, 580.00, '长城润滑油'),
(6, 'R134a', '环保制冷剂', '13.6kg/罐', 'kg', 200, 100, 45.00, '大金'),
(7, 'CJX2-3210', '交流接触器', '32A 220V', '个', 25, 10, 85.00, '正泰电器')
ON DUPLICATE KEY UPDATE part_name = VALUES(part_name), stock_quantity = VALUES(stock_quantity);

-- 初始化示例设备数据
INSERT INTO equipment (customer_id, category_id, equipment_model, serial_number, install_address, install_date, last_maintenance_date, next_maintenance_date, status) VALUES
(1, 1, 'FANUC R-2000iC', 'FANUC-R2000-2023-001', '上海市浦东新区张江路888号1号车间', '2023-03-15', '2024-03-10', '2024-06-10', 'NORMAL'),
(1, 2, 'GA75VSD', 'ATLAS-GA75-2022-015', '上海市浦东新区张江路888号动力站', '2022-06-20', '2024-04-05', '2024-07-05', 'NORMAL'),
(2, 1, 'SIEMENS S7-1500产线', 'SIMATIC-1500-2023-008', '苏州市工业园区金鸡湖大道168号SMT车间', '2023-08-10', '2024-04-20', '2024-07-20', 'NORMAL'),
(2, 2, 'GA55VSD+', 'ATLAS-GA55-2023-022', '苏州市工业园区金鸡湖大道168号动力站', '2023-01-18', '2024-03-15', '2024-06-15', 'NORMAL'),
(3, 3, 'YSD-30W冷水机组', 'YORK-YSD30-2021-003', '杭州市余杭区良渚街道工业园冷库', '2021-11-05', '2024-01-08', '2024-07-08', 'NORMAL'),
(4, 2, 'ZR315VSD离心空压机', 'ATLAS-ZR315-2020-007', '无锡市惠山区堰桥街道工业园区动力中心', '2020-09-12', '2024-04-18', '2024-07-18', 'NORMAL'),
(5, 3, 'CWA-100螺杆冷水机', 'CARRIER-CWA100-2022-012', '南京市江宁区科学园芝兰路制药车间', '2022-05-25', '2024-02-20', '2024-08-20', 'NORMAL')
ON DUPLICATE KEY UPDATE serial_number = VALUES(serial_number), last_maintenance_date = VALUES(last_maintenance_date);

-- 初始化示例维保工单
INSERT INTO maintenance_order (order_no, equipment_id, customer_id, order_type, order_status, plan_date, actual_date, engineer, work_hours, work_content) VALUES
('WO20240601001', 1, 1, 'PERIODIC', 'COMPLETED', '2024-06-01', '2024-06-01', '李工程师', 4.0, '完成自动化机器人季度维保：清洁设备、检查电气系统、润滑导轨丝杠、检查安全装置'),
('WO20240605002', 2, 1, 'PERIODIC', 'COMPLETED', '2024-06-05', '2024-06-05', '王工程师', 3.5, '完成空压机季度维保：更换空气滤芯、检查油位、更换油滤芯、清洁冷却器'),
('WO20240610003', 6, 4, 'PERIODIC', 'IN_PROGRESS', '2024-06-10', NULL, '张工程师', NULL, '离心空压机季度维保进行中'),
('WO20240615004', 4, 2, 'PERIODIC', 'PENDING', '2024-06-15', NULL, NULL, NULL, '待执行：空压机季度维保'),
('WO20240615005', 2, 1, 'FAULT', 'PENDING', '2024-06-15', NULL, NULL, NULL, '故障报修：空压机高温报警')
ON DUPLICATE KEY UPDATE order_status = VALUES(order_status);

-- 初始化示例备件消耗
INSERT INTO order_part_consumption (order_id, part_id, part_name, part_model, spec, unit, quantity, unit_price) VALUES
(1, 1, '深沟球轴承', '6205-2RS', '内径25mm外径52mm宽15mm', '个', 2, 45.00),
(1, 3, '骨架油封', 'TC-40*62*8', '内径40mm外径62mm厚8mm', '个', 4, 12.00),
(2, 3, '空压机空气滤芯', 'C1176', '适用75kW螺杆机', '个', 1, 185.00),
(2, 4, '空压机机油滤芯', 'C23632', '适用37-90kW机型', '个', 1, 95.00)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- 初始化示例维保计划
INSERT INTO maintenance_plan (equipment_id, plan_date, plan_type, cycle_months, plan_status) VALUES
(1, '2024-09-10', 'PERIODIC', 3, 'PENDING'),
(2, '2024-10-05', 'PERIODIC', 3, 'PENDING'),
(3, '2024-07-20', 'PERIODIC', 3, 'PENDING'),
(4, '2024-09-15', 'PERIODIC', 3, 'PENDING'),
(5, '2025-01-08', 'PERIODIC', 6, 'PENDING'),
(7, '2025-02-20', 'PERIODIC', 6, 'PENDING')
ON DUPLICATE KEY UPDATE plan_date = VALUES(plan_date);
