# NEOP 商用任务营销+电商会员一体化系统 - Code Wiki

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈](#2-技术栈)
3. [架构设计](#3-架构设计)
4. [模块职责](#4-模块职责)
5. [关键类与函数说明](#5-关键类与函数说明)
6. [数据库设计](#6-数据库设计)
7. [API接口设计](#7-api接口设计)
8. [依赖关系](#8-依赖关系)
9. [项目运行方式](#9-项目运行方式)
10. [定时任务](#10-定时任务)
11. [安全与权限](#11-安全与权限)

---

## 1. 项目概述

**NEOP** 是一套商用级别的任务营销+电商会员一体化系统，旨在为企业提供完整的移动端营销解决方案。

**核心业务能力：**
- 任务分发与审核打款
- 电商商城交易
- 会员积分体系
- 邀请裂变营销
- 数据统计分析

**项目结构：**
```
neop/
├── neop-backend/       # Java后端服务 (Spring Boot)
├── neop-admin-web/     # Vue3后台管理系统
└── neop-uniapp/        # UniApp移动端应用
```

---

## 2. 技术栈

### 2.1 后端技术栈

| 分类 | 技术 | 版本 |
| :--- | :--- | :--- |
| 语言 | Java | 21 |
| 框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis Plus | 3.5.7 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.0+ |
| 分布式锁 | Redisson | 3.27.0 |
| 微信支付 | weixin-java-pay | 4.6.0 |
| JWT | jjwt | 0.11.5 |
| 工具库 | Hutool | 5.8.25 |

### 2.2 前端技术栈

| 分类 | 技术 | 版本 |
| :--- | :--- | :--- |
| 后台框架 | Vue | 3.4.21 |
| 后台UI | Element Plus | 2.7.0 |
| 路由 | Vue Router | 4.3.0 |
| 状态管理 | Pinia | 2.1.7 |
| 构建工具 | Vite | 5.1.6 |
| 移动端 | UniApp | - |

---

## 3. 架构设计

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      客户端层                                   │
│   ┌─────────────────┐    ┌─────────────────────────────────┐   │
│   │  UniApp 移动端   │    │         Vue3 后台管理            │   │
│   └────────┬────────┘    └──────────────┬──────────────────┘   │
└────────────┼────────────────────────────┼───────────────────────┘
             │                            │
             ▼                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API网关层                                  │
│              Spring Boot Controller                             │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      业务逻辑层                                  │
│   Service层 ──→ 业务处理 ──→ Mapper层 ──→ 数据库                │
└────────────────────────────┬────────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                      数据存储层                                  │
│   MySQL (业务数据)    │    Redis (缓存/锁)    │    Flyway (迁移) │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 模块划分

| 模块 | 目录 | 职责说明 |
| :--- | :--- | :--- |
| 系统权限 | `controller/sys/` | 管理员、角色、菜单、权限管理 |
| 用户管理 | `controller/user/` | 用户信息、地址管理 |
| 营销管理 | `controller/marketing/` | 会员套餐、积分、邀请 |
| 电商交易 | `controller/trade/` | 商品、购物车、订单 |
| 任务打款 | `controller/task/` | 任务配置、审核、打款 |
| 数据统计 | `controller/stat/` | 数据看板、统计报表 |
| 微信回调 | `controller/wechat/` | 支付回调处理 |

### 3.3 核心流程图

#### 3.3.1 用户登录流程

```
用户 → 微信授权 → 获取code → 后端换取openid → 查询/创建用户 → 签发JWT → 返回Token
```

#### 3.3.2 任务领取流程

```
用户 → 领取任务 → 库存检查(Redis) → 创建领取记录 → 更新库存 → 返回成功
```

#### 3.3.3 订单处理流程

```
创建订单 → 生成支付订单 → 微信支付 → 支付回调 → 更新订单状态 → 扣减库存
```

---

## 4. 模块职责

### 4.1 系统权限模块

**职责：** 后台管理员权限管理、菜单管理、角色管理

**核心文件：**
- `SysAdminController.java` - 管理员增删改查
- `SysRoleController.java` - 角色管理
- `SysMenuController.java` - 菜单管理
- `SysDictController.java` - 数据字典
- `SysConfigController.java` - 系统配置

### 4.2 用户基础模块

**职责：** 用户信息管理、微信绑定、收货地址

**核心文件：**
- `UserController.java` - 用户信息接口
- `UserAddressController.java` - 收货地址管理
- `UserService.java` / `UserServiceImpl.java` - 用户业务逻辑

### 4.3 营销增值模块

**职责：** 会员套餐、积分体系、邀请奖励

**核心文件：**
- `MemberPackageController.java` - 会员套餐管理
- `PointService.java` - 积分服务
- `InviteCodeUtil.java` - 邀请码生成

### 4.4 电商交易模块

**职责：** 商品管理、购物车、订单处理

**核心文件：**
- `ShopController.java` - 商品、购物车、订单接口
- `ShopService.java` - 电商业务逻辑

### 4.5 任务打款模块

**职责：** 任务配置、任务领取、审核、打款

**核心文件：**
- `TaskController.java` - 任务相关接口
- `TaskService.java` / `TaskServiceImpl.java` - 任务业务逻辑
- `WechatPayService.java` - 微信企业付款

### 4.6 数据统计模块

**职责：** 每日数据统计、数据看板

**核心文件：**
- `DataStatisticsController.java` - 统计数据接口
- `DataStatisticsService.java` - 统计服务

---

## 5. 关键类与函数说明

### 5.1 工具类

#### 5.1.1 JwtUtil

**位置：** `util/JwtUtil.java`

**功能：** JWT Token生成和解析工具

**核心方法：**

| 方法名 | 功能说明 | 参数 | 返回值 |
| :--- | :--- | :--- | :--- |
| `generateMobileToken` | 生成移动端Token | `userId`, `nickname`, `inviteCode` | String (Token) |
| `generateAdminToken` | 生成后台端Token | `adminId`, `username`, `nickname` | String (Token) |
| `parseToken` | 解析Token | `token` | Claims |
| `getTokenRemainingTime` | 获取Token剩余有效期 | `token` | long (毫秒) |

#### 5.1.2 InviteCodeUtil

**位置：** `util/InviteCodeUtil.java`

**功能：** 邀请码生成工具

#### 5.1.3 OrderNoUtil

**位置：** `util/OrderNoUtil.java`

**功能：** 订单号生成工具

#### 5.1.4 AesEncryptUtil

**位置：** `util/AesEncryptUtil.java`

**功能：** AES加密解密工具

### 5.2 配置类

#### 5.2.1 JwtInterceptor

**位置：** `config/JwtInterceptor.java`

**功能：** JWT Token校验拦截器

**核心逻辑：**
1. 拦截请求，提取Authorization Header中的Token
2. 解析Token验证合法性
3. 检查Redis黑名单
4. 将用户信息存入请求上下文

#### 5.2.2 OperationLogAspect

**位置：** `config/OperationLogAspect.java`

**功能：** 操作日志切面，自动记录管理员操作

#### 5.2.3 RateLimitAspect

**位置：** `config/RateLimitAspect.java`

**功能：** 限流切面，防止接口被恶意调用

### 5.3 服务类

#### 5.3.1 UserService

**位置：** `service/UserService.java`

**核心方法：**

| 方法名 | 功能说明 |
| :--- | :--- |
| `wechatLogin` | 微信登录（自动注册） |
| `getUserInfo` | 获取用户个人信息 |
| `updateUser` | 更新用户信息 |
| `bindPhone` | 绑定手机号 |
| `inviteInfo` | 获取邀请信息 |

#### 5.3.2 TaskService

**位置：** `service/TaskService.java`

**核心方法：**

| 方法名 | 功能说明 |
| :--- | :--- |
| `frontList` | 前台任务列表 |
| `receiveTask` | 领取任务 |
| `submitTask` | 提交任务 |
| `auditPass` | 审核通过 |
| `auditReject` | 审核驳回 |
| `grantPay` | 授权打款 |

#### 5.3.3 WechatPayService

**位置：** `service/WechatPayService.java`

**功能：** 微信支付相关服务（支付下单、企业付款）

### 5.4 实体类

#### 5.4.1 UserInfo

**位置：** `entity/UserInfo.java`

**字段说明：**

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 用户ID |
| `nickname` | String | 用户昵称 |
| `avatar` | String | 头像地址 |
| `phone` | String | 手机号 |
| `sex` | Integer | 性别 |
| `status` | Integer | 状态 |
| `inviteCode` | String | 邀请码 |
| `parentId` | Long | 上级用户ID |

#### 5.4.2 TaskInfo

**位置：** `entity/TaskInfo.java`

**字段说明：**

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 任务ID |
| `taskTitle` | String | 任务标题 |
| `taskCover` | String | 任务封面图 |
| `taskIntro` | String | 任务简介 |
| `taskContent` | String | 任务详情 |
| `rewardAmount` | BigDecimal | 奖励金额 |
| `totalNum` | Integer | 任务总数量 |
| `dayNum` | Integer | 每日限领数量 |
| `expireMinute` | Integer | 有效时长(分钟) |
| `status` | Integer | 状态 |

#### 5.4.3 Order

**位置：** `entity/Order.java`

**字段说明：**

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long | 订单ID |
| `orderNo` | String | 订单编号 |
| `userId` | Long | 用户ID |
| `totalAmount` | BigDecimal | 订单总金额 |
| `payAmount` | BigDecimal | 实付金额 |
| `status` | Integer | 订单状态 |
| `addressId` | Long | 收货地址ID |

### 5.5 公共类

#### 5.5.1 Result

**位置：** `common/Result.java`

**功能：** 全局统一返回结构

```java
public class Result<T> {
    private Integer code;   // 状态码
    private String msg;     // 消息
    private T data;         // 数据
    
    public static <T> Result<T> success(T data)
    public static <T> Result<T> success()
    public static <T> Result<T> error(Integer code, String msg)
    public static <T> Result<T> error(String msg)
}
```

#### 5.5.2 Constants

**位置：** `common/Constants.java`

**功能：** 全局常量定义

**主要常量：**

| 常量名 | 值 | 说明 |
| :--- | :--- | :--- |
| `TOKEN_BLACKLIST_KEY` | `neop:token:blacklist:` | Token黑名单前缀 |
| `TASK_STOCK_KEY` | `neop:task:stock:` | 任务库存前缀 |
| `SIGN_KEY` | `neop:sign:userId:` | 签到标记前缀 |
| `INVITE_REWARD_POINT` | 10 | 邀请奖励积分 |
| `SIGN_REWARD_POINT` | 10 | 签到奖励积分 |
| `ORDER_AUTO_CANCEL_MINUTE` | 15 | 订单自动取消时间(分钟) |

---

## 6. 数据库设计

### 6.1 数据表总览

| 模块 | 表名 | 说明 |
| :--- | :--- | :--- |
| 系统权限 | `sys_menu` | 系统菜单表 |
| | `sys_role` | 系统角色表 |
| | `sys_role_menu` | 角色菜单关联表 |
| | `sys_admin` | 后台管理员表 |
| | `sys_admin_role` | 管理员角色关联表 |
| | `sys_dict` | 数据字典表 |
| | `sys_dict_data` | 字典数据表 |
| | `sys_config` | 系统全局配置表 |
| | `sys_operation_log` | 系统操作日志表 |
| 用户基础 | `user_info` | 用户主表 |
| | `user_wechat` | 用户微信绑定表 |
| | `user_address` | 用户收货地址表 |
| 营销增值 | `member_package` | 会员充值套餐表 |
| | `member_user` | 用户会员信息表 |
| | `point_user` | 用户积分表 |
| | `point_log` | 积分流水记录表 |
| | `invite_user` | 用户邀请关系表 |
| | `invite_reward_log` | 邀请奖励记录表 |
| 电商交易 | `product_category` | 商品分类表 |
| | `product` | 商品表 |
| | `cart` | 购物车表 |
| | `pay_order` | 充值支付订单表 |
| | `order` | 订单主表 |
| | `order_product` | 订单明细表 |
| 任务打款 | `task_info` | 任务配置表 |
| | `task_user_receive` | 用户任务领取记录表 |
| | `task_submit` | 任务提交工单表 |
| | `task_pay_log` | 任务打款日志表 |
| 数据统计 | `data_statistics` | 每日数据统计表 |

### 6.2 核心表关系

```
user_info ───┬──→ user_wechat (微信绑定)
             ├──→ user_address (收货地址)
             ├──→ member_user (会员信息)
             ├──→ point_user (积分账户)
             ├──→ invite_user (邀请关系)
             └──→ task_user_receive (任务领取)

task_info ───→ task_user_receive ───→ task_submit ───→ task_pay_log

product ───→ cart ───→ order ───→ order_product
```

### 6.3 初始化数据

系统初始化包含：
- 默认管理员账号：`admin` / `admin123`
- 超级管理员角色
- 基础菜单数据
- 系统字典配置

---

## 7. API接口设计

### 7.1 接口分类

| 模块 | 基础路径 | 说明 |
| :--- | :--- | :--- |
| 认证 | `/api/auth` | 登录、注册、验证码 |
| 用户 | `/api/user` | 用户信息、地址 |
| 营销 | `/api/marketing` | 会员、积分 |
| 任务 | `/api/task` | 任务领取、审核、打款 |
| 商城 | `/api/shop` | 商品、购物车、订单 |
| 后台 | `/api/sys` | 系统管理 |

### 7.2 认证接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/auth/login` | POST | 手机号+密码登录 |
| `/api/auth/register` | POST | 用户注册 |
| `/api/auth/send-code` | POST | 发送短信验证码 |
| `/api/user/wechat-login` | POST | 微信登录 |

### 7.3 用户接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/user/info` | GET | 获取用户信息 |
| `/api/user/update` | POST | 更新用户信息 |
| `/api/user/bind-phone` | POST | 绑定手机号 |
| `/api/user/invite-info` | GET | 获取邀请信息 |

### 7.4 任务接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/task/list` | GET | 任务列表 |
| `/api/task/info/{id}` | GET | 任务详情 |
| `/api/task/receive` | POST | 领取任务 |
| `/api/task/submit` | POST | 提交任务 |
| `/api/task/my` | GET | 我的任务 |

### 7.5 商城接口

| 接口 | 方法 | 说明 |
| :--- | :--- | :--- |
| `/api/shop/category` | GET | 商品分类 |
| `/api/shop/product` | GET | 商品列表 |
| `/api/shop/product/{id}` | GET | 商品详情 |
| `/api/shop/cart` | GET | 购物车列表 |
| `/api/shop/cart/add` | POST | 添加购物车 |
| `/api/shop/order/create` | POST | 创建订单 |

---

## 8. 依赖关系

### 8.1 Maven依赖树

```
neop-backend
├── spring-boot-starter-web           # Web框架
├── spring-boot-starter-validation    # 参数校验
├── spring-boot-starter-aop           # AOP支持
├── spring-boot-starter-actuator      # 健康检查
├── mybatis-plus-boot-starter         # ORM框架
├── mysql-connector-j                 # MySQL驱动
├── flyway-core                       # 数据库迁移
├── flyway-mysql                      # MySQL支持
├── spring-boot-starter-data-redis    # Redis支持
├── redisson-spring-boot-starter      # 分布式锁
├── weixin-java-pay                   # 微信支付
├── hutool-all                        # 工具库
├── lombok                            # 简化代码
├── spring-security-crypto            # 加密工具
├── fastjson2                         # JSON处理
├── jjwt-api/impl/jackson             # JWT支持
├── jsoup                             # HTML解析(XSS防护)
└── cos_api-bundle                    # 腾讯云COS
```

### 8.2 前端依赖

```json
{
  "vue": "^3.4.21",
  "vue-router": "^4.3.0",
  "pinia": "^2.1.7",
  "axios": "^1.6.7",
  "element-plus": "^2.7.0",
  "@element-plus/icons-vue": "^2.3.1",
  "dayjs": "^1.11.10",
  "qs": "^6.12.0"
}
```

---

## 9. 项目运行方式

### 9.1 环境要求

| 依赖 | 版本 |
| :--- | :--- |
| JDK | 21+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| Node.js | 18+ |

### 9.2 后端启动

**步骤1：配置数据库连接**

编辑 `neop-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/neop_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: 127.0.0.1
    port: 6379
```

**步骤2：启动应用**

```bash
cd neop-backend
mvn spring-boot:run
```

**访问地址：** http://localhost:8080

### 9.3 后台管理启动

```bash
cd neop-admin-web
npm install
npm run dev
```

**访问地址：** http://localhost:5173

### 9.4 移动端运行

```bash
cd neop-uniapp
npm install

# H5预览
npm run dev:h5

# 微信小程序
npm run dev:mp-weixin
```

### 9.5 初始化账号

- **后台管理员：** admin / admin123

---

## 10. 定时任务

系统包含6个定时任务，定义于 `ScheduledTasks.java`：

| 任务名称 | Cron表达式 | 功能说明 |
| :--- | :--- | :--- |
| 任务过期扫描 | `0 */30 * * * ?` | 扫描过期任务，更新状态为5(已过期) |
| 支付订单超时关闭 | `0 */5 * * * ?` | 关闭15分钟未支付的订单 |
| 每日数据统计 | `0 0 0 * * ?` | 每日凌晨统计前一日数据 |
| 会员过期扫描 | `0 0 1 * * ?` | 扫描过期会员，取消会员资格 |
| 打款失败自动重试 | `0 */10 * * * ?` | 重试失败的打款记录(最多3次) |
| 订单自动确认收货 | `0 0 2 * * ?` | 自动确认7天前发货的订单 |

---

## 11. 安全与权限

### 11.1 JWT认证

- Token存储：`Authorization: Bearer xxx`
- Token有效期：移动端7天，后台端2小时
- 黑名单机制：登出时加入Redis黑名单

### 11.2 权限控制

- 基于角色的访问控制(RBAC)
- 菜单权限配置
- 按钮级权限控制

### 11.3 限流机制

- 使用Redis实现接口限流
- 默认配置：每个IP每分钟最多请求100次

### 11.4 安全特性

- 密码使用BCrypt加密
- SQL注入防护(MyBatis Plus)
- XSS攻击防护(jsoup)
- 请求参数校验(@Valid)
- 异常统一处理

---

## 附录：目录结构

```
neop-backend/src/main/java/com/gongziyu/neop/
├── NeopApplication.java          # 启动类
├── annotation/                   # 自定义注解
│   ├── OperationLog.java
│   └── RateLimit.java
├── common/                       # 公共类
│   ├── Constants.java
│   ├── PageDTO.java
│   ├── PageResult.java
│   └── Result.java
├── config/                       # 配置类
│   ├── CorsConfig.java
│   ├── JwtInterceptor.java
│   ├── MybatisPlusConfig.java
│   ├── OperationLogAspect.java
│   ├── RateLimitAspect.java
│   ├── RedisConfig.java
│   ├── RedissonConfig.java
│   └── WebMvcConfig.java
├── controller/                   # 控制器
│   ├── AuthController.java
│   ├── MessageController.java
│   ├── UploadController.java
│   ├── marketing/
│   ├── stat/
│   ├── sys/
│   ├── task/
│   ├── trade/
│   ├── user/
│   └── wechat/
├── entity/                       # 实体类
│   ├── base/BaseEntity.java
│   └── *.java
├── exception/                    # 异常处理
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── LockAcquisitionException.java
├── mapper/                       # Mapper接口
│   └── *.java
├── service/                      # 服务层
│   ├── *.java
│   └── impl/
├── task/                         # 定时任务
│   └── ScheduledTasks.java
└── util/                         # 工具类
    ├── AesEncryptUtil.java
    ├── FileUploadUtil.java
    ├── InviteCodeUtil.java
    ├── JwtUtil.java
    └── OrderNoUtil.java
```

---

**文档版本：** v1.0.0  
**生成日期：** 2026-05-05  
**项目地址：** https://github.com/gongziyu/neop