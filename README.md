# Sistema de Gerenciamento de Produtos - CRUD com Interface Web

Sistema completo de gerenciamento de produtos desenvolvido em Java com Spring Boot, incluindo interface web responsiva, testes avançados e arquitetura robusta.

## Descrição do Projeto

Este sistema implementa operações CRUD (Create, Read, Update, Delete) para gerenciamento de produtos, com foco em:

- Robustez e tratamento gracioso de falhas
- Validações fail early e fail gracefully
- Testes avançados incluindo fuzz testing e simulação de falhas
- Interface web intuitiva e responsiva
- Cobertura de código superior a 85%

## Tecnologias Utilizadas

- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5
- Mockito
- Selenium WebDriver
- Jacoco (cobertura de código)

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/crud/system/
│   │   ├── controller/      # Endpoints REST
│   │   ├── service/         # Lógica de negócio
│   │   ├── repository/      # Acesso a dados
│   │   ├── model/           # Entidades JPA
│   │   └── exception/       # Tratamento de erros
│   └── resources/
│       ├── static/          # Frontend (HTML/CSS/JS)
│       └── application.properties
└── test/
    └── java/com/crud/system/
        ├── controller/      # Testes de controller
        ├── service/         # Testes de serviço
        ├── integration/     # Testes de integração
        ├── selenium/        # Testes de interface
        ├── fuzz/            # Testes de fuzzing
        └── failure/         # Simulação de falhas
```

## Pré-requisitos

Antes de executar o sistema, certifique-se de ter instalado:

- JDK 17 ou superior
- Maven 3.6 ou superior
- Navegador web moderno (Chrome, Firefox, Edge)

Para verificar as instalações:

```bash
java -version
mvn -version
```

---

## Manual de Execução

### 1. Iniciar o Sistema

#### Opção A: Usando Maven

No diretório raiz do projeto, execute:

```bash
mvn spring-boot:run
```

O sistema será iniciado na porta 8080. Aguarde a mensagem:

```
Started ProductManagementApplication in X.XXX seconds
```

#### Opção B: Usando JAR Compilado

Compile o projeto:

```bash
mvn clean package -DskipTests
```

Execute o JAR gerado:

```bash
java -jar target/product-management-0.0.1-SNAPSHOT.jar
```

#### Acessar o Sistema

Após iniciar, acesse no navegador:

```
http://localhost:8080
```

A interface web será carregada automaticamente.

#### Console H2 (opcional)

Para visualizar o banco de dados, acesse:

```
http://localhost:8080/h2-console
```

Configurações de conexão:
- JDBC URL: `jdbc:h2:mem:productdb`
- User: `sa`
- Password: (deixar em branco)

### 2. Executar os Testes

#### Executar Todos os Testes

```bash
mvn test
```

Este comando executa toda a suíte de testes (186 testes):
- Testes unitários
- Testes de integração
- Testes de controller
- Testes de exceções
- Testes Selenium
- Fuzz testing (73 testes)
- Simulação de falhas (14 testes)

#### Executar Testes Específicos

Apenas testes unitários de serviço:

```bash
mvn test -Dtest=ProductServiceTest
```

Apenas testes de integração:

```bash
mvn test -Dtest=ProductIntegrationTest
```

Apenas testes Selenium:

```bash
mvn test -Dtest=ProductSeleniumTest
```

Apenas fuzz testing:

```bash
mvn test -Dtest=ProductFuzzTest
```

#### Executar Testes com Relatório de Cobertura

```bash
mvn clean test jacoco:report
```

### 3. Interpretar os Resultados

#### Resultado dos Testes no Terminal

Ao final da execução, você verá:

```
[INFO] Results:
[INFO]
[INFO] Tests run: 186, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

**Interpretação:**
- **Tests run**: Total de testes executados
- **Failures**: Asserções que falharam (comportamento incorreto)
- **Errors**: Exceções não tratadas durante execução
- **Skipped**: Testes ignorados
- **BUILD SUCCESS**: Todos os testes passaram

Se houver falhas, o Maven exibirá detalhes:

```
[ERROR] Failures:
[ERROR]   ProductServiceTest.shouldCreateProduct:45
    Expected: 5
    Actual: 4
```

#### Relatório de Cobertura de Código

Após executar `mvn jacoco:report`, abra o relatório:

```
target/site/jacoco/index.html
```

**Métricas apresentadas:**

1. **Missed Instructions / Cov.**: Percentual de instruções executadas
2. **Missed Branches / Cov.**: Percentual de ramificações (if/else) testadas
3. **Cxty (Complexidade)**: Complexidade ciclomática do código
4. **Lines**: Linhas de código cobertas
5. **Methods**: Métodos testados
6. **Classes**: Classes cobertas

**Interpretação das cores:**
- **Verde**: Cobertura adequada (70-100%)
- **Amarelo**: Cobertura parcial (50-70%)
- **Vermelho**: Cobertura insuficiente (0-50%)

**Cobertura por Pacote:**
- `com.crud.system.controller`: 100% (todos os endpoints testados)
- `com.crud.system.service`: 87% (lógica de negócio bem coberta)
- `com.crud.system.model`: 82% (entidades e validações)
- `com.crud.system.exception`: 63% (tratamento de erros)

**Cobertura Total do Projeto: 88%** (meta: 85%)

#### Tipos de Testes e Seus Objetivos

**Testes Unitários (ProductServiceTest, 24 testes)**
- Validam lógica de negócio isoladamente
- Usam mocks para dependências
- Exemplo: criação de produto, validação de duplicatas

**Testes de Integração (ProductIntegrationTest, 10 testes)**
- Validam fluxo completo da aplicação
- Testam comunicação entre camadas
- Banco de dados real (H2 em memória)

**Testes de Controller (ProductControllerTest, 10 testes)**
- Validam endpoints REST
- Testam serialização JSON
- Verificam códigos HTTP corretos

**Testes Selenium (ProductSeleniumTest, 10 testes)**
- Validam interface web
- Simulam interação do usuário
- Testam formulários, tabelas, botões

**Fuzz Testing (ProductFuzzTest, 73 testes)**
- Detectam vulnerabilidades
- Testam com entradas maliciosas (SQL injection, XSS)
- Validam tratamento de strings gigantes e caracteres especiais

**Simulação de Falhas (ProductFailureSimulationTest, 14 testes)**
- Testam comportamento em cenários adversos
- Simulam timeouts, erros de rede, banco indisponível
- Validam fail gracefully

#### Logs de Execução

Durante a execução, o sistema gera logs no console:

```
2026-02-15 10:30:15 - Criando novo produto: Notebook Dell
2026-02-15 10:30:15 - Produto criado com sucesso. ID: 1
```

**Níveis de log:**
- **INFO**: Operações normais
- **DEBUG**: Detalhes de execução (busca, validação)
- **WARN**: Situações inesperadas mas tratadas
- **ERROR**: Erros que impedem operação

### 4. Executar Limpeza

Para remover arquivos compilados e caches:

```bash
mvn clean
```

Para recompilar do zero:

```bash
mvn clean install
```

---

## Funcionalidades do Sistema

### Operações Disponíveis

1. **Criar Produto**: Cadastro com validações de campos obrigatórios
2. **Listar Produtos**: Visualização de todos os produtos cadastrados
3. **Editar Produto**: Atualização de informações existentes
4. **Excluir Produto**: Remoção com confirmação
5. **Buscar por Nome**: Filtro em tempo real
6. **Filtrar Estoque Baixo**: Produtos com quantidade menor que 10

### Validações Implementadas

#### Frontend (JavaScript)
- Campos obrigatórios
- Tamanho mínimo/máximo
- Valores numéricos positivos
- Formato de preço

#### Backend (Bean Validation)
- `@NotBlank`: Nome, categoria, descrição
- `@Min(0.01)`: Preço maior que zero
- `@Min(0)`: Quantidade não negativa
- `@Size(min=3, max=100)`: Tamanhos de texto
- Regra de negócio: Nome único (não permite duplicatas)

### Tratamento de Erros

O sistema implementa fail early e fail gracefully:

**Fail Early:**
- Validações no frontend antes de enviar
- Validações Bean Validation no controller
- Verificação de duplicatas antes de salvar
- Validação de IDs positivos

**Fail Gracefully:**
- GlobalExceptionHandler captura todas as exceções
- Retorna mensagens claras sem expor detalhes internos
- Nunca exibe stack traces ao usuário
- Logs detalhados no servidor para debugging

**Códigos HTTP retornados:**
- `200 OK`: Operação bem-sucedida
- `201 Created`: Recurso criado
- `400 Bad Request`: Dados inválidos
- `404 Not Found`: Recurso não encontrado
- `422 Unprocessable Entity`: Violação de regra de negócio
- `500 Internal Server Error`: Erro inesperado

---

## Solução de Problemas

### Porta 8080 já está em uso

Erro: `Port 8080 is already in use`

Solução: Altere a porta no `application.properties`:

```properties
server.port=8081
```

### Testes Selenium falhando

Erro: `WebDriver not found`

Solução: Os testes Selenium usam ChromeDriver automaticamente via WebDriverManager. Certifique-se de ter o Chrome instalado.

### Erro de compilação

Erro: `package does not exist`

Solução: Limpe e recompile:

```bash
mvn clean install
```

### Banco de dados não persiste dados

Comportamento esperado: O sistema usa H2 em memória. Os dados são perdidos ao reiniciar.

Para persistência, altere em `application.properties`:

```properties
spring.datasource.url=jdbc:h2:file:./data/productdb
```

---

## Arquitetura e Padrões

### Arquitetura em Camadas

- **Controller**: Recebe requisições HTTP, valida entrada, retorna respostas
- **Service**: Contém lógica de negócio, validações, orquestração
- **Repository**: Acesso a dados via JPA
- **Model**: Entidades de domínio com validações
- **Exception**: Tratamento centralizado de erros

### Padrões Utilizados

- **Dependency Injection**: Injeção via Spring
- **Repository Pattern**: Abstração de acesso a dados
- **Service Layer**: Separação de responsabilidades
- **Exception Handling**: Tratamento global com @RestControllerAdvice
- **DTO Pattern**: Transfer objects para API
- **Builder Pattern**: Construção de objetos de teste

### Princípios SOLID

- **Single Responsibility**: Cada classe tem uma responsabilidade
- **Open/Closed**: Extensível sem modificar código existente
- **Dependency Inversion**: Depende de abstrações (interfaces)

---

## Documentação Adicional

Para mais detalhes, consulte:

- `docs/MANUAL_DE_USO.md`: Guia completo do usuário
- `docs/RELATORIO_TECNICO.md`: Documentação técnica detalhada
- `docs/QUICKSTART.md`: Guia rápido de início
- `docs/ENTREGA.md`: Checklist de entrega do projeto

---

## Autor

Desenvolvido como parte do Teste de Performance 3 - Desenvolvimento de Novo Sistema CRUD com Interface Web.

## Licença

Este projeto é de uso acadêmico.
