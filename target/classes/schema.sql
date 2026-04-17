-- H2数据库脚本（兼容MySQL语法）

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    email VARCHAR(100) DEFAULT NULL UNIQUE,
    phone VARCHAR(20) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    role TINYINT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `level` TINYINT DEFAULT 1,
    `icon` VARCHAR(255) DEFAULT NULL,
    `sort` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10,2) NOT NULL,
    `original_price` DECIMAL(10,2) DEFAULT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `version` INT DEFAULT 0,
    `category_id` BIGINT NOT NULL,
    `seller_id` BIGINT NOT NULL,
    `main_image` VARCHAR(1000) DEFAULT NULL,
    `images` TEXT,
    `sales` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `review_count` INT DEFAULT 0,
    `rating` DECIMAL(2,1) DEFAULT 5.0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 地址表
CREATE TABLE IF NOT EXISTS `address` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `receiver_name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `province` VARCHAR(50) NOT NULL,
    `city` VARCHAR(50) NOT NULL,
    `district` VARCHAR(50) NOT NULL,
    `detail_address` VARCHAR(200) NOT NULL,
    `zip_code` VARCHAR(10) DEFAULT NULL,
    `is_default` TINYINT DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `selected` TINYINT DEFAULT 1,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    pay_amount DECIMAL(12,2) NOT NULL,
    member_discount DECIMAL(12,2) DEFAULT 0,  -- 会员折扣金额
    user_coupon_id BIGINT DEFAULT NULL,  -- 使用的优惠券 ID
    pay_type TINYINT DEFAULT NULL,
    status TINYINT DEFAULT 0,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(500) NOT NULL,
    remark VARCHAR(500) DEFAULT NULL,
    pay_time TIMESTAMP DEFAULT NULL,
    ship_time TIMESTAMP DEFAULT NULL,
    receive_time TIMESTAMP DEFAULT NULL,
    complete_time TIMESTAMP DEFAULT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
    );

-- 订单详情表
CREATE TABLE IF NOT EXISTS `order_detail` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(200) NOT NULL,
    `product_image` VARCHAR(255) DEFAULT NULL,
    `product_price` DECIMAL(10,2) NOT NULL,
    `quantity` INT NOT NULL,
    `total_amount` DECIMAL(12,2) NOT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 用户行为表
CREATE TABLE IF NOT EXISTS `user_behavior` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `behavior_type` TINYINT NOT NULL,
    `score` DECIMAL(3,2) DEFAULT NULL,
    `remark` VARCHAR(255) DEFAULT NULL,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
);

-- 评价表
CREATE TABLE IF NOT EXISTS `review` (
     `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
     `user_id` BIGINT NOT NULL,
     `product_id` BIGINT NOT NULL,
     `order_id` BIGINT,  -- 改为可空
     `rating` TINYINT NOT NULL,
     `content` TEXT,
     `images` TEXT,
     `status` TINYINT DEFAULT 1,
     `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     `deleted` TINYINT DEFAULT 0
);

-- ==================== 优惠券、会员卡和积分系统 ====================

-- 优惠券模板表
CREATE TABLE IF NOT EXISTS coupon_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '券名称',
    type TINYINT NOT NULL COMMENT '类型：1-满减，2-折扣',
    discount_amount DECIMAL(10,2) COMMENT '减免金额',
    discount_rate DECIMAL(3,2) COMMENT '折扣率 (0-1)',
    min_purchase DECIMAL(10,2) COMMENT '最低消费金额',
    max_discount DECIMAL(10,2) COMMENT '最大优惠金额 (折扣券用)',
    total_count INT NOT NULL COMMENT '发放总数',
    issued_count INT DEFAULT 0 COMMENT '已发放数量',
    per_user_limit INT DEFAULT 1 COMMENT '每人限领',
    status TINYINT DEFAULT 1 COMMENT '状态：1-可用，0-停用',
    valid_start TIMESTAMP COMMENT '有效开始时间',
    valid_end TIMESTAMP COMMENT '有效结束时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    coupon_template_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    order_id BIGINT COMMENT '使用的订单 ID',
    get_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time TIMESTAMP DEFAULT NULL COMMENT '使用时间',
    valid_start TIMESTAMP COMMENT '有效开始时间',
    valid_end TIMESTAMP COMMENT '有效结束时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 会员卡等级表
CREATE TABLE IF NOT EXISTS membership_level (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    level_code TINYINT NOT NULL COMMENT '等级代码：1-普通，2-白银，3-黄金，4-钻石',
    min_points INT NOT NULL COMMENT '所需最低积分',
    discount_rate DECIMAL(3,2) DEFAULT 1.0 COMMENT '商品折扣率',
    points_rate DECIMAL(3,2) DEFAULT 1.0 COMMENT '积分倍率',
    free_shipping TINYINT DEFAULT 0 COMMENT '是否免运费',
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 用户会员表
CREATE TABLE IF NOT EXISTS user_membership (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    level_id BIGINT NOT NULL COMMENT '会员等级 ID',
    points INT DEFAULT 0 COMMENT '当前积分',
    total_points INT DEFAULT 0 COMMENT '累计积分',
    used_points INT DEFAULT 0 COMMENT '已消耗积分',
    growth_value INT DEFAULT 0 COMMENT '成长值',
    expire_date TIMESTAMP COMMENT '会员到期时间',
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 积分流水表
CREATE TABLE IF NOT EXISTS points_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INT NOT NULL COMMENT '积分变动数量 (+/-)',
    type TINYINT NOT NULL COMMENT '类型：1-获取，2-消费，3-兑换',
    source VARCHAR(100) COMMENT '来源：购物/签到/活动等',
    related_id BIGINT COMMENT '关联 ID(订单 ID 等)',
    description VARCHAR(500) COMMENT '描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 积分兑换商品表
CREATE TABLE IF NOT EXISTS points_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    points_price INT NOT NULL COMMENT '积分价格',
    cash_price DECIMAL(10,2) COMMENT '现金价格 (可混合支付)',
    stock INT DEFAULT 0,
    image VARCHAR(1000),
    status TINYINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 积分兑换订单表
CREATE TABLE IF NOT EXISTS points_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    points_used INT NOT NULL,
    cash_paid DECIMAL(10,2) DEFAULT 0,
    status TINYINT DEFAULT 0 COMMENT '0-待兑换，1-已完成',
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
);

-- 修改 sys_user 表添加会员相关字段
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS membership_level TINYINT DEFAULT 1 COMMENT '会员等级';
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS total_points INT DEFAULT 0 COMMENT '总积分';

-- 修改 orders 表添加优惠券和积分字段
ALTER TABLE orders ADD COLUMN IF NOT EXISTS coupon_id BIGINT COMMENT '使用的优惠券 ID';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS coupon_discount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠券减免金额';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS points_earned INT DEFAULT 0 COMMENT '获得的积分';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS membership_discount DECIMAL(10,2) DEFAULT 0 COMMENT '会员折扣金额';
