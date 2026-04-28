# NEOP项目 - 全模块开发文档（最终商用完整版）

本文档为**gongziyu.com旗下NEOP单商户全功能商用系统**终极开发手册，包含**所有业务模块需求、数据库设计、字段关系、接口规范、状态流转、AI分步开发计划、强制编码规则、测试校验标准、前后端完整目录、完整28张表SQL脚本**。无需二次解读，可直接交付AI全自动逐模块开发、编码、联调、落地，全程无余额资金池，采用**任务单独审核+后台授权打款+用户单笔微信提现**核心模式，适配所有商用场景。

---

## 一、项目基础信息

- **项目名称**：NEOP
- **所属主体**：gongziyu.com
- **项目定位**：商用任务营销+电商会员一体化系统
- **后端**：Java 21 + SpringBoot + MyBatis-Plus
- **数据库**：MySQL 8.4
- **缓存**：Redis 7（必选，用于Token黑名单、幂等校验、计数限流、热数据缓存）
- **前端**：Vue3+PC后台 + UniApp多端（H5/小程序/APP）
- **服务环境**：Nginx 1.28、Node.js 24、Python3
- **支付能力**：微信企业付款到零钱（单笔任务独立打款）

### 1.1 系统架构图 & 部署拓扑

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端                                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                  │
│  │ 微信小程序 │  │  H5浏览器  │  │  APP端   │                  │
│  └─────┬────┘  └─────┬────┘  └─────┬────┘                  │
└────────┼─────────────┼─────────────┼────────────────────────┘
         │             │             │
         ▼             ▼             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Nginx 1.28 (反向代理)                      │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  HTTPS终止 + 负载均衡 + 静态资源托管                      │  │
│  │  /api/*      → 后端SpringBoot :8080                    │  │
│  │  /admin/*    → Vue3后台管理 :3000                       │  │
│  │  /           → UniApp H5 :8081                         │  │
│  └───────────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
│ SpringBoot   │  │   MySQL 8.4  │  │   Redis 7        │
│ :8080        │  │   :3306      │  │   :6379          │
│              │  │              │  │                  │
│ - REST API   │──▶ neop_db     │  │ - Token黑名单    │
│ - 微信支付    │  │ - 28张表     │  │ - 幂等校验       │
│ - 定时任务    │  │              │  │ - 计数限流       │
│ - 文件上传    │  └──────────────┘  │ - 热数据缓存     │
└──────┬───────┘                    └──────────────────┘
       │
       ▼
┌──────────────────┐
│  微信支付API      │
│  - 统一下单       │
│  - 企业付款到零钱  │
│  - 支付回调通知    │
└──────────────────┘
```

### 1.2 端口规划 & Nginx 代理规则

| 服务 | 端口 | 说明 |
|------|------|------|
| Nginx | 80/443 | 对外统一入口，HTTPS终止 |
| SpringBoot | 8080 | 后端API服务，仅Nginx内网访问 |
| Vue3 Admin | 3000 | 后台管理前端dev模式，生产由Nginx托管dist |
| UniApp H5 | 8081 | 移动端H5 dev模式，生产由Nginx托管dist |
| MySQL | 3306 | 数据库，仅内网访问 |
| Redis | 6379 | 缓存服务，仅内网访问，必须设置密码 |

**Nginx 关键代理配置参考**：

```nginx
server {
    listen 443 ssl;
    server_name neop.gongziyu.com;

    ssl_certificate     /etc/nginx/cert/neop.pem;
    ssl_certificate_key /etc/nginx/cert/neop.key;

    # 后端API代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 20m;
    }

    # 后台管理前端
    location /admin/ {
        alias /var/www/neop-admin-web/dist/;
        try_files $uri $uri/ /admin/index.html;
    }

    # 移动端H5
    location / {
        alias /var/www/neop-uniapp/dist/build/h5/;
        try_files $uri $uri/ /index.html;
    }
}
```

### 1.3 HTTPS 证书配置说明

- 证书类型：使用宝塔面板提供的免费证书（自动申请Let's Encrypt证书）
- 证书路径：宝塔面板默认路径 `/www/server/nginx/conf/cert/` 目录
- 自动续期：宝塔面板自动续签，无需额外配置
- 微信支付回调必须使用 HTTPS
- 宝塔配置说明：在宝塔面板→网站→设置→SSL中申请证书并开启HTTPS强制跳转

---

## 二、全局通用强制规则（AI 100%严格执行）

### 2.1 业务核心约束

- 全程**禁止余额钱包、禁止资金池、禁止余额流转**，所有现金奖励绑定单个任务工单，独立结算、独立提现、独立打款日志
- 所有用户数据严格隔离，用户仅可查看自身数据，后台拥有全部权限
- 所有状态流转**禁止跨状态更新**，必须按既定流程执行
- 所有接口必须做登录拦截、权限校验、参数非空校验
- 所有图片上传统一复用系统OSS/本地上传工具类，多图采用英文逗号分隔存储

### 2.2 通用状态规范

- 通用启用禁用：0=禁用、1=启用
- 通用删除逻辑：全部采用**逻辑删除**，不物理删除数据
- 时间字段：统一使用datetime，默认CURRENT_TIMESTAMP，更新字段自动更新
- 主键规则：所有表主键统一bigint自增，关联字段统一对应主键类型
- 金额字段：统一使用 DECIMAL(10,2)，禁止使用 FLOAT/DOUBLE，Java 对应 BigDecimal
- 字符集：所有表统一 utf8mb4，支持 emoji 存储

### 2.3 代码开发规范 & 项目包结构

- **后端根包**：com.gongziyu.neop
- com.gongziyu.neop.controller —— 控制器接口层
- com.gongziyu.neop.service —— 业务逻辑层
- com.gongziyu.neop.service.impl —— 业务实现层
- com.gongziyu.neop.mapper —— 数据库映射层
- com.gongziyu.neop.entity —— 数据库实体
- com.gongziyu.neop.config —— 全局配置类
- com.gongziyu.neop.util —— 工具类（微信、加密、通用）
- com.gongziyu.neop.task —— 定时任务
- com.gongziyu.neop.exception —— 全局异常处理
- com.gongziyu.neop.common —— 公共返回、常量、分页
- com.gongziyu.neop.annotation —— 自定义注解（限流、操作日志等）

### 2.4 全局统一返回 Result 结构

**普通接口**

```JSON
{ "code": 200, "msg": "success", "data": {} }
```

**分页接口**

```JSON
{
  "code": 200,
  "msg": "success",
  "data": {
    "total": 120,
    "pages": 12,
    "current": 1,
    "size": 10,
    "records": []
  }
}
```

**错误返回**

```JSON
{ "code": 500, "msg": "错误提示", "data": null }
```

### 2.5 核心业务编码规则

#### 2.5.1 用户邀请码生成规则

- 格式：**8位大写字母+数字**，排除易混淆字符（0/O、1/I/L）
- 可用字符集：`ABCDEFGHJKMNPQRSTVWXYZ23456789`
- 生成算法：`RandomStringUtils.random(8, CHARSET)` + 数据库 UNIQUE KEY 保证唯一
- 生成时机：用户首次注册时自动生成，存入 `user_info.invite_code`

#### 2.5.2 订单号 / 交易号生成规则

| 编号类型 | 格式 | 示例 | 说明 |
|----------|------|------|------|
| 电商订单号 | `NO` + yyyyMMddHHmmss + 6位随机数 | NO20260426183000123456 | 20位，含日期可排序 |
| 会员充值订单号 | `MB` + yyyyMMddHHmmss + 6位随机数 | MB20260426183000123456 | 20位，与电商订单区分前缀 |
| 任务交易单号 | `TP` + yyyyMMddHHmmss + 6位随机数 | TP20260426183000123456 | 20位，任务打款专用 |

- 随机数使用 `ThreadLocalRandom` 生成，确保并发安全
- 数据库 UNIQUE KEY 保证唯一，冲突时重新生成

#### 2.5.3 会员时长叠加规则

| 当前状态 | 叠加逻辑 | 示例 |
|----------|----------|------|
| 非会员 | 新到期时间 = 当前时间 + 套餐天数 | 4月26日购买30天套餐 → 到期5月26日 |
| 会员未过期 | 新到期时间 = 原到期时间 + 套餐天数 | 原到期5月10日，续费30天 → 到期6月9日 |
| 会员已过期 | 新到期时间 = 当前时间 + 套餐天数 | 原到期4月20日(已过)，续费30天 → 到期5月26日 |

- 更新 `member_user.is_member = 1`，更新 `expire_time`
- 如果 `member_user` 记录不存在则新建

#### 2.5.4 任务剩余可领取数量计算规则

- **总剩余数量** = `task_info.total_num` - COUNT(`task_user_receive` 中未过期且未删除的记录)
- **每日剩余数量** = `task_info.day_num` - COUNT(`task_user_receive` 中当日创建且未删除的记录)
- 已过期的不消耗总名额（过期自动释放）
- 已驳回的可重新提交，仍占用名额（未释放）
- 每日计数以 `create_time` 的日期为准，不区分审核状态

---

## 三、前后端完整项目目录（AI必须按此创建）

### 3.1 后端（SpringBoot）

```Plaintext
neop-backend/
├── pom.xml
├── src/main/java/com/gongziyu/neop/
│   ├── NeopApplication.java
│   ├── controller/       # 接口层
│   │   ├── sys/          # 系统管理接口
│   │   ├── user/         # 用户相关接口
│   │   ├── marketing/    # 营销模块接口
│   │   ├── trade/        # 电商交易接口
│   │   ├── task/         # 任务模块接口
│   │   ├── stat/         # 统计模块接口
│   │   └── wechat/       # 微信相关接口
│   ├── service/          # 业务接口
│   ├── service/impl/     # 业务实现
│   ├── mapper/           # 数据库映射
│   ├── entity/           # 数据表实体
│   ├── dto/              # 请求/响应数据传输对象
│   ├── config/           # 全局配置
│   ├── util/             # 工具类
│   ├── task/             # 定时任务
│   ├── exception/        # 异常处理
│   ├── common/           # 统一返回、分页
│   └── annotation/       # 自定义注解
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   ├── cert/             # 微信证书
│   ├── mapper/           # MyBatis XML映射
│   └── db/migration/     # Flyway增量SQL脚本
```

### 3.2 后台管理（Vue3 + Vite）

```Plaintext
neop-admin-web/
├── index.html
├── vite.config.js
├── src/
│   ├── main.js
│   ├── router/        # 路由
│   ├── store/         # Pinia
│   ├── api/           # 接口请求
│   ├── views/         # 页面
│   │   ├── sys/
│   │   ├── user/
│   │   ├── marketing/
│   │   ├── trade/
│   │   ├── task/
│   │   └── stat/
│   ├── components/    # 公共组件
│   ├── utils/         # 工具
│   └── assets/        # 静态资源
```

### 3.3 移动端（UniApp）

```Plaintext
neop-uniapp/
├── App.vue
├── main.js
├── pages.json
├── manifest.json
├── pages/
│   ├── index/       # 任务广场
│   ├── user/        # 个人中心
│   ├── task/        # 任务相关
│   ├── trade/       # 电商
│   └── common/      # 公共页面
├── components/
├── utils/
└── static/
```

---

## 四、后端核心配置模板（application.yml）

### 4.1 application.yml 主配置

```yaml
server:
  port: 8080
  servlet:
    context-path: /

spring:
  application:
    name: neop
  profiles:
    active: dev
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/neop_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: your_db_password
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      password: your_redis_password
      database: 0
      timeout: 5000
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 3000
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: Asia/Shanghai

mybatis-plus:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.gongziyu.neop.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto

logging:
  level:
    com.gongziyu.neop: debug
    com.gongziyu.neop.mapper: debug
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"
```

### 4.2 application-dev.yml 开发环境

```yaml
neop:
  jwt:
    secret: neop-dev-jwt-secret-key-must-be-at-least-32-characters
    mobile-expire: 604800000    # 7天（毫秒）
    admin-expire: 7200000       # 2小时（毫秒）
  upload:
    type: cos                   # cos / oss / local
    # 腾讯云COS配置（生产环境推荐）
    cos-secret-id: your_secret_id
    cos-secret-key: your_secret_key
    cos-region: ap-shanghai
    cos-bucket: neop-prod-1234567890
    cos-domain: https://cdn.gongziyu.com
    # 阿里云OSS配置（备选）
    # oss-endpoint: https://oss-cn-hangzhou.aliyuncs.com
    # oss-access-key-id: your_access_key_id
    # oss-access-key-secret: your_access_key_secret
    # oss-bucket-name: neop-prod
    # oss-domain: https://cdn.gongziyu.com
  wechat:
    mp-appid: your_mp_appid
    mp-secret: your_mp_secret
    mch-id: your_mch_id
    api-key: your_api_key
    cert-path: classpath:cert/apiclient_cert.p12
```

### 4.3 application-prod.yml 生产环境

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql.internal:3306/neop_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai&useSSL=true
    username: neop_prod
    password: ${DB_PASSWORD}
  data:
    redis:
      host: redis.internal
      password: ${REDIS_PASSWORD}

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

logging:
  level:
    com.gongziyu.neop: info

neop:
  jwt:
    secret: ${JWT_SECRET}
  upload:
    type: cos                   # cos / oss / local
    # 腾讯云COS配置
    cos-secret-id: ${COS_SECRET_ID}
    cos-secret-key: ${COS_SECRET_KEY}
    cos-region: ap-shanghai
    cos-bucket: neop-prod
    cos-domain: https://cdn.gongziyu.com
    # 阿里云OSS配置（备选）
    # oss-endpoint: https://oss-cn-hangzhou.aliyuncs.com
    # oss-access-key-id: ${OSS_AK}
    # oss-access-key-secret: ${OSS_SK}
    # oss-bucket-name: neop-prod
    # oss-domain: https://cdn.gongziyu.com
  wechat:
    mp-appid: ${WX_MP_APPID}
    mp-secret: ${WX_MP_SECRET}
    mch-id: ${WX_MCH_ID}
    api-key: ${WX_API_KEY}
    cert-path: /data/neop/cert/apiclient_cert.p12
```

---

## 五、Redis 缓存规划

### 5.1 缓存 Key 命名规范

统一前缀 `neop:`，格式：`neop:模块:业务:标识`

| Key 模式 | 类型 | 过期时间 | 用途 |
|----------|------|----------|------|
| `neop:token:blacklist:{userId}` | String | 与Token剩余有效期一致 | 用户登出/踢人时Token黑名单 |
| `neop:wechat:callback:idempotent:{orderNo}` | String | 24h | 微信支付回调幂等校验 |
| `neop:task:receive:count:userId:{taskId}:{date}` | String | 至当日23:59:59 | 用户当日任务领取次数计数 |
| `neop:task:stock:{taskId}` | String | 无过期 | 任务剩余库存（高并发扣减用） |
| `neop:sign:userId:{date}` | String | 至当日23:59:59 | 每日签到防重复标记 |
| `neop:rate:limit:{api}:{userId}` | String | 按限流窗口 | 接口限流计数 |
| `neop:dict:cache:{dictCode}` | Hash | 24h | 字典数据缓存 |
| `neop:config:cache:{configKey}` | String | 24h | 系统配置缓存 |
| `neop:product:stock:{productId}` | String | 无过期 | 商品库存缓存（下单扣减） |

### 5.2 缓存使用规则

- **缓存穿透防护**：查询结果为空时缓存空值，过期时间设5分钟
- **缓存更新策略**：后台修改数据时主动删除缓存，下次查询时重建
- **分布式锁**：使用 Redisson 或 `SET NX EX` 实现分布式锁，用于库存扣减等并发场景
- **序列化**：统一使用 JSON 序列化，便于调试和跨语言兼容

---

## 六、全项目模块清单（共6大模块28张数据表）

1. **系统权限模块**（8张）：菜单、角色、角色菜单、管理员、管理员角色、字典、字典数据、系统配置
2. **用户基础模块**（3张）：用户主表、微信绑定、用户收货地址
3. **营销增值模块**（6张）：会员套餐、用户会员、用户积分、积分流水、邀请关系、邀请奖励
4. **电商交易模块**（6张）：支付订单、商品分类、商品、购物车、订单主表、订单明细
5. **任务打款模块**（4张）：任务配置、用户任务领取、任务提交工单、任务打款日志
6. **数据统计模块**（1张）：每日数据统计表

---

## 七、完整数据库SQL（28张表 + 索引 + 初始化数据）

```SQL
-- ==============================
-- NEOP 项目完整建表+初始化数据库脚本
-- 主体：gongziyu.com
-- 适配最终开发手册：无资金池、无余额逻辑
-- 包含：全部28张数据表 + 业务索引 + 基础初始化数据
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
('upload_type','上传方式','local','local/oss',1),
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
```

---

## 八、接口设计详细规范

### 8.1 通用前置规范（所有接口统一遵循）

#### 8.1.1 统一请求约束

- **身份校验**：所有前台接口需校验用户登录Token，所有后台接口需校验管理员Token+权限
- **参数校验**：所有必填参数非空校验、数值类型范围校验、字符串长度校验
- **分页统一参数**：所有分页接口固定传参 current(当前页,默认1)、size(每页条数,默认10)
- **排序规则**：默认创建时间倒序，支持自定义sort字段排序

#### 8.1.2 全局统一错误码规范

| 错误码 | 错误信息 | 适用场景 |
|--------|----------|----------|
| 401 | 未登录或Token失效 | 未携带Token、Token过期、Token非法 |
| 403 | 权限不足，禁止访问 | 管理员无对应菜单/操作权限 |
| 400 | 请求参数非法 | 参数缺失、格式错误、数值越界 |
| 429 | 请求过于频繁，请稍后再试 | 触发接口限流 |
| 500 | 服务器内部异常 | 数据库异常、第三方接口调用失败、业务逻辑异常 |
| 1001 | 任务不存在或已下架 | 查询/操作已删除、下架的任务 |
| 1002 | 今日任务领取次数已达上限 | 触发任务每日限领规则 |
| 1003 | 您已领取过该任务，请勿重复领取 | 同一用户重复领取同一未过期任务 |
| 1004 | 任务已过期，无法操作 | 操作状态为已过期的任务工单 |
| 1005 | 当前任务状态不支持提交 | 非待提交/已驳回状态的任务提交材料 |
| 1006 | 任务未审核通过，无法申请提现 | 提现前置校验失败 |
| 1007 | 该任务已完成打款，请勿重复操作 | 重复申请已打款任务提现 |
| 2001 | 商品不存在或已下架 | 操作无效商品数据 |
| 2002 | 商品库存不足 | 下单时库存扣减失败 |
| 3001 | 会员套餐不存在或已禁用 | 充值无效会员套餐 |
| 3002 | 今日已完成签到，请勿重复签到 | 用户重复执行每日签到 |
| 4001 | 邀请码无效或不存在 | 新用户绑定无效邀请关系 |
| 5001 | 订单不存在或状态异常 | 操作已关闭/已取消/不存在订单 |
| 5002 | 订单已支付，无需重复支付 | 重复发起订单支付 |
| 6001 | 收货地址不存在 | 操作无效地址 |

#### 8.1.3 接口限流 & 防刷规则

| 接口 | 限流策略 | 实现方式 |
|------|----------|----------|
| POST /api/task/receive | 同一用户每秒最多2次 | Redis滑动窗口 |
| POST /api/task/submit | 同一用户每秒最多2次 | Redis滑动窗口 |
| POST /api/task/withdraw/apply | 同一用户每分钟最多3次 | Redis滑动窗口 |
| POST /api/user/point/sign | 同一用户每日1次 | Redis SET NX |
| POST /api/shop/order/create | 同一用户每秒最多1次 | Redis滑动窗口 |
| POST /api/admin/login | 同一IP每分钟最多5次 | Redis滑动窗口 |
| GET /api/task/list | 同一IP每秒最多10次 | Redis滑动窗口 |

- 超出限流返回 429 状态码
- 后台注解实现：自定义 `@RateLimit(key, limit, period)` 注解 + AOP 拦截

#### 8.1.4 敏感数据脱敏规则

| 字段 | 脱敏规则 | 示例 |
|------|----------|------|
| 手机号 | 中间4位用*替代 | 138****1234 |
| 身份证 | 中间8位用*替代 | 310***********1234 |
| 管理员密码 | 接口返回时永远不输出 | 返回字段中移除password |
| 微信openid | 日志中脱敏，只显示前6后4 | o6kMp1****abcd |
| 收货地址 | 后台列表中间3位用*替代 | 浦东***路100号 |

- 实现方式：Jackson 自定义序列化器 `@Sensitive(type = SensitiveType.PHONE)`

#### 8.1.5 核心业务规则

##### 任务状态机完整规则

状态枚举：1=待提交、2=待审核、3=已通过、4=已驳回、5=已过期

```
领取任务 ──→ [1待提交] ──提交──→ [2待审核] ──通过──→ [3已通过] ──提现──→ 打款
                                │                    │
                                └──驳回──→ [4已驳回] ──重提──→ [2待审核]
                                
任何未完成状态 ──过期──→ [5已过期] (定时任务扫描)
```

- 过期规则：领取后到达expire_time自动变更为5已过期，定时任务每30分钟扫描一次
- 重提规则：任务驳回（4）后，支持用户重新提交材料，状态回改为2待审核
- 过期处理：任务过期后，不可提交、不可审核、不可提现，自动释放任务剩余可领取名额

##### 订单状态机完整规则

状态枚举：0=待付款、1=待发货、2=待收货、3=已完成、4=已取消

```
下单 ──→ [0待付款] ──支付──→ [1待发货] ──发货──→ [2待收货] ──确认收货──→ [3已完成]
    │                          │
    └──超时/取消──→ [4已取消]   └──退款──→ [4已取消]
```

- 超时取消：15分钟未支付自动取消（定时任务）
- 发货：后台操作，记录发货时间
- 确认收货：用户操作或发货7天后自动确认
- 退款：暂不实现线上退款流程，后台可手动标记取消并备注原因

##### 提现三校验硬性规则（全局唯一）

任务提现必须同时满足三个条件，缺一不可：

1. 任务工单审核状态 = 3已通过
2. 提现状态 = 0未提现
3. 任务奖励金额 > 0

##### 积分量化规则

- 邀请奖励：成功邀请1名新用户注册，邀请人获得**10积分**
- 每日签到：每日首次签到奖励**10积分**
- 会员套餐积分：按套餐配置give_point字段为准，购买后实时到账
- 积分消耗：暂仅预留接口，无默认消耗场景，后续业务扩展使用

##### 数据统计口径（定时任务Cron：0 0 0 * * ? 每日凌晨0点执行）

- register_num：当日新增注册用户数（user_info创建时间为当日）
- active_num：当日登录/领取任务/提交任务的去重用户数
- order_num：当日支付成功的电商订单数
- order_amount：当日电商订单实付总金额
- task_num：当日审核通过的任务完成数
- task_pay_amount：当日微信成功打款的任务总金额

---

### 8.2 前台移动端核心接口

#### 8.2.1 任务列表接口

**接口地址**：GET /api/task/list

**接口描述**：分页查询上架任务列表，支持排序、筛选

**是否登录**：否（登录后返回用户对应领取状态）

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页条数 |

**响应data分页结构**：

```JSON
{
  "total": 0,
  "pages": 0,
  "current": 1,
  "size": 10,
  "records": [
    {
      "id": 1,
      "taskTitle": "任务标题",
      "taskCover": "任务封面图URL",
      "taskIntro": "任务简介",
      "rewardAmount": 5.00,
      "dayNum": 10,
      "totalNum": 100,
      "status": 1,
      "sort": 0
    }
  ]
}
```

**核心业务逻辑**：仅查询状态=1上架的任务，按sort倒序、创建时间倒序排序，实时计算每日剩余可领、总剩余可领数量

#### 8.2.2 任务详情接口

**接口地址**：GET /api/task/info/{taskId}

**接口描述**：查询单个任务详细信息

**是否登录**：否

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务主键ID（路径参数） |

**响应data结构**：返回任务全量字段（标题、封面、简介、详情、奖励、限额、过期时长、状态等）

**核心校验**：任务不存在/下架/已删除返回1001错误

#### 8.2.3 领取任务接口

**接口地址**：POST /api/task/receive

**接口描述**：用户领取任务，生成任务领取工单

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| taskId | Long | 是 | 任务ID |

**核心业务校验顺序**：

1. 校验任务是否存在、上架、未删除
2. 校验用户今日该任务领取次数是否超出day_num限额
3. 校验用户是否存在未过期、未完成的同任务工单（禁止重复领取）
4. 校验任务剩余可领取数量是否充足

**并发安全**：使用 Redis 分布式锁 `neop:lock:task:receive:{taskId}:{userId}` 防止重复领取

**数据处理逻辑**：

- 计算expire_time：当前时间 + 任务配置expire_minute分钟
- 新增task_user_receive记录，初始状态：audit_status=1待提交、grant_pay=0未授权、withdraw_status=0未提现
- 自动带入用户微信openid（从user_wechat表获取）

**响应data结构**：`{"receiveId": 1, "expireTime": "2026-04-27 18:30:00"}`

#### 8.2.4 提交任务接口

**接口地址**：POST /api/task/submit

**接口描述**：用户提交任务完成材料，进入审核状态

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiveId | Long | 是 | 任务领取记录ID |
| submitImages | String | 是 | 提交截图，多图英文逗号分隔，最多9张 |
| submitNote | String | 否 | 提交备注，最大500字 |

**核心校验**：仅audit_status=1待提交或4已驳回可提交，其他状态返回1005错误；未过期才可提交

**数据处理**：新增task_submit工单，更新task_user_receive状态为2待审核

#### 8.2.5 我的任务列表接口

**接口地址**：GET /api/task/my/list

**接口描述**：查询当前用户所有任务工单，支持状态筛选

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |
| auditStatus | Integer | 否 | 审核状态筛选 |

**响应结构**：分页返回用户任务工单，关联任务标题、奖励、状态、提交时间、审核信息

#### 8.2.6 任务提现申请接口

**接口地址**：POST /api/task/withdraw/apply

**接口描述**：用户提交任务提现申请，进入后台待打款状态

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiveId | Long | 是 | 任务领取记录ID |

**核心三校验（硬性）**：审核通过、未提现、奖励金额大于0，不满足直接报错

**幂等处理**：Redis `neop:wechat:callback:idempotent:{receiveId}` 防止重复提现

**数据处理**：生成唯一本地交易单号trade_no，初始化task_pay_log，设置withdraw_status=1处理中

#### 8.2.7 提现记录接口

**接口地址**：GET /api/task/withdraw/log

**接口描述**：查询用户所有任务提现记录

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

**响应结构**：分页返回打款日志，包含任务标题、打款金额、状态、申请时间、完成时间

---

### 8.3 用户 & 微信授权模块

#### 8.3.1 微信静默登录接口

**接口地址**：GET /api/user/wechat/login

**是否登录**：否

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| code | String | 是 | 微信前端授权临时code |
| inviteCode | String | 否 | 邀请人邀请码（新用户绑定） |

**核心业务逻辑**：

1. 调用微信接口通过code换取openid、session_key
2. 根据openid查询用户，存在则直接登录，不存在则自动注册新用户
3. 携带inviteCode且为新用户：校验邀请码有效性，绑定上下级关系
4. 签发移动端JWT Token（有效期7天）

**响应data结构**：

```JSON
{
  "token": "JWT登录令牌",
  "userInfo": {
    "id": 1,
    "nickname": "用户昵称",
    "avatar": "用户头像URL",
    "phone": "138****1234",
    "inviteCode": "ABCD1234",
    "usablePoint": 100,
    "memberStatus": 1,
    "memberExpireTime": "2026-12-31 23:59:59"
  }
}
```

#### 8.3.2 获取用户个人信息接口

**接口地址**：GET /api/user/info

**是否登录**：是

**响应结构**：返回用户全量信息、积分、会员状态、邀请统计数据

#### 8.3.3 用户积分签到接口

**接口地址**：POST /api/user/point/sign

**是否登录**：是

**核心校验**：查询Redis签到标记 `neop:sign:userId:{userId}:{date}`，存在则返回3002错误

**业务逻辑**：
1. 新增point_log签到记录
2. 用户积分+10（更新point_user）
3. 设置Redis签到标记，过期至当日23:59:59

**响应结构**：`{"point": 10, "usablePoint": 110, "msg": "签到成功"}`

#### 8.3.4 积分流水记录接口

**接口地址**：GET /api/user/point/log

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |
| type | Integer | 否 | 类型筛选：1获取 2消耗 |

**响应**：分页返回积分变动记录（签到、邀请、会员赠送等）

#### 8.3.5 我的邀请记录接口

**接口地址**：GET /api/user/invite/log

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

**响应**：分页返回已邀请用户列表、邀请时间、奖励积分，统计总邀请人数

#### 8.3.6 收货地址列表接口

**接口地址**：GET /api/user/address/list

**是否登录**：是

**响应**：返回用户所有收货地址，默认地址排在首位

#### 8.3.7 新增收货地址接口

**接口地址**：POST /api/user/address/save

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 收货人姓名，最大50字 |
| phone | String | 是 | 收货人电话，最大20字 |
| province | String | 是 | 省份 |
| city | String | 是 | 城市 |
| district | String | 是 | 区县 |
| detail | String | 是 | 详细地址，最大255字 |
| isDefault | Integer | 否 | 是否默认：0否 1是，默认0 |

**业务逻辑**：如果设为默认地址，先取消原默认地址

#### 8.3.8 编辑收货地址接口

**接口地址**：POST /api/user/address/update

**是否登录**：是

**参数**：同新增，增加 id（必填）

**校验**：地址必须属于当前用户

#### 8.3.9 删除收货地址接口

**接口地址**：POST /api/user/address/delete

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**校验**：地址必须属于当前用户

#### 8.3.10 设为默认地址接口

**接口地址**：POST /api/user/address/setDefault

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

---

### 8.4 会员套餐模块

#### 8.4.1 会员套餐列表接口

**接口地址**：GET /api/member/package/list

**是否登录**：否

**业务逻辑**：只查询状态=1启用的会员套餐，按sort升序排序

**响应字段**：套餐ID、套餐名称、价格、有效天数、赠送积分、排序权重

#### 8.4.2 会员充值下单接口

**接口地址**：POST /api/member/order/create

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| packageId | Long | 是 | 会员套餐ID |

**核心校验**：套餐是否存在、是否启用，否则返回3001

**业务逻辑**：生成会员充值订单（前缀MB）、唯一订单号、待支付状态

**响应**：`{"orderId": 1, "orderNo": "MB20260426183000123456", "amount": 99.00}`

#### 8.4.3 会员订单支付接口

**接口地址**：POST /api/member/order/pay

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 会员充值订单ID |

**校验**：订单存在、属于当前用户、状态=待支付

**业务逻辑**：调用微信统一下单接口，返回前端拉起支付所需全部参数

#### 8.4.4 会员支付回调接口

**接口地址**：POST /api/wechat/member/callback

**核心逻辑**（严格按顺序执行）：
1. 读取原始XML报文，验签（签名校验步骤见第九章）
2. Redis幂等校验：`neop:wechat:callback:idempotent:{orderNo}`，已处理则直接返回SUCCESS
3. 更新pay_order状态为已支付，记录pay_time
4. 计算会员到期时间（按2.5.3叠加规则），更新member_user
5. 发放套餐赠送积分：point_user.usable_point += give_point，新增point_log记录
6. 设置Redis幂等标记，过期24小时
7. 返回SUCCESS XML

---

### 8.5 电商商城模块

#### 8.5.1 商品分类列表

**接口地址**：GET /api/shop/category/list

**是否登录**：否

**响应**：树形结构返回所有启用商品分类

```JSON
[
  {
    "id": 1,
    "categoryName": "分类名称",
    "parentId": 0,
    "sort": 1,
    "children": []
  }
]
```

#### 8.5.2 商品列表接口

**接口地址**：GET /api/shop/product/list

**是否登录**：否

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryId | Long | 否 | 分类ID |
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

**校验**：只查询上架、库存>0商品

#### 8.5.3 商品详情接口

**接口地址**：GET /api/shop/product/info/{productId}

**是否登录**：否

**核心校验**：商品不存在/下架返回2001

#### 8.5.4 购物车列表接口

**接口地址**：GET /api/shop/cart/list

**是否登录**：是

**响应**：返回当前用户购物车列表，包含商品信息、数量、选中状态

#### 8.5.5 加入购物车接口

**接口地址**：POST /api/shop/cart/add

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| num | Integer | 是 | 数量，最小1 |

**业务逻辑**：商品已在购物车则数量+num，否则新增记录

#### 8.5.6 修改购物车接口

**接口地址**：POST /api/shop/cart/update

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 购物车记录ID |
| num | Integer | 否 | 修改数量 |
| isChecked | Integer | 否 | 修改选中状态 |

#### 8.5.7 删除购物车接口

**接口地址**：POST /api/shop/cart/delete

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | String | 是 | 购物车记录ID，多个英文逗号分隔 |

#### 8.5.8 创建电商订单接口

**接口地址**：POST /api/shop/order/create

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| addressId | Long | 是 | 收货地址ID |
| cartIds | String | 否 | 购物车ID，逗号分隔（从购物车结算时传） |
| productId | Long | 否 | 商品ID（直接购买时传） |
| num | Integer | 否 | 购买数量（直接购买时传） |
| remark | String | 否 | 订单备注 |

**说明**：cartIds和productId二选一，同时传以cartIds为准

**核心校验**：商品存在、上架、库存充足，不足返回2002；地址存在且属于当前用户

**并发安全**：使用 Redis 分布式锁 `neop:lock:order:create:{userId}` 防止重复下单

**业务**：锁库存（product.stock -= num）、生成订单主表(前缀NO)+明细表、待支付状态、清空已选购物车

#### 8.5.9 电商订单支付接口

**接口地址**：POST /api/shop/order/pay

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 电商订单ID |

**校验**：订单存在、属于当前用户、待支付状态

#### 8.5.10 电商订单回调接口

**接口地址**：POST /api/wechat/shop/callback

**核心逻辑**：
1. 验签 + Redis幂等校验
2. 更新订单状态为待发货(1)
3. 扣减真实库存：product.stock -= num, product.sales += num
4. 设置Redis幂等标记
5. 返回SUCCESS XML

#### 8.5.11 我的订单列表接口

**接口地址**：GET /api/shop/order/my/list

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |
| status | Integer | 否 | 订单状态筛选 |

**响应**：分页返回订单列表，含订单商品信息

#### 8.5.12 订单详情接口

**接口地址**：GET /api/shop/order/info/{orderId}

**是否登录**：是

**校验**：订单必须属于当前用户

#### 8.5.13 取消订单接口

**接口地址**：POST /api/shop/order/cancel

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**校验**：仅待付款(0)状态可取消

**业务**：更新状态为已取消(4)、释放锁定库存（product.stock += num）

#### 8.5.14 确认收货接口

**接口地址**：POST /api/shop/order/confirm

**是否登录**：是

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |

**校验**：仅待收货(2)状态可确认

**业务**：更新状态为已完成(3)、记录finish_time

---

### 8.6 后台管理全量接口

#### 8.6.1 后台登录接口

**接口地址**：POST /api/admin/login

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 管理员账号 |
| password | String | 是 | 管理员密码 |

**加密规则**：密码BCrypt加盐匹配，签发后台JWT（2小时有效期）

**响应**：`{"token": "JWT令牌", "adminInfo": {"id": 1, "username": "admin", "nickname": "超级管理员", "avatar": ""}}`

#### 8.6.2 后台数据看板接口

**接口地址**：GET /api/admin/dashboard

**返回统计数据**：总用户数、今日新增、今日活跃、今日订单金额、今日打款金额、待审核任务数、待打款数量

#### 8.6.3 用户管理列表接口

**接口地址**：GET /api/admin/user/list

**权限标识**：user:list

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称模糊搜索 |
| phone | String | 否 | 手机号精确搜索 |
| memberStatus | Integer | 否 | 会员状态筛选 |
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

**响应**：用户基础信息（手机号脱敏）、会员状态、积分、注册时间、邀请人数

#### 8.6.4 会员套餐后台CRUD接口

**权限标识**：market:package:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 列表 | /api/admin/member/package/list | GET |
| 新增 | /api/admin/member/package/save | POST |
| 编辑 | /api/admin/member/package/update | POST |
| 删除 | /api/admin/member/package/delete | POST |
| 启用禁用 | /api/admin/member/package/status | POST |

**新增/编辑参数**：packageName(必填)、price(必填)、dayNum(必填)、givePoint(必填)、sort、status

#### 8.6.5 商品分类后台CRUD接口

**权限标识**：shop:category:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 树形列表 | /api/admin/shop/category/tree | GET |
| 新增 | /api/admin/shop/category/save | POST |
| 编辑 | /api/admin/shop/category/update | POST |
| 删除 | /api/admin/shop/category/delete | POST |

**参数**：categoryName(必填)、parentId、sort、status

#### 8.6.6 商品后台CRUD接口

**权限标识**：shop:goods:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 分页列表 | /api/admin/shop/goods/list | GET |
| 详情 | /api/admin/shop/goods/info/{id} | GET |
| 新增 | /api/admin/shop/goods/save | POST |
| 编辑 | /api/admin/shop/goods/update | POST |
| 删除 | /api/admin/shop/goods/delete | POST |
| 上下架 | /api/admin/shop/goods/status | POST |

**新增/编辑参数**：categoryId(必填)、productName(必填)、productImg、productImages、price(必填)、originalPrice、stock(必填)、content、sort、status

#### 8.6.7 订单管理后台接口

**权限标识**：shop:order:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 分页列表 | /api/admin/shop/order/list | GET |
| 详情 | /api/admin/shop/order/info/{id} | GET |
| 发货 | /api/admin/shop/order/send | POST |
| 取消 | /api/admin/shop/order/cancel | POST |

**发货参数**：orderId(必填)

**发货业务**：更新状态为待收货(2)、记录send_time

**列表筛选参数**：orderNo、status、startDate、endDate、current、size

#### 8.6.8 任务后台CRUD接口

**权限标识**：task:info:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 分页列表 | /api/admin/task/list | GET |
| 详情 | /api/admin/task/info/{id} | GET |
| 新增 | /api/admin/task/save | POST |
| 编辑 | /api/admin/task/update | POST |
| 删除 | /api/admin/task/delete | POST |
| 上下架 | /api/admin/task/status | POST |

**新增/编辑参数**：taskTitle(必填)、taskCover、taskIntro、taskContent、rewardAmount(必填)、totalNum(必填)、dayNum(必填)、expireMinute(必填)、sort、status

#### 8.6.9 任务审核接口

**权限标识**：task:audit:*

| 操作 | 接口地址 | 请求方式 |
|------|----------|----------|
| 待审核列表 | /api/admin/task/audit/list | GET |
| 审核通过 | /api/admin/task/audit/pass | POST |
| 审核驳回 | /api/admin/task/audit/reject | POST |

**审核参数**：receiveId(必填)、auditNote(驳回时必填)

**通过业务**：更新audit_status=3、记录audit_time
**驳回业务**：更新audit_status=4、记录audit_note和audit_time

#### 8.6.10 授权打款接口

**接口地址**：POST /api/admin/task/grant/pay

**权限标识**：task:paylog:grant

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiveId | Long | 是 | 任务领取记录ID |

**业务逻辑**：校验审核已通过 → 设置grant_pay=1 → 调用微信企业付款接口 → 更新打款日志

#### 8.6.11 打款日志接口

**接口地址**：GET /api/admin/task/pay/log

**权限标识**：task:paylog:list

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payStatus | Integer | 否 | 打款状态筛选 |
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

#### 8.6.12 操作日志接口

**接口地址**：GET /api/admin/log/list

**权限标识**：sys:log:list

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| module | String | 否 | 操作模块筛选 |
| operatorName | String | 否 | 操作人搜索 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |
| current | Integer | 否 | 页码 |
| size | Integer | 否 | 条数 |

**响应**：分页返回操作日志，包含操作人、模块、描述、请求参数、IP、耗时等

---

## 九、微信支付核心技术规范

### 9.1 微信企业付款实现规范

- 证书：resources/cert/apiclient_cert.p12
- 回调地址：https://neop.gongziyu.com/api/wechat/pay/callback
- 状态：处理中→成功/失败
- 失败自动重试+后台手动补发

### 9.2 微信企业付款最终技术标准

- 调用版本：**微信支付V2接口**（兼容p12证书）
- 接口地址：**https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers**
- 签名算法：HMAC-SHA256
- 证书文件：apiclient_cert.p12
- 失败重试机制：自动重试3次，间隔30s、60s、120s
- 回调报文格式：标准V2 XML格式
- 回调成功返回：`<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>`

### 9.3 微信支付签名验证详细步骤

**签名生成（请求发送时）**：

1. 将所有非空参数按字典序（ASCII码）排序
2. 使用URL键值对格式拼接：`key1=value1&key2=value2&key=API密钥`
3. 对拼接字符串做 HMAC-SHA256 运算，结果转大写
4. 将 sign 字段加入请求参数

**签名验证（回调接收时）**：

1. 接收原始XML报文，解析为Map
2. 取出 sign 字段值，从Map中移除 sign 和 sign_type
3. 将剩余非空参数按字典序排序
4. 使用URL键值对格式拼接：`key1=value1&key2=value2&key=API密钥`
5. 对拼接字符串做 HMAC-SHA256 运算，结果转大写
6. 比对计算值与接收的sign是否一致，不一致则验签失败

**注意事项**：
- key 为微信商户平台设置的 API 密钥
- 空值参数不参与签名
- 参数名区分大小写

### 9.4 微信支付回调处理流程（统一模板）

```
接收原始报文 → 解析XML → 验签 → Redis幂等校验 → 业务处理 → 设置幂等标记 → 返回SUCCESS
```

**问题说明**：回调地址 https://neop.gongziyu.com/api/wechat/pay/callback 出现网页解析失败，原因为：微信回调为**XML纯文本报文POST请求**，非网页HTML页面，浏览器直接打开会解析失败，属于正常现象，不影响接口服务。

---

## 十、认证权限规范

### 10.1 登录认证方式

- 移动端：微信静默授权登录，获取openid自动注册/登录，签发JWT令牌
- 后台端：账号密码登录，BCrypt加密校验密码，签发JWT令牌

### 10.2 JWT统一配置

- 过期时间：移动端7天、后台端2小时
- 加密算法：HS256
- 密钥：至少32字符的随机字符串，生产环境从环境变量读取
- Token结构：`{ "userId": 1, "type": "mobile/admin", "exp": 1714108800 }`
- 权限拦截：后台接口使用@PreAuthorize注解匹配角色/菜单权限

### 10.3 密码加密规则

所有管理员密码统一使用**BCrypt加盐加密**，禁止明文存储、禁止自定义加密算法

### 10.4 Token 黑名单机制

- 用户主动登出时，将Token存入Redis黑名单，过期时间=Token剩余有效期
- 每次请求校验Token时，先查Redis黑名单，存在则返回401
- 后台可强制踢人：将指定用户Token加入黑名单

---

## 十一、前端统一交互规范

### 11.1 通用规范

- 全局请求：统一封装Token携带、请求超时、异常拦截
- 全局响应：200成功正常渲染，401自动跳转登录，403提示权限不足，429提示请求频繁，500提示系统异常
- 图片上传：单图/多图统一工具类，最多支持9张图片上传
- 表单校验：所有必填项前端实时校验，长度、格式提前拦截，减少无效请求

### 11.2 后台管理UI设计规范

- **UI框架**：Element Plus
- **色彩体系**：
  - 主色：#409EFF（Element Plus默认蓝）
  - 成功色：#67C23A
  - 警告色：#E6A23C
  - 危险色：#F56C6C
  - 信息色：#909399
- **字体**：系统默认字体栈 `-apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif`
- **布局**：左侧菜单 + 顶部导航栏 + 内容区域，经典后台布局
- **表格**：统一使用Element Plus el-table，支持分页、搜索、排序
- **表单**：统一使用el-form，必填项显示红色星号，校验失败显示提示

### 11.3 移动端UI设计规范

- **UI框架**：uni-ui + 自定义主题
- **色彩体系**：同后台管理
- **适配**：rpx响应式单位，750rpx设计稿
- **交互**：下拉刷新、上拉加载更多、骨架屏占位

---

## 十二、定时任务完整清单

| 任务名称 | Cron表达式 | 执行频率 | 核心逻辑 |
|----------|-----------|----------|----------|
| 任务过期扫描 | `0 */30 * * * ?` | 每30分钟 | 扫描audit_status IN (1,2)且expire_time < NOW()的记录，更新为5已过期 |
| 每日数据统计 | `0 0 0 * * ?` | 每日凌晨 | 统计全站数据并写入data_statistics表 |
| 支付订单超时关闭 | `0 */5 * * * ?` | 每5分钟 | 扫描status=0且create_time < NOW()-15分钟的订单，更新为已取消、释放库存 |
| 会员过期扫描 | `0 0 1 * * ?` | 每日凌晨1点 | 扫描expire_time < NOW()的member_user记录，更新is_member=0 |
| 打款失败自动重试 | `0 */10 * * * ?` | 每10分钟 | 扫描pay_status=3且retry_count < 3的记录，执行重试 |
| 订单自动确认收货 | `0 0 2 * * ?` | 每日凌晨2点 | 扫描status=2且send_time < NOW()-7天的订单，更新为已完成 |

---

## 十三、并发安全规范

### 13.1 分布式锁使用场景

| 场景 | 锁Key | 锁时长 | 说明 |
|------|-------|--------|------|
| 任务领取 | `neop:lock:task:receive:{taskId}:{userId}` | 5秒 | 防止重复领取 |
| 电商下单 | `neop:lock:order:create:{userId}` | 5秒 | 防止重复下单 |
| 库存扣减 | `neop:lock:stock:product:{productId}` | 3秒 | 防止超卖 |
| 提现申请 | `neop:lock:withdraw:{receiveId}` | 5秒 | 防止重复提现 |
| 积分变动 | `neop:lock:point:{userId}` | 3秒 | 防止积分计算异常 |

### 13.2 库存扣减策略

**商品库存**：
1. 下单时：`product.stock -= num`（数据库乐观锁 `WHERE stock >= num`）
2. 支付回调：`product.sales += num`（销量增加）
3. 取消订单：`product.stock += num`（库存回补）

**任务库存**：
1. 领取时：通过 `task_user_receive` 表记录数计算剩余，不直接扣减
2. 高并发场景：Redis预扣减 `neop:task:stock:{taskId}`，异步同步到数据库

### 13.3 幂等处理

所有写操作接口必须保证幂等：
- 微信支付回调：Redis幂等Key，24小时过期
- 提现申请：检查withdraw_status，0才可申请
- 签到：Redis签到标记，当日有效
- 领取任务：检查是否存在未过期同任务工单

---

## 十四、日志规范

### 14.1 后端日志级别

| 级别 | 使用场景 |
|------|----------|
| ERROR | 系统异常、第三方调用失败、数据不一致 |
| WARN | 业务校验失败、限流触发、重试中 |
| INFO | 关键业务操作（支付、打款、审核）、定时任务执行 |
| DEBUG | 请求参数、SQL执行、方法入参出参（仅开发环境） |

### 14.2 操作日志

- 实现方式：自定义 `@OperationLog(module = "任务管理", description = "审核通过")` 注解 + AOP
- 记录内容：操作人、模块、描述、请求URL、请求参数、响应结果、IP、耗时
- 记录时机：后台管理接口的所有写操作（新增/编辑/删除/审核/打款等）
- 存储位置：`sys_operation_log` 表
- 保留策略：保留最近6个月，超出自动清理

### 14.3 关键业务日志输出规范

```java
// 支付回调
log.info("[支付回调] orderNo={}, status={}, amount={}", orderNo, status, amount);

// 任务打款
log.info("[任务打款] receiveId={}, userId={}, amount={}, tradeNo={}", receiveId, userId, amount, tradeNo);
log.error("[任务打款失败] receiveId={}, reason={}", receiveId, failReason);

// 定时任务
log.info("[定时任务-过期扫描] 扫描到{}条过期记录", count);
```

---

## 十五、健康检查 & 监控

### 15.1 SpringBoot Actuator 配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

- 健康检查端点：`GET /actuator/health`，返回数据库、Redis连接状态
- 仅内网可访问（Nginx不代理 `/actuator` 路径）

### 15.2 关键监控指标

| 指标 | 告警阈值 | 说明 |
|------|----------|------|
| API响应时间 P99 | > 3秒 | 接口性能异常 |
| 错误率 | > 5% | 系统异常 |
| 数据库连接池使用率 | > 80% | 连接池不足 |
| Redis内存使用率 | > 80% | 缓存异常 |
| 待审核任务积压 | > 100条 | 需人工介入 |
| 打款失败积压 | > 10条 | 需人工介入 |

---

## 十六、数据库版本迁移策略

### 16.1 使用 Flyway 管理数据库版本

- SQL脚本路径：`src/main/resources/db/migration/`
- 命名规则：`V{版本号}__{描述}.sql`，如 `V1.0.0__init_schema.sql`、`V1.0.1__add_operation_log.sql`
- 初始全量脚本：`V1.0.0__init_schema.sql`（当前第七章的完整SQL）
- 后续变更：每个增量变更一个版本文件，禁止修改已执行过的版本文件

### 16.2 Flyway Maven 配置

```yaml
# application.yml 中添加
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1.0.0
```

---

## 十七、AI全模块分步开发计划（严格顺序）

| 阶段 | 开发内容 | 交付物 | 预估工期 |
|------|----------|--------|----------|
| 1 | 环境与数据库初始化 | 项目骨架+建表脚本+Flyway+配置文件 | 0.5天 |
| 2 | 系统+用户基础模块 | RBAC权限+管理员登录+用户微信登录+收货地址 | 2天 |
| 3 | 营销模块 | 会员套餐+充值支付+积分签到+邀请体系 | 2天 |
| 4 | 电商交易模块 | 商品+购物车+订单+支付回调 | 3天 |
| 5 | 任务模块（核心） | 任务CRUD+领取+提交+审核+提现 | 3天 |
| 6 | 微信企业付款 | 企业付款接口+回调+重试+补发 | 1.5天 |
| 7 | 数据统计+定时任务 | 数据看板+6个定时任务 | 1天 |
| 8 | UniApp移动端 | 全部页面+接口对接 | 4天 |
| 9 | Vue3后台管理 | 全部页面+接口对接 | 4天 |
| 10 | 联调、测试、部署 | 全链路测试+Nginx部署+HTTPS | 2天 |

---

## 十八、AI开发强制禁忌

- 禁止余额、资金池、余额充值
- 禁止跳过提现三校验
- 禁止自动打款（必须后台人工授权）
- 禁止跨状态更新
- 邀请仅奖励积分
- pay_order 仅限会员充值
- 禁止跳过微信回调验签
- 禁止跳过支付回调幂等校验
- 禁止在接口返回中输出密码明文
- 禁止使用 FLOAT/DOUBLE 存储金额

---

## 十九、项目最终交付成果

✅ 完整可商用单商户系统
✅ 前后端全套代码
✅ 权限+用户+电商+营销+任务闭环
✅ 任务审核→授权→单笔微信提现
✅ 无资金池、合规安全
✅ Redis缓存+分布式锁+限流防刷
✅ 操作日志+数据监控
✅ 数据库版本迁移（Flyway）
✅ 可直接上线部署

---

## 二十、全局异常处理规范（AI强制遵循）

### 20.1 异常分级与处理优先级

异常必须按以下**优先级顺序**捕获和处理：

| 优先级 | 异常类型 | 处理方式 | HTTP状态码 | 返回码 |
|--------|----------|----------|------------|--------|
| 1 | 自定义业务异常 `BusinessException` | 直接返回业务错误码和消息 | 200 | 业务错误码(1001-6001) |
| 2 | 参数校验异常 `MethodArgumentNotValidException` | 提取第一个校验失败信息返回 | 400 | 400 |
| 3 | 数据绑定异常 `BindException` | 提取绑定失败信息 | 400 | 400 |
| 4 | 权限异常 `AccessDeniedException` | 返回权限不足提示 | 403 | 403 |
| 5 | 认证异常 `AuthenticationException` | 返回未登录提示 | 401 | 401 |
| 6 | 第三方调用异常 `WechatApiException` | 记录日志+返回服务异常 | 200 | 500 |
| 7 | 数据库异常 `DataAccessException` | 记录日志+返回服务异常 | 200 | 500 |
| 8 | 未知异常 `Exception` | 记录日志+返回服务异常 | 200 | 500 |

### 20.2 全局异常处理器实现规范

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 20.2.1 业务异常（最高优先级）
     * 用于：任务不存在、库存不足、状态异常等业务规则校验失败
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("[业务异常] code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 20.2.2 参数校验异常
     * 用于：@Valid/@Validated注解校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String firstError = e.getBindingResult().getFieldError() != null 
            ? e.getBindingResult().getFieldError().getDefaultMessage() 
            : "参数校验失败";
        log.warn("[参数校验异常] {}", firstError);
        return Result.error(400, firstError);
    }

    /**
     * 20.2.3 自定义参数校验异常
     * 用于：手动校验参数后抛出的异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("[参数异常] {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 20.2.4 微信API调用异常
     * 用于：微信支付、企业付款等第三方调用失败
     */
    @ExceptionHandler(WechatApiException.class)
    public Result<Void> handleWechatException(WechatApiException e) {
        log.error("[微信API异常] code={}, msg={}", e.getCode(), e.getMessage(), e);
        // 触发告警（如：打款连续失败）
        if (e.getCode() == 5001) {
            // TODO: 发送告警通知管理员
        }
        return Result.error(500, "微信服务异常，请稍后重试");
    }

    /**
     * 20.2.5 数据库操作异常
     * 用于：数据库连接失败、SQL执行异常
     */
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccessException(DataAccessException e) {
        log.error("[数据库异常]", e);
        return Result.error(500, "数据库操作异常，请联系管理员");
    }

    /**
     * 20.2.6 分布式锁获取失败异常
     * 用于：高并发场景下获取锁失败
     */
    @ExceptionHandler(LockAcquisitionException.class)
    public Result<Void> handleLockException(LockAcquisitionException e) {
        log.warn("[锁获取失败] {}", e.getMessage());
        return Result.error(429, "操作过于频繁，请稍后再试");
    }

    /**
     * 20.2.7 未知异常（最低优先级，必须放在最后）
     * 用于：所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknownException(Exception e) {
        log.error("[未知异常]", e);
        return Result.error(500, "服务器内部异常");
    }
}
```

### 20.3 自定义业务异常类

```java
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(500, message);
    }

    // 快捷创建方法
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String message) {
        return new BusinessException(500, message);
    }
}
```

### 20.4 异常抛出规范

```java
// ✅ 正确示例：业务校验失败直接抛出业务异常
if (taskInfo.getStatus() != 1) {
    throw BusinessException.of(1001, "任务不存在或已下架");
}

// ✅ 正确示例：参数校验失败抛出参数异常
if (receiveId == null || receiveId <= 0) {
    throw new IllegalArgumentException("任务领取记录ID不能为空");
}

// ❌ 错误示例：不要捕获异常后返回null
try {
    // ...
} catch (Exception e) {
    return null;  // 错误！必须抛出异常
}

// ✅ 正确示例：捕获异常后包装为业务异常抛出
try {
    wechatPayService.transfer(params);
} catch (WechatApiException e) {
    throw new WechatApiException(e.getCode(), "微信打款失败：" + e.getMessage());
}
```

---

## 二十一、事务管理规范（AI强制遵循）

### 21.1 事务边界定义（核心）

| 业务操作 | 事务要求 | 传播行为 | 超时时间 | 说明 |
|----------|----------|----------|----------|------|
| 任务审核通过+积分发放 | **必须同一事务** | REQUIRED | 30秒 | 审核状态和积分必须同时成功或失败 |
| 支付回调处理 | **必须开启事务** | REQUIRED | 30秒 | 订单状态、会员到期时间、积分必须原子性更新 |
| 任务领取 | **分布式锁+事务** | REQUIRED | 10秒 | 先获取分布式锁，再开启事务 |
| 电商下单 | **分布式锁+事务** | REQUIRED | 15秒 | 库存扣减、订单生成必须原子性操作 |
| 打款操作 | **必须同一事务** | REQUIRED | 30秒 | 打款日志、任务状态必须同时更新 |

### 21.2 事务实现规范

```java
@Service
@Slf4j
public class TaskAuditService {

    /**
     * 21.2.1 任务审核通过（必须在同一事务中完成）
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void auditPass(Long receiveId, Long adminId) {
        // 1. 查询任务领取记录（加行锁，防止并发审核）
        TaskUserReceive receive = taskUserReceiveMapper.selectForUpdate(receiveId);
        
        // 2. 校验状态（必须在事务中校验）
        if (receive.getAuditStatus() != 2) {
            throw BusinessException.of(1005, "当前任务状态不支持审核通过");
        }
        
        // 3. 更新审核状态
        receive.setAuditStatus(3);
        receive.setAuditTime(LocalDateTime.now());
        taskUserReceiveMapper.updateById(receive);
        
        // 4. 如果任务有积分奖励，发放积分（同一事务）
        TaskInfo taskInfo = taskInfoMapper.selectById(receive.getTaskId());
        if (taskInfo.getRewardPoint() != null && taskInfo.getRewardPoint() > 0) {
            pointService.addPoint(receive.getUserId(), taskInfo.getRewardPoint(), "task", "任务审核通过奖励");
        }
        
        // 5. 记录操作日志（同一事务）
        operationLogService.record(adminId, "任务管理", "审核通过", "receiveId=" + receiveId);
        
        // 如果以上任何一步失败，整个事务回滚
    }

    /**
     * 21.2.2 查询并加行锁（防止并发问题）
     * 使用：SELECT ... FOR UPDATE
     */
    public TaskUserReceive selectForUpdate(Long receiveId) {
        return taskUserReceiveMapper.selectForUpdate(receiveId);
    }
}

@Mapper
public interface TaskUserReceiveMapper extends BaseMapper<TaskUserReceive> {
    @Select("SELECT * FROM task_user_receive WHERE id = #{receiveId} FOR UPDATE")
    TaskUserReceive selectForUpdate(Long receiveId);
}
```

### 21.3 分布式事务处理方案

**场景：支付回调处理（涉及多个表更新）**

```java
@Service
@Slf4j
public class WechatCallbackService {

    /**
     * 21.3.1 支付回调处理（分布式事务）
     * 方案：本地事务 + 幂等校验 + 补偿机制
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void handlePayCallback(String orderNo, String wechatPayNo) {
        // 1. 幂等校验（必须在事务外完成，使用Redis）
        String idempotentKey = "neop:wechat:callback:idempotent:" + orderNo;
        if (redisTemplate.hasKey(idempotentKey)) {
            log.info("[支付回调] 重复回调，直接返回成功 orderNo={}", orderNo);
            return;
        }

        // 2. 开启事务，更新订单状态
        PayOrder payOrder = payOrderMapper.selectByOrderNo(orderNo);
        if (payOrder == null || payOrder.getStatus() == 1) {
            // 订单不存在或已支付，设置幂等标记后返回
            redisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
            return;
        }
        payOrder.setStatus(1);
        payOrder.setPayTime(LocalDateTime.now());
        payOrderMapper.updateById(payOrder);

        // 3. 更新会员到期时间（按2.5.3叠加规则）
        memberService.extendMember(payOrder.getUserId(), payOrder.getPackageId());

        // 4. 发放套餐赠送积分
        memberService.grantPackagePoint(payOrder.getUserId(), payOrder.getPackageId());

        // 5. 设置幂等标记（事务提交后执行）
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
                log.info("[支付回调] 幂等标记已设置 orderNo={}", orderNo);
            }
        });

        log.info("[支付回调] 处理成功 orderNo={}, wechatPayNo={}", orderNo, wechatPayNo);
    }
}
```

### 21.4 事务使用禁忌

```java
// ❌ 禁忌1：事务内调用第三方接口（会导致事务持有时间过长）
@Transactional
public void wrongMethod() {
    updateOrderStatus();  // 数据库操作
    wechatPayService.transfer();  // ❌ 错误！第三方调用可能超时，导致事务一直持有
}

// ✅ 正确做法：先完成数据库操作并提交事务，再调用第三方
@Transactional
public void rightMethod() {
    updateOrderStatus();  // 数据库操作
}  // 事务提交

// 事务提交后再调用第三方
wechatPayService.transfer();


// ❌ 禁忌2：事务内执行耗时操作（如：睡眠、大量计算）
@Transactional
public void wrongMethod() {
    updateData();
    Thread.sleep(5000);  // ❌ 错误！事务会持有5秒
}

// ❌ 禁忌3：捕获异常但不抛出（会导致事务不回滚）
@Transactional
public void wrongMethod() {
    try {
        updateData();
    } catch (Exception e) {
        log.error("错误", e);
        // ❌ 错误！异常被捕获，事务不会回滚
    }
}

// ✅ 正确做法：抛出异常让事务回滚
@Transactional
public void rightMethod() {
    try {
        updateData();
    } catch (Exception e) {
        log.error("错误", e);
        throw e;  // ✅ 正确！抛出异常，事务回滚
    }
}
```

---

## 二十二、安全加固规范（AI强制遵循）

### 22.1 SQL注入防护

| 场景 | 正确做法 | 错误做法 |
|------|----------|----------|
| MyBatis查询 | 使用 `#{}` 占位符 | 使用 `${}` 字符串拼接 |
| 动态排序 | 白名单校验排序字段 | 直接拼接用户输入 |
| 动态表名 | 禁止用户输入表名 | 使用 `${tableName}` |

```java
// ✅ 正确示例：使用 #{} 占位符
@Select("SELECT * FROM user_info WHERE id = #{userId}")
UserInfo selectById(Long userId);

// ❌ 错误示例：使用 ${} 字符串拼接（SQL注入风险）
@Select("SELECT * FROM user_info WHERE id = ${userId}")  // 禁止！
UserInfo selectById(Long userId);

// ✅ 正确示例：动态排序字段白名单校验
public List<TaskInfo> listTask(String orderBy) {
    // 白名单校验：只允许 sort、create_time、reward_amount
    List<String> allowedOrderFields = Arrays.asList("sort", "create_time", "reward_amount");
    if (!allowedOrderFields.contains(orderBy)) {
        orderBy = "sort DESC, create_time DESC";  // 默认排序
    }
    return taskInfoMapper.selectList(new QueryWrapper<TaskInfo>().orderBy(true, false, orderBy));
}
```

### 22.2 XSS防护

```java
// 22.2.1 后端存储前过滤（使用Jsoup）
public class XssUtil {
    private static final Whitelist WHITELIST = Whitelist.basicWithImages()
            .addAttributes("a", "href", "title")
            .addAttributes("img", "src", "alt")
            .addProtocols("a", "href", "http", "https");

    public static String clean(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        return Jsoup.clean(content, WHITELIST);
    }
}

// 使用AOP自动过滤所有@RequestBody参数
@Aspect
@Component
@Slf4j
public class XssAspect {
    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public Object xssFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                args[i] = XssUtil.clean((String) args[i]);
            }
        }
        return joinPoint.proceed(args);
    }
}

// 22.2.2 前端自动转义（Vue自动完成）
// Vue模板中使用 {{ }} 会自动转义HTML
// 如果需要显示HTML，必须使用 v-html 并确保内容已后端过滤
```

### 22.3 敏感数据加密存储

| 字段 | 加密方式 | 密钥管理 | 说明 |
|------|----------|----------|------|
| 手机号 `user_info.phone` | AES-128-CBC | 环境变量 `PHONE_ENCRYPT_KEY` | 存储和查询都需要加密/解密 |
| 收货地址 `user_address.detail` | AES-128-CBC | 同手机号密钥 | 详细地址加密存储 |
| 微信openid `user_wechat.openid` | 不加密 | - | openid本身不是敏感数据 |
| 管理员密码 `sys_admin.password` | BCrypt | - | 单向哈希，不可解密 |

```java
// 22.3.1 AES加密工具类
@Component
@Slf4j
public class AesEncryptUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static String key;  // 从环境变量读取

    @Value("${neop.encrypt.key}")
    public void setKey(String key) {
        AesEncryptUtil.key = key;
    }

    public static String encrypt(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("[AES加密] 失败 content={}", content, e);
            throw BusinessException.of("加密失败");
        }
    }

    public static String decrypt(String encrypted) {
        if (StringUtils.isBlank(encrypted)) {
            return encrypted;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("[AES解密] 失败 encrypted={}", encrypted, e);
            throw BusinessException.of("解密失败");
        }
    }
}

// 22.3.2 手机号加密存储示例
@Data
@TableName("user_info")
public class UserInfo {
    private Long id;
    private String nickname;
    
    // 手机号加密存储
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String phone;  // 数据库中存储的是加密后的字符串

    // 设置手机号时自动加密
    public void setPhone(String phone) {
        this.phone = AesEncryptUtil.encrypt(phone);
    }

    // 获取手机号时自动解密
    public String getPhone() {
        return AesEncryptUtil.decrypt(this.phone);
    }
}
```

### 22.4 文件上传安全

```java
// 22.4.1 文件类型白名单校验
@Component
public class FileUploadUtil {
    // 允许的文件类型
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;  // 10MB

    public static void validate(MultipartFile file) {
        // 1. 文件大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw BusinessException.of(400, "文件大小不能超过10MB");
        }

        // 2. 文件类型校验（基于Content-Type）
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw BusinessException.of(400, "不支持的文件类型：" + contentType);
        }

        // 3. 文件扩展名校验
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.of(400, "不支持的文件扩展名：" + extension);
        }

        // 4. 文件内容校验（防止图片中包含恶意代码）
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw BusinessException.of(400, "文件内容不是有效的图片");
            }
        } catch (Exception e) {
            throw BusinessException.of(400, "文件内容校验失败");
        }
    }
}
```

---

## 二十三、测试规范（AI强制遵循）

### 23.1 单元测试覆盖率要求

| 模块 | 最低覆盖率 | 说明 |
|------|------------|------|
| 工具类（util） | 100% | 所有工具方法必须有单元测试 |
| 服务层（service） | ≥80% | 核心业务逻辑必须覆盖 |
| 控制器层（controller） | ≥70% | 主要接口必须有测试 |
| 持久层（mapper） | 不要求 | 由服务层测试覆盖 |

### 23.2 单元测试编写规范

```java
@SpringBootTest
@Slf4j
public class TaskReceiveServiceTest {
    @Autowired
    private TaskReceiveService taskReceiveService;

    @MockBean
    private WechatPayService wechatPayService;  // 模拟第三方服务

    @Test
    @DisplayName("领取任务-成功")
    public void testReceiveTask_Success() {
        // 1. 准备测试数据
        Long userId = 1L;
        Long taskId = 1L;

        // 2. 执行测试
        Long receiveId = taskReceiveService.receiveTask(userId, taskId);

        // 3. 验证结果
        assertNotNull(receiveId);
        TaskUserReceive receive = taskUserReceiveMapper.selectById(receiveId);
        assertEquals(1, receive.getAuditStatus());  // 初始状态为待提交
        assertEquals(rewardAmount, receive.getRewardAmount());
    }

    @Test
    @DisplayName("领取任务-任务已下架")
    public void testReceiveTask_TaskDisabled() {
        // 1. 准备测试数据（任务状态为0）
        Long userId = 1L;
        Long taskId = 2L;  // 已下架的任务

        // 2. 验证异常抛出
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            taskReceiveService.receiveTask(userId, taskId);
        });

        // 3. 验证异常信息
        assertEquals(1001, exception.getCode());
        assertEquals("任务不存在或已下架", exception.getMessage());
    }

    @Test
    @DisplayName("领取任务-并发重复领取")
    public void testReceiveTask_ConcurrentDuplicate() {
        // 使用多线程模拟并发场景
        Long userId = 1L;
        Long taskId = 1L;
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    taskReceiveService.receiveTask(userId, taskId);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // 验证：只有1个线程成功，其余9个线程都失败
        assertEquals(1, successCount.get());
        assertEquals(threadCount - 1, failCount.get());
    }
}
```

### 23.3 接口测试规范

```java
// 23.3.1 使用Postman生成接口测试集合
// 导出为JSON格式，纳入版本控制：/docs/postman/NEOP_API_Test_Collection.json

// 23.3.2 使用Spring MockMvc编写接口测试
@AutoConfigureMockMvc
@SpringBootTest
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/task/list - 成功")
    public void testTaskList_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/task/list")
                        .param("current", "1")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + getTestToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("POST /api/task/receive - 未登录")
    public void testTaskReceive_Unauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/task/receive")
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401));
    }
}
```

### 23.4 压力测试场景定义

| 测试场景 | 并发数 | 持续时间 | 成功标准 |
|----------|--------|----------|----------|
| 任务领取接口 | 100并发 | 1分钟 | 响应时间P99<3秒，错误率<1% |
| 电商下单接口 | 50并发 | 1分钟 | 响应时间P99<5秒，错误率<1%，无超卖 |
| 支付回调接口 | 200并发 | 1分钟 | 响应时间P99<2秒，错误率<0.1% |
| 任务提交接口 | 80并发 | 1分钟 | 响应时间P99<3秒，错误率<1% |

```xml
<!-- 23.4.1 JMeter测试计划示例：/docs/jmeter/task_receive_test.jmx -->
<!-- 使用JMeter GUI创建测试计划，保存为.jmx文件，纳入版本控制 -->
```

---

## 二十四、API版本管理策略

### 24.1 版本化规则

- **URL路径版本化**：`/api/v1/task/list`（推荐，简单直观）
- **请求头版本化**：`Accept: application/vnd.neop.v1+json`（可选，更RESTful）

### 24.2 版本兼容性处理

| 变更类型 | 是否兼容 | 处理方式 |
|----------|----------|----------|
| 新增字段 | ✅ 兼容 | 直接新增，旧版本客户端忽略未知字段 |
| 删除字段 | ❌ 不兼容 | 创建新版本 `/api/v2/...` |
| 修改字段类型 | ❌ 不兼容 | 创建新版本 `/api/v2/...` |
| 修改业务逻辑 | ⚠️ 可能不兼容 | 评估影响范围，必要时创建新版本 |

### 24.3 版本生命周期管理

| 阶段 | 说明 | 持续时间 |
|------|------|----------|
| 活跃 | 当前版本，持续更新 | - |
| 维护 | 不再新增功能，只修复Bug | 6个月 |
| 废弃 | 返回警告Header，提示升级 | 3个月 |
| 下线 | 返回404或301重定向到新版本 | - |

---

## 二十五、数据归档和清理策略

### 25.1 数据归档规则

| 表名 | 归档条件 | 归档目标 | 归档频率 |
|------|----------|----------|----------|
| `sys_operation_log` | 创建时间>6个月 | `sys_operation_log_archive_YYYY` | 每月1日 |
| `task_pay_log` | 打款时间>3年 | `task_pay_log_archive_YYYY` | 每年1月1日 |
| `pay_order` | 支付时间>3年 | `pay_order_archive_YYYY` | 每年1月1日 |
| `order` | 完成时间>3年 | `order_archive_YYYY` | 每年1月1日 |

### 25.2 数据清理规则

| 表名 | 清理条件 | 清理频率 |
|------|----------|----------|
| `sys_operation_log_archive_*` | 归档时间>3年 | 每年1月1日 |
| `task_submit.submit_images` | 任务完成时间>1年 | 每月1日（清空图片URL，保留记录） |
| Redis缓存 | 过期时间到达 | 自动清理 |

### 25.3 归档实现示例

```java
// 25.3.1 操作日志归档定时任务
@Component
@Slf4j
public class DataArchiveTask {
    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;

    /**
     * 每月1日凌晨3点执行归档
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void archiveOperationLog() {
        // 1. 计算归档时间节点（6个月前）
        LocalDateTime archiveTime = LocalDateTime.now().minusMonths(6);
        String archiveTableName = "sys_operation_log_archive_" + archiveTime.getYear();

        // 2. 创建归档表（如果不存在）
        sysOperationLogMapper.createArchiveTableIfNotExists(archiveTableName);

        // 3. 迁移数据
        int migratedCount = sysOperationLogMapper.migrateToArchive(archiveTableName, archiveTime);
        log.info("[数据归档] 操作日志归档完成，归档表={}, 归档条数={}", archiveTableName, migratedCount);

        // 4. 删除已归档的数据
        if (migratedCount > 0) {
            int deletedCount = sysOperationLogMapper.deleteArchivedData(archiveTime);
            log.info("[数据归档] 已删除归档数据，删除条数={}", deletedCount);
        }
    }
}
```

---

## 二十六、项目依赖版本（AI强制遵循，禁止自选版本）

### 26.1 后端核心依赖版本（pom.xml）

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.gongziyu</groupId>
    <artifactId>neop-backend</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>  <!-- 必须！对应Java 21 -->
        <relativePath/>
    </parent>
    
    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <weixin-java.version>4.6.0</weixin-java.version>
        <hutool.version>5.8.25</hutool.version>
        <redisson.version>3.27.0</redisson.version>
    </properties>
    
    <dependencies>
        <!-- SpringBoot核心 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        
        <!-- 数据库 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        
        <!-- Redis + 分布式锁 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>${redisson.version}</version>
        </dependency>
        
        <!-- 微信支付（V2接口，兼容p12证书） -->
        <dependency>
            <groupId>com.github.binarywang</groupId>
            <artifactId>weixin-java-pay</artifactId>
            <version>${weixin-java.version}</version>
        </dependency>
        
        <!-- 工具库 -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>${hutool.version}</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>2.0.48</version>
        </dependency>
        
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        
        <!-- XSS防护 -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.17.2</version>
        </dependency>
        
        <!-- 测试 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 26.2 前端核心依赖版本

**Vue3后台管理（package.json）**
```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vite": "^5.2.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "element-plus": "^2.7.0",
    "axios": "^1.7.0",
    "dayjs": "^1.11.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "sass": "^1.72.0"
  }
}
```

**UniApp移动端**
- 使用HBuilderX创建项目，选择Vue3 + Vite模板
- 安装uni-ui组件库：`npm install @dcloudio/uni-ui`

---

## 二十七、代码生成器模板（AI强制遵循）

### 27.1 基类定义（所有实体必须继承）

```java
// 27.1.1 BaseEntity - 所有表的公共字段
package com.gongziyu.neop.entity.base;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

// 27.1.2 BaseController - 所有Controller的公共方法
package com.gongziyu.neop.controller.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.common.Result;
import java.util.Map;

public abstract class BaseController {
    
    /**
     * 统一分页参数处理
     */
    protected <T> IPage<T> getPage(Map<String, Object> params) {
        // 实现分页逻辑
    }
    
    /**
     * 统一成功返回
     */
    protected <T> Result<T> success(T data) {
        return Result.success(data);
    }
    
    /**
     * 统一失败返回
     */
    protected <T> Result<T> error(String msg) {
        return Result.error(500, msg);
    }
}
```

### 27.2 MyBatis-Plus代码生成器配置

```java
// 27.2.1 代码生成器（仅首次运行，生成28张表的Entity/Mapper/Service）
// 运行一次后即可删除此文件
@SpringBootTest
public class CodeGenerator {
    @Test
    public void generate() {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/neop_db?useUnicode=true&characterEncoding=utf8mb4", 
                "root", "password")
            .globalConfig(builder -> {
                builder.author("NEOP Team")
                       .outputDir("neop-backend/src/main/java")
                       .commentDate("yyyy-MM-dd")
                       .enableSwagger();
            })
            .packageConfig(builder -> {
                builder.parent("com.gongziyu.neop")
                       .entity("entity")
                       .mapper("mapper")
                       .service("service")
                       .serviceImpl("service.impl")
                       .controller("controller");
            })
            .strategyConfig(builder -> {
                builder.entityBuilder()
                       .enableLombok()
                       .enableTableFieldAnnotation()
                       .idType(IdType.AUTO)
                       .formatFileName("%s")
                       .superClass(BaseEntity.class)  // 继承BaseEntity
                       .addSuperEntityColumns("id", "deleted", "create_time", "update_time");
                builder.controllerBuilder()
                       .enableRestStyle()
                       .formatFileName("%sController")
                       .superClass(BaseController.class);  // 继承BaseController
            })
            .execute();
    }
}
```

### 27.3 实体类示例（任务配置表）

```java
// 27.3.1 TaskInfo.java - 继承BaseEntity，无需重复写id/delete/createTime/updateTime
package com.gongziyu.neop.entity;

import com.gongziyu.neop.entity.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_info")
public class TaskInfo extends BaseEntity {
    private String taskTitle;
    private String taskCover;
    private String taskIntro;
    private String taskContent;
    private BigDecimal rewardAmount;
    private Integer totalNum;
    private Integer dayNum;
    private Integer expireMinute;
    private Integer sort;
    private Integer status;
}
```

---

## 二十八、JWT Token生成与校验代码（AI强制遵循）

### 28.1 Token生成工具类

```java
// 28.1.1 JwtUtil.java - Token生成和解析
package com.gongziyu.neop.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "neop.jwt")
public class JwtUtil {
    private String secret;
    private Long mobileExpire;  // 移动端过期时间（7天）
    private Long adminExpire;   // 后台端过期时间（2小时）
    
    /**
     * 生成移动端Token
     */
    public String generateMobileToken(Long userId, String nickname, String inviteCode) {
        return Jwts.builder()
                .setClaims(Map.of(
                    "userId", userId,
                    "type", "mobile",
                    "nickname", nickname,
                    "inviteCode", inviteCode
                    // 注意：禁止放入敏感字段如password、openid
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + mobileExpire))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 生成后台端Token
     */
    public String generateAdminToken(Long adminId, String username, String nickname) {
        return Jwts.builder()
                .setClaims(Map.of(
                    "adminId", adminId,
                    "type", "admin",
                    "username", username,
                    "nickname", nickname
                    // 注意：禁止放入password字段
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + adminExpire))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 解析Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(401, "Token已过期");
        } catch (JwtException e) {
            throw new BusinessException(401, "Token非法");
        }
    }
    
    /**
     * 获取签名密钥
     */
    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

### 28.2 Token拦截器

```java
// 28.2.1 JwtInterceptor.java - Token校验拦截器
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 从Header中获取Token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token) || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或Token失效");
        }
        token = token.substring(7);
        
        // 2. 解析Token
        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String type = claims.get("type", String.class);
        
        // 3. 检查Redis黑名单
        String blacklistKey = "neop:token:blacklist:" + userId;
        if (redisTemplate.hasKey(blacklistKey)) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }
        
        // 4. 将用户信息存入请求上下文
        request.setAttribute("userId", userId);
        request.setAttribute("userType", type);
        
        return true;
    }
}
```

---

## 二十九、增删改查标准实现范式（AI强制遵循）

### 29.1 标准范式说明

**所有模块的增删改查必须严格遵循此范式**，以"任务管理"为例，后续所有模块（会员、电商、商品分类）**完全复用此模板**，只变更实体名和字段名。

### 29.2 完整代码示例（任务管理模块）

```java
// 29.2.1 Controller层 - 只做参数校验和结果返回
@RestController
@RequestMapping("/api/admin/task")
public class TaskController extends BaseController {
    @Autowired
    private TaskService taskService;
    
    /**
     * 分页列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('task:info:list')")
    public Result<IPage<TaskInfo>> list(TaskQueryDTO queryDTO) {
        IPage<TaskInfo> page = taskService.list(queryDTO);
        return success(page);
    }
    
    /**
     * 新增
     */
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('task:info:save')")
    public Result<Void> save(@Valid @RequestBody TaskSaveDTO saveDTO) {
        taskService.save(saveDTO);
        return success(null);
    }
    
    /**
     * 编辑
     */
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('task:info:update')")
    public Result<Void> update(@Valid @RequestBody TaskUpdateDTO updateDTO) {
        taskService.update(updateDTO);
        return success(null);
    }
    
    /**
     * 删除
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('task:info:delete')")
    public Result<Void> delete(@RequestParam Long id) {
        taskService.delete(id);
        return success(null);
    }
}

// 29.2.2 Service接口定义
public interface TaskService extends IService<TaskInfo> {
    IPage<TaskInfo> list(TaskQueryDTO queryDTO);
    void save(TaskSaveDTO saveDTO);
    void update(TaskUpdateDTO updateDTO);
    void delete(Long id);
}

// 29.2.3 ServiceImpl - 业务逻辑层
@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private RedissonClient redissonClient;
    
    @Override
    public IPage<TaskInfo> list(TaskQueryDTO queryDTO) {
        LambdaQueryWrapper<TaskInfo> wrapper = new LambdaQueryWrapper<>();
        // 条件查询
        if (StringUtils.isNotBlank(queryDTO.getTaskTitle())) {
            wrapper.like(TaskInfo::getTaskTitle, queryDTO.getTaskTitle());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(TaskInfo::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(TaskInfo::getSort, TaskInfo::getCreateTime);
        
        return taskInfoMapper.selectPage(queryDTO.getPage(), wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(TaskSaveDTO saveDTO) {
        // 1. DTO转Entity
        TaskInfo taskInfo = BeanUtil.copyProperties(saveDTO, TaskInfo.class);
        
        // 2. 数据校验（如：奖励金额不能为负）
        if (taskInfo.getRewardAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw BusinessException.of(400, "奖励金额不能为负数");
        }
        
        // 3. 保存
        taskInfoMapper.insert(taskInfo);
        
        // 4. 删除缓存（如果有）
        redissonClient.getBucket("neop:task:cache:" + taskInfo.getId()).delete();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TaskUpdateDTO updateDTO) {
        // 1. 查询是否存在
        TaskInfo taskInfo = taskInfoMapper.selectById(updateDTO.getId());
        if (taskInfo == null) {
            throw BusinessException.of(1001, "任务不存在");
        }
        
        // 2. 更新
        BeanUtil.copyProperties(updateDTO, taskInfo);
        taskInfoMapper.updateById(taskInfo);
        
        // 3. 删除缓存
        redissonClient.getBucket("neop:task:cache:" + taskInfo.getId()).delete();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 1. 逻辑删除（BaseEntity自动处理deleted字段）
        taskInfoMapper.deleteById(id);
        
        // 2. 删除缓存
        redissonClient.getBucket("neop:task:cache:" + id).delete();
    }
}

// 29.2.4 Mapper层 - 简单SQL用MyBatis-Plus，复杂SQL写XML
@Mapper
public interface TaskInfoMapper extends BaseMapper<TaskInfo> {
    // 如需复杂SQL，在此定义方法，然后在TaskInfoMapper.xml中写SQL
}
```

### 29.3 DTO定义规范

```java
// 29.3.1 查询DTO - 继承PageDTO
@Data
public class TaskQueryDTO extends PageDTO {
    private String taskTitle;
    private Integer status;
}

// 29.3.2 新增DTO - 使用@Valid校验
@Data
public class TaskSaveDTO {
    @NotBlank(message = "任务标题不能为空")
    @Length(max = 200, message = "任务标题最长200字")
    private String taskTitle;
    
    @NotNull(message = "奖励金额不能为空")
    @DecimalMin(value = "0.01", message = "奖励金额最小0.01")
    private BigDecimal rewardAmount;
    
    @NotNull(message = "总数量不能为空")
    @Min(value = 0, message = "总数量不能小于0")
    private Integer totalNum;
}

// 29.3.3 编辑DTO - 必须包含ID
@Data
public class TaskUpdateDTO extends TaskSaveDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
}

// 29.3.4 分页基类
@Data
public class PageDTO {
    private Integer current = 1;
    private Integer size = 10;
    
    public <T> IPage<T> getPage() {
        Page<T> page = new Page<>(current, size);
        return page;
    }
}
```

---

## 三十、接口测试与验证标准（AI强制遵循）

### 30.1 测试要求

**完成每个阶段的开发后，必须提供可导入Postman/ApiFox的JSON接口集合**，最少包含以下3个测试场景。

### 30.2 测试用例规范

```json
// 30.2.1 Postman测试集合示例（导出为JSON）
{
  "info": {
    "name": "NEOP API测试集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "任务管理",
      "item": [
        {
          "name": "1. 新增任务-正常流",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/admin/task/save",
            "header": [
              {"key": "Authorization", "value": "Bearer {{adminToken}}"},
              {"key": "Content-Type", "value": "application/json"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"taskTitle\": \"测试任务\", \"rewardAmount\": 5.00, \"totalNum\": 100, \"dayNum\": 10, \"expireMinute\": 10080}"
            }
          },
          "response": [
            {
              "name": "成功响应",
              "code": 200,
              "body": "{\"code\": 200, \"msg\": \"success\", \"data\": null}"
            }
          ]
        },
        {
          "name": "2. 新增任务-异常流（缺少必填项）",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/admin/task/save",
            "header": [
              {"key": "Authorization", "value": "Bearer {{adminToken}}"},
              {"key": "Content-Type", "value": "application/json"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"rewardAmount\": 5.00}"  // 缺少taskTitle
            }
          },
          "response": [
            {
              "name": "失败响应",
              "code": 400,
              "body": "{\"code\": 400, \"msg\": \"任务标题不能为空\", \"data\": null}"
            }
          ]
        },
        {
          "name": "3. 新增任务-边界流（重复提交）",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/admin/task/save",
            "header": [
              {"key": "Authorization", "value": "Bearer {{adminToken}}"},
              {"key": "Content-Type", "value": "application/json"}
            ],
            "body": {
              "mode": "raw",
              "raw": "{\"taskTitle\": \"测试任务\", \"rewardAmount\": 5.00}"
            }
          },
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "// 连续发送2次请求，第二次应该返回429或业务错误",
                  "pm.test('重复提交测试', function() {",
                  "    pm.expect(pm.response.code).to.be.oneOf([200, 429]);",
                  "});"
                ]
              }
            }
          ]
        }
      ]
    }
  ]
}
```

### 30.3 必须测试的3个场景

| 测试场景 | 说明 | 期望结果 |
|----------|------|----------|
| **正常流** | 请求参数正确，Token有效 | 返回code=200，业务数据正确 |
| **异常流** | 参数缺失/格式错误/Token过期 | 返回对应错误码（400/401/403/500） |
| **边界流** | 重复提交/并发请求/数据边界值 | 返回429或业务错误码，数据一致性正确 |

### 30.4 压力测试脚本示例（JMeter）

```xml
<!-- 30.4.1 任务领取接口压力测试 -->
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <TestPlan testname="任务领取接口压力测试">
    <ThreadGroup testname="并发用户组" enabled="true">
      <elementProp name="ThreadGroup.arguments">
        <Arguments>
          <Argument name="线程数" value="100"/>
          <Argument name=" ramp-up时间(秒)" value="10"/>
          <Argument name="循环次数" value="10"/>
        </Arguments>
      </elementProp>
    </ThreadGroup>
    <!-- HTTP请求默认值 -->
    <!-- HTTP Header管理器 -->
    <!-- 任务领取请求 -->
    <!-- 响应断言 -->
  </TestPlan>
</jmeterTestPlan>
```

---

## 三十一、UniApp移动端固定结构（AI强制遵循）

### 31.1 页面固定结构模板

**所有UniApp页面必须严格按照此模板生成**，确保结构一致、易于维护。

```javascript
// 31.1.1 标准页面模板（taskList.vue）
<template>
  <view class="page-container">
    <!-- 加载状态 -->
    <uni-load-more v-if="loadingStatus === 'loading'" status="loading"></uni-load-more>
    
    <!-- 空状态 -->
    <view v-if="loadingStatus === 'empty'" class="empty-state">
      <image src="/static/empty.png" mode="aspectFit" style="width: 300rpx; height: 300rpx;"></image>
      <text class="empty-text">暂无数据</text>
    </view>
    
    <!-- 数据列表 -->
    <view v-if="loadingStatus === 'success'" class="data-list">
      <view v-for="(item, index) in dataList" :key="item.id" class="list-item">
        <image :src="item.taskCover" mode="aspectFill" style="width: 200rpx; height: 150rpx;"></image>
        <view class="item-info">
          <text class="item-title">{{ item.taskTitle }}</text>
          <text class="item-reward">奖励：¥{{ item.rewardAmount }}</text>
        </view>
      </view>
    </view>
    
    <!-- 上拉加载更多 -->
    <uni-load-more v-if="loadingStatus === 'success'" :status="loadMoreStatus"></uni-load-more>
  </view>
</template>

<script>
export default {
  data() {
    return {
      loadingStatus: 'loading',  // 加载状态：loading/ success/ empty/ error
      loadMoreStatus: 'more',    // 上拉加载状态：more/ loading/ noMore
      dataList: [],
      queryParams: {
        current: 1,
        size: 10
      },
      total: 0
    };
  },
  
  onLoad(options) {
    // 必须处理参数接收和错误兜底
    try {
      if (options.taskId) {
        this.taskId = options.taskId;
      }
    } catch (error) {
      console.error('页面参数解析失败', error);
      uni.showToast({ title: '页面参数错误', icon: 'none' });
    }
    
    this.fetchData();
  },
  
  onPullDownRefresh() {
    // 下拉刷新
    this.queryParams.current = 1;
    this.dataList = [];
    this.fetchData();
  },
  
  onReachBottom() {
    // 上拉加载更多
    if (this.loadMoreStatus === 'noMore') {
      return;
    }
    this.queryParams.current++;
    this.fetchData();
  },
  
  methods: {
    async fetchData() {
      try {
        this.loadingStatus = this.dataList.length === 0 ? 'loading' : 'success';
        this.loadMoreStatus = 'loading';
        
        const res = await this.$request({
          url: '/api/task/list',
          method: 'GET',
          data: this.queryParams
        });
        
        if (res.code === 200) {
          const newData = res.data.records || [];
          this.total = res.data.total;
          
          if (this.queryParams.current === 1) {
            this.dataList = newData;
          } else {
            this.dataList = this.dataList.concat(newData);
          }
          
          this.loadingStatus = this.dataList.length === 0 ? 'empty' : 'success';
          this.loadMoreStatus = this.dataList.length >= this.total ? 'noMore' : 'more';
        } else {
          throw new Error(res.msg || '请求失败');
        }
      } catch (error) {
        console.error('获取数据失败', error);
        this.loadingStatus = 'error';
        uni.showToast({ title: error.message || '网络错误', icon: 'none' });
      } finally {
        uni.stopPullDownRefresh();
      }
    }
  }
};
</script>

<style scoped>
.page-container {
  padding: 20rpx;
}

.list-item {
  display: flex;
  padding: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.item-info {
  flex: 1;
  margin-left: 20rpx;
}

.item-title {
  font-size: 32rpx;
  font-weight: bold;
}

.item-reward {
  font-size: 28rpx;
  color: #f56c6c;
  margin-top: 10rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
  margin-top: 20rpx;
}
</style>
```

### 31.2 统一请求工具封装

```javascript
// 31.2.1 utils/request.js - 统一请求拦截器
const baseUrl = 'https://neop.gongziyu.com/api';

const request = (options) => {
  return new Promise((resolve, reject) => {
    // 1. 自动携带Token
    let header = {
      'Content-Type': 'application/json',
      ...options.header
    };
    
    const token = uni.getStorageSync('token');
    if (token) {
      header['Authorization'] = 'Bearer ' + token;
    }
    
    // 2. 请求超时设置
    const timeout = options.timeout || 30000;
    
    // 3. 发起请求
    uni.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      timeout: timeout,
      success: (res) => {
        // 4. 统一响应处理
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            resolve(res.data);
          } else if (res.data.code === 401) {
            // Token过期，跳转登录
            uni.removeStorageSync('token');
            uni.navigateTo({ url: '/pages/common/login' });
            reject(new Error(res.data.msg));
          } else if (res.data.code === 429) {
            uni.showToast({ title: '操作过于频繁', icon: 'none' });
            reject(new Error(res.data.msg));
          } else {
            uni.showToast({ title: res.data.msg || '请求失败', icon: 'none' });
            reject(new Error(res.data.msg));
          }
        } else {
          uni.showToast({ title: '服务器错误', icon: 'none' });
          reject(new Error('服务器错误'));
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误', icon: 'none' });
        reject(err);
      }
    });
  });
};

export default request;
```

### 31.3 图片使用规范

```vue
<!-- 31.3.1 所有图片必须使用<image>标签并设置固定宽高比占位 -->
<!-- 禁止：使用<img>标签（UniApp不支持） -->
<!-- 禁止：不设置宽高导致页面跳动 -->

<!-- ✅ 正确示例 -->
<image 
  :src="item.taskCover" 
  mode="aspectFill" 
  style="width: 200rpx; height: 150rpx;"
  lazy-load="true"
></image>

<!-- ❌ 错误示例 -->
<img :src="item.taskCover" />  <!-- UniApp不支持 -->
<image :src="item.taskCover"></image>  <!-- 未设置宽高，页面会跳动 -->
```

---

## 三十二、Flyway增量脚本严格规范（AI强制遵循）

### 32.1 核心规则

**在开发过程中，严禁修改已存在的Flyway SQL文件**。任何表结构或数据的变更，都必须新建一个增量脚本文件。

### 32.2 命名规范

| 操作类型 | 文件名格式 | 示例 |
|----------|----------|------|
| 初始建表 | `V{大版本}__init_schema.sql` | `V1.0.0__init_schema.sql` |
| 新增字段 | `V{版本}__add_{表名}_{字段名}.sql` | `V1.0.1__add_task_info_reward_point.sql` |
| 修改字段 | `V{版本}__modify_{表名}_{字段名}.sql` | `V1.0.2__modify_product_price_type.sql` |
| 新增索引 | `V{版本}__add_index_{表名}_{索引名}.sql` | `V1.0.3__add_index_user_phone.sql` |
| 数据初始化 | `V{版本}__init_{表名}_data.sql` | `V1.0.4__init_sys_config_data.sql` |

### 32.3 版本号规则

```text
主版本.次版本.修订号
  │       │       │
  │       │       └─ 修订号：bug修复、字段调整（AI开发每个阶段+1）
  │       └───────── 次版本：新增功能模块（每完成一个模块+1）
  └───────────────── 主版本：重大架构调整（项目完成+1）
```

### 32.4 增量脚本示例

```sql
-- 32.4.1 示例：新增字段（V1.0.1__add_task_info_reward_point.sql）
-- 描述：任务配置表新增奖励积分字段
-- 日期：2026-04-26
-- 作者：NEOP Team

-- 1. 新增字段
ALTER TABLE task_info 
ADD COLUMN reward_point INT NOT NULL DEFAULT 0 COMMENT '完成任务奖励积分' 
AFTER reward_amount;

-- 2. 添加索引（如果需要）
-- CREATE INDEX idx_reward_point ON task_info(reward_point);

-- 3. 数据迁移（如果需要）
-- UPDATE task_info SET reward_point = 10 WHERE reward_amount > 0;
```

```sql
-- 32.4.2 示例：修改字段类型（V1.0.2__modify_product_price_type.sql）
-- 描述：商品表价格字段从DECIMAL(10,2)改为DECIMAL(12,2)
-- 日期：2026-04-27

-- 1. 修改字段类型
ALTER TABLE product 
MODIFY COLUMN price DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '售价';

ALTER TABLE product 
MODIFY COLUMN original_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '原价';

-- 2. 验证修改结果
-- SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'product' AND COLUMN_NAME = 'price';
```

### 32.5 Flyway校验机制

```yaml
# 32.5.1 启用Flyway校验（application.yml）
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 1.0.0
    validate-on-migrate: true  # 必须！防止已执行的SQL被修改
    clean-disabled: true       # 必须！防止误删数据库
```

### 32.6 AI开发流程规范

```text
AI开发每个模块时的标准流程：

1. 创建功能分支：git checkout -b feature/任务模块
2. 编写代码：生成Entity/Service/Controller
3. 如需要修改数据库：
   a. 创建新的Flyway增量脚本（V1.0.1__xxx.sql）
   b. 绝不修改已有的Flyway脚本
4. 本地测试：运行项目，Flyway自动执行增量脚本
5. 编写测试用例：Postman集合 + 单元测试
6. 提交代码：git commit -m "feat: 新增任务管理模块"
7. 合并到开发分支：git checkout develop && git merge feature/任务模块
```

---

> （注：文档部分内容可能由 AI 生成）
