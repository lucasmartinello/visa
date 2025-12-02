# 📘 Contrato da API – Cartões (Cards API)

## 🏷 Base URL

/api/cards

### 📌 1. Consultar cartão – Verifica se o cartão existe

GET /api/cards/check<br>
Descrição

Verifica se um cartão existe na base.
Retorna o ID caso exista.

{
"id": 12345
}

❌ 404 Not Found

"Card not found"

### 📌 2. Inserção de um único cartão

POST /api/cards/single<br>
Descrição

Insere um cartão individualmente.

Body (JSON)
{
"cardNumber": "ABC123"
}

Responses
✔ 200 OK

Retorna o ID do cartão criado.

12345

❌ 409 Conflict

"Card already exist"

### 📌 3. Inserção em lote via arquivo

POST /api/cards/upload<br>
Descrição

Recebe um arquivo TXT contendo vários números de cartão e insere todos.

Form-Data

Responses
✔ 200 OK

Retorna uma lista com os IDs criados.

[123, 124, 125, 200]

### 🔐 4. Autenticação – Login

POST /auth/login<br>
Descrição

Valida credenciais do usuário e retorna um token JWT caso as informações estejam corretas.

Body (JSON)
{
"username": "usuario123",
"password": "senhaSegura"
}

Responses
✔ 200 OK

Retorna o token JWT.

{
"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}

❌ 401 Unauthorized

"User not found"

"Invalid password"

### 🔒 Erros Comuns

400	Request malformado<br>
404	Cartão não encontrado<br>
409	Cartão duplicado<br>
500	Erro interno inesperado