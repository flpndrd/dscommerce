# DSCommerce

## 📄 Descrição

API REST de e-commerce desenvolvida como projeto do curso Java Spring Essential da DevSuperior, com catálogo de produtos, pedidos e controle de acesso por token JWT.

## 🎯 Objetivo do projeto

Entregar o projeto DSCommerce estruturado e com todas as funcionalidades implementadas, usando banco de dados H2, conforme os critérios de correção do desafio.

## 🛠 Tecnologias utilizadas

- Java 25
- Spring Boot 4.1.1
- Spring Data JPA
- Hibernate
- Bean Validation
- Spring Security (Resource Server com JWT)
- Banco de dados H2
- JUnit
- Mockito
- AssertJ
- JaCoCo

## 📐 Modelo de Domínio

- **User**: `userId`, `name`, `email` (único), `phone`, `birthDate`, `password`, com papéis (`ROLE_CLIENT`, `ROLE_ADMIN`)
- **Role**: `roleId`, `authority`
- **Category**: `categoryId`, `name`
- **Product**: `productId`, `name`, `description`, `price` (`BigDecimal`), `imageUrl`, com uma ou mais categorias
- **Order**: `orderId`, `moment`, `status`, cliente, pagamento e itens
- **OrderItem**: `quantity` e `price` (o preço do produto no momento do pedido)
- **Payment**: `paymentId` (o mesmo id do pedido), `moment`

No JSON, os ids continuam como `id` e a imagem como `imgUrl`, como no contrato do curso.

## 📦 Carga Inicial do Banco de Dados

A aplicação carrega automaticamente 3 categorias, 25 produtos, 2 usuários e 3 pedidos:

| Usuário | Senha | Papéis |
|---|---|---|
| `maria@gmail.com` | `123456` | CLIENT |
| `alex@gmail.com` | `123456` | CLIENT, ADMIN |

## 🔎 Funcionalidades

### Login

| Método | Endpoint | Acesso |
|---|---|---|
| POST | /oauth2/token | Cliente da aplicação (Basic) |

### Produtos

| Método | Endpoint | Acesso |
|---|---|---|
| GET | /products | Público |
| GET | /products/{productId} | Público |
| POST | /products | ADMIN |
| PUT | /products/{productId} | ADMIN |
| DELETE | /products/{productId} | ADMIN |

### Categorias

| Método | Endpoint | Acesso |
|---|---|---|
| GET | /categories | Público |

### Usuários

| Método | Endpoint | Acesso |
|---|---|---|
| GET | /users/me | ADMIN, CLIENT |

### Pedidos

| Método | Endpoint | Acesso |
|---|---|---|
| GET | /orders/{orderId} | ADMIN (qualquer pedido), CLIENT (só os próprios) |
| POST | /orders | CLIENT |

- A busca de produtos é paginada (`content` e `page`) e filtra pelo trecho do nome no parâmetro `name`, sem diferenciar maiúsculas de minúsculas.
- O pedido é criado com status `WAITING_PAYMENT`, para o cliente logado, com o preço atual de cada produto.

## ⚠️ Tratamento de Exceções

Os erros seguem o formato `ProblemDetail` (RFC 9457).

- `400 Bad Request` — produto ou categoria inexistente no corpo da requisição, produto repetido no pedido, `grant_type` diferente de `password` ou parâmetro obrigatório ausente
- `401 Unauthorized` — token ausente ou inválido, ou credenciais de login inválidas
- `403 Forbidden` — usuário logado sem o papel exigido pela rota
- `404 Not Found` — produto ou pedido inexistente; o pedido de outro cliente também responde 404, para não revelar que ele existe
- `409 Conflict` — exclusão de produto que está em pedidos
- `422 Unprocessable Content` — erro de validação, com uma entrada por erro na propriedade `errors` (`fieldName` e `message`); um campo pode ter mais de uma

## 🔐 Segurança

- **Login:** o `POST /oauth2/token` valida o cliente da aplicação (Basic) e o usuário (`username` e `password` no formulário), e devolve um JWT com as claims `username` e `authorities`.

## ⚙️ Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `CLIENT_ID` | `myclientid` | Id do cliente da aplicação no login |
| `CLIENT_SECRET` | `myclientsecret` | Segredo do cliente da aplicação no login |
| `JWT_DURATION` | `86400` | Duração do token, em segundos |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens liberadas para o front-end |

Os valores padrão de `CLIENT_ID` e `CLIENT_SECRET` são os do ambiente do Postman do curso.

## 🚀 Como Executar

1. Clonar o repositório
2. Importar o projeto em sua IDE como projeto Maven
3. Executar a classe principal da aplicação (anotada com `@SpringBootApplication`) pela IDE ou via terminal:

```bash
./mvnw spring-boot:run
```

## ✅ Validação

Após executar a aplicação, o banco de dados pode ser acessado via H2 Console para verificação das tabelas e dados inseridos.

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuário: sa
- Senha: (vazia)

## 🧪 Testes

Os testes unitários cobrem os services, o usuário autenticado e o erro inesperado; os testes de integração cobrem todos os endpoints, inclusive os critérios do desafio e os erros 400, 401, 403, 404, 409 e 422:

```bash
./mvnw clean verify
```

O relatório do JaCoCo, com os testes unitários e de integração somados, fica em `target/site/jacoco/index.html`. Ficam fora da medição a classe principal e as classes de configuração.

## 📌 Exemplos de Requisições

### Login

```http
POST /oauth2/token
Authorization: Basic bXljbGllbnRpZDpteWNsaWVudHNlY3JldA==
Content-Type: application/x-www-form-urlencoded
```

```
grant_type=password&username=maria@gmail.com&password=123456
```

Nas requisições seguintes, o token vai no cabeçalho `Authorization: Bearer <access_token>`.

### Busca paginada de produtos

```http
GET /products?name=pc&page=0&size=12&sort=name
```

### Inserção de produto (ADMIN)

```http
POST /products
```

```json
{
  "name": "Novo Produto",
  "description": "Descrição do produto",
  "price": 199.90,
  "imgUrl": "https://example.com/img.jpg",
  "categories": [
    {
      "id": 1
    }
  ]
}
```

### Criação de pedido (CLIENT)

```http
POST /orders
```

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

### Busca de pedido por ID

```http
GET /orders/1
```

### Dados do usuário logado

```http
GET /users/me
```
