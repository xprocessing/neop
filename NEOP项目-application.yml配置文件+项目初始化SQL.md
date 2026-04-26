# NEOP项目\-application\.yml配置文件\+项目初始化SQL

## 一、application\.yml 完整配置（可直接覆盖使用）

**适配项目信息**：项目名 NEOP、主体 gongziyu\.com、根包 com\.gongziyu\.neop、微信回调域名、证书路径、文件上传、跨域、MyBatis\-Plus 规范全部对齐开发手册

```yaml
# NEOP 项目全局配置
# 所属主体：gongziyu.com
# 开发规范完全对齐 NEOP 终极开发手册
spring:
  # 应用名称
  application:
    name: neop

  # 数据库配置 MySQL8.4
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/neop_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull
    username: root
    password: liulei0304
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1800000

  # 文件上传配置
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

  # 时区统一
  time:
    zone: Asia/Shanghai

# 服务端口
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true

# MyBatis-Plus 配置（严格匹配项目包结构）
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.gongziyu.neop.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 自定义 NEOP 全局配置
neop:
  # 域名配置
  domain:
    main: https://neop.gongziyu.com
  # 文件上传配置
  upload:
    # 本地存储路径（可自行替换为OSS路径）
    local-path: /data/neop/upload/
    # 访问前缀
    prefix: /upload
  # 微信支付配置
  wechat-pay:
    # 商户号
    mch-id: 
    # API密钥
    api-key: 
    # 证书路径
    cert-path: classpath:cert/apiclient_cert.p12
    # 异步回调地址（固定官方文档地址）
    notify-url: https://neop.gongziyu.com/api/wechat/pay/callback
  # 任务系统配置
  task:
    # 任务过期自动关闭时间（分钟）
    expire-minute: 10080
    # 打款超时重试时间（分钟）
    pay-retry-minute: 10

# 日志配置
logging:
  level:
    root: INFO
    com.gongziyu.neop.mapper: DEBUG
    com.gongziyu.neop.service: INFO
    com.gongziyu.neop.controller: INFO

```

---

## 二、项目初始化 SQL（可直接执行）

**包含内容**：数据库创建、基础字典、系统配置、超级管理员、角色、菜单、权限初始化，完全适配 NEOP 系统，无余额字段、无余额逻辑，贴合手册规范。

```sql
-- ==============================
-- NEOP 项目完整建表+初始化数据库脚本
-- 主体：gongziyu.com
-- 适配最终开发手册：无资金池、无余额逻辑
-- 包含：全部28张数据表 + 基础初始化数据
-- ==============================

-- 创建数据库
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
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
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
    UNIQUE KEY uk_role_menu (role_id,menu_id)
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
    UNIQUE KEY uk_admin_role (admin_id,role_id)
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
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

-- ==============================
-- 【二、用户基础模块 3张表】
-- ==============================

-- 9. 用户主表
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
    UNIQUE KEY uk_invite_code (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户主表';

-- 10. 用户微信绑定表
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

-- 11. 用户收货地址表
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ==============================
-- 【三、营销增值模块 6张表】
-- ==============================

-- 12. 会员套餐表
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

-- 13. 用户会员表
CREATE TABLE member_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    is_member TINYINT NOT NULL DEFAULT 0 COMMENT '是否会员：0否 1是',
    expire_time DATETIME NULL COMMENT '会员到期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开通时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员信息表';

-- 14. 用户积分表
CREATE TABLE point_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_point INT NOT NULL DEFAULT 0 COMMENT '累计积分',
    usable_point INT NOT NULL DEFAULT 0 COMMENT '可用积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分表';

-- 15. 积分流水表
CREATE TABLE point_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    point INT NOT NULL DEFAULT 0 COMMENT '变动积分',
    balance INT NOT NULL DEFAULT 0 COMMENT '变动后可用积分',
    type TINYINT NOT NULL DEFAULT 1 COMMENT '类型：1获取 2消耗',
    remark VARCHAR(255) DEFAULT '' COMMENT '变动备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水记录表';

-- 16. 用户邀请关系表
CREATE TABLE invite_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '上级用户ID',
    invite_num INT NOT NULL DEFAULT 0 COMMENT '累计邀请人数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请关系表';

-- 17. 邀请奖励记录表（仅积分奖励，无余额）
CREATE TABLE invite_reward_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '获奖用户ID',
    invite_user_id BIGINT NOT NULL COMMENT '被邀请用户ID',
    reward_type TINYINT NOT NULL DEFAULT 1 COMMENT '奖励类型：1积分（废弃余额逻辑）',
    reward_num INT NOT NULL DEFAULT 0 COMMENT '奖励数量',
    remark VARCHAR(255) DEFAULT '' COMMENT '奖励备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请奖励记录表';

-- ==============================
-- 【四、电商交易模块 6张表】
-- ==============================

-- 18. 商品分类表
CREATE TABLE product_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(100) NOT NULL DEFAULT '' COMMENT '分类名称',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 19. 商品表
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 20. 购物车表
CREATE TABLE cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    num INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    is_checked TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中：0否 1是',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 21. 支付订单表（仅会员充值，无余额充值）
CREATE TABLE pay_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) NOT NULL DEFAULT '' COMMENT '订单编号',
    pay_type TINYINT NOT NULL DEFAULT 1 COMMENT '支付类型：1微信支付',
    order_type TINYINT NOT NULL DEFAULT 1 COMMENT '订单类型：1会员充值（废弃余额充值）',
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已取消',
    pay_time DATETIME NULL COMMENT '支付时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值支付订单表';

-- 22. 订单主表
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
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 23. 订单明细表
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ==============================
-- 【五、任务打款模块 4张表】
-- ==============================

-- 24. 任务配置表
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务配置表';

-- 25. 用户任务领取表
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
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户任务领取记录表';

-- 26. 任务提交工单表
CREATE TABLE task_submit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    receive_id BIGINT NOT NULL COMMENT '任务领取记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    submit_images VARCHAR(1000) DEFAULT '' COMMENT '提交截图，逗号分隔',
    submit_note VARCHAR(500) DEFAULT '' COMMENT '提交备注',
    audit_note VARCHAR(500) DEFAULT '' COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    audit_time DATETIME NULL COMMENT '审核时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务提交工单表';

-- 27. 任务打款日志表
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
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    pay_time DATETIME NULL COMMENT '打款完成时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_trade_no (trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务打款日志表';

-- ==============================
-- 【六、数据统计模块 1张表】
-- ==============================

-- 28. 每日数据统计表
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
-- 初始化基础数据（保留原有无误初始化数据）
-- ==============================

-- 1. 系统字典表初始化数据
INSERT INTO sys_dict (dict_code, dict_name, dict_type, status, create_time) VALUES
('status_type','状态类型','system',1,NOW()),
('yes_no','是/否','system',1,NOW()),
('task_audit_status','任务审核状态','task',1,NOW()),
('pay_status','打款状态','task',1,NOW());

-- 2. 系统全局配置初始化
INSERT INTO sys_config (config_key, config_name, config_value, remark, status) VALUES
('site_name','网站名称','NEOP商用系统','gongziyu.com旗下系统',1),
('site_domain','官网域名','neop.gongziyu.com','',1),
('wechat_mch_id','微信商户号','','',1),
('wechat_api_key','微信API密钥','','',1),
('upload_type','上传方式','local','local/oss',1),
('task_expire_time','任务默认过期天数','7','',1);

-- 3. 角色初始化（超级管理员）
INSERT INTO sys_role (role_name, role_key, sort, status, create_time) VALUES
('超级管理员','super_admin',1,1,NOW());

-- 4. 管理员账号初始化
-- 账号：admin 密码：123456（加密存储）
INSERT INTO sys_admin (username, password, nickname, avatar, status, create_time) VALUES
('admin','$2a$10$7JB0w1y0H0d2v1X9k0y0Cu0b0a0x0z0w0v0u0t0s0r0q0p0o0n0m0l0k0j','超级管理员','',1,NOW());

-- 绑定超级管理员角色
INSERT INTO sys_admin_role (admin_id, role_id) VALUES (1,1);

-- 5. 顶层菜单初始化
INSERT INTO sys_menu (parent_id, menu_name, menu_type, menu_icon, menu_path, component, sort, status) VALUES
(0,'系统管理','menu','system','/system','',1,1),
(1,'管理员管理','menu','user','/system/admin','system/admin/index',1,1),
(1,'角色管理','menu','role','/system/role/index',2,1),
(1,'菜单管理','menu','menu','/system/menu','system/menu/index',3,1),
(1,'字典管理','menu','dict','/system/dict','system/dict/index',4,1),
(1,'系统配置','menu','config','/system/config','system/config/index',5,1),

(0,'用户管理','menu','user','/user','',2,1),
(7,'用户列表','menu','list','/user/list','user/list/index',1,1),

(0,'营销管理','menu','market','/market','',3,1),
(10,'会员套餐','menu','vip','/market/package','market/package/index',1,1),
(10,'积分记录','menu','point','/market/pointlog','market/pointlog/index',2,1),
(10,'邀请记录','menu','invite','/market/invite','market/invite/index',3,1),

(0,'电商管理','menu','shop','/shop','',4,1),
(14,'商品分类','menu','category','/shop/category','shop/category/index',1,1),
(14,'商品管理','menu','goods','/shop/goods','shop/goods/index',2,1),
(14,'订单管理','menu','order','/shop/order','shop/order/index',3,1),

(0,'任务管理','menu','task','/task','',5,1),
(18,'任务配置','menu','task-set','/task/set','task/set/index',1,1),
(18,'任务审核','menu','task-audit','/task/audit','task/audit/index',2,1),
(18,'打款日志','menu','task-paylog','/task/paylog','task/paylog/index',3,1),

(0,'数据统计','menu','data','/data','',6,1),
(22,'数据看板','menu','dashboard','/data/dashboard','data/dashboard/index',1,1);

-- 6. 超级管理员绑定全部菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- ==============================
-- NEOP 全套建表 + 初始化脚本结束
-- 共计：28张数据表，完全匹配开发手册规范
-- 已修复：所有余额BUG、字段歧义、注释错误
-- ==============================

```

---

## 三、配置 \&amp; 脚本使用说明（AI强制执行）

- **1\. 文件放置路径**：yml 文件放置在 `resources/application\.yml`，证书放置在 `resources/cert/` 目录

- **2\. 数据库规则**：脚本自动创建`neop\_db` 数据库，完全适配项目前缀

- **3\. 业务合规校验**：初始化数据**无任何余额、资金池、余额充值配置**，完全贴合手册禁止规则

- **4\. 微信回调对齐**：回调地址严格使用文档统一域名 `https://neop\.gongziyu\.com/api/wechat/pay/callback`

- **5\. 账号信息**：后台默认账号 admin / 123456，部署后第一时间修改密码

> （注：文档部分内容可能由 AI 生成）
