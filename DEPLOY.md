# 钓迹 App 部署指南

## 项目结构

```
diaoji-app/      # 前端 Vue3 项目
diaoji-server/   # 后端 Spring Boot 项目
```

## 环境要求

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |
| MinIO | 最新版 |

---

## 后端部署

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE diaoji_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入表结构（如果有 SQL 文件）
# mysql -u root -p diaoji_system < sql/schema.sql
```

### 2. 配置文件

编辑 `src/main/resources/application.yml`：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3307/diaoji_system?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
    username: root
    password: root123456
  data:
    redis:
      host: localhost
      port: 6380

minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin123
  bucket: diaoji

jwt:
  secret: your-256-bit-secret-key-here-make-it-long-enough
```

### 3. 编译运行

```bash
# 开发模式
mvn spring-boot:run -DskipTests

# 生产打包
mvn clean package -DskipTests
java -jar target/diaoji-server-1.0.0.jar
```

### 4. Docker 部署（可选）

```bash
# 构建 Docker 镜像
docker build -t diaoji-server .

# 运行容器
docker run -d -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3307/diaoji_system \
  -e SPRING_DATASOURCE_PASSWORD=root123456 \
  diaoji-server
```

---

## 前端部署

### 1. 安装依赖

```bash
cd diaoji-app
npm install
```

### 2. 配置

编辑 `.env.production`：

```env
VITE_API_BASE_URL=https://your-domain.com/api
VITE_TENCENT_MAP_KEY=your-tencent-map-key
```

### 3. 开发运行

```bash
npm run dev
# 访问 http://localhost:3000
```

### 4. 生产构建

```bash
npm run build
# 产物在 dist/ 目录
```

### 5. Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/diaoji-app/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://127.0.0.1:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## Docker Compose 一键部署

创建 `docker-compose.yml`：

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: diaoji_system
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6380:6379"

  minio:
    image: minio/minio:latest
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data

  backend:
    build: ./diaoji-server
    ports:
      - "8081:8081"
    depends_on:
      - mysql
      - redis
      - minio
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/diaoji_system
      SPRING_DATASOURCE_PASSWORD: root123456

  frontend:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./diaoji-app/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/nginx.conf

volumes:
  mysql_data:
  minio_data:
```

启动：

```bash
docker-compose up -d
```

---

## 测试账号

| 手机号 | 密码 |
|--------|------|
| 13800000000 | 123456 |

---

## 常见问题

### 1. 后端启动失败

检查 MySQL/Redis 是否运行：
```bash
lsof -i :3307  # MySQL
lsof -i :6380  # Redis
lsof -i :9000  # MinIO
```

### 2. 图片上传失败

确认 MinIO bucket 已创建且公开：
```bash
curl http://localhost:9000/minio/health/live
```

### 3. 前端跨域

开发模式已配置 Vite 代理，生产环境需配置 Nginx 反向代理。

---

## API 端点

| 模块 | 端点 | 说明 |
|------|------|------|
| 认证 | POST /api/auth/login | 登录 |
| 认证 | POST /api/auth/register | 注册 |
| 钓点 | GET /api/spot/nearby | 附近钓点 |
| 钓点 | POST /api/spot/add | 添加钓点 |
| 渔获 | POST /api/record/add | 添加渔获 |
| 渔获 | GET /api/record/list | 渔获列表 |
| 钓友圈 | GET /api/feed | 动态列表 |
| 钓友圈 | POST /api/feed/post | 发布动态 |
| 评论 | GET /api/feed/post/{id}/comments | 评论列表 |
| 评论 | POST /api/feed/post/{id}/comment | 发表评论 |

---

## 技术栈

**后端**：Spring Boot 3.2.5 + MyBatis-Plus + JWT + MinIO

**前端**：Vue 3 + Vite + Vant 4 + 腾讯地图 SDK

**存储**：MySQL 8.0 + Redis 7 + MinIO
