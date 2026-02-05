-- 创建共享记账应用数据库（带数据冗余优化版本）
-- 注意: PostgreSQL中创建数据库需要在不连接到目标数据库的情况下执行
-- 此处仅提供语法，实际执行时需要连接到默认数据库(如postgres)
-- CREATE DATABASE expense_sharing_app WITH ENCODING='UTF8' LC_COLLATE='en_US.UTF8' LC_CTYPE='en_US.UTF8';

-- ========================================
-- 基础表定义（与原版本保持一致）
-- ========================================

-- 角色表，存储系统中所有可能的角色
CREATE TABLE IF NOT EXISTS roles
(
    id          SERIAL PRIMARY KEY,                            -- 主键ID
    role_name   VARCHAR(50) NOT NULL UNIQUE,                   -- 角色名称，唯一
    description TEXT,                                          -- 角色描述
    role_type   VARCHAR(20) NOT NULL,                          -- 角色类型：ADMIN（管理员）或USER（普通用户）
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 更新时间
    is_deleted  BOOLEAN DEFAULT FALSE,                         -- 是否已逻辑删除
    deleted_at  TIMESTAMP                                      -- 逻辑删除时间
);

-- 用户表，存储用户信息（添加统计冗余字段）
CREATE TABLE IF NOT EXISTS users
(
    id         SERIAL PRIMARY KEY,                            -- 主键ID
    username   VARCHAR(50)  NOT NULL UNIQUE,                  -- 用户名，唯一
    nickname   VARCHAR(50)  NOT NULL,                         -- 昵称
    avatar     VARCHAR(255) DEFAULT NULL,                     -- 头像URL，可选
    bio        TEXT DEFAULT NULL,                              -- 用户简介，可选
    phone      VARCHAR(20) DEFAULT NULL,                      -- 手机号码，可选
    password   VARCHAR(255) NOT NULL,                         -- 密码，加密存储
    email      VARCHAR(100) NOT NULL UNIQUE,                  -- 电子邮箱，唯一
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 更新时间
    is_deleted BOOLEAN DEFAULT FALSE,                         -- 是否已逻辑删除
    deleted_at TIMESTAMP,                                     -- 逻辑删除时间
    
    -- 【数据冗余优化】新增统计冗余字段，避免复杂统计查询
    ledger_count     INT DEFAULT 0,                           -- 参与的账本数量（JOINED状态）
    created_ledger_count INT DEFAULT 0,                       -- 创建的账本数量
    total_paid       DECIMAL(12, 2) DEFAULT 0,                -- 累计支付金额
    total_owed       DECIMAL(12, 2) DEFAULT 0,                -- 累计欠款金额（负数表示别人欠他）
    last_activity_at TIMESTAMP                                -- 最后活动时间
);

-- 用户角色关联表，存储用户和角色的多对多关系
CREATE TABLE IF NOT EXISTS user_roles
(
    id         SERIAL PRIMARY KEY,                            -- 主键ID
    user_id    BIGINT NOT NULL,                               -- 用户ID，关联users表的id
    role_id    BIGINT NOT NULL,                               -- 角色ID，关联roles表的id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 更新时间
    is_deleted BOOLEAN DEFAULT FALSE,                         -- 是否已逻辑删除
    deleted_at TIMESTAMP,                                     -- 逻辑删除时间
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)     -- 唯一约束，确保一个用户不会重复分配同一个角色
);
CREATE INDEX idx_user_id_roles ON user_roles (user_id);       -- 用户ID索引
CREATE INDEX idx_role_id_users ON user_roles (role_id);       -- 角色ID索引

-- 账本表，存储所有账本信息（添加统计冗余字段）
CREATE TABLE IF NOT EXISTS account_ledgers
(
    id          SERIAL PRIMARY KEY,                            -- 主键ID
    ledger_name VARCHAR(100) NOT NULL,                         -- 账本名称
    description TEXT,                                          -- 账本描述
    creator_id  BIGINT       NOT NULL,                         -- 创建者ID，关联users表的id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 更新时间
    is_deleted  BOOLEAN DEFAULT FALSE,                         -- 是否已逻辑删除
    deleted_at  TIMESTAMP,                                     -- 逻辑删除时间
    
    -- 【数据冗余优化】新增统计冗余字段，避免复杂聚合查询
    creator_nickname   VARCHAR(50),                            -- 冗余创建者昵称，避免关联users表
    member_count      INT DEFAULT 1,                           -- 成员数量（JOINED状态）
    invited_count     INT DEFAULT 0,                           -- 待加入成员数量（INVITED状态）
    total_expenses    DECIMAL(12, 2) DEFAULT 0,                -- 总支出金额
    record_count      INT DEFAULT 0,                           -- 支出记录数量
    last_expense_date DATE,                                    -- 最后消费日期
    last_activity_at  TIMESTAMP                                -- 最后活动时间
);
CREATE INDEX idx_creator_id ON account_ledgers (creator_id);   -- 创建者ID索引
CREATE INDEX idx_ledger_name ON account_ledgers (ledger_name); -- 账本名称索引（支持模糊搜索）
CREATE INDEX idx_last_activity ON account_ledgers (last_activity_at); -- 最后活动时间索引（支持按活跃度排序）

-- 支出类别表，存储所有可能的支出类别
CREATE TABLE IF NOT EXISTS expense_categories
(
    id            SERIAL PRIMARY KEY,                          -- 主键ID
    category_name VARCHAR(50) NOT NULL UNIQUE,                 -- 类别名称，如"餐饮"、"交通"等
    description   TEXT,                                        -- 类别描述
    is_default    BOOLEAN              DEFAULT FALSE,          -- 是否为默认类别，TRUE表示新账本创建时默认添加
    is_system     BOOLEAN              DEFAULT FALSE,          -- 是否为系统类别，TRUE表示不可删除
    creator_id    BIGINT,                                      -- 创建者ID，NULL表示系统创建，否则关联users表的id
    display_order INT         NOT NULL DEFAULT 0,              -- 默认显示顺序
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    is_deleted    BOOLEAN DEFAULT FALSE,                       -- 是否已逻辑删除
    deleted_at    TIMESTAMP,                                   -- 逻辑删除时间
    
    -- 【数据冗余优化】新增统计字段
    usage_count   INT DEFAULT 0                                -- 使用次数统计
);
CREATE INDEX idx_creator_id_categories ON expense_categories (creator_id); -- 创建者ID索引
CREATE INDEX idx_category_usage ON expense_categories (usage_count); -- 使用次数索引（支持热门类别排序）

-- 账本类别关联表，记录每个账本使用的类别
CREATE TABLE IF NOT EXISTS ledger_categories
(
    id            SERIAL PRIMARY KEY,                          -- 主键ID
    ledger_id     BIGINT NOT NULL,                             -- 账本ID，关联account_ledgers表的id
    category_id   BIGINT NOT NULL,                             -- 类别ID，关联expense_categories表的id
    display_order INT    NOT NULL DEFAULT 0,                   -- 在此账本中的显示顺序
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,   -- 创建时间
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,   -- 更新时间
    is_deleted    BOOLEAN DEFAULT FALSE,                       -- 是否已逻辑删除
    deleted_at    TIMESTAMP,                                   -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段，避免关联查询
    category_name VARCHAR(50),                                 -- 冗余类别名称
    usage_count   INT DEFAULT 0,                               -- 在此账本中的使用次数
    
    CONSTRAINT unique_ledger_category UNIQUE (ledger_id, category_id) -- 唯一约束，一个账本对一个类别只能有一条关联
);
CREATE INDEX idx_ledger_id ON ledger_categories (ledger_id);         -- 账本ID索引
CREATE INDEX idx_category_id ON ledger_categories (category_id);     -- 类别ID索引
CREATE INDEX idx_ledger_category_usage ON ledger_categories (ledger_id, usage_count); -- 复合索引，支持账本内热门类别排序

-- 账本成员表，追踪每个账本中的用户（添加冗余字段）
CREATE TABLE IF NOT EXISTS ledger_members
(
    id          SERIAL PRIMARY KEY,                            -- 主键ID
    ledger_id   BIGINT    NOT NULL,                            -- 账本ID，关联account_ledgers表的id
    user_id     BIGINT    NOT NULL,                            -- 用户ID，关联users表的id
    join_status VARCHAR(10) CHECK (join_status IN ('INVITED', 'JOINED')) DEFAULT 'INVITED', -- 加入状态：已邀请或已加入
    invited_at  TIMESTAMP                  DEFAULT CURRENT_TIMESTAMP,     -- 邀请时间
    joined_at   TIMESTAMP NULL,                                           -- 加入时间
    is_deleted  BOOLEAN DEFAULT FALSE,                                    -- 是否已逻辑删除
    deleted_at  TIMESTAMP,                                                -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段，避免复杂的余额计算查询
    user_nickname    VARCHAR(50),                              -- 冗余用户昵称
    ledger_name      VARCHAR(100),                             -- 冗余账本名称
    total_paid       DECIMAL(12, 2) DEFAULT 0,                 -- 该成员在此账本的总支付金额
    total_shared     DECIMAL(12, 2) DEFAULT 0,                 -- 该成员在此账本的总分摊金额
    balance          DECIMAL(12, 2) DEFAULT 0,                 -- 余额（正数表示别人欠他，负数表示他欠别人）
    record_count     INT DEFAULT 0,                            -- 参与的支出记录数量
    last_activity_at TIMESTAMP,                                -- 在此账本中的最后活动时间
    
    CONSTRAINT unique_ledger_user UNIQUE (ledger_id, user_id)   -- 唯一约束，一个用户在一个账本中只能有一条记录
);
CREATE INDEX idx_ledger_id_members ON ledger_members (ledger_id);   -- 账本ID索引
CREATE INDEX idx_user_id_members ON ledger_members (user_id);       -- 用户ID索引
CREATE INDEX idx_member_balance ON ledger_members (ledger_id, balance); -- 复合索引，支持按余额排序
CREATE INDEX idx_member_activity ON ledger_members (ledger_id, last_activity_at); -- 复合索引，支持按活跃度排序

-- 支出记录表，存储账目记录（添加大量冗余字段）
CREATE TABLE IF NOT EXISTS expense_records
(
    id           SERIAL PRIMARY KEY,                          -- 主键ID
    ledger_id    BIGINT         NOT NULL,                     -- 所属账本ID，关联account_ledgers表的id
    category_id  BIGINT         NOT NULL,                     -- 支出类别ID，关联expense_categories表的id
    payer_id     BIGINT         NOT NULL,                     -- 付款人用户ID，关联users表的id
    amount       DECIMAL(10, 2) NOT NULL,                     -- 支出金额
    description  TEXT,                                        -- 支出描述
    expense_date DATE           NOT NULL,                     -- 支出日期
    created_by   BIGINT         NOT NULL,                     -- 记录创建者ID，关联users表的id
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- 创建时间
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- 更新时间
    is_deleted   BOOLEAN DEFAULT FALSE,                       -- 是否已逻辑删除
    deleted_at   TIMESTAMP,                                   -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段，避免频繁的N表关联查询
    ledger_name      VARCHAR(100),                            -- 冗余账本名称
    category_name    VARCHAR(50),                             -- 冗余类别名称
    payer_nickname   VARCHAR(50),                             -- 冗余付款人昵称
    creator_nickname VARCHAR(50),                             -- 冗余创建者昵称
    
    -- 【数据冗余优化】新增预计算字段，避免复杂的聚合查询
    participant_count INT DEFAULT 0,                          -- 参与人数
    avg_amount       DECIMAL(10, 2) DEFAULT 0,                -- 人均分摊金额
    participants_info TEXT,                                   -- JSON格式存储参与者信息，格式：[{"user_id":1,"nickname":"张三","amount":50.00}]
    
    -- 【数据冗余优化】状态字段
    has_settlement   BOOLEAN DEFAULT FALSE                     -- 是否已有结算记录
);
CREATE INDEX idx_ledger_id_records ON expense_records (ledger_id);         -- 账本ID索引
CREATE INDEX idx_category_id_records ON expense_records (category_id);     -- 类别ID索引
CREATE INDEX idx_payer_id ON expense_records (payer_id);                   -- 付款人ID索引
CREATE INDEX idx_created_by ON expense_records (created_by);               -- 创建者ID索引
CREATE INDEX idx_expense_date ON expense_records (expense_date);           -- 支出日期索引
CREATE INDEX idx_ledger_date_amount ON expense_records (ledger_id, expense_date, amount); -- 复合索引，支持账本内按日期和金额查询
CREATE INDEX idx_payer_nickname ON expense_records (payer_nickname);      -- 付款人昵称索引，支持按昵称搜索

-- 支出参与者表，追踪每笔支出的参与用户（添加冗余字段）
CREATE TABLE IF NOT EXISTS expense_participants
(
    id         SERIAL PRIMARY KEY,                            -- 主键ID
    record_id  BIGINT         NOT NULL,                       -- 关联的支出记录ID，关联expense_records表的id
    user_id    BIGINT         NOT NULL,                       -- 参与者用户ID，关联users表的id
    amount     DECIMAL(10, 2) NOT NULL,                       -- 分摊金额
    is_deleted BOOLEAN DEFAULT FALSE,                         -- 是否已逻辑删除
    deleted_at TIMESTAMP,                                     -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段
    user_nickname    VARCHAR(50),                             -- 冗余用户昵称
    expense_amount   DECIMAL(10, 2),                          -- 冗余总支出金额
    expense_date     DATE,                                    -- 冗余支出日期
    ledger_id        BIGINT,                                  -- 冗余账本ID
    category_name    VARCHAR(50),                             -- 冗余类别名称
    
    CONSTRAINT unique_record_user UNIQUE (record_id, user_id) -- 唯一约束，一个用户在一条支出记录中只能有一条参与记录
);
CREATE INDEX idx_record_id ON expense_participants (record_id);           -- 支出记录ID索引
CREATE INDEX idx_user_id_participants ON expense_participants (user_id);  -- 用户ID索引
CREATE INDEX idx_ledger_user_date ON expense_participants (ledger_id, user_id, expense_date); -- 复合索引，支持用户在账本中的消费查询

-- 邀请通知表，管理加入账本的邀请流程
CREATE TABLE IF NOT EXISTS invitations
(
    id           SERIAL PRIMARY KEY,                          -- 主键ID
    ledger_id    BIGINT NOT NULL,                             -- 被邀请加入的账本ID，关联account_ledgers表的id
    sender_id    BIGINT NOT NULL,                             -- 邀请发送者用户ID，关联users表的id
    recipient_id BIGINT NOT NULL,                             -- 邀请接收者用户ID，关联users表的id
    status       VARCHAR(10) CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')) DEFAULT 'PENDING', -- 邀请状态：待处理、已接受或已拒绝
    created_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    is_deleted   BOOLEAN DEFAULT FALSE,                                              -- 是否已逻辑删除
    deleted_at   TIMESTAMP,                                                          -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段，避免关联查询
    ledger_name       VARCHAR(100),                          -- 冗余账本名称
    sender_nickname   VARCHAR(50),                           -- 冗余发送者昵称
    recipient_nickname VARCHAR(50),                          -- 冗余接收者昵称
    
    CONSTRAINT unique_ledger_recipient UNIQUE (ledger_id, recipient_id) -- 唯一约束，一个用户对一个账本只能有一条待处理的邀请
);
CREATE INDEX idx_ledger_id_invitations ON invitations (ledger_id); -- 账本ID索引
CREATE INDEX idx_sender_id ON invitations (sender_id);         -- 发送者ID索引
CREATE INDEX idx_recipient_id ON invitations (recipient_id);   -- 接收者ID索引
CREATE INDEX idx_status_created ON invitations (status, created_at); -- 复合索引，支持按状态和时间查询

-- 结算记录表，追踪用户之间的付款结算（添加冗余字段）
CREATE TABLE IF NOT EXISTS settlements
(
    id          SERIAL PRIMARY KEY,                          -- 主键ID
    ledger_id   BIGINT         NOT NULL,                     -- 关联的账本ID，关联account_ledgers表的id
    payer_id    BIGINT         NOT NULL,                     -- 付款人用户ID，关联users表的id
    receiver_id BIGINT         NOT NULL,                     -- 收款人用户ID，关联users表的id
    amount      DECIMAL(10, 2) NOT NULL,                     -- 结算金额
    status      VARCHAR(10) CHECK (status IN ('PENDING', 'COMPLETED')) DEFAULT 'PENDING', -- 结算状态：待处理或已完成
    created_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    is_deleted  BOOLEAN DEFAULT FALSE,                                    -- 是否已逻辑删除
    deleted_at  TIMESTAMP,                                                -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段
    ledger_name       VARCHAR(100),                          -- 冗余账本名称
    payer_nickname    VARCHAR(50),                           -- 冗余付款人昵称
    receiver_nickname VARCHAR(50),                           -- 冗余收款人昵称
    description       TEXT                                   -- 结算说明
);
CREATE INDEX idx_ledger_id_settlements ON settlements (ledger_id);     -- 账本ID索引
CREATE INDEX idx_payer_id_settlements ON settlements (payer_id);       -- 付款人ID索引
CREATE INDEX idx_receiver_id ON settlements (receiver_id);             -- 收款人ID索引
CREATE INDEX idx_status_amount ON settlements (status, amount);        -- 复合索引，支持按状态和金额查询

-- 通知表，用于推送消息给用户（支持多种通知类型、已读/未读状态、6个月自动清理）
CREATE TABLE IF NOT EXISTS notifications
(
    id          SERIAL PRIMARY KEY,                          -- 主键ID
    user_id     BIGINT         NOT NULL,                     -- 接收者用户ID，关联users表的id
    type        VARCHAR(20) CHECK (type IN ('INVITATION', 'SYSTEM', 'SETTLEMENT')) NOT NULL, -- 通知类型：邀请、系统、结算
    title       VARCHAR(255)   NOT NULL,                     -- 通知标题
    content     TEXT,                                        -- 通知内容
    is_read     BOOLEAN DEFAULT FALSE,                       -- 是否已读
    read_at     TIMESTAMP,                                   -- 读取时间
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- 创建时间
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,         -- 更新时间
    is_deleted  BOOLEAN DEFAULT FALSE,                       -- 是否已逻辑删除（用户可手动删除）
    deleted_at  TIMESTAMP,                                   -- 逻辑删除时间
    
    -- 【数据冗余优化】新增冗余字段，避免关联查询
    user_nickname VARCHAR(50)                                -- 冗余接收者昵称，避免关联users表查询
);
CREATE INDEX idx_user_id_notifications ON notifications (user_id);          -- 用户ID索引，支持按用户查询
CREATE INDEX idx_is_read ON notifications (is_read);                        -- 已读状态索引，支持按已读/未读查询
CREATE INDEX idx_created_at_notifications ON notifications (created_at);    -- 创建时间索引，支持按时间排序和6月清理
CREATE INDEX idx_notification_type ON notifications (type);                 -- 通知类型索引，支持按类型筛选
CREATE INDEX idx_user_unread ON notifications (user_id, is_read);           -- 复合索引，支持查询用户未读通知

-- 审计日志表，记录账本、成员、条目、结算等操作的审计记录（不可修改，仅增不删）
CREATE TABLE IF NOT EXISTS audit_logs
(
    id          SERIAL PRIMARY KEY,                          -- 主键ID
    entity_type VARCHAR(20) CHECK (entity_type IN ('LEDGER', 'MEMBER', 'ENTRY', 'SETTLEMENT', 'CATEGORY')) NOT NULL, -- 实体类型
    entity_id   BIGINT         NOT NULL,                     -- 实体ID，对应的记录ID
    action      VARCHAR(10) CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')) NOT NULL, -- 操作类型
    actor_id    BIGINT         NOT NULL,                     -- 操作者用户ID，关联users表的id
    ledger_id   BIGINT,                                      -- 关联的账本ID（可选，提供操作上下文）
    changes     TEXT,                                        -- 变更内容，TEXT或JSON格式存储前后值对比
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP          -- 创建时间，审计日志仅记录创建时间
    
    -- 注意：审计日志是不可变的，不包含updated_at、is_deleted等字段
);
CREATE INDEX idx_entity_type ON audit_logs (entity_type);                   -- 实体类型索引，支持按类型查询
CREATE INDEX idx_entity_id ON audit_logs (entity_id);                       -- 实体ID索引，支持查询特定实体的操作历史
CREATE INDEX idx_actor_id ON audit_logs (actor_id);                         -- 操作者ID索引，支持查询用户的操作记录
CREATE INDEX idx_ledger_id_audit ON audit_logs (ledger_id);                 -- 账本ID索引，支持查询账本相关操作
CREATE INDEX idx_created_at_audit ON audit_logs (created_at);               -- 创建时间索引，支持按时间查询和排序
CREATE INDEX idx_entity_type_id_time ON audit_logs (entity_type, entity_id, created_at); -- 复合索引，支持查询特定实体的操作时间线

-- ========================================
-- 【新增】汇总视图表（物化视图的表实现）
-- ========================================

-- 支出记录汇总视图表，避免复杂的多表关联查询
CREATE TABLE IF NOT EXISTS expense_summary_view
(
    id                SERIAL PRIMARY KEY,                     -- 主键ID
    record_id         BIGINT NOT NULL UNIQUE,                 -- 关联的支出记录ID
    ledger_id         BIGINT NOT NULL,                        -- 账本ID
    ledger_name       VARCHAR(100) NOT NULL,                  -- 账本名称
    category_name     VARCHAR(50) NOT NULL,                   -- 类别名称
    payer_id          BIGINT NOT NULL,                        -- 付款人ID
    payer_nickname    VARCHAR(50) NOT NULL,                   -- 付款人昵称
    creator_nickname  VARCHAR(50) NOT NULL,                   -- 创建者昵称
    amount            DECIMAL(10, 2) NOT NULL,                -- 支出金额
    description       TEXT,                                   -- 支出描述
    expense_date      DATE NOT NULL,                          -- 支出日期
    participant_count INT NOT NULL,                           -- 参与人数
    avg_amount        DECIMAL(10, 2) NOT NULL,                -- 人均金额
    participants      TEXT NOT NULL,                          -- JSON格式参与者详细信息
    created_at        TIMESTAMP NOT NULL,                     -- 创建时间
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP     -- 更新时间
);
CREATE INDEX idx_expense_summary_ledger ON expense_summary_view (ledger_id);
CREATE INDEX idx_expense_summary_date ON expense_summary_view (expense_date);
CREATE INDEX idx_expense_summary_payer ON expense_summary_view (payer_id);
CREATE INDEX idx_expense_summary_amount ON expense_summary_view (ledger_id, amount);

-- 账本成员余额汇总视图表，避免复杂的余额计算查询
CREATE TABLE IF NOT EXISTS ledger_balance_summary_view
(
    id              SERIAL PRIMARY KEY,                       -- 主键ID
    ledger_id       BIGINT NOT NULL,                          -- 账本ID
    user_id         BIGINT NOT NULL,                          -- 用户ID
    ledger_name     VARCHAR(100) NOT NULL,                    -- 账本名称
    user_nickname   VARCHAR(50) NOT NULL,                     -- 用户昵称
    total_paid      DECIMAL(12, 2) DEFAULT 0,                 -- 总支付金额
    total_shared    DECIMAL(12, 2) DEFAULT 0,                 -- 总分摊金额
    balance         DECIMAL(12, 2) DEFAULT 0,                 -- 余额
    record_count    INT DEFAULT 0,                            -- 参与记录数
    last_activity   TIMESTAMP,                                -- 最后活动时间
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 更新时间
    
    CONSTRAINT unique_ledger_user_balance UNIQUE (ledger_id, user_id)
);
CREATE INDEX idx_balance_summary_ledger ON ledger_balance_summary_view (ledger_id);
CREATE INDEX idx_balance_summary_user ON ledger_balance_summary_view (user_id);
CREATE INDEX idx_balance_summary_balance ON ledger_balance_summary_view (ledger_id, balance);

-- ========================================
-- 简单触发器函数定义
-- ========================================

-- 函数：自动更新updated_at字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 函数：自动设置逻辑删除时间
CREATE OR REPLACE FUNCTION set_deleted_at()
RETURNS TRIGGER AS $$
BEGIN
    -- 只有当is_deleted从FALSE变为TRUE时才设置deleted_at
    IF NEW.is_deleted = TRUE AND OLD.is_deleted = FALSE THEN
        NEW.deleted_at = CURRENT_TIMESTAMP;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 函数：自动为新账本添加默认类别（简单触发器）
CREATE OR REPLACE FUNCTION add_default_categories_to_new_ledger()
RETURNS TRIGGER AS $$
BEGIN
    -- 为新账本添加所有默认类别，同时填充冗余的category_name字段
    INSERT INTO ledger_categories (ledger_id, category_id, category_name, display_order)
    SELECT NEW.id, ec.id, ec.category_name, ec.display_order
    FROM expense_categories ec
    WHERE ec.is_default = TRUE AND ec.is_deleted = FALSE;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========================================
-- 触发器创建（只包含简单触发器）
-- ========================================

-- 为所有表创建自动更新updated_at字段的触发器
CREATE TRIGGER update_roles_updated_at 
    BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_user_roles_updated_at 
    BEFORE UPDATE ON user_roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_account_ledgers_updated_at 
    BEFORE UPDATE ON account_ledgers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_categories_updated_at 
    BEFORE UPDATE ON expense_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ledger_categories_updated_at 
    BEFORE UPDATE ON ledger_categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ledger_members_updated_at 
    BEFORE UPDATE ON ledger_members
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_records_updated_at 
    BEFORE UPDATE ON expense_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_expense_participants_updated_at 
    BEFORE UPDATE ON expense_participants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_invitations_updated_at 
    BEFORE UPDATE ON invitations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_settlements_updated_at 
    BEFORE UPDATE ON settlements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at 
    BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 为所有表创建自动设置deleted_at的触发器
CREATE TRIGGER set_roles_deleted_at 
    BEFORE UPDATE OF is_deleted ON roles
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_users_deleted_at 
    BEFORE UPDATE OF is_deleted ON users
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_user_roles_deleted_at 
    BEFORE UPDATE OF is_deleted ON user_roles
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_account_ledgers_deleted_at 
    BEFORE UPDATE OF is_deleted ON account_ledgers
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_expense_categories_deleted_at 
    BEFORE UPDATE OF is_deleted ON expense_categories
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_ledger_categories_deleted_at 
    BEFORE UPDATE OF is_deleted ON ledger_categories
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_ledger_members_deleted_at 
    BEFORE UPDATE OF is_deleted ON ledger_members
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_expense_records_deleted_at 
    BEFORE UPDATE OF is_deleted ON expense_records
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_expense_participants_deleted_at 
    BEFORE UPDATE OF is_deleted ON expense_participants
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_invitations_deleted_at 
    BEFORE UPDATE OF is_deleted ON invitations
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_settlements_deleted_at 
    BEFORE UPDATE OF is_deleted ON settlements
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

CREATE TRIGGER set_notifications_deleted_at 
    BEFORE UPDATE OF is_deleted ON notifications
    FOR EACH ROW EXECUTE FUNCTION set_deleted_at();

-- 为新账本自动添加默认类别的触发器
CREATE TRIGGER add_default_categories_trigger
    AFTER INSERT ON account_ledgers
    FOR EACH ROW EXECUTE FUNCTION add_default_categories_to_new_ledger();

-- ========================================
-- 初始数据插入
-- ========================================

-- 插入系统默认角色
INSERT INTO roles (role_name, description, role_type)
VALUES ('SUPER_ADMIN', '超级管理员，拥有系统最高权限', 'ADMIN'),
       ('ADMIN', '普通管理员，拥有一般管理权限', 'ADMIN'),
       ('LEDGER_OWNER', '账本所有者，可以管理自己创建的账本', 'USER'),
       ('LEDGER_PARTICIPANT', '账本参与者，可以参与他人的账本', 'USER');

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

-- ========================================
-- PostgreSQL表和字段注释
-- ========================================

-- 角色表注释
COMMENT ON TABLE roles IS '角色表，存储系统中所有可能的角色';
COMMENT ON COLUMN roles.id IS '主键ID，自增长';
COMMENT ON COLUMN roles.role_name IS '角色名称，唯一约束，如SUPER_ADMIN、ADMIN等';
COMMENT ON COLUMN roles.description IS '角色描述，说明该角色的用途和权限范围';
COMMENT ON COLUMN roles.role_type IS '角色类型，ADMIN表示管理员角色，USER表示普通用户角色';
COMMENT ON COLUMN roles.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN roles.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN roles.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN roles.deleted_at IS '逻辑删除时间，通过触发器自动设置';

-- 用户表注释
COMMENT ON TABLE users IS '用户表，存储用户信息，包含统计冗余字段以优化查询性能';
COMMENT ON COLUMN users.id IS '主键ID，自增长';
COMMENT ON COLUMN users.username IS '用户名，唯一约束，用于登录';
COMMENT ON COLUMN users.nickname IS '用户昵称，用于显示';
COMMENT ON COLUMN users.avatar IS '头像URL，可选字段，存储用户头像链接';
COMMENT ON COLUMN users.bio IS '用户简介，可选字段，用户自我介绍';
COMMENT ON COLUMN users.phone IS '手机号码，可选字段，用于联系和验证';
COMMENT ON COLUMN users.password IS '密码，加密存储，不能明文保存';
COMMENT ON COLUMN users.email IS '电子邮箱，唯一约束，用于登录和通知';
COMMENT ON COLUMN users.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN users.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN users.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN users.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN users.ledger_count IS '参与的账本数量（JOINED状态），冗余字段避免COUNT查询';
COMMENT ON COLUMN users.created_ledger_count IS '创建的账本数量，冗余字段避免COUNT查询';
COMMENT ON COLUMN users.total_paid IS '累计支付金额，冗余字段避免SUM查询';
COMMENT ON COLUMN users.total_owed IS '累计欠款金额，负数表示别人欠他，正数表示他欠别人';
COMMENT ON COLUMN users.last_activity_at IS '最后活动时间，用于统计活跃度';

-- 用户角色关联表注释
COMMENT ON TABLE user_roles IS '用户角色关联表，存储用户和角色的多对多关系';
COMMENT ON COLUMN user_roles.id IS '主键ID，自增长';
COMMENT ON COLUMN user_roles.user_id IS '用户ID，关联users表的id';
COMMENT ON COLUMN user_roles.role_id IS '角色ID，关联roles表的id';
COMMENT ON COLUMN user_roles.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN user_roles.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN user_roles.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN user_roles.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON CONSTRAINT unique_user_role ON user_roles IS '唯一约束，确保一个用户不会重复分配同一个角色';

-- 账本表注释
COMMENT ON TABLE account_ledgers IS '账本表，存储所有账本信息，包含统计冗余字段以避免复杂聚合查询';
COMMENT ON COLUMN account_ledgers.id IS '主键ID，自增长';
COMMENT ON COLUMN account_ledgers.ledger_name IS '账本名称，用户自定义';
COMMENT ON COLUMN account_ledgers.description IS '账本描述，可选字段，用于说明账本用途';
COMMENT ON COLUMN account_ledgers.creator_id IS '创建者ID，关联users表的id';
COMMENT ON COLUMN account_ledgers.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN account_ledgers.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN account_ledgers.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN account_ledgers.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN account_ledgers.creator_nickname IS '创建者昵称冗余字段，避免关联users表查询';
COMMENT ON COLUMN account_ledgers.member_count IS '成员数量（JOINED状态），冗余字段避免COUNT查询';
COMMENT ON COLUMN account_ledgers.invited_count IS '待加入成员数量（INVITED状态），冗余字段';
COMMENT ON COLUMN account_ledgers.total_expenses IS '总支出金额，冗余字段避免SUM查询';
COMMENT ON COLUMN account_ledgers.record_count IS '支出记录数量，冗余字段避免COUNT查询';
COMMENT ON COLUMN account_ledgers.last_expense_date IS '最后消费日期，用于显示账本活跃程度';
COMMENT ON COLUMN account_ledgers.last_activity_at IS '最后活动时间，包括创建、更新等任何操作';

-- 支出类别表注释
COMMENT ON TABLE expense_categories IS '支出类别表，存储所有可能的支出类别';
COMMENT ON COLUMN expense_categories.id IS '主键ID，自增长';
COMMENT ON COLUMN expense_categories.category_name IS '类别名称，如"餐饮"、"交通"等，唯一约束';
COMMENT ON COLUMN expense_categories.description IS '类别描述，详细说明该类别的用途';
COMMENT ON COLUMN expense_categories.is_default IS '是否为默认类别，TRUE表示新账本创建时默认添加';
COMMENT ON COLUMN expense_categories.is_system IS '是否为系统类别，TRUE表示不可删除的预设类别';
COMMENT ON COLUMN expense_categories.creator_id IS '创建者ID，NULL表示系统创建，否则关联users表的id';
COMMENT ON COLUMN expense_categories.display_order IS '默认显示顺序，数字越小越靠前显示';
COMMENT ON COLUMN expense_categories.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN expense_categories.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN expense_categories.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN expense_categories.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN expense_categories.usage_count IS '使用次数统计，冗余字段用于热门类别排序';

-- 账本类别关联表注释
COMMENT ON TABLE ledger_categories IS '账本类别关联表，记录每个账本使用的类别';
COMMENT ON COLUMN ledger_categories.id IS '主键ID，自增长';
COMMENT ON COLUMN ledger_categories.ledger_id IS '账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN ledger_categories.category_id IS '类别ID，关联expense_categories表的id';
COMMENT ON COLUMN ledger_categories.display_order IS '在此账本中的显示顺序，可自定义排序';
COMMENT ON COLUMN ledger_categories.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN ledger_categories.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN ledger_categories.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN ledger_categories.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN ledger_categories.category_name IS '冗余类别名称，避免关联expense_categories表查询';
COMMENT ON COLUMN ledger_categories.usage_count IS '在此账本中的使用次数，用于热门类别排序';
COMMENT ON CONSTRAINT unique_ledger_category ON ledger_categories IS '唯一约束，一个账本对一个类别只能有一条关联记录';

-- 账本成员表注释
COMMENT ON TABLE ledger_members IS '账本成员表，追踪每个账本中的用户，包含余额等冗余字段';
COMMENT ON COLUMN ledger_members.id IS '主键ID，自增长';
COMMENT ON COLUMN ledger_members.ledger_id IS '账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN ledger_members.user_id IS '用户ID，关联users表的id';
COMMENT ON COLUMN ledger_members.join_status IS '加入状态，INVITED表示已邀请，JOINED表示已加入';
COMMENT ON COLUMN ledger_members.invited_at IS '邀请时间，记录何时被邀请加入账本';
COMMENT ON COLUMN ledger_members.joined_at IS '加入时间，记录何时正式加入账本，可为空';
COMMENT ON COLUMN ledger_members.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN ledger_members.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN ledger_members.user_nickname IS '冗余用户昵称，避免关联users表查询';
COMMENT ON COLUMN ledger_members.ledger_name IS '冗余账本名称，避免关联account_ledgers表查询';
COMMENT ON COLUMN ledger_members.total_paid IS '该成员在此账本的总支付金额，冗余字段';
COMMENT ON COLUMN ledger_members.total_shared IS '该成员在此账本的总分摊金额，冗余字段';
COMMENT ON COLUMN ledger_members.balance IS '余额，正数表示别人欠他，负数表示他欠别人';
COMMENT ON COLUMN ledger_members.record_count IS '参与的支出记录数量，冗余字段';
COMMENT ON COLUMN ledger_members.last_activity_at IS '在此账本中的最后活动时间';
COMMENT ON CONSTRAINT unique_ledger_user ON ledger_members IS '唯一约束，一个用户在一个账本中只能有一条记录';

-- 支出记录表注释
COMMENT ON TABLE expense_records IS '支出记录表，存储账目记录，包含大量冗余字段以避免N表关联查询';
COMMENT ON COLUMN expense_records.id IS '主键ID，自增长';
COMMENT ON COLUMN expense_records.ledger_id IS '所属账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN expense_records.category_id IS '支出类别ID，关联expense_categories表的id';
COMMENT ON COLUMN expense_records.payer_id IS '付款人用户ID，关联users表的id';
COMMENT ON COLUMN expense_records.amount IS '支出金额，使用DECIMAL保证精度';
COMMENT ON COLUMN expense_records.description IS '支出描述，用户填写的备注信息';
COMMENT ON COLUMN expense_records.expense_date IS '支出日期，实际发生消费的日期';
COMMENT ON COLUMN expense_records.created_by IS '记录创建者ID，关联users表的id';
COMMENT ON COLUMN expense_records.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN expense_records.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN expense_records.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN expense_records.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN expense_records.ledger_name IS '冗余账本名称，避免关联account_ledgers表查询';
COMMENT ON COLUMN expense_records.category_name IS '冗余类别名称，避免关联expense_categories表查询';
COMMENT ON COLUMN expense_records.payer_nickname IS '冗余付款人昵称，避免关联users表查询';
COMMENT ON COLUMN expense_records.creator_nickname IS '冗余创建者昵称，避免关联users表查询';
COMMENT ON COLUMN expense_records.participant_count IS '参与人数，预计算字段避免COUNT查询';
COMMENT ON COLUMN expense_records.avg_amount IS '人均分摊金额，预计算字段避免除法运算';
COMMENT ON COLUMN expense_records.participants_info IS 'JSON格式参与者信息，避免关联expense_participants表查询';
COMMENT ON COLUMN expense_records.has_settlement IS '是否已有结算记录，状态字段用于快速筛选';

-- 支出参与者表注释
COMMENT ON TABLE expense_participants IS '支出参与者表，追踪每笔支出的参与用户，包含冗余字段';
COMMENT ON COLUMN expense_participants.id IS '主键ID，自增长';
COMMENT ON COLUMN expense_participants.record_id IS '关联的支出记录ID，关联expense_records表的id';
COMMENT ON COLUMN expense_participants.user_id IS '参与者用户ID，关联users表的id';
COMMENT ON COLUMN expense_participants.amount IS '分摊金额，该用户需要承担的金额';
COMMENT ON COLUMN expense_participants.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN expense_participants.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN expense_participants.user_nickname IS '冗余用户昵称，避免关联users表查询';
COMMENT ON COLUMN expense_participants.expense_amount IS '冗余总支出金额，避免关联expense_records表查询';
COMMENT ON COLUMN expense_participants.expense_date IS '冗余支出日期，避免关联expense_records表查询';
COMMENT ON COLUMN expense_participants.ledger_id IS '冗余账本ID，避免关联expense_records表查询';
COMMENT ON COLUMN expense_participants.category_name IS '冗余类别名称，避免关联expense_records表查询';
COMMENT ON CONSTRAINT unique_record_user ON expense_participants IS '唯一约束，一个用户在一条支出记录中只能有一条参与记录';

-- 邀请通知表注释
COMMENT ON TABLE invitations IS '邀请通知表，管理加入账本的邀请流程';
COMMENT ON COLUMN invitations.id IS '主键ID，自增长';
COMMENT ON COLUMN invitations.ledger_id IS '被邀请加入的账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN invitations.sender_id IS '邀请发送者用户ID，关联users表的id';
COMMENT ON COLUMN invitations.recipient_id IS '邀请接收者用户ID，关联users表的id';
COMMENT ON COLUMN invitations.status IS '邀请状态，PENDING表示待处理，ACCEPTED表示已接受，REJECTED表示已拒绝';
COMMENT ON COLUMN invitations.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN invitations.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN invitations.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN invitations.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN invitations.ledger_name IS '冗余账本名称，避免关联account_ledgers表查询';
COMMENT ON COLUMN invitations.sender_nickname IS '冗余发送者昵称，避免关联users表查询';
COMMENT ON COLUMN invitations.recipient_nickname IS '冗余接收者昵称，避免关联users表查询';
COMMENT ON CONSTRAINT unique_ledger_recipient ON invitations IS '唯一约束，一个用户对一个账本只能有一条待处理的邀请';

-- 结算记录表注释
COMMENT ON TABLE settlements IS '结算记录表，追踪用户之间的付款结算';
COMMENT ON COLUMN settlements.id IS '主键ID，自增长';
COMMENT ON COLUMN settlements.ledger_id IS '关联的账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN settlements.payer_id IS '付款人用户ID，关联users表的id';
COMMENT ON COLUMN settlements.receiver_id IS '收款人用户ID，关联users表的id';
COMMENT ON COLUMN settlements.amount IS '结算金额，实际转账的金额';
COMMENT ON COLUMN settlements.status IS '结算状态，PENDING表示待处理，COMPLETED表示已完成';
COMMENT ON COLUMN settlements.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN settlements.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN settlements.is_deleted IS '逻辑删除标志，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN settlements.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN settlements.ledger_name IS '冗余账本名称，避免关联account_ledgers表查询';
COMMENT ON COLUMN settlements.payer_nickname IS '冗余付款人昵称，避免关联users表查询';
COMMENT ON COLUMN settlements.receiver_nickname IS '冗余收款人昵称，避免关联users表查询';
COMMENT ON COLUMN settlements.description IS '结算说明，用户填写的转账备注';

-- 通知表注释
COMMENT ON TABLE notifications IS '通知表，用于推送各类消息给用户，支持邀请、系统、结算等类型，支持已读/未读状态，半年自动清理';
COMMENT ON COLUMN notifications.id IS '主键ID，自增长';
COMMENT ON COLUMN notifications.user_id IS '接收者用户ID，关联users表的id';
COMMENT ON COLUMN notifications.type IS '通知类型，INVITATION表示邀请，SYSTEM表示系统通知，SETTLEMENT表示结算提醒';
COMMENT ON COLUMN notifications.title IS '通知标题，简短描述通知内容';
COMMENT ON COLUMN notifications.content IS '通知内容，详细说明信息';
COMMENT ON COLUMN notifications.is_read IS '是否已读，默认为FALSE表示未读';
COMMENT ON COLUMN notifications.read_at IS '读取时间，用户阅读通知的时间';
COMMENT ON COLUMN notifications.created_at IS '创建时间，自动设置为当前时间戳';
COMMENT ON COLUMN notifications.updated_at IS '更新时间，通过触发器自动维护';
COMMENT ON COLUMN notifications.is_deleted IS '逻辑删除标志，用户可手动删除通知，FALSE表示未删除，TRUE表示已删除';
COMMENT ON COLUMN notifications.deleted_at IS '逻辑删除时间，通过触发器自动设置';
COMMENT ON COLUMN notifications.user_nickname IS '冗余接收者昵称，避免关联users表查询';

-- 审计日志表注释
COMMENT ON TABLE audit_logs IS '审计日志表，记录账本、成员、条目、结算等关键操作，仅增不删，用于追踪数据变更历史和合规审计';
COMMENT ON COLUMN audit_logs.id IS '主键ID，自增长';
COMMENT ON COLUMN audit_logs.entity_type IS '实体类型，LEDGER表示账本，MEMBER表示成员，ENTRY表示条目，SETTLEMENT表示结算，CATEGORY表示类别';
COMMENT ON COLUMN audit_logs.entity_id IS '实体ID，对应的具体记录ID，用于定位被操作的数据';
COMMENT ON COLUMN audit_logs.action IS '操作类型，CREATE表示新建，UPDATE表示更新，DELETE表示删除';
COMMENT ON COLUMN audit_logs.actor_id IS '操作者用户ID，关联users表的id，记录谁执行了该操作';
COMMENT ON COLUMN audit_logs.ledger_id IS '关联的账本ID（可选），关联account_ledgers表的id，提供操作的上下文';
COMMENT ON COLUMN audit_logs.changes IS '变更内容，TEXT或JSON格式，记录操作的详细信息（如前后值对比）';
COMMENT ON COLUMN audit_logs.created_at IS '创建时间，自动设置为当前时间戳，记录操作发生的时间';

-- 支出记录汇总视图表注释
COMMENT ON TABLE expense_summary_view IS '支出记录汇总视图表，预计算常用查询结果以提升性能';
COMMENT ON COLUMN expense_summary_view.id IS '主键ID，自增长';
COMMENT ON COLUMN expense_summary_view.record_id IS '关联的支出记录ID，关联expense_records表的id';
COMMENT ON COLUMN expense_summary_view.ledger_id IS '账本ID，用于按账本筛选';
COMMENT ON COLUMN expense_summary_view.ledger_name IS '账本名称，预计算字段';
COMMENT ON COLUMN expense_summary_view.category_name IS '类别名称，预计算字段';
COMMENT ON COLUMN expense_summary_view.payer_id IS '付款人ID，用于按付款人筛选';
COMMENT ON COLUMN expense_summary_view.payer_nickname IS '付款人昵称，预计算字段';
COMMENT ON COLUMN expense_summary_view.creator_nickname IS '创建者昵称，预计算字段';
COMMENT ON COLUMN expense_summary_view.amount IS '支出金额，预计算字段';
COMMENT ON COLUMN expense_summary_view.description IS '支出描述，预计算字段';
COMMENT ON COLUMN expense_summary_view.expense_date IS '支出日期，预计算字段';
COMMENT ON COLUMN expense_summary_view.participant_count IS '参与人数，预计算字段';
COMMENT ON COLUMN expense_summary_view.avg_amount IS '人均金额，预计算字段';
COMMENT ON COLUMN expense_summary_view.participants IS 'JSON格式参与者详细信息，预计算字段';
COMMENT ON COLUMN expense_summary_view.created_at IS '创建时间，预计算字段';
COMMENT ON COLUMN expense_summary_view.updated_at IS '更新时间，记录汇总数据的刷新时间';

-- 账本成员余额汇总视图表注释
COMMENT ON TABLE ledger_balance_summary_view IS '账本成员余额汇总表，避免复杂的余额计算查询';
COMMENT ON COLUMN ledger_balance_summary_view.id IS '主键ID，自增长';
COMMENT ON COLUMN ledger_balance_summary_view.ledger_id IS '账本ID，用于按账本筛选';
COMMENT ON COLUMN ledger_balance_summary_view.user_id IS '用户ID，用于按用户筛选';
COMMENT ON COLUMN ledger_balance_summary_view.ledger_name IS '账本名称，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.user_nickname IS '用户昵称，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.total_paid IS '总支付金额，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.total_shared IS '总分摊金额，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.balance IS '余额，预计算字段，正数表示别人欠他';
COMMENT ON COLUMN ledger_balance_summary_view.record_count IS '参与记录数，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.last_activity IS '最后活动时间，预计算字段';
COMMENT ON COLUMN ledger_balance_summary_view.updated_at IS '更新时间，记录汇总数据的刷新时间';
COMMENT ON CONSTRAINT unique_ledger_user_balance ON ledger_balance_summary_view IS '唯一约束，每个账本的每个用户只能有一条汇总记录';

-- 触发器函数注释
COMMENT ON FUNCTION update_updated_at_column() IS '触发器函数：自动更新updated_at字段为当前时间戳';
COMMENT ON FUNCTION set_deleted_at() IS '触发器函数：当is_deleted设置为TRUE时自动设置deleted_at字段';
COMMENT ON FUNCTION add_default_categories_to_new_ledger() IS '触发器函数：为新创建的账本自动添加默认支出类别';

-- ========================================
-- 使用说明和最佳实践
-- ========================================

-- 【数据冗余字段维护说明】
-- 1. 冗余字段通过业务代码维护，不使用复杂触发器
-- 2. 定期校验和修复冗余数据的一致性
-- 3. 新增记录时同步更新相关冗余字段
-- 4. 删除或更新记录时同步更新统计字段

-- 【查询优化说明】
-- 1. 大部分查询可以直接使用冗余字段，避免JOIN操作
-- 2. 使用汇总视图表进行复杂统计查询
-- 3. 合理利用索引提升查询性能

-- 【维护说明】
-- 1. 简单触发器：自动更新时间戳、设置删除时间、添加默认类别
-- 2. 复杂业务逻辑：通过业务代码实现，便于调试和维护
-- 3. 定期运行数据一致性检查和修复程序

-- 【性能优化效果】
-- 1. 原需要5-6表关联的查询现在可以单表完成
-- 2. 复杂的统计查询可以直接读取预计算结果
-- 3. 减少数据库CPU和I/O负载
-- 4. 提升应用响应速度

-- 【数据一致性保障】
-- 1. 使用事务确保数据操作的原子性
-- 2. 业务代码中包含数据校验逻辑
-- 3. 定期执行数据一致性检查任务
-- 4. 设置监控告警及时发现数据异常