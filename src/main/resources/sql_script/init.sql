-- 创建共享记账应用数据库
CREATE DATABASE IF NOT EXISTS expense_sharing_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE expense_sharing_app;

-- 用户表，存储用户信息
CREATE TABLE IF NOT EXISTS users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username   VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名，唯一',
    password   VARCHAR(255) NOT NULL COMMENT '密码，加密存储',
    email      VARCHAR(100) NOT NULL UNIQUE COMMENT '电子邮箱，唯一',
    role       ENUM ('ADMIN', 'USER') DEFAULT 'USER' COMMENT '用户角色：管理员或普通用户',
    created_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 账本表，存储所有账本信息
CREATE TABLE IF NOT EXISTS account_books
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_name   VARCHAR(100) NOT NULL COMMENT '账本名称',
    description TEXT COMMENT '账本描述',
    creator_id  BIGINT       NOT NULL COMMENT '创建者ID，关联users表的id',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_creator_id (creator_id) COMMENT '创建者ID索引'
);

-- 支出类别表，存储所有可能的支出类别
CREATE TABLE IF NOT EXISTS expense_categories
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_name VARCHAR(50) NOT NULL UNIQUE COMMENT '类别名称，如"餐饮"、"交通"等',
    description   TEXT COMMENT '类别描述',
    is_default    BOOLEAN              DEFAULT FALSE COMMENT '是否为默认类别，TRUE表示新账本创建时默认添加',
    is_system     BOOLEAN              DEFAULT FALSE COMMENT '是否为系统类别，TRUE表示不可删除',
    creator_id    BIGINT COMMENT '创建者ID，NULL表示系统创建，否则关联users表的id',
    display_order INT         NOT NULL DEFAULT 0 COMMENT '默认显示顺序',
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_creator_id (creator_id) COMMENT '创建者ID索引'
);

-- 账本类别关联表，记录每个账本使用的类别
CREATE TABLE IF NOT EXISTS book_categories
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id       BIGINT NOT NULL COMMENT '账本ID，关联account_books表的id',
    category_id   BIGINT NOT NULL COMMENT '类别ID，关联expense_categories表的id',
    display_order INT    NOT NULL DEFAULT 0 COMMENT '在此账本中的显示顺序',
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_book_category (book_id, category_id) COMMENT '唯一约束，一个账本对一个类别只能有一条关联',
    INDEX idx_book_id (book_id) COMMENT '账本ID索引',
    INDEX idx_category_id (category_id) COMMENT '类别ID索引'
);

-- 账本成员表，追踪每个账本中的用户
CREATE TABLE IF NOT EXISTS book_members
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id     BIGINT    NOT NULL COMMENT '账本ID，关联account_books表的id',
    user_id     BIGINT    NOT NULL COMMENT '用户ID，关联users表的id',
    join_status ENUM ('INVITED', 'JOINED') DEFAULT 'INVITED' COMMENT '加入状态：已邀请或已加入',
    invited_at  TIMESTAMP                  DEFAULT CURRENT_TIMESTAMP COMMENT '邀请时间',
    joined_at   TIMESTAMP NULL COMMENT '加入时间',
    UNIQUE KEY unique_book_user (book_id, user_id) COMMENT '唯一约束，一个用户在一个账本中只能有一条记录',
    INDEX idx_book_id (book_id) COMMENT '账本ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引'
);

-- 支出记录表，存储账目记录
CREATE TABLE IF NOT EXISTS expense_records
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id      BIGINT         NOT NULL COMMENT '所属账本ID，关联account_books表的id',
    category_id  BIGINT         NOT NULL COMMENT '支出类别ID，关联expense_categories表的id',
    payer_id     BIGINT         NOT NULL COMMENT '付款人用户ID，关联users表的id',
    amount       DECIMAL(10, 2) NOT NULL COMMENT '支出金额',
    description  TEXT COMMENT '支出描述',
    expense_date DATE           NOT NULL COMMENT '支出日期',
    created_by   BIGINT         NOT NULL COMMENT '记录创建者ID，关联users表的id',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_id (book_id) COMMENT '账本ID索引',
    INDEX idx_category_id (category_id) COMMENT '类别ID索引',
    INDEX idx_payer_id (payer_id) COMMENT '付款人ID索引',
    INDEX idx_created_by (created_by) COMMENT '创建者ID索引'
);

-- 支出参与者表，追踪每笔支出的参与用户
CREATE TABLE IF NOT EXISTS expense_participants
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    record_id BIGINT         NOT NULL COMMENT '关联的支出记录ID，关联expense_records表的id',
    user_id   BIGINT         NOT NULL COMMENT '参与者用户ID，关联users表的id',
    amount    DECIMAL(10, 2) NOT NULL COMMENT '分摊金额',
    UNIQUE KEY unique_record_user (record_id, user_id) COMMENT '唯一约束，一个用户在一条支出记录中只能有一条参与记录',
    INDEX idx_record_id (record_id) COMMENT '支出记录ID索引',
    INDEX idx_user_id (user_id) COMMENT '用户ID索引'
);

-- 邀请通知表，管理加入账本的邀请流程
CREATE TABLE IF NOT EXISTS invitations
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id      BIGINT NOT NULL COMMENT '被邀请加入的账本ID，关联account_books表的id',
    sender_id    BIGINT NOT NULL COMMENT '邀请发送者用户ID，关联users表的id',
    recipient_id BIGINT NOT NULL COMMENT '邀请接收者用户ID，关联users表的id',
    status       ENUM ('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING' COMMENT '邀请状态：待处理、已接受或已拒绝',
    created_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_book_recipient (book_id, recipient_id) COMMENT '唯一约束，一个用户对一个账本只能有一条待处理的邀请',
    INDEX idx_book_id (book_id) COMMENT '账本ID索引',
    INDEX idx_sender_id (sender_id) COMMENT '发送者ID索引',
    INDEX idx_recipient_id (recipient_id) COMMENT '接收者ID索引'
);

-- 结算记录表，追踪用户之间的付款结算
CREATE TABLE IF NOT EXISTS settlements
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    book_id     BIGINT         NOT NULL COMMENT '关联的账本ID，关联account_books表的id',
    payer_id    BIGINT         NOT NULL COMMENT '付款人用户ID，关联users表的id',
    receiver_id BIGINT         NOT NULL COMMENT '收款人用户ID，关联users表的id',
    amount      DECIMAL(10, 2) NOT NULL COMMENT '结算金额',
    status      ENUM ('PENDING', 'COMPLETED') DEFAULT 'PENDING' COMMENT '结算状态：待处理或已完成',
    created_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_book_id (book_id) COMMENT '账本ID索引',
    INDEX idx_payer_id (payer_id) COMMENT '付款人ID索引',
    INDEX idx_receiver_id (receiver_id) COMMENT '收款人ID索引'
);

-- 插入系统默认支出类别
INSERT INTO expense_categories (category_name, description, is_default, is_system, display_order)
VALUES ('餐饮', '日常餐饮支出，包括外卖、堂食等', TRUE, TRUE, 10),
       ('交通', '公共交通、打车、自驾油费等出行支出', TRUE, TRUE, 20),
       ('购物', '购买商品的支出', TRUE, TRUE, 30),
       ('服饰', '服装、鞋帽、配饰等支出', TRUE, TRUE, 40),
       ('娱乐', '电影、游戏、聚会等娱乐活动支出', TRUE, TRUE, 50),
       ('住宿', '酒店、民宿等住宿支出', TRUE, TRUE, 60),
       ('日用品', '日常生活用品支出', TRUE, TRUE, 70),
       ('医疗', '医院、药品等医疗支出', TRUE, TRUE, 80),
       ('教育', '学费、书籍、培训等教育支出', TRUE, TRUE, 90),
       ('其他', '未分类支出', TRUE, TRUE, 100);

-- 创建触发器：新建账本时自动添加默认类别
DELIMITER //
DROP TRIGGER IF EXISTS after_account_book_insert;
CREATE TRIGGER after_account_book_insert
    AFTER INSERT
    ON account_books
    FOR EACH ROW
BEGIN
    -- 为新账本添加所有默认类别
    INSERT INTO book_categories (book_id, category_id, display_order)
    SELECT NEW.id, id, display_order
    FROM expense_categories
    WHERE is_default = TRUE;
END //
DELIMITER ;
