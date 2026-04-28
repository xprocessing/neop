# Neop - 全栈任务积分电商平台

<div align="center">

![Neop Logo](https://img.shields.io/badge/Neop-任务积分电商-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)
![Vue3](https://img.shields.io/badge/Vue-3.4.0-green)
![UniApp](https://img.shields.io/badge/UniApp-3.0.0-orange)

**一个基于 Spring Boot + Vue3 + UniApp 的全栈任务积分电商平台**

[在线演示](#) · [开发文档](./neop开发文档.md) · [问题反馈](#)

</div>

---

## 📖 项目简介

Neop 是一个完整的任务积分电商平台，集成了**任务系统**、**积分商城**、**分销体系**、**内容社区**等核心功能。项目采用前后端分离架构，提供完整的移动端（UniApp）和后台管理端（Vue3）解决方案。

### ✨ 核心特性

- 🎯 **任务系统**：多种任务类型，自动审核，积分奖励
- 🛒 **积分商城**：商品管理，订单流程，支付集成
- 👥 **分销体系**：三级分销，邀请奖励，团队管理
- 📝 **内容社区**：文章发布，评论互动，点赞收藏
- 📊 **数据看板**：可视化统计，运营分析
- 🔐 **权限管理**：RBAC 权限模型，细粒度控制
- 📱 **多端适配**：微信小程序、H5、App 多端发布

---

## 🏗️ 项目架构

```
Neop 平台
├── neop-backend (后端服务)
│   ├── Spring Boot 3.2.0
│   ├── MySQL 8.0 + Redis
│   ├── Flyway 数据库版本管理
│   └── 28 张核心业务表
│
├── neop-admin-web (后台管理)
│   ├── Vue 3.4 + Element Plus
│   ├── Vite 5 构建工具
│   ├── 20 个功能页面
│   └── 完整的权限管理系统
│
└── neop-uniapp (移动端)
    ├── UniApp 3.0 跨端框架
    ├── Vue 3 + Pinia 状态管理
    ├── 27 个功能页面
    └── 支持微信小程序/H5/App
```

---

## 🛠️ 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| Spring Security | 6.2.0 | 安全框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7.0+ | 缓存数据库 |
| Flyway | 9.22.3 | 数据库版本管理 |
| JWT | 0.12.3 | 令牌认证 |
| Swagger/Knife4j | 4.3.0 | API 文档 |
| WxJava | 4.6.0 | 微信 SDK |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式框架 |
| Vite | 5.0.0 | 构建工具 |
| Element Plus | 2.5.0 | UI 组件库 |
| Pinia | 2.1.0 | 状态管理 |
| Vue Router | 4.2.0 | 路由管理 |
| Axios | 1.6.0 | HTTP 客户端 |

### 移动端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| UniApp | 3.0.0 | 跨端框架 |
| Vue | 3.3.0 | 渐进式框架 |
| Pinia | 2.1.0 | 状态管理 |
| Uni UI | 1.4.0 | UI 组件库 |
| William-Zhang | 1.3.3 | 扩展组件库 |

---

## 📁 目录结构

```
neop/
├── neop-backend/              # 后端服务
│   ├── src/main/java/com/neop/
│   │   ├── common/          # 公共模块
│   │   ├── config/          # 配置类
│   │   ├── modules/         # 业务模块
│   │   │   ├── user/       # 用户模块
│   │   │   ├── task/       # 任务模块
│   │   │   ├── product/    # 商品模块
│   │   │   ├── order/      # 订单模块
│   │   │   ├── payment/    # 支付模块
│   │   │   ├── article/    # 文章模块
│   │   │   ├── qa/         # 问答模块
│   │   │   └── system/     # 系统模块
│   │   └── NeopApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml  # 主配置
│   │   ├── db/migration/    # Flyway 脚本
│   │   └── mapper/          # MyBatis XML
│   └── pom.xml
│
├── neop-admin-web/           # Vue3 后台管理
│   ├── src/
│   │   ├── api/             # API 接口
│   │   ├── components/      # 公共组件
│   │   ├── layout/          # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面组件
│   │       ├── dashboard/   # 数据看板
│   │       ├── user/        # 用户管理
│   │       ├── task/        # 任务管理
│   │       ├── product/     # 商品管理
│   │       ├── order/       # 订单管理
│   │       ├── payment/     # 支付管理
│   │       ├── content/     # 内容管理
│   │       ├── distribution/# 分销管理
│   │       ├── system/      # 系统管理
│   │       └── profile/     # 个人中心
│   ├── public/
│   └── package.json
│
├── neop-uniapp/              # UniApp 移动端
│   ├── pages/
│   │   ├── index/          # 首页
│   │   ├── task/           # 任务页面
│   │   ├── shop/           # 商城页面
│   │   ├── article/        # 文章页面
│   │   ├── qa/             # 问答页面
│   │   ├── user/           # 用户页面
│   │   ├── auth/           # 认证页面
│   │   ├── search/         # 搜索页面
│   │   ├── message/        # 消息页面
│   │   ├── about/          # 关于页面
│   │   └── help/           # 帮助页面
│   ├── components/         # 组件
│   ├── store/              # 状态管理
│   ├── utils/              # 工具函数
│   ├── static/             # 静态资源
│   └── pages.json          # 路由配置
│
├── dev-log/                  # 开发日志
│   ├── 2026-04-21_需求分析与技术选型.md
│   ├── 2026-04-22_数据库设计与后端搭建.md
│   ├── 2026-04-23_Vue3后台管理前端开发.md
│   ├── 2026-04-24_UniApp移动端开发.md
│   ├── 2026-04-25_核心业务功能实现.md
│   ├── 2026-04-26_系统优化与测试.md
│   ├── 2026-04-27_部署准备与文档完善.md
│   └── 2026-04-28_额外页面创建完成.md
│
└── neop开发文档.md           # 完整开发文档
```

---

## 🚀 快速开始

### 环境要求

- **JDK**: 17+
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **Maven**: 3.8+
- **HBuilderX**: 3.8+ (UniApp 开发)

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/neop.git
cd neop
```

### 2. 后端启动

#### 2.1 创建数据库

```sql
CREATE DATABASE neop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 配置数据库连接

编辑 `neop-backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/neop?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

#### 2.3 启动后端服务

```bash
cd neop-backend
mvn spring-boot:run
```

后端启动后访问：http://localhost:8080/neop

Swagger 文档：http://localhost:8080/neop/doc.html

### 3. 后台管理启动

```bash
cd neop-admin-web
npm install
npm run dev
```

访问：http://localhost:5173

默认账号：admin / admin123

### 4. 移动端启动

1. 使用 HBuilderX 打开 `neop-uniapp` 目录
2. 配置小程序 AppID（公众号平台申请）
3. 点击「运行」→「运行到小程序模拟器」→「微信开发者工具」

或者运行到浏览器（H5）：

```bash
# 在 HBuilderX 中点击「运行」→「运行到浏览器」→「Chrome」
```

---

## 📊 功能模块

### 🎯 任务系统

- ✅ 多种任务类型（签到、分享、邀请、阅读文章、观看视频、自定义）
- ✅ 任务审核机制（自动/手动）
- ✅ 积分奖励规则
- ✅ 任务完成统计

### 🛒 积分商城

- ✅ 商品管理（发布、上下架、库存）
- ✅ 订单流程（确认订单、支付、发货、收货）
- ✅ 支付集成（微信支付、余额支付）
- ✅ 退款售后

### 👥 分销体系

- ✅ 三级分销结构
- ✅ 邀请码/邀请链接
- ✅ 分销佣金结算
- ✅ 团队管理

### 📝 内容社区

- ✅ 文章发布与审核
- ✅ 评论与回复
- ✅ 点赞与收藏
- ✅ 问答系统

### 👤 用户系统

- ✅ 手机号+验证码登录
- ✅ 微信授权登录
- ✅ 个人信息管理
- ✅ 积分明细与余额

### 🔧 系统管理

- ✅ 用户管理
- ✅ 角色权限管理
- ✅ 菜单管理
- ✅ 操作日志
- ✅ 数据看板

---

## 🔧 配置指南

### 后端配置

#### 1. 微信配置

编辑 `neop-backend/src/main/resources/application.yml`：

```yaml
wechat:
  appid: your_wechat_appid
  secret: your_wechat_secret
  mch-id: your_merchant_id
  api-v3-key: your_api_v3_key
  cert-path: /data/neop/cert/apiclient_cert.p12
```

#### 2. Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password
    database: 0
```

#### 3. 文件上传配置

```yaml
upload:
  type: cos  # cos | oss | local
  cos:
    secret-id: your_secret_id
    secret-key: your_secret_key
    bucket: your_bucket
    region: ap-guangzhou
  local:
    path: /data/neop/uploads
```

### 前端配置

#### 1. API 地址配置

**后台管理**：编辑 `neop-admin-web/src/utils/request.js`

```javascript
const BASE_URL = 'http://localhost:8080/neop'
```

**移动端**：编辑 `neop-uniapp/utils/request.js`

```javascript
const BASE_URL = 'http://localhost:8080/neop'
```

#### 2. 小程序配置

编辑 `neop-uniapp/manifest.json`：

```json
{
  "mp-weixin": {
    "appid": "your_wechat_appid",
    "setting": {
      "urlCheck": false
    }
  }
}
```

---

## 📦 部署指南

### 后端部署

#### 1. 打包后端

```bash
cd neop-backend
mvn clean package -DskipTests
```

生成文件：`target/neop-backend-1.0.0.jar`

#### 2. 上传服务器

```bash
scp target/neop-backend-1.0.0.jar root@your_server:/data/neop/
```

#### 3. 启动服务

```bash
# 方式一：直接启动
java -jar /data/neop/neop-backend-1.0.0.jar --spring.profiles.active=prod

# 方式二：使用 systemd（推荐）
# 创建服务文件 /etc/systemd/system/neop.service
[Unit]
Description=Neop Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/data/neop
ExecStart=/usr/bin/java -jar /data/neop/neop-backend-1.0.0.jar --spring.profiles.active=prod
Restart=always

[Install]
WantedBy=multi-user.target

# 启动服务
systemctl daemon-reload
systemctl enable neop
systemctl start neop
systemctl status neop
```

### 前端部署

#### 1. 打包后台管理

```bash
cd neop-admin-web
npm run build
```

生成文件：`dist/` 目录

#### 2. 上传到 Nginx

```bash
scp -r dist/* root@your_server:/usr/share/nginx/html/neop-admin
```

#### 3. 配置 Nginx

编辑 `/etc/nginx/conf.d/neop.conf`：

```nginx
server {
    listen 80;
    server_name admin.neop.com;

    root /usr/share/nginx/html/neop-admin;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /prod-api/ {
        proxy_pass http://localhost:8080/neop/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 移动端部署

#### 微信小程序

1. 在 HBuilderX 中点击「发行」→「小程序-微信」
2. 填写小程序名称和 AppID
3. 上传代码到微信公众平台
4. 提交审核并发布

#### H5 部署

```bash
# 在 HBuilderX 中点击「发行」→「网站-H5手机版」
# 生成文件在 unpackage/dist/build/h5/
```

---

## 🧪 测试指南

### 后端测试

```bash
# 运行所有测试
cd neop-backend
mvn test

# 运行指定测试
mvn test -Dtest=UserControllerTest
```

### 前端测试

```bash
# 后台管理
cd neop-admin-web
npm run test

# 移动端
cd neop-uniapp
npm run test
```

---

## 📚 开发文档

详细开发文档请查看：

- 📖 [完整开发文档](./neop开发文档.md)
- 📝 [开发日志](./dev-log/)
- 🔧 [API 文档](http://localhost:8080/neop/doc.html) (启动后端后访问)

---

## 👥 团队分工

| 角色 | 职责 | 技能要求 |
|------|------|----------|
| 后端开发 | 业务逻辑、API 开发 | Java、Spring Boot、MySQL |
| 前端开发 | 后台管理页面 | Vue3、Element Plus、Vite |
| 移动端开发 | UniApp 页面 | Vue3、UniApp、微信小程序 |
| UI 设计 | 界面设计、交互优化 | Figma、Sketch |
| 测试 | 功能测试、性能测试 | 测试工具、自动化测试 |
| 运维 | 部署、监控、运维 | Linux、Docker、Nginx |

---

## 🤝 贡献指南

我们欢迎任何形式的贡献！

### 贡献流程

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- **Java**: 遵循阿里巴巴 Java 开发手册
- **JavaScript/Vue**: 遵循 ESLint 规范
- **提交信息**: 遵循 Conventional Commits

---

## 📄 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

---

## 🙏 致谢

感谢以下开源项目的支持：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue](https://vuejs.org/)
- [UniApp](https://uniapp.dcloud.net.cn/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [WxJava](https://github.com/Wechat-Group/WxJava)

---

## 📮 联系方式

- **作者**: Neo
- **邮箱**: your-email@example.com
- **GitHub**: [yourusername](https://github.com/yourusername)
- **Issues**: [提交问题](https://github.com/yourusername/neop/issues)

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给它一个 Star！⭐**

Made with ❤️ by Neo

</div>
