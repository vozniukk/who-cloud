-- Business Service 1: Equipment & Material Management System
-- Initial data for reference tables

-- Insert Object Statuses
INSERT INTO object_statuses (name, translations, created_at) VALUES
('WORKING', '{"en": "Working", "ru": "Исправно", "uk": "Справний"}', CURRENT_TIMESTAMP),
('STORED', '{"en": "Stored", "ru": "На хранении", "uk": "На зберіганні"}', CURRENT_TIMESTAMP),
('BROKEN', '{"en": "Broken", "ru": "Поломано", "uk": "Зламаний"}', CURRENT_TIMESTAMP),
('WRITTEN_OFF', '{"en": "Written Off", "ru": "Списано", "uk": "Списано"}', CURRENT_TIMESTAMP);

-- Insert User Statuses
INSERT INTO custodian_statuses (name, translations, created_at) VALUES
('ACTIVE', '{"en": "Active", "ru": "Активный", "uk": "Активний"}', CURRENT_TIMESTAMP),
('INACTIVE', '{"en": "Inactive", "ru": "Неактивный", "uk": "Неактивний"}', CURRENT_TIMESTAMP);

-- Insert Contract Types
INSERT INTO contract_types (name, translations, created_at) VALUES
('PERMANENT', '{"en": "Permanent", "ru": "Постоянный", "uk": "Постійний"}', CURRENT_TIMESTAMP),
('TEMPORARY', '{"en": "Temporary", "ru": "Временный", "uk": "Тимчасовий"}', CURRENT_TIMESTAMP),
('CONTRACT', '{"en": "Contract", "ru": "Контрактный", "uk": "Контрактний"}', CURRENT_TIMESTAMP),
('INTERN', '{"en": "Intern", "ru": "Стажер", "uk": "Стажер"}', CURRENT_TIMESTAMP);

-- Insert Categories (with hierarchy support)
INSERT INTO categories (name, parent_id, translations, created_at) VALUES
-- Root categories
('COMPUTERS', NULL, '{"en": "Computers", "ru": "Компьютеры", "uk": "Комп''ютери"}', CURRENT_TIMESTAMP),
('FURNITURE', NULL, '{"en": "Furniture", "ru": "Мебель", "uk": "Меблі"}', CURRENT_TIMESTAMP),
('TOOLS', NULL, '{"en": "Tools", "ru": "Инструменты", "uk": "Інструменти"}', CURRENT_TIMESTAMP),
('OFFICE_EQUIPMENT', NULL, '{"en": "Office Equipment", "ru": "Офисная техника", "uk": "Офісна техніка"}', CURRENT_TIMESTAMP),
('NETWORK_EQUIPMENT', NULL, '{"en": "Network Equipment", "ru": "Сетевое оборудование", "uk": "Мережеве обладнання"}', CURRENT_TIMESTAMP),
('VEHICLES', NULL, '{"en": "Vehicles", "ru": "Транспорт", "uk": "Транспорт"}', CURRENT_TIMESTAMP),
('CONSUMABLES', NULL, '{"en": "Consumables", "ru": "Расходные материалы", "uk": "Витратні матеріали"}', CURRENT_TIMESTAMP);

-- Subcategories for COMPUTERS
INSERT INTO categories (name, parent_id, translations, created_at) VALUES
('DESKTOP', (SELECT id FROM categories WHERE name = 'COMPUTERS'), '{"en": "Desktop Computers", "ru": "Настольные компьютеры", "uk": "Настільні комп''ютери"}', CURRENT_TIMESTAMP),
('LAPTOP', (SELECT id FROM categories WHERE name = 'COMPUTERS'), '{"en": "Laptops", "ru": "Ноутбуки", "uk": "Ноутбуки"}', CURRENT_TIMESTAMP),
('MONITOR', (SELECT id FROM categories WHERE name = 'COMPUTERS'), '{"en": "Monitors", "ru": "Мониторы", "uk": "Монітори"}', CURRENT_TIMESTAMP);

-- Subcategories for FURNITURE
INSERT INTO categories (name, parent_id, translations, created_at) VALUES
('DESK', (SELECT id FROM categories WHERE name = 'FURNITURE'), '{"en": "Desks", "ru": "Столы", "uk": "Столи"}', CURRENT_TIMESTAMP),
('CHAIR', (SELECT id FROM categories WHERE name = 'FURNITURE'), '{"en": "Chairs", "ru": "Стулья", "uk": "Стільці"}', CURRENT_TIMESTAMP),
('CABINET', (SELECT id FROM categories WHERE name = 'FURNITURE'), '{"en": "Cabinets", "ru": "Шкафы", "uk": "Шафи"}', CURRENT_TIMESTAMP);

-- Insert sample custodians
INSERT INTO custodians (auth_user_id, first_name, last_name, position, phone, identification_number, hire_date, contract_type_id, status_id, created_at) VALUES
('John', 'Doe', 'IT Manager', '+380501234567', 'EMP001', '2023-01-15', 
 (SELECT id FROM contract_types WHERE name = 'PERMANENT'),
 (SELECT id FROM custodian_statuses WHERE name = 'ACTIVE'), 
 CURRENT_TIMESTAMP),

('Jane', 'Smith', 'Office Administrator', '+380502345678', 'EMP002', '2023-03-20',
 (SELECT id FROM contract_types WHERE name = 'PERMANENT'),
 (SELECT id FROM custodian_statuses WHERE name = 'ACTIVE'),
 CURRENT_TIMESTAMP),

('Mike', 'Johnson', 'Developer', '+380503456789', 'EMP003', '2024-01-10',
 (SELECT id FROM contract_types WHERE name = 'CONTRACT'),
 (SELECT id FROM custodian_statuses WHERE name = 'ACTIVE'),
 CURRENT_TIMESTAMP),

('Sarah', 'Williams', 'Intern', '+380504567890', 'EMP004', '2024-06-01',
 (SELECT id FROM contract_types WHERE name = 'INTERN'),
 (SELECT id FROM custodian_statuses WHERE name = 'ACTIVE'),
 CURRENT_TIMESTAMP);

-- Insert sample equipment
INSERT INTO equipment (manufacturer, release_date, commissioning_date, status_id, current_custodian_id, inventory_number, serial_number, category_id, created_at) VALUES
-- Desktop Computer
('Dell', '2023-01-01', '2023-01-15',
 (SELECT id FROM object_statuses WHERE name = 'WORKING'),
 (SELECT id FROM custodians WHERE identification_number = 'EMP001'),
 'INV-COMP-001', 'SN-DELL-12345',
 (SELECT id FROM categories WHERE name = 'DESKTOP'),
 CURRENT_TIMESTAMP),

-- Laptop
('Lenovo', '2023-06-01', '2023-06-15',
 (SELECT id FROM object_statuses WHERE name = 'WORKING'),
 (SELECT id FROM custodians WHERE identification_number = 'EMP003'),
 'INV-COMP-002', 'SN-LENOVO-67890',
 (SELECT id FROM categories WHERE name = 'LAPTOP'),
 CURRENT_TIMESTAMP),

-- Monitor
('Samsung', '2023-02-01', '2023-03-20',
 (SELECT id FROM object_statuses WHERE name = 'WORKING'),
 (SELECT id FROM custodians WHERE identification_number = 'EMP002'),
 'INV-COMP-003', 'SN-SAMSUNG-11111',
 (SELECT id FROM categories WHERE name = 'MONITOR'),
 CURRENT_TIMESTAMP),

-- Office Desk
('IKEA', '2022-12-01', '2023-01-15',
 (SELECT id FROM object_statuses WHERE name = 'WORKING'),
 (SELECT id FROM custodians WHERE identification_number = 'EMP001'),
 'INV-FURN-001', NULL,
 (SELECT id FROM categories WHERE name = 'DESK'),
 CURRENT_TIMESTAMP),

-- Equipment in warehouse (no owner)
('HP', '2022-11-01', '2023-01-01',
 (SELECT id FROM object_statuses WHERE name = 'STORED'),
 NULL,
 'INV-COMP-004', 'SN-HP-99999',
 (SELECT id FROM categories WHERE name = 'LAPTOP'),
 CURRENT_TIMESTAMP);

-- Insert history records for created equipment
INSERT INTO equipment_history (equipment_id, action_type, timestamp, from_custodian_id, to_custodian_id, details, user_id, ip_address) VALUES
((SELECT id FROM equipment WHERE inventory_number = 'INV-COMP-001'), 'CREATED', CURRENT_TIMESTAMP, NULL, 
 (SELECT id FROM custodians WHERE identification_number = 'EMP001'), 
 'Initial equipment registration', 1, '127.0.0.1'),

((SELECT id FROM equipment WHERE inventory_number = 'INV-COMP-002'), 'CREATED', CURRENT_TIMESTAMP, NULL,
 (SELECT id FROM custodians WHERE identification_number = 'EMP003'),
 'Initial equipment registration', 1, '127.0.0.1'),

((SELECT id FROM equipment WHERE inventory_number = 'INV-COMP-003'), 'CREATED', CURRENT_TIMESTAMP, NULL,
 (SELECT id FROM custodians WHERE identification_number = 'EMP002'),
 'Initial equipment registration', 1, '127.0.0.1'),

((SELECT id FROM equipment WHERE inventory_number = 'INV-FURN-001'), 'CREATED', CURRENT_TIMESTAMP, NULL,
 (SELECT id FROM custodians WHERE identification_number = 'EMP001'),
 'Initial equipment registration', 1, '127.0.0.1'),

((SELECT id FROM equipment WHERE inventory_number = 'INV-COMP-004'), 'CREATED', CURRENT_TIMESTAMP, NULL, NULL,
 'Equipment placed in warehouse', 1, '127.0.0.1');

-- Insert user history records
INSERT INTO custodian_history (user_id, action_type, timestamp, details, ip_address) VALUES
((SELECT id FROM custodians WHERE identification_number = 'EMP001'), 'CREATED', CURRENT_TIMESTAMP, 
 'User registered in the system', '127.0.0.1'),

((SELECT id FROM custodians WHERE identification_number = 'EMP002'), 'CREATED', CURRENT_TIMESTAMP,
 'User registered in the system', '127.0.0.1'),

((SELECT id FROM custodians WHERE identification_number = 'EMP003'), 'CREATED', CURRENT_TIMESTAMP,
 'User registered in the system', '127.0.0.1'),

((SELECT id FROM custodians WHERE identification_number = 'EMP004'), 'CREATED', CURRENT_TIMESTAMP,
 'User registered in the system', '127.0.0.1');
