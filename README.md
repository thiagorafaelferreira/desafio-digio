# Purchase Analytics API

API REST desenvolvida em Spring Boot para análise de compras de vinhos e recomendações personalizadas para clientes.

## Tecnologias

- **Java 21**
- **Spring Boot 3.5.6**
- **Maven**
- **SpringDoc OpenAPI** (Swagger)
- **Spring Security**
- **Spring Actuator**

## Funcionalidades

### 1. Clientes Fiéis
Identifica e ranqueia os 3 clientes mais fiéis com base no valor total gasto e frequência de compras.

**Endpoint:** `GET /v1/clientes-fieis`

**Resposta:**
```json
[
  {
    "cpf": "12345678900",
    "nome": "João Silva",
    "totalGasto": 5000.00,
    "quantidadeCompras": 15
  }
]
```

### 2. Listagem de Compras
Lista todas as compras ordenadas por valor crescente.

**Endpoint:** `GET /v1/compras`

**Resposta:**
```json
[
  {
    "cpf": "12345678900",
    "nome": "João Silva",
    "codigoProduto": "VINHO001",
    "tipoVinho": "Tinto",
    "preco": 150.00,
    "safra": "2020",
    "anoCompra": 2023,
    "quantidade": 2,
    "valorTotal": 300.00
  }
]
```

### 3. Maior Compra do Ano
Retorna a compra de maior valor em um ano específico.

**Endpoint:** `GET /v1/maior-compra/{ano}`

**Parâmetros:**
- `ano` (path): Ano com 4 dígitos (ex: 2023)

### 4. Recomendação de Vinhos
Analisa o histórico de compras e recomenda o tipo de vinho que cada cliente mais compra.

**Endpoint:** `GET /v1/recomendacao/cliente/tipo`

**Resposta:**
```json
[
  {
    "nome": "João Silva",
    "cpf": "12345678900",
    "tipoVinho": "Tinto",
    "preco": 150.00,
    "safra": "2020"
  }
]
```

## Configuração

### Pré-requisitos

- Java 21 ou superior
- Maven 3.6+

### Executando a Aplicação

1. Clone o repositório
2. Execute o comando:

```bash
./mvnw spring-boot:run
```

Ou no Windows:

```bash
mvnw.cmd spring-boot:run
```

### Usando Docker para rodar a aplicacao

1. Build the Docker image:
   ```bash
   docker build -t digio-app .
   ```

2. Run the container:
   ```bash
   docker run -d -p 8080:8080 --name digio-container digio-app
   ```

A aplicação estará disponível em `http://localhost:8080`

### Autenticação

A API utiliza autenticação básica (Basic Auth):

- **Usuário:** `admin`
- **Senha:** `password`

Para alterar as credenciais, edite o arquivo `src/main/resources/application.yml` 
ou passe por variavel de ambiente os novos valores:

```yaml
spring:
  security:
    user:
      name: ${SECURITY_USER_NAME:superadmin}
      password: ${SECURITY_USER_PASSWORD:superpassword}
      roles: ${SECURITY_USER_ROLE:ADMIN}
```

## Documentação da API

A documentação interativa Swagger está disponível em:

```
http://localhost:8080/swagger-ui.html
```

## Fonte de Dados

A aplicação consome dados de um serviço externo hospedado no Vercel Blob Storage:

- **Produtos:** Lista de vinhos com código, tipo, preço e safra
- **Clientes:** Lista de clientes com CPF, nome e histórico de compras

A configuração da URL base está em `application.yml`:

```yaml
vercel:
  base-url: https://rgr3viiqdl8sikgv.public.blob.vercel-storage.com
```

## Monitoramento

A aplicação expõe endpoints do Spring Actuator para monitoramento:

- **Health:** `GET /actuator/health`
- **Metrics:** `GET /actuator/metrics`
- **Info:** `GET /actuator/info`

## Estrutura do Projeto

```
src/main/java/com/bancodigio/purchase/analytics/api/
├── Application.java              # Entry point
├── controller/                   # Endpoints REST
│   ├── ClientesController.java
│   ├── CompraController.java
│   └── RecomendacaoController.java
├── service/                      # Lógica de negócio
│   ├── ClienteService.java
│   ├── CompraService.java
│   └── RecomendacaoService.java
├── client/                       # Cliente HTTP externo
│   └── VercelClient.java
├── gateway/                      # Tratamento de erros externos
│   └── VercelGateway.java
├── dto/                          # Objetos de transferência
├── config/                       # Configurações
└── exception/                    # Exceções customizadas
```

## Tratamento de Erros

A API retorna respostas padronizadas para erros:

```json
{
  "status": 404,
  "message": "Compra não encontrada para o ano especificado",
  "timestamp": "2025-10-15T10:30:00"
}
```

### Códigos HTTP

- `200` - Sucesso
- `400` - Requisição inválida
- `404` - Recurso não encontrado
- `422` - Erro no serviço externo (4xx)
- `500` - Erro interno do servidor
- `503` - Serviço externo indisponível
- `504` - Timeout no serviço externo

## Build

Para compilar o projeto:

```bash
./mvnw clean package
```

O arquivo JAR será gerado em `target/purchase.analytics.api-0.0.1-SNAPSHOT.jar`

Para executar o JAR:

```bash
java -jar target/purchase.analytics.api-0.0.1-SNAPSHOT.jar
```

## Testes

Para executar os testes:

```bash
./mvnw test
```

