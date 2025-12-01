# API de Cartões

Esta API permite autenticação com token, inserção de cartões, consulta de cartões e inserção em lote.
O objetivo do serviço é fornecer uma interface simples, segura e performática para manipulação de cartões no ambiente corporativo.

## 📦 Requisitos

Antes de iniciar, você precisa ter:

JDK 17+

MySQL

Postman para testes

Porta padrão: 8080

Todas as requisições são armazenadas na tabela api_logs no banco

No MySQL criar schema chamado carddb antes de rodar a aplicação. Ao rodar o sistema vai criar as tabelas e inserir o usuário admin <br>
O sistema vai se logar no banco em localhost:3306/carddb com usuário _root_ e senha _root_

## ▶️ Como executar

Clone o repositório:

git clone https://github.com/lucasmartinello/visa.git
cd api-cartoes


Execute a aplicação:

mvn spring-boot:run


A API estará disponível em:

http://localhost:8080

## 🔐 Autenticação

Todos os endpoints, exceto o de login, requerem token JWT no header:

Authorization: Bearer <seu-token>

Obtenha o token chamando o endpoint /auth/login

## 📡 Endpoints

#### 1. Autenticação

   POST /auth/login
Autentica o usuário e retorna um token JWT.

**Body**
{
"username": "admin",
"password": "123456"
}

**Resposta**
{
"token": "<jwt-aqui>"
}

#### 2. Inserir Cartão

   POST /cards/single

Cria um novo cartão no sistema.

Headers
Authorization: Bearer <token>

Body
{
"cardNumber": "1111111111"
}

Resposta
444

#### 3. Inserir Cartões em Lote

   POST /cards/upload

Permite inserir vários cartões de uma só vez através de um arquivo txt

Body
form-data
Inserir arquivo .TXT no formato correto

Resposta
[
1,2,3
]

#### 4. Consultar Cartão

   GET /cards/check?cardNumber={number}

Retorna o id único do cartão no banco.

Headers
Authorization: Bearer <token>

Exemplo
GET /cards/check?cardNumber=1111111111

Resposta
{
"id": 444
}

## 📞 Contato

Para dúvidas, sugestões ou melhorias, envie uma mensagem em lucas.martinello@gmail.com