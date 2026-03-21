# Sistema de Gerenciamento de Produtos e Pedidos

![CI/CD](https://github.com/evertonrocha2/block-proj/actions/workflows/ci.yml/badge.svg)

Sistema de gerenciamento de produtos e pedidos com Spring Boot, interface web e pipeline CI/CD automatizado.

## Sobre

CRUD de produtos integrado com módulo de pedidos, com controle de estoque automático. Quando um pedido é criado, o estoque dos produtos é reduzido; quando cancelado, é devolvido.

### Stack

- Java 17 / Spring Boot 3.2
- Spring Data JPA + H2 (banco em memória)
- Maven + JaCoCo (cobertura >= 90%)
- JUnit 5, Mockito, Selenium, JavaFaker
- GitHub Actions (CI/CD)

## Como rodar

```bash
# subir a aplicação
mvn spring-boot:run

# rodar testes
mvn clean verify

# gerar relatório de cobertura
mvn jacoco:report
# abre target/site/jacoco/index.html
```

Acesse em http://localhost:8080. Console H2 em http://localhost:8080/h2-console (user: `sa`, senha vazia, url: `jdbc:h2:mem:productdb`).

## Estrutura

```
src/main/java/com/crud/system/
├── controller/       # REST endpoints (ProductController, OrderController)
├── service/          # Lógica de negócio (interfaces + implementações)
├── repository/       # Acesso a dados (JPA)
├── model/            # Entidades (Product, Order, OrderItem, OrderStatus)
├── dto/              # DTOs e Mappers
└── exception/        # Tratamento global de erros

src/test/java/com/crud/system/
├── service/          # Testes unitários (ProductServiceTest, OrderServiceTest)
├── controller/       # Testes de controller com MockMvc
├── integration/      # Testes end-to-end com H2
├── selenium/         # Testes de interface (headless Chrome)
├── fuzzing/          # Fuzz testing (SQL injection, XSS, etc)
├── failure/          # Simulação de falhas (timeout, rede)
├── model/            # Testes de entidades
├── dto/              # Testes de DTOs e mappers
└── exception/        # Testes do exception handler
```

## API

### Produtos (`/api/products`)

- `POST /api/products` — criar produto
- `GET /api/products` — listar todos
- `GET /api/products/{id}` — buscar por ID
- `PUT /api/products/{id}` — atualizar
- `DELETE /api/products/{id}` — deletar
- `GET /api/products/category/{cat}` — filtrar por categoria
- `GET /api/products/search?name=...` — buscar por nome
- `GET /api/products/low-stock` — estoque baixo

### Pedidos (`/api/orders`)

- `POST /api/orders` — criar pedido (reduz estoque)
- `GET /api/orders` — listar todos
- `GET /api/orders/{id}` — buscar por ID
- `PUT /api/orders/{id}/confirm` — confirmar
- `PUT /api/orders/{id}/deliver` — marcar entregue
- `PUT /api/orders/{id}/cancel` — cancelar (devolve estoque)
- `DELETE /api/orders/{id}` — deletar
- `GET /api/orders/status/{status}` — filtrar por status
- `GET /api/orders/client?email=...` — buscar por cliente

## Testes

296 testes no total:

- **Unitários**: ProductServiceTest (24), OrderServiceTest (45)
- **Controller**: ProductControllerTest (10), OrderControllerTest (12)
- **Integração**: ProductIntegrationTest (10), GlobalExceptionHandlerIntegrationTest (9)
- **Selenium**: ProductSeleniumTest (10)
- **Fuzz**: ProductFuzzTest (73) — SQL injection, XSS, Unicode, overflow
- **Falhas**: ProductFailureSimulationTest (14) — timeout, rede, rollback
- **Modelo/DTO**: ProductTest (22), OrderTest (30), MapperTest (15), ErrorResponseTest (6), CustomExceptionsTest (8)

## CI/CD (GitHub Actions)

O pipeline roda automaticamente em push/PR:

1. **Build & Testes** — compila, roda testes, gera cobertura JaCoCo, faz upload dos artefatos
2. **Análise de Segurança** — OWASP Dependency Check nas dependências
3. **Deploy Staging** — sobe a aplicação, health check, testes pós-deploy via API
4. **Deploy Produção** — aprovação manual, só na branch main

## Refatorações aplicadas

- Interfaces de serviço (`IProductService`, `IOrderService`) pra inversão de dependência
- DTOs separando o que entra/sai da API das entidades do banco
- `GlobalExceptionHandler` com métodos `extractPath()` e `buildErrorResponse()` pra evitar duplicação
- Configuração externalizada (`product.low-stock.threshold` no application.properties)
- `lombok.config` pra JaCoCo ignorar código gerado pelo Lombok
- Perfil de teste separado (`application-test.properties`)
