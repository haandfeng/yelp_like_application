# Yelp-Like (Microservices Example)

> **Overview**: This repository is an e-commerce style sample project (named *yelp_like*) built with Spring Boot / Spring Cloud Alibaba, containing gateway, user, item, cart, order, payment services and common modules. Supports JWT authentication, Feign calls, RabbitMQ async messaging, Knife4j online API docs, and Nacos configuration center.

---

## Directory Structure

```
yelp_like/
  .idea/
  cart-service/
  item-service/
  pay-service/
  pom.xml
  trade-service/
  user-service/
  yelp-api/
  yelp-common/
  yelp-gateway/
  yelp-service/
```

### Module Description
- **yelp-gateway**: Spring Cloud Gateway (global filters, auth, dynamic routing).
- **yelp-api**: Feign clients and DTOs reused across services (`ItemClient/TradeClient/CartClient/UserClient/PayClient` etc.).
- **yelp-common**: Common utilities and configuration (exceptions, interceptors, `UserContext`, RabbitMQ message converters, etc.).
- **item-service**: Item and search APIs (`/items/**`, `/search/**`), backed by Elasticsearch for full-text/聚合查询。
- **cart-service**: Cart APIs (`/carts/**`).
- **user-service**: User, address, and login APIs (`/users/**`, `/addresses/**`).
- **trade-service**: Order service (`/orders/**`), includes payment status and delayed message listeners.
- **pay-service**: Payment order service (`/pay-orders/**`).
- **yelp-service**: **Monolithic aggregation service** (contains all Controllers, useful for quick local testing). Cannot run together with the gateway on default ports.

### Default Ports (can be changed in each module’s `application.yaml`)
| Module | Port |
|---|---|
| yelp-gateway | 8080 |
| item-service | 8081 |
| cart-service | 8082 |
| user-service | 8084 |
| trade-service | 8085 |
| pay-service | 8086 |
| yelp-service (monolithic) | 8080 |

> ⚠️ Note: `yelp-gateway` and `yelp-service` both use `8080` by default — **do not run them simultaneously**. Either run the gateway + microservices or run the monolithic `yelp-service`.

---

## Tech Stack & Versions
- **JDK 11**, **Maven** (multi-module)
- Spring Boot 2.7.12
- Spring Cloud 2021.0.3, Spring Cloud Alibaba 2021.0.4.0 (Nacos config & registry)
- MyBatis-Plus (data access)
- RabbitMQ (async messaging)
- Knife4j (OpenAPI/Swagger)
- JWT (gateway authentication, JKS keystore)
- Optional: Sentinel (rate limiting/circuit breaking), Seata (distributed transactions, `shared-seata.yaml` in Nacos)

---

## Prerequisites

1. **JDK 11 + Maven 3.8+**
2. **MySQL 8.x**: Create the following databases (or adjust as needed), and configure the connection:
   - `yelp-item`, `yelp-cart`, `yelp-user`, `yelp-trade`, `yelp-pay`
3. **Redis 6+ (optional)**: Some cache examples expect a Redis instance (defaults to `redis://127.0.0.1:6379` — override through Nacos).
4. **Nacos 2.x** (config center + registry)  
   - Default `server-addr: 192.168.150.101:8848` in `bootstrap.yaml` — change to your Nacos address (e.g. `127.0.0.1:8848`).
   - Create the following **Shared Configs** in Nacos (dataId, format `yaml`):
     - `shared-jdbc.yaml`: Common DB connection:
       ```yaml
       spring:
         datasource:
           driver-class-name: com.mysql.cj.jdbc.Driver
           url: jdbc:mysql://${yelp.db.host}:3306/${yelp.db.database}?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
           username: root
           password: ${yelp.db.pw}
       ```
     - `shared-redis.yaml` (可选): 统一 Redis 连接。
     - `shared-log.yaml`: Log level & format.
     - `shared-swagger.yaml`: Knife4j/Swagger config (optional).
     - `shared-seata.yaml`: Seata config (if using distributed transactions).
5. **RabbitMQ 3.x** (for async order/payment messages)  
   Example (`trade-service` default):
   ```yaml
   spring:
     rabbitmq:
       host: 192.168.150.101
       port: 5672
       virtual-host: /yelp_like
       username: yelp_like
       password: 123
   ```
   Adjust for your environment.
6. **JWT keystore (JKS)**  
   - Default path: `classpath:yelp_like.jks` (alias `yelp_like`, password `yelp_like123`).
   - Repo contains `hmall.jks`, not matching config. **Option A:** change `yelp.jwt.location` to `classpath:hmall.jks`; **Option B:** generate `yelp_like.jks`:
     ```bash
     keytool -genkeypair -alias yelp_like -keyalg RSA -keypass yelp_like123 -keystore yelp_like.jks -storepass yelp_like123
     ```
7. **Sentinel Dashboard & Prometheus**  
   - Sentinel client properties are now in each service. Set `SENTINEL_DASHBOARD` & `SENTINEL_TRANSPORT_PORT` env vars or use defaults (`localhost:8719`).  
   - Import the provided `shared-sentinel` config into Nacos (or edit `spring.cloud.sentinel.datasource` rules) to manage flow/degrade rules centrally.  
   - Actuator + Micrometer Prometheus registry is enabled; scrape `<host>:<port>/actuator/prometheus`.

---

## Configuration Profiles & Local Overrides

- Every service ships with three config files:
  - `application.yaml`: shared defaults (ports, feature toggles, Sentinel, Actuator, etc.).
  - `application-dev.yaml`: dev/demo overrides typically pointing to a remote dev env.
  - `application-local.yaml`: minimal overrides for running everything on your laptop.
- To activate the local profile, export `SPRING_PROFILES_ACTIVE=local` (or pass `-Dspring-boot.run.profiles=local`).

Examples:

```bash
# Run cart-service against local MySQL/Nacos/RabbitMQ
SPRING_PROFILES_ACTIVE=local mvn -pl cart-service -am spring-boot:run

# Or via java -jar
SPRING_PROFILES_ACTIVE=local java -jar cart-service/target/cart-service*.jar
```

`application-local.yaml` only contains sensitive bits (`yelp.db.host`, `yelp.db.pw`). Keep your actual credentials in Nacos or environment variables; do not commit them.

---

## Build & Run

### 1) Clone & Build
```bash
mvn -T 1C -DskipTests clean package
```

### 1.5) Local Quickstart Checklist

1. Start **MySQL**, **Nacos**, **RabbitMQ**, (optional) **Redis/Elasticsearch** locally or via Docker.
2. Import the `shared-*.yaml` configs mentioned above into Nacos; verify that each service can read `shared-jdbc.yaml`.
3. Export `SPRING_CLOUD_NACOS_SERVER_ADDR=<host>:8848` and (optionally) `SENTINEL_DASHBOARD=<host>:8719`.
4. Activate the local profile when running (`SPRING_PROFILES_ACTIVE=local`).
5. Launch whichever service you’re working on with `mvn -pl <module> -am spring-boot:run`.

Once a service is up you can hit `http://localhost:<port>/doc.html` to verify Knife4j and `http://localhost:<port>/actuator/health` for readiness.

### 2) Run Mode A: **Microservices + Gateway**
Start in order: Nacos → MySQL → RabbitMQ → business services → gateway.

Examples:
```bash
# Run a single module
mvn -pl item-service -am spring-boot:run
mvn -pl user-service -am spring-boot:run
mvn -pl cart-service -am spring-boot:run
mvn -pl trade-service -am spring-boot:run
mvn -pl pay-service -am spring-boot:run
mvn -pl yelp-gateway -am spring-boot:run

# Or use jar
java -jar item-service/target/item-service*.jar
...
java -jar yelp-gateway/target/yelp-gateway*.jar
```

### 3) Run Mode B: **Monolithic (yelp-service)**
```bash
# Default port 8080 — don't run with gateway
mvn -pl yelp-service -am spring-boot:run
# Or
java -jar yelp-service/target/yelp-service*.jar
```

## Cloud / AWS Deployment

- 参考 `deploy/aws/docker-compose.yaml` 与 `deploy/aws/README.md`，可在 EC2 上一键拉起 MySQL/Redis/RabbitMQ/Nacos/Sentinel/Elasticsearch 及 `yelp-service`、Prometheus、Grafana。
- 核心步骤：
  1. 构建并推送 `yelp-service`（或其它模块）镜像到 ECR 或本地 registry。
  2. SSH 到 EC2，导出 `AWS_REGION`、`AWS_LOG_GROUP`、`MYSQL_ROOT_PASSWORD` 等环境变量。
  3. 运行 `docker compose up -d` 即可拉起整套依赖。
- Compose 默认启用 `awslogs` 日志驱动，可直接将容器日志写入 CloudWatch，并通过 Prometheus 抓取 `/actuator/prometheus` 暴露的指标。
- 需要扩展为完整微服务拓扑时，可以在同一 compose 中新增 `item-service` 等镜像，并复用 `SPRING_CLOUD_NACOS_SERVER_ADDR=nacos:8848`。

---

## API Entry Points & Examples

### Knife4j Docs (default)
- `item-service`: <http://localhost:8081/doc.html>
- `cart-service`: <http://localhost:8082/doc.html>
- `user-service`: <http://localhost:8084/doc.html>
- `trade-service`: <http://localhost:8085/doc.html>
- `pay-service`: <http://localhost:8086/doc.html>
- (if using gateway, routes depend on your rules)

### Login & Get Token
```bash
curl -X POST http://localhost:8084/users/login   -H 'Content-Type: application/json'   -d '{"username":"test","password":"123456"}'
# Response includes token, userId, username, balance
```

### Access via Gateway
```bash
# Public APIs (excluded in gateway excludePaths):
curl http://localhost:8080/items/1
curl 'http://localhost:8080/search?q=phone'

# Protected APIs (need Authorization header):
curl -H "Authorization: Bearer <token>" http://localhost:8080/carts
```

---

## Development Tips

- **Centralized config in Nacos**: `bootstrap.yaml` pulls `shared-*.yaml`, each service uses `yelp.db.database` for DB name.
- **Sentinel**: Dashboard address/transport port can be controlled via environment variables. Flow rules can be managed through Nacos using the `${spring.application.name}-flow-rules` dataId under `SENTINEL_GROUP`.
- **Metrics/Health**: Actuator endpoints (`/actuator/health`, `/actuator/prometheus`) are exposed across services for Grafana/CloudWatch integration.
- **Feign user context**: `yelp-api` defines Feign default config, passing headers with `UserContext`.
- **Messaging & orders**: `trade-service` listens for payment status & delayed messages; `pay-service` handles payment order creation & callback simulation.
- **Swagger/Knife4j**: Toggle with `knife4j.enable=true`, or manage in `shared-swagger.yaml`.
- **Port conflicts**: Don't let `yelp-gateway` and `yelp-service` run on same port.

---

## FAQ

1. **Error: `yelp_like.jks` not found**  
   - Fix config or generate the file as above.

2. **Nacos connection failure**  
   - Update `bootstrap.yaml` server-addr, ensure Nacos is running.

3. **DB connection failure**  
   - Set `yelp.db.host`, `yelp.db.pw` in Nacos `shared-jdbc.yaml`. Ensure DBs/tables exist.

4. **RabbitMQ connection failure**  
   - Update `spring.rabbitmq.*` for your env, create vhost/user.

5. **Switching monolith/microservices**  
   - Microservices: run gateway + services; Monolith: only `yelp-service`. Don't run both on same port.

---

## License
No license included. Add one before public release (e.g. MIT/Apache-2.0).

---

Need me to generate a **Nacos `shared-jdbc.yaml` template** or startup script for your OS? Tell me your DB and Nacos/RabbitMQ addresses, I'll provide examples.
