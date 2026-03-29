# Sistema de Gerenciamento de Produtos e Pedidos

![CI/CD Pipeline](https://github.com/evertonrocha2/block-proj/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![Coverage](https://img.shields.io/badge/Cobertura-90%25+-brightgreen)
![Testes](https://img.shields.io/badge/Testes-296+-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Sistema de gerenciamento de produtos e pedidos com Spring Boot, interface web e pipeline CI/CD automatizado com deploy para Vercel e ambientes staging/prodution.

---

Link do frontend: https://block-proj-xi.vercel.app/

---
## Arquitetura do Sistema

```
                    +------------------+
                    |   Frontend Web   |
                    |  (HTML/JS/CSS)   |
                    +--------+---------+
                             |
                    +--------v---------+
                    |  ProductController|
                    |  OrderController  |
                    +--------+---------+
                             |  DTOs (Request/Response)
                    +--------v---------+
                    | IProductService   |
                    | IOrderService     |  <-- Interfaces (Inversao de Dependencia)
                    +--------+---------+
                             |
                    +--------v---------+
                    | ProductService    |
                    | OrderService      |  <-- Implementacoes
                    +--------+---------+
                             |
                    +--------v---------+
                    | ProductRepository |
                    | OrderRepository   |  <-- Spring Data JPA
                    +--------+---------+
                             |
                    +--------v---------+
                    |     H2 Database   |
                    +-------------------+
```

### Stack Tecnologica

| Camada | Tecnologia |
|--------|-----------|
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.2.0 |
| **Persistencia** | Spring Data JPA + H2 (em memoria) |
| **Build** | Maven + JaCoCo (cobertura >= 90%) |
| **Testes** | JUnit 5, Mockito, Selenium 4, REST Assured, JavaFaker |
| **CI/CD** | GitHub Actions (6 jobs) |
| **Deploy Frontend** | Vercel |
| **Seguranca** | OWASP Dependency Check (SAST) + Testes DAST |

---

## Como Rodar

```bash
# Subir a aplicacao
mvn spring-boot:run

# Rodar testes com cobertura
mvn clean verify

# Gerar relatorio de cobertura
mvn jacoco:report
# Abrir target/site/jacoco/index.html
```

Acesse em http://localhost:8080. Console H2 em http://localhost:8080/h2-console (user: `sa`, senha vazia, url: `jdbc:h2:mem:productdb`).

---

## Estrutura do Projeto

```
src/main/java/com/crud/system/
├── controller/       # REST endpoints com DTOs (ProductController, OrderController)
├── service/          # Logica de negocio (interfaces + implementacoes)
├── repository/       # Acesso a dados (Spring Data JPA)
├── model/            # Entidades (Product, Order, OrderItem, OrderStatus)
├── dto/              # DTOs imutaveis, Mappers e validacao
└── exception/        # Tratamento global de erros (ErrorResponse imutavel)

src/main/resources/static/
├── index.html        # Interface web do sistema
├── app.js            # Logica frontend com validacao e retry
└── styles.css        # Estilos monocromaticos responsivos

src/test/java/com/crud/system/
├── service/          # Testes unitarios (ProductServiceTest, OrderServiceTest)
├── controller/       # Testes de controller com MockMvc
├── integration/      # Testes end-to-end com H2
├── selenium/         # Testes de interface (headless Chrome)
├── fuzzing/          # Fuzz testing (SQL injection, XSS, Unicode, overflow)
├── failure/          # Simulacao de falhas (timeout, rede, rollback)
├── model/            # Testes de entidades
├── dto/              # Testes de DTOs e mappers
└── exception/        # Testes do exception handler
```

---

## API Endpoints

### Produtos (`/api/products`)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| `POST` | `/api/products` | Criar produto (via DTO) |
| `GET` | `/api/products` | Listar todos |
| `GET` | `/api/products/{id}` | Buscar por ID |
| `PUT` | `/api/products/{id}` | Atualizar (via DTO) |
| `DELETE` | `/api/products/{id}` | Deletar |
| `GET` | `/api/products/category/{cat}` | Filtrar por categoria |
| `GET` | `/api/products/search?name=...` | Buscar por nome |
| `GET` | `/api/products/low-stock` | Estoque baixo |

### Pedidos (`/api/orders`)

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| `POST` | `/api/orders` | Criar pedido (reduz estoque) |
| `GET` | `/api/orders` | Listar todos |
| `GET` | `/api/orders/{id}` | Buscar por ID |
| `PUT` | `/api/orders/{id}/confirm` | Confirmar pedido |
| `PUT` | `/api/orders/{id}/deliver` | Marcar como entregue |
| `PUT` | `/api/orders/{id}/cancel` | Cancelar (devolve estoque) |
| `DELETE` | `/api/orders/{id}` | Deletar pedido |
| `GET` | `/api/orders/status/{status}` | Filtrar por status |
| `GET` | `/api/orders/client?email=...` | Buscar por cliente |

---

## Estrategia de Testes

**296+ testes** organizados em categorias:

| Categoria | Classe | Qtd | Tipo |
|-----------|--------|-----|------|
| Unitarios | ProductServiceTest | 24 | Mock |
| Unitarios | OrderServiceTest | 45 | Mock |
| Controller | ProductControllerTest | 10 | MockMvc |
| Controller | OrderControllerTest | 12 | MockMvc |
| Integracao | ProductIntegrationTest | 10 | H2 real |
| Integracao | GlobalExceptionHandlerIntegrationTest | 9 | H2 real |
| Selenium | ProductSeleniumTest | 10 | Headless Chrome |
| Fuzz | ProductFuzzTest | 73 | SQL injection, XSS, Unicode |
| Falhas | ProductFailureSimulationTest | 14 | Timeout, rede, rollback |
| Modelo | ProductTest, OrderTest | 52 | Unitario |
| DTO/Mapper | MapperTest | 15 | Unitario |
| Excecoes | ErrorResponseTest, CustomExceptionsTest | 12 | Unitario |

### Cobertura minima: **90%** (linha, por pacote) — aplicada via JaCoCo.

---

## Pipeline CI/CD (GitHub Actions)

O pipeline roda automaticamente em push/PR e contem **6 jobs**:

```
                    +-------------------+
                    |  build-and-test   |
                    | (Build + Testes   |
                    |  + Cobertura 90%) |
                    +--------+----------+
                             |
                +------------+------------+
                |                         |
      +---------v----------+   +----------v---------+
      |   security-sast    |   |   security-dast    |
      | (OWASP Dep Check)  |   | (SQL/XSS/Headers)  |
      +---------+----------+   +----------+----------+
                |                         |
                +------------+------------+
                             |
                    +--------v---------+     +------------------+
                    |  deploy-staging  |     |  deploy-vercel   |
                    | (API + Selenium  |     | (Frontend static)|
                    |  pos-deploy)     |     +------------------+
                    +--------+---------+
                             |
                    +--------v---------+
                    |   deploy-prod    |
                    | (Aprovacao manual)|
                    +------------------+
```

### Detalhamento dos Jobs

| Job | Descricao | Gatilho |
|-----|-----------|---------|
| **build-and-test** | Compila, roda 296+ testes, gera cobertura JaCoCo (90%+), upload artefatos | Push/PR |
| **security-sast** | OWASP Dependency Check (CVSS >= 9 falha o build) | Apos build |
| **security-dast** | Testes dinamicos: SQL injection, XSS, headers de seguranca | Apos build |
| **deploy-staging** | Deploy staging + health check + testes pos-deploy API + Selenium | Apos seguranca |
| **deploy-vercel** | Deploy frontend estatico no Vercel | Push na main |
| **deploy-prod** | Deploy producao com aprovacao manual (environment protection) | Push na main, apos staging |

### Ambientes e Protecoes

| Ambiente | Protecao | Deploy |
|----------|----------|--------|
| **staging** | Environment protection rules | Automatico apos testes |
| **production** | Aprovacao manual obrigatoria | Somente branch main |
| **vercel** | Token via secrets | Frontend automatico |

### Variaveis e Secrets

| Secret | Descricao |
|--------|-----------|
| `VERCEL_TOKEN` | Token de autenticacao do Vercel para deploy frontend |

---

## Refatoracoes Aplicadas

### Principio da Imutabilidade
- `ErrorResponse` com campos `final`, sem setters, lista protegida por `Collections.unmodifiableList`
- Separacao de consultas (readOnly) e modificadores nos servicos

### Separacao de Responsabilidades
- Controllers usam **DTOs** (ProductRequestDTO/ProductResponseDTO) em vez de entidades
- **Mappers** (ProductMapper, OrderMapper) convertem entre camadas
- Interfaces de servico (`IProductService`, `IOrderService`) para inversao de dependencia

### Clausulas de Guarda
- Validacoes de entrada no inicio dos metodos com retorno rapido
- `GlobalExceptionHandler` com metodo `buildErrorResponse()` eliminando duplicacao

### Clean Code
- Constantes para mensagens de erro repetidas nos servicos
- Configuracao externalizada (`product.low-stock.threshold` no application.properties)
- `lombok.config` para JaCoCo ignorar codigo gerado pelo Lombok
- Perfil de teste separado (`application-test.properties`)

---

## Deploy no Vercel

O frontend estatico (HTML/JS/CSS) e deployado automaticamente no Vercel via GitHub Actions.

### Configuracao

1. Crie uma conta no [Vercel](https://vercel.com)
2. Gere um token em Settings > Tokens
3. Adicione como secret no repositorio: `Settings > Secrets > VERCEL_TOKEN`
4. O deploy acontece automaticamente no push para `main`

### Nota sobre o Backend

O backend Java/Spring Boot requer um servidor com JVM. Para deploy completo:
- **Railway** ou **Render** (free tier) para o JAR
- **Vercel** para o frontend estatico
- Configure `API_BASE_URL` em `app.js` para apontar ao backend em producao
