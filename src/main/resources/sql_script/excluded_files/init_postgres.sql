-- 创建共享记账应用数据库
-- 注意: PostgreSQL中创建数据库需要在不连接到目标数据库的情况下执行
-- 此处仅提供语法，实际执行时需要连接到默认数据库(如postgres)
-- CREATE DATABASE expense_sharing_app WITH ENCODING='UTF8' LC_COLLATE='en_US.UTF8' LC_CTYPE='en_US.UTF8';

-- 角色表，存储系统中所有可能的角色
CREATE TABLE IF NOT EXISTS roles
(
    id          SERIAL PRIMARY KEY,                            -- 主键ID
    role_name   VARCHAR(50) NOT NULL UNIQUE,                   -- 角色名称，唯一
    description TEXT,                                          -- 角色描述
    role_type   VARCHAR(20) NOT NULL,                          -- 角色类型：ADMIN（管理员）或USER（普通用户）
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP            -- 更新时间
);

-- 用户表，存储用户信息
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP            -- 更新时间
);

-- 注意：在PostgreSQL中，可以使用COMMENT ON语句添加表和列的注释
-- 以下是PostgreSQL中添加注释的正确语法，但在当前SQL解析器中可能会报错
-- 在实际PostgreSQL环境中使用时，请取消以下注释：
COMMENT ON TABLE roles IS '角色表，存储系统中所有可能的角色';
COMMENT ON COLUMN roles.id IS '主键ID';
COMMENT ON COLUMN roles.role_name IS '角色名称，唯一';
COMMENT ON COLUMN roles.description IS '角色描述';
COMMENT ON COLUMN roles.role_type IS '角色类型：ADMIN（管理员）或USER（普通用户）';
COMMENT ON COLUMN roles.created_at IS '创建时间';
COMMENT ON COLUMN roles.updated_at IS '更新时间';

COMMENT ON TABLE users IS '用户表，存储用户信息';
COMMENT ON COLUMN users.id IS '主键ID';
COMMENT ON COLUMN users.username IS '用户名，唯一';
COMMENT ON COLUMN users.nickname IS '昵称';
COMMENT ON COLUMN users.avatar IS '头像URL，可选';
COMMENT ON COLUMN users.bio IS '用户简介，可选';
COMMENT ON COLUMN users.phone IS '手机号码，可选';
COMMENT ON COLUMN users.password IS '密码，加密存储';
COMMENT ON COLUMN users.email IS '电子邮箱，唯一';
COMMENT ON COLUMN users.created_at IS '创建时间';
COMMENT ON COLUMN users.updated_at IS '更新时间';

-- 用户角色关联表，存储用户和角色的多对多关系
CREATE TABLE IF NOT EXISTS user_roles
(
    id         SERIAL PRIMARY KEY,                            -- 主键ID
    user_id    BIGINT NOT NULL,                               -- 用户ID，关联users表的id
    role_id    BIGINT NOT NULL,                               -- 角色ID，关联roles表的id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 创建时间
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,           -- 更新时间
    CONSTRAINT unique_user_role UNIQUE (user_id, role_id)     -- 唯一约束，确保一个用户不会重复分配同一个角色
);
CREATE INDEX idx_user_id_roles ON user_roles (user_id);       -- 用户ID索引
CREATE INDEX idx_role_id_users ON user_roles (role_id);       -- 角色ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：
COMMENT ON TABLE user_roles IS '用户角色关联表，存储用户和角色的多对多关系';
COMMENT ON COLUMN user_roles.id IS '主键ID';
COMMENT ON COLUMN user_roles.user_id IS '用户ID，关联users表的id';
COMMENT ON COLUMN user_roles.role_id IS '角色ID，关联roles表的id';
COMMENT ON COLUMN user_roles.created_at IS '创建时间';
COMMENT ON COLUMN user_roles.updated_at IS '更新时间';
COMMENT ON CONSTRAINT unique_user_role ON user_roles IS '唯一约束，确保一个用户不会重复分配同一个角色';

-- 账本表，存储所有账本信息
CREATE TABLE IF NOT EXISTS account_ledgers
(
    id          SERIAL PRIMARY KEY,                  -- 主键ID
    ledger_name   VARCHAR(100) NOT NULL,               -- 账本名称
    description TEXT,                                -- 账本描述
    creator_id  BIGINT       NOT NULL,               -- 创建者ID，关联users表的id
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);
CREATE INDEX idx_creator_id ON account_ledgers (creator_id); -- 创建者ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：
COMMENT ON TABLE account_ledgers IS '账本表，存储所有账本信息';
COMMENT ON COLUMN account_ledgers.id IS '主键ID';
COMMENT ON COLUMN account_ledgers.ledger_name IS '账本名称';
COMMENT ON COLUMN account_ledgers.description IS '账本描述';
COMMENT ON COLUMN account_ledgers.creator_id IS '创建者ID，关联users表的id';
COMMENT ON COLUMN account_ledgers.created_at IS '创建时间';
COMMENT ON COLUMN account_ledgers.updated_at IS '更新时间';

-- 支出类别表，存储所有可能的支出类别
CREATE TABLE IF NOT EXISTS expense_categories
(
    id            SERIAL PRIMARY KEY,                      -- 主键ID
    category_name VARCHAR(50) NOT NULL UNIQUE,             -- 类别名称，如"餐饮"、"交通"等
    description   TEXT,                                    -- 类别描述
    is_default    BOOLEAN              DEFAULT FALSE,      -- 是否为默认类别，TRUE表示新账本创建时默认添加
    is_system     BOOLEAN              DEFAULT FALSE,      -- 是否为系统类别，TRUE表示不可删除
    creator_id    BIGINT,                                  -- 创建者ID，NULL表示系统创建，否则关联users表的id
    display_order INT         NOT NULL DEFAULT 0,          -- 默认显示顺序
    created_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at    TIMESTAMP            DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);
CREATE INDEX idx_creator_id_categories ON expense_categories (creator_id); -- 创建者ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE expense_categories IS '支出类别表，存储所有可能的支出类别';
COMMENT ON COLUMN expense_categories.id IS '主键ID';
COMMENT ON COLUMN expense_categories.category_name IS '类别名称，如"餐饮"、"交通"等';
COMMENT ON COLUMN expense_categories.description IS '类别描述';
COMMENT ON COLUMN expense_categories.is_default IS '是否为默认类别，TRUE表示新账本创建时默认添加';
COMMENT ON COLUMN expense_categories.is_system IS '是否为系统类别，TRUE表示不可删除';
COMMENT ON COLUMN expense_categories.creator_id IS '创建者ID，NULL表示系统创建，否则关联users表的id';
COMMENT ON COLUMN expense_categories.display_order IS '默认显示顺序';
COMMENT ON COLUMN expense_categories.created_at IS '创建时间';
COMMENT ON COLUMN expense_categories.updated_at IS '更新时间';


-- 账本类别关联表，记录每个账本使用的类别
CREATE TABLE IF NOT EXISTS ledger_categories
(
    id            SERIAL PRIMARY KEY,                      -- 主键ID
    ledger_id       BIGINT NOT NULL,                         -- 账本ID，关联account_ledgers表的id
    category_id   BIGINT NOT NULL,                         -- 类别ID，关联expense_categories表的id
    display_order INT    NOT NULL DEFAULT 0,               -- 在此账本中的显示顺序
    created_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    CONSTRAINT unique_ledger_category UNIQUE (ledger_id, category_id) -- 唯一约束，一个账本对一个类别只能有一条关联
);
CREATE INDEX idx_ledger_id ON ledger_categories (ledger_id);         -- 账本ID索引
CREATE INDEX idx_category_id ON ledger_categories (category_id); -- 类别ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE ledger_categories IS '账本类别关联表，记录每个账本使用的类别';
COMMENT ON COLUMN ledger_categories.id IS '主键ID';
COMMENT ON COLUMN ledger_categories.ledger_id IS '账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN ledger_categories.category_id IS '类别ID，关联expense_categories表的id';
COMMENT ON COLUMN ledger_categories.display_order IS '在此账本中的显示顺序';
COMMENT ON COLUMN ledger_categories.created_at IS '创建时间';
COMMENT ON COLUMN ledger_categories.updated_at IS '更新时间';
COMMENT ON CONSTRAINT unique_ledger_category ON ledger_categories IS '唯一约束，一个账本对一个类别只能有一条关联';


-- 账本成员表，追踪每个账本中的用户
CREATE TABLE IF NOT EXISTS ledger_members
(
    id          SERIAL PRIMARY KEY,                      -- 主键ID
    ledger_id     BIGINT    NOT NULL,                      -- 账本ID，关联account_ledgers表的id
    user_id     BIGINT    NOT NULL,                      -- 用户ID，关联users表的id
    join_status VARCHAR(10) CHECK (join_status IN ('INVITED', 'JOINED')) DEFAULT 'INVITED', -- 加入状态：已邀请或已加入
    invited_at  TIMESTAMP                  DEFAULT CURRENT_TIMESTAMP,     -- 邀请时间
    joined_at   TIMESTAMP NULL,                                           -- 加入时间
    CONSTRAINT unique_ledger_user UNIQUE (ledger_id, user_id)                 -- 唯一约束，一个用户在一个账本中只能有一条记录
);
CREATE INDEX idx_ledger_id_members ON ledger_members (ledger_id);   -- 账本ID索引
CREATE INDEX idx_user_id_members ON ledger_members (user_id);   -- 用户ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE ledger_members IS '账本成员表，追踪每个账本中的用户';
COMMENT ON COLUMN ledger_members.id IS '主键ID';
COMMENT ON COLUMN ledger_members.ledger_id IS '账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN ledger_members.user_id IS '用户ID，关联users表的id';
COMMENT ON COLUMN ledger_members.join_status IS '加入状态：已邀请或已加入';
COMMENT ON COLUMN ledger_members.invited_at IS '邀请时间';
COMMENT ON COLUMN ledger_members.joined_at IS '加入时间';
COMMENT ON CONSTRAINT unique_ledger_user ON ledger_members IS '唯一约束，一个用户在一个账本中只能有一条记录';


-- 支出记录表，存储账目记录
CREATE TABLE IF NOT EXISTS expense_records
(
    id           SERIAL PRIMARY KEY,                      -- 主键ID
    ledger_id      BIGINT         NOT NULL,                 -- 所属账本ID，关联account_ledgers表的id
    category_id  BIGINT         NOT NULL,                 -- 支出类别ID，关联expense_categories表的id
    payer_id     BIGINT         NOT NULL,                 -- 付款人用户ID，关联users表的id
    amount       DECIMAL(10, 2) NOT NULL,                 -- 支出金额
    description  TEXT,                                    -- 支出描述
    expense_date DATE           NOT NULL,                 -- 支出日期
    created_by   BIGINT         NOT NULL,                 -- 记录创建者ID，关联users表的id
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,     -- 创建时间
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP      -- 更新时间
);
CREATE INDEX idx_ledger_id_records ON expense_records (ledger_id);         -- 账本ID索引
CREATE INDEX idx_category_id_records ON expense_records (category_id); -- 类别ID索引
CREATE INDEX idx_payer_id ON expense_records (payer_id);               -- 付款人ID索引
CREATE INDEX idx_created_by ON expense_records (created_by);           -- 创建者ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE expense_records IS '支出记录表，存储账目记录';
COMMENT ON COLUMN expense_records.id IS '主键ID';
COMMENT ON COLUMN expense_records.ledger_id IS '所属账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN expense_records.category_id IS '支出类别ID，关联expense_categories表的id';
COMMENT ON COLUMN expense_records.payer_id IS '付款人用户ID，关联users表的id';
COMMENT ON COLUMN expense_records.amount IS '支出金额';
COMMENT ON COLUMN expense_records.description IS '支出描述';
COMMENT ON COLUMN expense_records.expense_date IS '支出日期';
COMMENT ON COLUMN expense_records.created_by IS '记录创建者ID，关联users表的id';
COMMENT ON COLUMN expense_records.created_at IS '创建时间';
COMMENT ON COLUMN expense_records.updated_at IS '更新时间';


-- 支出参与者表，追踪每笔支出的参与用户
CREATE TABLE IF NOT EXISTS expense_participants
(
    id        SERIAL PRIMARY KEY,                      -- 主键ID
    record_id BIGINT         NOT NULL,                 -- 关联的支出记录ID，关联expense_records表的id
    user_id   BIGINT         NOT NULL,                 -- 参与者用户ID，关联users表的id
    amount    DECIMAL(10, 2) NOT NULL,                 -- 分摊金额
    CONSTRAINT unique_record_user UNIQUE (record_id, user_id) -- 唯一约束，一个用户在一条支出记录中只能有一条参与记录
);
CREATE INDEX idx_record_id ON expense_participants (record_id);           -- 支出记录ID索引
CREATE INDEX idx_user_id_participants ON expense_participants (user_id);  -- 用户ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE expense_participants IS '支出参与者表，追踪每笔支出的参与用户';
COMMENT ON COLUMN expense_participants.id IS '主键ID';
COMMENT ON COLUMN expense_participants.record_id IS '关联的支出记录ID，关联expense_records表的id';
COMMENT ON COLUMN expense_participants.user_id IS '参与者用户ID，关联users表的id';
COMMENT ON COLUMN expense_participants.amount IS '分摊金额';
COMMENT ON CONSTRAINT unique_record_user ON expense_participants IS '唯一约束，一个用户在一条支出记录中只能有一条参与记录';


-- 邀请通知表，管理加入账本的邀请流程
CREATE TABLE IF NOT EXISTS invitations
(
    id           SERIAL PRIMARY KEY,                      -- 主键ID
    ledger_id      BIGINT NOT NULL,                         -- 被邀请加入的账本ID，关联account_ledgers表的id
    sender_id    BIGINT NOT NULL,                         -- 邀请发送者用户ID，关联users表的id
    recipient_id BIGINT NOT NULL,                         -- 邀请接收者用户ID，关联users表的id
    status       VARCHAR(10) CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')) DEFAULT 'PENDING', -- 邀请状态：待处理、已接受或已拒绝
    created_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at   TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    CONSTRAINT unique_ledger_recipient UNIQUE (ledger_id, recipient_id) -- 唯一约束，一个用户对一个账本只能有一条待处理的邀请
);
CREATE INDEX idx_ledger_id_invitations ON invitations (ledger_id); -- 账本ID索引
CREATE INDEX idx_sender_id ON invitations (sender_id);         -- 发送者ID索引
CREATE INDEX idx_recipient_id ON invitations (recipient_id);   -- 接收者ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE invitations IS '邀请通知表，管理加入账本的邀请流程';
COMMENT ON COLUMN invitations.id IS '主键ID';
COMMENT ON COLUMN invitations.ledger_id IS '被邀请加入的账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN invitations.sender_id IS '邀请发送者用户ID，关联users表的id';
COMMENT ON COLUMN invitations.recipient_id IS '邀请接收者用户ID，关联users表的id';
COMMENT ON COLUMN invitations.status IS '邀请状态：待处理、已接受或已拒绝';
COMMENT ON COLUMN invitations.created_at IS '创建时间';
COMMENT ON COLUMN invitations.updated_at IS '更新时间';
COMMENT ON CONSTRAINT unique_ledger_recipient ON invitations IS '唯一约束，一个用户对一个账本只能有一条待处理的邀请';


-- 结算记录表，追踪用户之间的付款结算
CREATE TABLE IF NOT EXISTS settlements
(
    id          SERIAL PRIMARY KEY,                      -- 主键ID
    ledger_id     BIGINT         NOT NULL,                 -- 关联的账本ID，关联account_ledgers表的id
    payer_id    BIGINT         NOT NULL,                 -- 付款人用户ID，关联users表的id
    receiver_id BIGINT         NOT NULL,                 -- 收款人用户ID，关联users表的id
    amount      DECIMAL(10, 2) NOT NULL,                 -- 结算金额
    status      VARCHAR(10) CHECK (status IN ('PENDING', 'COMPLETED')) DEFAULT 'PENDING', -- 结算状态：待处理或已完成
    created_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    updated_at  TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP  -- 更新时间
);
CREATE INDEX idx_ledger_id_settlements ON settlements (ledger_id);     -- 账本ID索引
CREATE INDEX idx_payer_id_settlements ON settlements (payer_id);   -- 付款人ID索引
CREATE INDEX idx_receiver_id ON settlements (receiver_id);         -- 收款人ID索引

-- 在PostgreSQL中添加此表的注释（在实际环境中取消注释）：

COMMENT ON TABLE settlements IS '结算记录表，追踪用户之间的付款结算';
COMMENT ON COLUMN settlements.id IS '主键ID';
COMMENT ON COLUMN settlements.ledger_id IS '关联的账本ID，关联account_ledgers表的id';
COMMENT ON COLUMN settlements.payer_id IS '付款人用户ID，关联users表的id';
COMMENT ON COLUMN settlements.receiver_id IS '收款人用户ID，关联users表的id';
COMMENT ON COLUMN settlements.amount IS '结算金额';
COMMENT ON COLUMN settlements.status IS '结算状态：待处理或已完成';
COMMENT ON COLUMN settlements.created_at IS '创建时间';
COMMENT ON COLUMN settlements.updated_at IS '更新时间';


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

-- 创建触发器：新建账本时自动添加默认类别
-- 注意：以下是PostgreSQL特定语法，可能在其他SQL解析器中显示错误，但在PostgreSQL中是正确的

-- 在PostgreSQL中，触发器和函数的创建语法如下：

CREATE OR REPLACE FUNCTION add_default_categories()
RETURNS TRIGGER AS $$
BEGIN
    -- 为新账本添加所有默认类别
    INSERT INTO ledger_categories (ledger_id, category_id, display_order)
    SELECT NEW.id, id, display_order
    FROM expense_categories
    WHERE is_default = TRUE;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS after_account_ledger_insert ON account_ledgers;
CREATE TRIGGER after_account_ledger_insert
    AFTER INSERT ON account_ledgers
    FOR EACH ROW
    EXECUTE PROCEDURE add_default_categories();

-- 创建函数：自动更新updated_at字段
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为每个表创建更新时间触发器
CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_user_roles_updated_at BEFORE UPDATE ON user_roles
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_account_ledgers_updated_at BEFORE UPDATE ON account_ledgers
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_expense_categories_updated_at BEFORE UPDATE ON expense_categories
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_ledger_categories_updated_at BEFORE UPDATE ON ledger_categories
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_expense_records_updated_at BEFORE UPDATE ON expense_records
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_invitations_updated_at BEFORE UPDATE ON invitations
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

CREATE TRIGGER update_settlements_updated_at BEFORE UPDATE ON settlements
    FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- 注意：在实际PostgreSQL环境中，请取消上面注释的代码块，并使用它来创建触发器和函数
-- 由于当前环境可能使用MySQL解析器，上述PostgreSQL特定语法可能会报错
-- 以下是一个简化版本，仅用于说明目的

-- 在PostgreSQL中，可以使用以下方式实现自动更新时间戳：
-- 1. 使用触发器（如上面注释的代码）
-- 2. 或者在PostgreSQL 10+中使用生成列：
-- created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
-- updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
-- 并添加以下语句：
-- ALTER TABLE users ADD COLUMN updated_at TIMESTAMP GENERATED ALWAYS AS (CURRENT_TIMESTAMP) STORED;

-- 对于自动添加默认类别的功能，在PostgreSQL中应使用上述注释中的触发器实现
