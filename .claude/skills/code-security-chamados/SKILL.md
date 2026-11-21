---
name: code-security-chamados
description: Segurança do Sistema de Chamados de TI (Spring Security, BCrypt, JPA). Use ao escrever ou revisar login, permissões, DTOs, consultas, erros ou configuração do chamados_ti_senai.
---

# Code security — Sistema de Chamados de TI

Checklist de segurança do projeto `masato173/chamados_ti_senai` (Java 21, Spring Boot 4.1.2, Spring Security 7, Spring Data JPA, PostgreSQL, Flyway). Vale para escrever código novo e para revisar código existente. Responda em português (pt-BR).

Base: documentação técnica (RNF03, RNF04, §5, §8, §13, §15, §17.3, §20 "Segurança", §22), regras RA de `protocolo-alteracao-chamados` e OWASP API Security Top 10 (2023).

## 1. Quando usar

- Mudança em `config/`, `SecurityConfig`, login, `Usuario`, senha ou perfis.
- Controller novo ou rota nova.
- DTO de entrada ou saída.
- `@Query`, `Specification`, filtros, ordenação e paginação.
- `GlobalExceptionHandler` e logs.
- `application.properties`, `pom.xml`, migrations que tocam `usuarios`.
- Pedido explícito de auditoria de segurança.

## 2. O que proteger

| Ator | Pode | Não pode |
| --- | --- | --- |
| Anônimo | `POST /api/auth/login` | Qualquer outra rota |
| `ROLE_USER` | Abrir chamado, ver os próprios chamados, o próprio perfil e o histórico pessoal | Ver chamado de outro, usar rota de admin, mudar o próprio `perfil` |
| `ROLE_ADMIN` | O que a matriz da §5 permite | Desativar o último admin, apagar histórico |

Ativos: chamados de terceiros, senhas, contas de administrador, histórico de auditoria e credenciais do banco.

Particularidades da versão:

- Spring Security 7: configuração só com `SecurityFilterChain` como bean e DSL em lambda. `WebSecurityConfigurerAdapter`, `antMatchers` e `and()` não existem; use `requestMatchers`.
- Spring Boot 4: nos testes, `@MockitoBean` no lugar de `@MockBean`.

## 3. Checklist

Cada item tem um código `S-xx` para citar no relatório. Itens marcados *(proposta)* não estão na documentação: ao implementar, registre no `MEMORY.md`. Itens com *decisão a registrar* não podem ser decididos sem o autor (ver `protocolo-alteracao-chamados`, seção 7).

### A. Autenticação (OWASP API2)

- **S-01** Um único `PasswordEncoder` BCrypt como bean. Nunca `NoOpPasswordEncoder`, senha em texto puro ou comparação com `equals`; usar `passwordEncoder.matches(...)`.
- **S-02** `UserDetailsService` busca pelo `login`. `isEnabled()` devolve `false` quando `ativo = false` ou `excluido_em` está preenchido (§13.1).
- **S-03** `getAuthorities()` devolve `ROLE_USER` ou `ROLE_ADMIN`. `hasRole("ADMIN")` procura `ROLE_ADMIN`: prefixo errado bloqueia todo mundo ou libera a rota.
- **S-04** *(proposta)* Login com falha responde sempre `401` com a mesma mensagem para login inexistente, senha errada e conta inativa.
- **S-05** DTO com senha usa `@NotBlank` e `@Size(max = 72)`. O BCrypt só considera 72 bytes e as versões atuais do Spring Security recusam senha maior; caractere acentuado ocupa 2 bytes. Tamanho mínimo: *decisão a registrar*.
- **S-06** Troca de senha em rota própria (§8.2). Quem pode trocar é a decisão pendente 7.
- **S-07** Primeiro administrador nunca com senha ou hash fixo em migration, teste ou `application.properties` (repositório público). Forma de criação: *decisão a registrar*.

#### Se a decisão pendente 1 for **sessão**

- **S-08** CSRF ligado. `csrf` desligado com autenticação por cookie é BLOQUEANTE.
- **S-09** Cookie de sessão `HttpOnly`, `Secure` em produção (`server.servlet.session.cookie.secure=true`) e com `SameSite`.
- **S-10** Logout invalida a sessão. A proteção contra fixação de sessão do Spring fica no padrão.

#### Se a decisão pendente 1 for **JWT**

- **S-11** `SessionCreationPolicy.STATELESS`. Só neste caso o CSRF pode ser desligado, com o token no cabeçalho `Authorization`.
- **S-12** Chave em variável de ambiente, com no mínimo 256 bits para HS256. Nunca no repositório.
- **S-13** Assinatura e `exp` validadas em toda requisição; expiração curta (*decisão a registrar*). Preferir o suporte do Spring (`spring-boot-starter-oauth2-resource-server`) a um filtro escrito à mão.
- **S-14** Token sem senha nem dado pessoal além do necessário. Nunca em URL ou log.
- **S-15** Usuário desativado com token ainda válido: definir se `ativo` e `perfil` são conferidos a cada requisição ou se a expiração curta basta (*decisão a registrar*).

### B. Autorização (OWASP API1, API3, API5)

- **S-16** `SecurityFilterChain` nega por padrão: rotas públicas listadas uma a uma e `.anyRequest().authenticated()` por último.
- **S-17** Rotas de admin protegidas na `SecurityConfig` com `hasRole("ADMIN")` ou com `@PreAuthorize`. Com `@PreAuthorize`, confirmar `@EnableMethodSecurity` numa classe de configuração: sem ela, a anotação é ignorada sem erro.
- **S-18** Chamado de terceiro (BOLA). Para `ROLE_USER`, a consulta inclui o solicitante autenticado:

  ```java
  Optional<Chamado> findByIdAndSolicitanteIdAndExcluidoEmIsNull(Long id, Long solicitanteId);
  ```

  Nunca buscar só pelo `id` e confiar no controller. Status da resposta (`403` ou `404`): *decisão a registrar*.
- **S-19** Listagem de `ROLE_USER`: o filtro `solicitante` da requisição é ignorado e substituído pelo usuário autenticado (§15).
- **S-20** Atribuição em massa. DTO de entrada nunca traz `id`, `solicitanteId`, `status` na abertura, datas, `excluidoEm`, `ativo` nem `senha` (fora do cadastro e da troca de senha). `perfil` só em DTO de rota de admin. Entidade nunca é recebida com `@RequestBody`.
- **S-21** Resposta: `UsuarioResponseDTO` sem `senha`; relações viram DTO ou campos simples, nunca entidade.
- **S-22** Atendimentos no detalhe do chamado para `ROLE_USER`: só os "permitidos ao perfil" (§9.3). Quais são: *decisão a registrar*.

### C. Entrada, consultas e consumo de recursos (OWASP API4, API8)

- **S-23** Todo `@RequestBody` com `@Valid`. Tamanhos iguais aos da tabela (§13): `nome`, `login` e `local` com 150; `nome` da categoria com 100.
- **S-24** Campos `TEXT` (`problema`, `observacao`, `solucao`) com `@Size(max = ...)`. Sem limite, a API aceita corpo de qualquer tamanho. Valor: *decisão a registrar*.
- **S-25** Nada de concatenar texto em JPQL, SQL nativo ou `Specification`: só parâmetros (`:nome`, `cb.equal(...)`, `cb.like(...)` com valor vindo de variável).
- **S-26** Ordenação com lista fechada de campos (`id`, `abertoEm`, `status`...). O `sort` do `Pageable` aceita caminho aninhado: `?sort=solicitante.senha` ordena pelo hash da senha e vaza informação pela ordem. Campo fora da lista → `400`.
- **S-27** Página limitada: `@PageableDefault(size = 10)` e `spring.data.web.pageable.max-page-size` *(proposta, valor a decidir)*. O padrão do Spring aceita até 2000 itens por página.
- **S-28** JSON malformado, enum inválido e tipo errado na URL (`HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`) → `400` pelo handler, sem detalhe interno.

### D. Erros e logs (OWASP API8)

- **S-29** `GlobalExceptionHandler` no formato do `ARCHITECTURE.md` §7. Exceção inesperada → `500` com mensagem genérica; nunca `ex.getMessage()`, stack trace, SQL ou nome de constraint. Um `@ExceptionHandler(Exception.class)` também captura a `AccessDeniedException` lançada pelo `@PreAuthorize`: tratar `AccessDeniedException` → `403` antes, senão a negação vira `500`.
- **S-30** `DataIntegrityViolationException` → `409` com mensagem genérica (ex.: "Login já cadastrado."), sem o texto do PostgreSQL.
- **S-31** `server.error.include-stacktrace`, `server.error.include-message` e `server.error.include-binding-errors` nunca em `always`.
- **S-32** Log sem senha, token, cabeçalho `Authorization` ou corpo de requisição com senha. `record` gera `toString()` com todos os campos: DTO com `senha` sobrescreve o `toString()` ou nunca é logado.
- **S-33** Auditoria (`valor_anterior`, `valor_novo`) nunca guarda senha nem hash. Troca de senha registra só a ação.
- **S-34** `spring.jpa.show-sql` e o log de parâmetros do Hibernate desligados fora da máquina local.

### E. Integridade, transações e concorrência

- **S-35** Último administrador (§8.4): bloquear as **linhas** dos admins ativos, não a contagem. O PostgreSQL recusa bloqueio de linha (`FOR UPDATE`) em consulta com `count(*)`, então `@Lock(PESSIMISTIC_WRITE)` numa consulta de contagem falha. Forma segura:

  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  List<Usuario> findByPerfilAndAtivoTrueAndExcluidoEmIsNull(Perfil perfil);
  ```

  O service verifica o tamanho da lista dentro da mesma transação. O texto do `ARCHITECTURE.md` §5.4 diz "conta os administradores ativos": implementar com a lista acima.
- **S-36** Exceção de negócio estende `RuntimeException`. Exceção checked não desfaz a transação por padrão.
- **S-37** `HistoricoChamado` só de inserção: sem setters, colunas com `updatable = false`, nenhum `save` de registro existente, nenhum `delete`. *(proposta)* `@Immutable` do Hibernate na entidade.
- **S-38** Usuário excluído ou inativo não autentica; chamado excluído não aparece em consulta de negócio; categoria inativa não entra em chamado novo.

### F. Configuração e segredos (OWASP API8)

- **S-39** Repositório público: nenhum segredo em código, `application*.properties`, migration, teste ou histórico do Git. Segredo vazado é trocado na hora; apagar do Git não basta.
- **S-40** *(proposta)* O `application.properties` usa `${DB_PASSWORD:postgres}` como padrão local. Aceitável só na máquina de desenvolvimento; em ambiente compartilhado a variável é obrigatória, sem valor padrão.
- **S-41** `.gitignore` mantém `.env`, `.env.*` e `application-local.properties`.
- **S-42** CORS (depende da decisão pendente 2): origens explícitas vindas de configuração; nunca `*` junto com credenciais.
- **S-43** Cabeçalhos de segurança padrão do Spring Security ligados; não desligar `headers`.
- **S-44** `spring.jpa.hibernate.ddl-auto=validate` e `spring.jpa.open-in-view=false` mantidos.
- **S-45** Se o Actuator for adicionado: expor só `health`; o resto exige `ROLE_ADMIN`.

### G. Dependências

- **S-46** Versões gerenciadas pelo `spring-boot-starter-parent` 4.1.2. Não fixar versão de dependência gerenciada sem motivo registrado no `MEMORY.md`.
- **S-47** Dependência nova só com justificativa no `MEMORY.md`. Listar com `./mvnw dependency:tree` e conferir os alertas de vulnerabilidade do GitHub (Dependabot).

## 4. Testes de segurança obrigatórios

Derivados da §20 (Segurança). Cada rota nova traz os seus.

| Caso | Esperado |
| --- | --- |
| Rota protegida sem autenticação | `401` |
| `ROLE_USER` em cada rota de admin | `403` |
| `ROLE_USER` lê, pelo `id`, chamado de outro usuário | Bloqueado (`403` ou `404`, conforme a decisão) |
| `ROLE_USER` lista chamados passando `solicitante` de outro | Só os próprios |
| Abertura com `solicitanteId` de outro usuário no JSON | Solicitante salvo = usuário autenticado |
| Qualquer resposta com usuário | `jsonPath("$.senha").doesNotExist()` |
| Login de usuário inativo ou excluído | `401` |
| Desativar o último admin ativo | `409` |
| `?sort=solicitante.senha` | `400` |
| Erro inesperado | `500` sem stack trace nem SQL |

Dicas:

- `@WithMockUser(roles = "ADMIN")` cria um `User` do Spring, não um `Usuario`. Se o service lê a entidade do `SecurityContext`, use `@WithUserDetails` com usuário de teste ou `with(user(usuario))` de `SecurityMockMvcRequestPostProcessors`.
- Em `@WebMvcTest`, importe a configuração real (`@Import(SecurityConfig.class)`); sem ela, as regras da `SecurityFilterChain` não são testadas.

## 5. Severidade

| Nível | Exemplos |
| --- | --- |
| CRÍTICA | Acesso ou alteração de dado de outro usuário, subida de privilégio, rota de admin aberta, senha ou segredo exposto, login burlável |
| ALTA | Regra RA de segurança violada sem exploração imediata, CSRF desligado com sessão, `@PreAuthorize` sem `@EnableMethodSecurity`, auditoria apagável |
| MÉDIA | Detalhe interno na resposta de erro, falta de limite de página ou de tamanho, mensagem de login que revela se a conta existe, ordenação livre |
| BAIXA | Endurecimento: cabeçalhos, logs, configuração de produção |

CRÍTICA e ALTA reprovam a revisão e equivalem a BLOQUEANTE em `code-review-chamados`.

## 6. Verificar antes de reportar

- Arquivo e linha reais, e o cenário de ataque passo a passo: quem, qual requisição, o que obtém.
- **CONFIRMADO** só com teste ou requisição executada; senão **PLAUSÍVEL**.
- Item que depende de decisão pendente vai para "Decisões pendentes", não para achados.
- Nunca afirmar que algo é seguro sem ler o código que garante isso.

## 7. Formato do relatório

````markdown
# Revisão de segurança — <escopo>

**Veredito:** SEM ACHADOS CRÍTICOS OU ALTOS / REPROVADO
**Autenticação:** sessão / JWT / não decidida

## Achados

| # | Severidade | Item | OWASP | Arquivo:linha | Resumo |
| --- | --- | --- | --- | --- | --- |
| 1 | CRÍTICA | S-18 | API1 | `service/ChamadoService.java:52` | Chamado de terceiro lido pelo id |

### 1. <título>
- **Cenário de ataque:**
- **Evidência:** CONFIRMADO / PLAUSÍVEL —
- **Correção:**
  ```java
  // trecho corrigido
  ```
- **Teste que comprova a correção:**
  ```java
  // teste
  ```

## Testes de segurança faltando
-

## Decisões pendentes que bloqueiam itens
-

## Não verificado
-
````
