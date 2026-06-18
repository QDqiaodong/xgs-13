-- 0-init-grants.sql: 授权远程连接
CREATE USER IF NOT EXISTS 'maintenance'@'%' IDENTIFIED BY 'maintenance_2024';
GRANT ALL PRIVILEGES ON maintenance_db.* TO 'maintenance'@'%';
FLUSH PRIVILEGES;
