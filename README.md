# 小区共享车位管理系统 - 后端

Spring Boot 2.7 + MyBatis-Plus + MySQL。

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

## 快速开始

### 1. 创建数据库并初始化表结构

```bash
mysql -u root -p < src/main/resources/schema.sql
```

或手动执行 `src/main/resources/schema.sql` 中的 SQL。

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

- `spring.datasource.url`：数据库地址，默认 `jdbc:mysql://localhost:3306/parking_share`
- `spring.datasource.username` / `password`：数据库账号密码
- `wechat.miniapp.appid` / `secret`：微信小程序 AppID、AppSecret（用于登录接口）

### 3. 启动

```bash
mvn spring-boot:run
```

服务默认端口 **8080**，上下文路径 **/api**，即接口基地址：`http://localhost:8080/api`。

### 4. 小程序对接

**重要：微信小程序不允许使用 `localhost`，必须使用局域网 IP 地址。**

在小程序 `app.js` 的 `globalData` 中设置：

```js
baseURL: 'http://192.168.3.46:8080/api'  // 请替换为你的实际局域网 IP
```

**获取本机局域网 IP：**
- Windows: 运行 `ipconfig`，查找 IPv4 地址（通常是 192.168.x.x）
- Mac/Linux: 运行 `ifconfig` 或 `ip addr`

**注意事项：**
1. 确保手机/真机与电脑在同一局域网（WiFi）
2. 确保防火墙允许 8080 端口访问
3. 微信小程序官方要求使用 HTTPS，但在开发环境可以使用 HTTP（需在微信开发者工具中开启"不校验合法域名"）
4. 生产环境必须配置 HTTPS 并在微信小程序后台配置服务器域名

## 主要接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /user/login | 微信登录（body: { code }） |
| GET | /user/stats | 用户统计（需 Authorization） |
| GET | /community/list | 小区列表（可选 keyword） |
| GET | /parking/recommended | 推荐车位（可选 communityId, latitude, longitude） |
| GET | /parking/list | 车位列表（分页、keyword、communityId、sortType） |
| GET | /parking/{id} | 车位详情 |
| POST | /parking/publish | 发布车位（需登录） |
| POST | /order/create | 创建订单（需登录） |
| GET | /order/list | 订单列表（需登录） |
| GET | /order/{id} | 订单详情（需登录） |
| PUT | /order/{id}/cancel | 取消订单（需登录） |
| POST | /matching/smart-match | 智能匹配 |

## 项目结构

```
src/main/java/com/parking/
├── ParkingApplication.java    # 启动类
├── common/Result.java         # 统一响应封装
├── config/                    # 配置（CORS、MyBatis、微信等）
├── controller/                # 控制器
├── entity/                    # 实体类
├── mapper/                    # MyBatis Mapper
└── service/                   # 业务逻辑
```

## 注意事项

1. 微信登录需在微信公众平台配置小程序 AppID、AppSecret，并在本项目中正确配置。
2. 首次运行请先执行 `schema.sql` 建库建表，并插入初始小区数据。
3. Token 当前为内存存储，重启后失效；生产环境建议改用 Redis 或 JWT。
