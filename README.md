# Marketplace Desafio API

API REST de marketplace com catálogo de produtos, carrinho e confirmação de pagamento, protegida com autenticação JWT e senhas armazenadas com BCrypt. Projeto do desafio em grupo FIAP/CAIXAVERSO.

## Divisão do grupo
Grupo 4

Rodrigo Gomes 
Rodrigo Machado 
Michele Lima 
Kenia Santos
Luiz Eduardo

## Tecnologias

- Java 25
- Spring Boot 4.1.1 (Web, Data JPA, Security, OAuth2 Resource Server, Validation)
- PostgreSQL (Supabase, session pooler)
- Swagger/OpenAPI (springdoc)
- JUnit 5, AssertJ, Mockito e MockMvc
- Maven

## Como executar

### Pré-requisitos

- Java 25 e Maven instalados
- Banco PostgreSQL acessível (usamos o Supabase) com as tabelas criadas pelos scripts SQL do desafio

### Variáveis de ambiente

| Variável | Descrição | Obrigatória |
|---|---|---|
| `DB_PASSWORD` | Senha do banco. Não fica no repositório. | Sim |
| `DB_URL` | URL JDBC do banco. Há um valor padrão no `application.properties`. | Não |
| `DB_USER` | Usuário do banco. Há um valor padrão no `application.properties`. | Não |
| `JWT_SECRET` | Chave em Base64 com no mínimo 32 bytes. Há um valor padrão apenas para desenvolvimento. | Não |
| `JWT_ISSUER` | Emissor do token (`iss`). Padrão: `marketplace-desafio-api`. | Não |
| `JWT_EXPIRATION_SECONDS` | Validade do token em segundos. Padrão: `900`. | Não |

### Subindo a aplicação (PowerShell)

```powershell
$env:DB_PASSWORD="sua_senha_do_banco"
mvn spring-boot:run
```

- A aplicação sobe na porta **8082**.
- Swagger: http://localhost:8082/swagger-ui.html

### Rodando os testes

```powershell
mvn test
```

## Arquitetura

Camadas: `controller` → `service` → `repository` → `entity`, com `dto` para entrada e saída de dados.

- **Controllers** recebem e devolvem somente DTOs e delegam ao service. Não acessam repositories.
- **Entities** protegem o próprio estado (ex.: `Carrinho.finalizar()`, `ConfirmacaoPagamento.aprovar()`, `CatalogoProduto.baixarEstoque()`).
- **Services** coordenam regras que envolvem mais de uma entidade e abrem as transações com `@Transactional`. A conversão para DTO acontece dentro da transação, por causa dos relacionamentos `LAZY`.
- **Repositories** usam consultas derivadas do Spring Data JPA.
- **Tratamento de erros** centralizado no `GlobalExceptionHandler`, e o 401 de token ausente ou inválido no `JwtAuthenticationEntryPoint`.
- **Segurança**: `SecurityConfig` define as rotas públicas e protege as demais com Bearer JWT.

## Rotas

| Método | Rota | Acesso | Sucesso |
|---|---|---|---|
| POST | `/api/usuarios` | Pública | 201 |
| GET | `/api/usuarios/{id}` | Protegida | 200 |
| POST | `/api/auth/login` | Pública | 200 |
| GET | `/api/produtos` | Pública | 200 |
| GET | `/api/produtos/{id}` | Pública | 200 |
| POST | `/api/produtos` | Protegida | 201 |
| PUT | `/api/produtos/{id}` | Protegida | 200 |
| PATCH | `/api/produtos/{id}/estoque` | Protegida | 200 |
| POST | `/api/carrinhos` | Protegida | 201 |
| GET | `/api/carrinhos/{id}` | Protegida | 200 |
| GET | `/api/carrinhos/usuario/{usuarioId}` | Protegida | 200 |
| PATCH | `/api/carrinhos/{id}/quantidade` | Protegida | 200 |
| DELETE | `/api/carrinhos/{id}` | Protegida | 204 |
| POST | `/api/pagamentos` | Protegida | 201 |
| GET | `/api/pagamentos/{id}` | Protegida | 200 |
| PATCH | `/api/pagamentos/{id}/aprovar` | Protegida | 200 |
| PATCH | `/api/pagamentos/{id}/recusar` | Protegida | 200 |

As rotas de criação (`POST`) devolvem o header `Location` apontando para o novo recurso.

## Fluxo principal

1. Cadastrar usuário (`POST /api/usuarios`).
2. Fazer login (`POST /api/auth/login`) e copiar o `accessToken`.
3. No Swagger, clicar em **Authorize** e colar somente o token.
4. Consultar ou cadastrar produtos (`/api/produtos`).
5. Criar um carrinho (`POST /api/carrinhos`).
6. Criar a confirmação de pagamento, que nasce `PENDENTE` (`POST /api/pagamentos`).
7. Aprovar o pagamento (`PATCH /api/pagamentos/{id}/aprovar`): o estoque é baixado, o pagamento vira `PAGO` e o carrinho vira `FINALIZADO`.

## Contrato de erros

| Status | Quando |
|---|---|
| 400 | Campos inválidos (Bean Validation) ou JSON malformado |
| 401 | Token ausente, inválido ou expirado; credenciais incorretas |
| 404 | Recurso não encontrado |
| 409 | Conflito de negócio (e-mail duplicado, pagamento já processado, estoque insuficiente, estado incompatível) |
| 500 | Erro interno inesperado |

Formato padronizado:

```json
{
  "timestamp": "2026-10-07T19:52:34.227Z",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Pagamento já foi processado anteriormente.",
  "caminho": "/api/pagamentos/2/aprovar",
  "campos": {}
}
```

Mapeamento das exceções de domínio:

| Exceção | Status |
|---|---|
| `RecursoNaoEncontradoException` | 404 |
| `ConflitoNegocioException` | 409 |
| `RegraNegocioException` | 409 |
| `CredenciaisInvalidasException` | 401 |

## Decisões de negócio

- **Senha**: o BCrypt é aplicado no service. A entity recebe apenas o hash, e nenhuma resposta devolve senha ou hash.
- **E-mail**: normalizado (sem espaços e em minúsculas) e único.
- **Login**: a mensagem de erro é a mesma para e-mail inexistente, usuário inativo e senha incorreta, para não revelar quais e-mails existem.
- **JWT**: assinado com HS256, validade de 15 minutos, com o id do usuário no campo `sub`.
- **Carrinho**: exige usuário ativo e produto ativo. Somente carrinho `ABERTO` pode ser alterado, finalizado ou cancelado.
- **Estoque**: não é validado na criação do carrinho. A baixa acontece somente na aprovação do pagamento.
- **DELETE de carrinho**: não apaga o registro. Muda o status para `CANCELADO`, mantendo o histórico.
- **Pagamento**: o `valorPago` é calculado a partir do total do carrinho, o cliente não o informa. O `idPagamento` é único e cada carrinho tem no máximo uma confirmação.
- **Dono do carrinho**: o usuário informado no pagamento precisa ser o dono do carrinho.
- **Aprovação**: acontece em uma única transação. Baixa o estoque, marca o pagamento como `PAGO` e finaliza o carrinho. Se o estoque for insuficiente, tudo é desfeito.
- **Pagamento processado**: só muda de estado uma vez (`PENDENTE` para `PAGO` ou `RECUSADO`). Uma nova tentativa retorna 409.
- **Recusa**: não altera o estoque nem o carrinho.

## Testes

| Classe | Tipo | O que comprova |
|---|---|---|
| `CarrinhoTest` | Unitário | Cálculo do total (preço x quantidade) e bloqueio de alteração em carrinho finalizado |
| `CatalogoProdutoTest` | Unitário | Baixa de estoque com saldo, bloqueio de baixa maior que o saldo e rejeição de preço zero |
| `CarrinhoControllerIntegrationTest` | Integração (`@WebMvcTest` + MockMvc) | Criação de carrinho com 201 e `Location`, e erro 400 para quantidade inválida |

Os testes unitários não iniciam o Spring nem acessam banco. O teste de integração usa o `CarrinhoService` mockado.

Para executar: `mvn test`.



## Evidências de execução

Os prints estão na pasta `docs/evidencias/`.

| Evidência | Arquivo |
|---|---|
| Cadastro de usuário sem senha na resposta (201) | `docs/evidencias/01-cadastro-usuario-201.png` |
| E-mail duplicado (409) | `docs/evidencias/02-email-duplicado-409.png` |
| Rota protegida sem token (401) | `docs/evidencias/03-sem-token-401.png` |
| Login com senha errada (401) | `docs/evidencias/04-login-senha-errada-401.png` |
| Login correto com token (200) | `docs/evidencias/05-login-ok.png` |
| Swagger autorizado | `docs/evidencias/06-authorize.png` |
| Estoque antes da aprovação | `docs/evidencias/07-estoque-antes.png` |
| Carrinho criado (`ABERTO`) | `docs/evidencias/08-carrinho-criado.png` |
| Pagamento pendente | `docs/evidencias/09-pagamento-pendente.png` |
| Pagamento aprovado (`PAGO`) | `docs/evidencias/10-pagamento-aprovado.png` |
| Estoque depois da aprovação | `docs/evidencias/11-estoque-depois.png` |
| Carrinho `FINALIZADO` | `docs/evidencias/12-carrinho-finalizado.png` |
| Aprovar duas vezes (409) | `docs/evidencias/13-aprovar-duas-vezes-409.png` |

## Observações de segurança

- Nenhuma senha de banco ou token fica versionada no repositório. A senha vem da variável de ambiente `DB_PASSWORD`.
- O `JWT_SECRET` padrão serve apenas para desenvolvimento. Em produção, deve ser definido por variável de ambiente.
