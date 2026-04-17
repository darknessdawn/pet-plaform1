-- 初始化数据

-- 插入测试用户（密码：123456）
INSERT INTO sys_user (username, password, nickname, email, phone, role, status) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '管理员', 'admin@pet.com', '13800138000', 1, 1),
    ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '张三', 'user1@pet.com', '13800138001', 0, 1),
    ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', '李四', 'user2@pet.com', '13800138002', 0, 1);

-- 插入测试地址
INSERT INTO `address` (`user_id`, `receiver_name`, `phone`, `province`, `city`, `district`, `detail_address`, `zip_code`, `is_default`) VALUES
     (2, '张三', '13800138001', '北京市', '北京市', '朝阳区', '朝阳北路 1 号', '100000', 1),
     (3, '李四', '13800138002', '上海市', '上海市', '浦东新区', '浦东大道 1 号', '200000', 1);
-- 插入商品分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `icon`, `sort`, `status`) VALUES
     ('宠物食品', 0, 1, 'food', 1, 1),
     ('宠物用品', 0, 1, 'supplies', 2, 1),
     ('宠物玩具', 0, 1, 'toy', 3, 1),
     ('宠物医疗', 0, 1, 'medical', 4, 1),
     ('狗粮', 1, 2, '', 1, 1),
     ('猫粮', 1, 2, '', 2, 1),
     ('零食', 1, 2, '', 3, 1),
     ('窝垫', 2, 2, '', 1, 1),
     ('餐具', 2, 2, '', 2, 1),
     ('牵引绳', 2, 2, '', 3, 1),
     ('球类玩具', 3, 2, '', 1, 1),
     ('咬胶玩具', 3, 2, '', 2, 1),
     ('驱虫药', 4, 2, '', 1, 1),
     ('保健品', 4, 2, '', 2, 1);

-- 插入商品数据（使用固定的 picsum 图片 ID，每个商品不同）
INSERT INTO `product` (`name`, `description`, `price`, `original_price`, `stock`, `category_id`, `seller_id`, `main_image`, `sales`, `status`, `rating`) VALUES
     ('皇家狗粮成犬粮', '高品质成犬粮，营养均衡', 168.00, 198.00, 100, 5, 1, '/api/images/product_1.jpg', 50, 1, 4.8),
     ('渴望猫粮无谷配方', '进口无谷猫粮，高蛋白', 298.00, 358.00, 80, 6, 1, '/api/images/product_2.jpg', 30, 1, 4.9),
     ('麦富迪狗零食鸡胸肉', '天然鸡胸肉，健康美味', 29.90, 39.90, 200, 7, 1, '/api/images/product_3.jpg', 150, 1, 4.7),
     ('宠物舒适窝垫', '柔软舒适，四季通用', 89.00, 129.00, 50, 8, 1, '/api/images/product_4.jpg', 80, 1, 4.6),
     ('不锈钢宠物碗', '食品级不锈钢，易清洗', 19.90, 29.90, 150, 9, 1, '/api/images/product_5.jpg', 120, 1, 4.5),
     ('伸缩牵引绳', '自动伸缩，安全便捷', 59.00, 79.00, 100, 10, 1, '/api/images/product_6.jpg', 90, 1, 4.7),
     ('耐咬橡胶球', '天然橡胶，安全无毒', 15.00, 25.00, 200, 11, 1, '/api/images/product_7.jpg', 180, 1, 4.8),
     ('鹿角咬胶', '天然鹿角，磨牙洁齿', 45.00, 65.00, 80, 12, 1, '/api/images/product_8.jpg', 60, 1, 4.6),
     ('宠物驱虫药', '体内外驱虫，安全有效', 78.00, 98.00, 60, 13, 1, '/api/images/product_9.jpg', 40, 1, 4.9),
     ('宠物营养膏', '补充维生素，增强免疫力', 38.00, 48.00, 100, 14, 1, '/api/images/product_10.jpg', 70, 1, 4.7),
     ('猫砂盆半封闭式', '防溅设计，易于清理', 128.00, 168.00, 40, 8, 1, '/api/images/product_11.jpg', 35, 1, 4.5),
     ('猫抓板瓦楞纸', '耐磨耐抓，保护家具', 25.00, 35.00, 120, 3, 1, '/api/images/product_12.jpg', 100, 1, 4.6),
     ('宠物湿巾', '温和清洁，无刺激', 12.90, 19.90, 300, 2, 1, '/api/images/product_13.jpg', 250, 1, 4.4),
     ('宠物指甲剪', '安全设计，易于操作', 18.00, 28.00, 80, 2, 1, '/api/images/product_14.jpg', 65, 1, 4.5),
     ('宠物背包外出包', '透气舒适，便携出行', 158.00, 198.00, 30, 2, 1, '/api/images/product_15.jpg', 25, 1, 4.7);

-- 插入用户行为数据（用于推荐算法）
INSERT INTO `user_behavior` (`user_id`, `product_id`, `behavior_type`, `score`, `remark`) VALUES
    (2, 1, 1, 1.0, '浏览'),
    (2, 1, 2, 2.0, '收藏'),
    (2, 3, 1, 1.0, '浏览'),
    (2, 3, 4, 5.0, '购买'),
    (2, 4, 1, 1.0, '浏览'),
    (2, 7, 4, 5.0, '购买'),
    (3, 2, 1, 1.0, '浏览'),
    (3, 2, 2, 2.0, '收藏'),
    (3, 2, 4, 5.0, '购买'),
    (3, 11, 1, 1.0, '浏览'),
    (3, 12, 4, 4.0, '购买'),
    (2, 5, 3, 3.0, '加入购物车'),
    (2, 6, 1, 1.0, '浏览'),
    (3, 8, 1, 1.0, '浏览'),
    (3, 9, 2, 2.0, '收藏');

-- ==================== 优惠券、会员卡和积分系统初始化数据 ====================

-- 会员等级
INSERT INTO membership_level (level_name, level_code, min_points, discount_rate, points_rate, free_shipping) VALUES
('普通会员', 1, 0, 1.00, 1.0, 0),
('白银会员', 2, 1000, 0.98, 1.2, 0),
('黄金会员', 3, 5000, 0.95, 1.5, 1),
('钻石会员', 4, 20000, 0.90, 2.0, 1);

-- 优惠券模板
INSERT INTO coupon_template (name, type, discount_amount, discount_rate, min_purchase, max_discount, total_count, per_user_limit, status, valid_start, valid_end) VALUES
('新人专享券', 1, 20.00, NULL, 100.00, NULL, 1000, 1, 1, NOW(), TIMESTAMPADD(DAY, 30, NOW())),
('满减优惠券', 1, 50.00, NULL, 300.00, NULL, 500, 1, 1, NOW(), TIMESTAMPADD(DAY, 15, NOW())),
('折扣券', 2, NULL, 0.80, 200.00, 100.00, 300, 1, 1, NOW(), TIMESTAMPADD(DAY, 7, NOW()));

-- 积分商品
INSERT INTO points_product (name, description, points_price, cash_price, stock, image, status) VALUES
('宠物零食礼包', '精选宠物零食组合，给爱宠的奖励', 500, 29.90, 100, '/images/points_1.jpg', 1),
('宠物玩具套装', '多种玩具组合，让宠物快乐成长', 1000, 59.90, 50, '/images/points_2.jpg', 1),
('宠物舒适窝垫', '柔软舒适的宠物窝，四季通用', 2000, 99.90, 30, '/images/points_3.jpg', 1);