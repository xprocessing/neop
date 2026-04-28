-- ==============================
-- NEOP 项目完整建表+初始化数据库脚本
-- 主体：gongziyu.com
-- 适配最终开发手册：无资金池、无余额逻辑
-- 包含：全部28张数据表 + 业务索引 + 基础初始化数据
-- ==============================

CREATE DATABASE IF NOT EXISTS neop_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE neop_db;

-- ==============================
-- 【一、系统权限模块 8张表】
-- ==============================

-- 1. 菜单表
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '菜单名称',
    menu_type VARCHAR(20) NOT NULL DEFAULT '' COMMENT '菜单类型：menu菜单、button按钮',
    menu_icon VARCHAR(100) DEFAULT '' COMMENT '菜单图标',
    menu_path VARCHAR(255) DEFAULT '' COMMENT '路由地址',
    component VARCHAR(255) DEFAULT '' COMMENT '前端组件路径',
    permission VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 2. 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL DEFAULT '' COMMENT '角色标识',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 3. 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id,menu_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 4. 管理员表
CREATE TABLE sys_admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL DEFAULT '' COMMENT '登录账号',
    password VARCHAR(100) NOT NULL DEFAULT '' COMMENT '登录密码',
    nickname VARCHAR(50) NOT NULL DEFAULT '' COMMENT '管理员昵称',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';

-- 5. 管理员角色关联表
CREATE TABLE sys_admin_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_admin_role (admin_id,role_id),
    INDEX idx_admin_id (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员角色关联表';

-- 6. 数据字典表
CREATE TABLE sys_dict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_code VARCHAR(50) NOT NULL DEFAULT '' COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '字典名称',
    dict_type VARCHAR(50) DEFAULT '' COMMENT '字典类型',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- 7. 字典数据表
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    label VARCHAR(100) NOT NULL DEFAULT '' COMMENT '字典标签',
    value VARCHAR(100) NOT NULL DEFAULT '' COMMENT '字典值',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 8. 系统配置表
CREATE TABLE sys_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(50) NOT NULL DEFAULT '' COMMENT '配置键名',
    config_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '配置名称',
    config_value VARCHAR(500) DEFAULT '' COMMENT '配置值',
    remark VARCHAR(255) DEFAULT '' COMMENT '备注说明',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统全局配置表';

-- 9. 操作日志表
CREATE TABLE sys_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    operator_type TINYINT NOT NULL DEFAULT 1 COMMENT '操作者类型：1管理员 2用户',
    operator_id BIGINT NOT NULL DEFAULT 0 COMMENT '操作者ID',
    operator_name VARCHAR(50) DEFAULT '' COMMENT '操作者名称',
    module VARCHAR(50) DEFAULT '' COMMENT '操作模块',
    description VARCHAR(255) DEFAULT '' COMMENT '操作描述',
    request_method VARCHAR(10) DEFAULT '' COMMENT '请求方法',
    request_url VARCHAR(255) DEFAULT '' COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip VARCHAR(50) DEFAULT '' COMMENT '操作IP',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1成功 0失败',
    error_msg TEXT COMMENT '错误信息',
    cost_time BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator (operator_type, operator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- ==============================
-- 【二、用户基础模块 3张表】
-- ==============================

-- 10. 用户主表
CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    nickname VARCHAR(50) DEFAULT '' COMMENT '用户昵称',
    avatar VARCHAR(255) DEFAULT '' COMMENT '用户头像',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    sex TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
    invite_code VARCHAR(32) DEFAULT '' COMMENT '个人邀请码',
    parent_id BIGINT DEFAULT 0 COMMENT '上级邀请用户ID',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_invite_code (invite_code),
    INDEX idx_phone (phone),
    INDEX idx_parent_id (parent_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- 11. 用户微信绑定表
CREATE TABLE user_wechat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    openid VARCHAR(100) NOT NULL DEFAULT '' COMMENT '微信openid',
    unionid VARCHAR(100) DEFAULT '' COMMENT '微信unionid',
    app_type VARCHAR(20) DEFAULT '' COMMENT '应用类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户微信绑定表';

-- 12. 用户收货地址表
CREATE TABLE user_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL DEFAULT '' COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL DEFAULT '' COMMENT '收货人电话',
    province VARCHAR(50) DEFAULT '' COMMENT '省份',
    city VARCHAR(50) DEFAULT '' COMMENT '城市',
    district VARCHAR(50) DEFAULT '' COMMENT '区县',
    detail VARCHAR(255) DEFAULT '' COMMENT '详细地址',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认：0否 1是',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ==============================
-- 【三、营销增值模块 6张表】
-- ==============================

-- 13. 会员套餐表
CREATE TABLE member_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    package_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '套餐名称',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '套餐价格',
    day_num INT NOT NULL DEFAULT 0 COMMENT '会员天数',
    give_point INT NOT NULL DEFAULT 0 COMMENT '赠送积分',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员充值套餐表';

-- 14. 用户会员表
CREATE TABLE member_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_member TINYINT NOT NULL DEFAULT 0 COMMENT '是否会员：0否 1是',
    expire_time DATETIME NULL COMMENT '会员到期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开通时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员信息表';

-- 15. 用户积分表
CREATE TABLE point_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_point INT NOT NULL DEFAULT 0 COMMENT '累计积分',
    usable_point INT NOT NULL DEFAULT 0 COMMENT '可用积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 16. 积分流水表
CREATE TABLE point_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    point INT NOT NULL DEFAULT 0 COMMENT '变动积分',
    balance INT NOT NULL DEFAULT 0 COMMENT '变动后可用积分',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1获取 2消耗',
    source VARCHAR(50) DEFAULT '' COMMENT '来源：sign签到/invite邀请/member会员/give赠送',
    remark VARCHAR(255) DEFAULT '' COMMENT '变动备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_user_create (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水记录表';

-- 17. 用户邀请关系表
CREATE TABLE invite_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级用户ID',
    invite_num INT NOT NULL DEFAULT 0 COMMENT '累计邀请人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请关系表';

-- 18. 邀请奖励记录表
CREATE TABLE invite_reward_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '获奖用户ID',
    invite_user_id BIGINT NOT NULL COMMENT '被邀请用户ID',
    reward_type TINYINT NOT NULL DEFAULT 1 COMMENT '奖励类型：1积分',
    reward_num INT NOT NULL DEFAULT 0 COMMENT '奖励数量',
    remark VARCHAR(255) DEFAULT '' COMMENT '奖励备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请奖励记录表';

-- ==============================
-- 【四、电商交易模块 6张表】
-- ==============================

-- 19. 商品分类表
CREATE TABLE product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 20. 商品表
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    product_name VARCHAR(200) NOT NULL DEFAULT '' COMMENT '商品名称',
    product_img VARCHAR(500) DEFAULT '' COMMENT '商品封面图',
    product_images VARCHAR(1000) DEFAULT '' COMMENT '商品轮播图，逗号分隔',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '售价',
    original_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    sales INT NOT NULL DEFAULT 0 COMMENT '销量',
    content TEXT COMMENT '商品详情',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_status (category_id, status),
    INDEX idx_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 21. 购物车表
CREATE TABLE cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    num INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    is_checked TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0否 1是',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 22. 支付订单表
CREATE TABLE pay_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT '订单编号',
    pay_type TINYINT NOT NULL DEFAULT 1 COMMENT '支付类型：1微信支付',
    order_type TINYINT NOT NULL DEFAULT 1 COMMENT '订单类型：1会员充值',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    package_id BIGINT DEFAULT NULL COMMENT '会员套餐ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消',
    pay_time DATETIME NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status_create (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值支付订单表';

-- 23. 订单主表
CREATE TABLE `order` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT '订单编号',
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待付款 1待发货 2待收货 3已完成 4已取消',
    address_id BIGINT DEFAULT 0 COMMENT '收货地址ID',
    receiver_name VARCHAR(50) DEFAULT '' COMMENT '收货人',
    receiver_phone VARCHAR(20) DEFAULT '' COMMENT '收货电话',
    receiver_address VARCHAR(255) DEFAULT '' COMMENT '收货地址',
    pay_time DATETIME NULL COMMENT '支付时间',
    send_time DATETIME NULL COMMENT '发货时间',
    finish_time DATETIME NULL COMMENT '完成时间',
    cancel_time DATETIME NULL COMMENT '取消时间',
    remark VARCHAR(255) DEFAULT '' COMMENT '订单备注',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_status (user_id, status),
    INDEX idx_status_create (status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 24. 订单明细表
CREATE TABLE order_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL DEFAULT '' COMMENT '商品名称',
    product_img VARCHAR(255) DEFAULT '' COMMENT '商品图片',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '下单单价',
    num INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    subtotal_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ==============================
-- 【五、任务打款模块 4张表】
-- ==============================

-- 25. 任务配置表
CREATE TABLE task_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_title VARCHAR(200) NOT NULL DEFAULT '' COMMENT '任务标题',
    task_cover VARCHAR(255) DEFAULT '' COMMENT '任务封面图',
    task_intro VARCHAR(500) DEFAULT '' COMMENT '任务简介',
    task_content TEXT COMMENT '任务图文步骤详情',
    reward_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单任务奖励金额',
    total_num INT NOT NULL DEFAULT 0 COMMENT '任务总数量',
    day_num INT NOT NULL DEFAULT 0 COMMENT '每日限领数量',
    expire_minute INT NOT NULL DEFAULT 10080 COMMENT '任务有效时长(分钟)',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0下架 1上架',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务配置表';

-- 26. 用户任务领取表
CREATE TABLE task_user_receive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    openid VARCHAR(100) DEFAULT '' COMMENT '用户微信openid',
    reward_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '本次任务奖励金额',
    audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '审核状态：1待提交 2待审核 3已通过 4已驳回 5已过期',
    grant_pay TINYINT NOT NULL DEFAULT 0 COMMENT '是否授权打款：0未授权 1已授权',
    withdraw_status TINYINT NOT NULL DEFAULT 0 COMMENT '提现状态：0未提现 1处理中 2已成功 3已失败',
    expire_time DATETIME NULL COMMENT '任务过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_task (user_id, task_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_user_create (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户任务领取记录表';

-- 27. 任务提交工单表
CREATE TABLE task_submit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    receive_id BIGINT NOT NULL COMMENT '任务领取记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    submit_images VARCHAR(1000) DEFAULT '' COMMENT '提交截图，逗号分隔',
    submit_note VARCHAR(500) DEFAULT '' COMMENT '提交备注',
    audit_note VARCHAR(500) DEFAULT '' COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    audit_time DATETIME NULL COMMENT '审核时间',
    INDEX idx_receive_id (receive_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务提交工单表';

-- 28. 任务打款日志表
CREATE TABLE task_pay_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    receive_id BIGINT NOT NULL COMMENT '任务领取记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_title VARCHAR(200) DEFAULT '' COMMENT '任务标题',
    trade_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT '本地交易单号',
    wechat_pay_no VARCHAR(64) DEFAULT '' COMMENT '微信支付单号',
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '打款金额',
    pay_status TINYINT NOT NULL DEFAULT 1 COMMENT '打款状态：1处理中 2成功 3失败',
    fail_reason VARCHAR(500) DEFAULT '' COMMENT '失败原因',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    pay_time DATETIME NULL COMMENT '打款完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_trade_no (trade_no),
    INDEX idx_user_id (user_id),
    INDEX idx_pay_status (pay_status),
    INDEX idx_receive_id (receive_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务打款日志表';

-- ==============================
-- 【六、数据统计模块 1张表】
-- ==============================

-- 29. 每日数据统计表
CREATE TABLE data_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    stat_date VARCHAR(20) NOT NULL DEFAULT '' COMMENT '统计日期',
    register_num INT NOT NULL DEFAULT 0 COMMENT '当日注册人数',
    active_num INT NOT NULL DEFAULT 0 COMMENT '当日活跃人数',
    order_num INT NOT NULL DEFAULT 0 COMMENT '当日订单数',
    order_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '当日交易总额',
    task_num INT NOT NULL DEFAULT 0 COMMENT '当日完成任务数',
    task_pay_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '当日任务打款总额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日数据统计表';

-- ==============================
-- 初始化基础数据
-- ==============================

-- 1. 系统字典
INSERT INTO sys_dict (dict_code, dict_name, dict_type, status) VALUES
('status_type','状态类型','system',1),
('yes_no','是/否','system',1),
('task_audit_status','任务审核状态','task',1),
('pay_status','打款状态','task',1),
('order_status','订单状态','trade',1),
('point_source','积分来源','marketing',1);

-- 2. 系统配置
INSERT INTO sys_config (config_key, config_name, config_value, remark, status) VALUES
('site_name','网站名称','NEOP商用系统','gongziyu.com旗下系统',1),
('site_domain','官网域名','neop.gongziyu.com','',1),
('wechat_mch_id','微信商户号','','',1),
('wechat_api_key','微信API密钥','','',1),
('upload_type','上传方式','local','local/cos/oss',1),
('task_expire_time','任务默认过期天数','7','',1),
('invite_reward_point','邀请奖励积分','10','邀请一人奖励积分',1),
('sign_reward_point','签到奖励积分','10','每日签到奖励积分',1),
('order_auto_cancel_minute','订单自动取消时间','15','未支付订单自动取消时间(分钟)',1);

-- 3. 角色
INSERT INTO sys_role (role_name, role_key, sort, status) VALUES
('超级管理员','super_admin',1,1);

-- 4. 管理员 admin / admin123 (BCrypt加密)
INSERT INTO sys_admin (username, password, nickname, status) VALUES
('admin','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH','超级管理员',1);

-- 5. 绑定角色
INSERT INTO sys_admin_role (admin_id, role_id) VALUES (1,1);

-- 6. 菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_type, menu_icon, menu_path, component, permission, sort, status) VALUES
(0,'系统管理','menu','system','/system','',NULL,1,1),
(1,'管理员管理','menu','user','/system/admin','system/admin/index','sys:admin:list',1,1),
(1,'角色管理','menu','role','/system/role','system/role/index','sys:role:list',2,1),
(1,'菜单管理','menu','menu','/system/menu','system/menu/index','sys:menu:list',3,1),
(1,'字典管理','menu','dict','/system/dict','system/dict/index','sys:dict:list',4,1),
(1,'系统配置','menu','config','/system/config','system/config/index','sys:config:list',5,1),
(1,'操作日志','menu','log','/system/log','system/log/index','sys:log:list',6,1),
(0,'用户管理','menu','user','/user','',NULL,2,1),
(8,'用户列表','menu','list','/user/list','user/list/index','user:list',1,1),
(0,'营销管理','menu','market','/market','',NULL,3,1),
(10,'会员套餐','menu','vip','/market/package','market/package/index','market:package:list',1,1),
(10,'积分记录','menu','point','/market/pointlog','market/pointlog/index','market:point:list',2,1),
(10,'邀请记录','menu','invite','/market/invite','market/invite/index','market:invite:list',3,1),
(0,'电商管理','menu','shop','/shop','',NULL,4,1),
(14,'商品分类','menu','category','/shop/category','shop/category/index','shop:category:list',1,1),
(14,'商品管理','menu','goods','/shop/goods','shop/goods/index','shop:goods:list',2,1),
(14,'订单管理','menu','order','/shop/order','shop/order/index','shop:order:list',3,1),
(0,'任务管理','menu','task','/task','',NULL,5,1),
(18,'任务配置','menu','task-set','/task/set','task/set/index','task:info:list',1,1),
(18,'任务审核','menu','task-audit','/task/audit','task/audit/index','task:audit:list',2,1),
(18,'打款日志','menu','task-paylog','/task/paylog','task/paylog/index','task:paylog:list',3,1),
(0,'数据统计','menu','data','/data','',NULL,6,1),
(22,'数据看板','menu','dashboard','/data/dashboard','data/dashboard/index','stat:dashboard',1,1);

-- 7. 授权菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
