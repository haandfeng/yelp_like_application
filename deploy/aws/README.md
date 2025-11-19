# AWS 部署指引

该目录提供一套可在 **Amazon EC2** 上快速复现 `yelp_like` 微服务的脚本，集成了 Nacos、Sentinel Dashboard、Prometheus、Grafana 以及 CloudWatch 日志。

## 1. 先决条件

1. 一台安装了 Docker / Docker Compose v2 的 Amazon Linux 2 或 Ubuntu 20.04 EC2。
2. 开通下列端口的安全组：`22`、`80`、`8080`、`8719`、`8848`、`9090`、`3000`、`9200`、`9300`、`15672`、`5672`。
3. 具备 AWS 访问凭证（用于 CloudWatch Logs），并在 EC2 上配置 `AWS_REGION`、`AWS_ACCESS_KEY_ID`、`AWS_SECRET_ACCESS_KEY`。
4. 构建 yelp_like 各服务镜像，并推送到 ECR（或者直接在 EC2 上执行 `docker build`）。

```bash
# 在项目根目录
docker build -t yelp-like/yelp-service -f yelp-service/Dockerfile .
docker tag yelp-like/yelp-service <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/yelp-like/yelp-service:latest
docker push <ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com/yelp-like/yelp-service:latest
```

将 `ECR_URI` 环境变量替换为你的镜像地址即可复用 compose。

## 2. 运行 docker-compose

```bash
cd deploy/aws
export AWS_REGION=us-east-1
export AWS_LOG_GROUP=/aws/ecs/yelp-like
export MYSQL_ROOT_PASSWORD=StrongPassw0rd
export RABBITMQ_PASSWORD=StrongPassw0rd
docker compose up -d
```

Compose 中包含：

| 服务 | 说明 |
| --- | --- |
| mysql、redis、rabbitmq | 基础依赖 |
| nacos | 配置中心/注册中心 |
| sentinel-dashboard | 监控限流规则 |
| elasticsearch | 搜索引擎（item-service 搜索入口） |
| yelp-service | 单体聚合服务（若需微服务模式，可照此新增模块） |
| prometheus + grafana | 指标采集与可视化 |

所有容器均使用 `awslogs` 日志驱动，请保证 EC2 上已安装 `awslogs` 插件（Amazon Linux 2 默认已包含）。不同环境可通过 `AWS_LOG_GROUP` / `AWS_REGION` 控制 CloudWatch 目的地。

## 3. Sentinel & Prometheus

- `sentinel-dashboard`：默认账号密码 `sentinel/sentinel`，监听 `8719` 端口。
- 所有微服务已暴露 `/actuator/prometheus`，Prometheus 配置位于 `deploy/aws/prometheus/prometheus.yml`，默认抓取 `yelp-service` 与 `nacos`。
- Grafana 账号：`admin/admin123`，首次登录后请立刻修改密码，导入官方 Spring Boot 或 JVM Dashboard 即可。

## 4. 自定义参数

在 `docker-compose.yaml` 中可以通过环境变量重写以下关键信息：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `ES_URIS` | `http://elasticsearch:9200` | item-service/es 客户端地址 |
| `SENTINEL_DASHBOARD` | `sentinel-dashboard:8719` | Sentinel 控制台 |
| `MYSQL_ROOT_PASSWORD` | `123` | MySQL root 密码 |
| `RABBITMQ_PASSWORD` | `123` | RabbitMQ 密码 |
| `AWS_LOG_GROUP` | `/aws/ecs/yelp-like` | CloudWatch 日志组 |

如需切换到多服务拓扑，只需基于此 compose 再添加 `item-service`、`trade-service` 等镜像，并将 `SPRING_CLOUD_NACOS_SERVER_ADDR` 指向 `nacos:8848` 即可。

## 5. 关停

```bash
docker compose down
# 保留数据卷
docker volume ls | grep yelp_like
```

必要时，可通过 `docker compose down -v` 同时清理数据卷。

