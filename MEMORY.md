# MEMORY — Sistema de Gerenciamento de Chamados de TI

Memória do projeto: o que já foi decidido, o que falta decidir e onde o desenvolvimento parou. Atualize este arquivo ao fim de cada etapa.

## Estado atual

| Item | Valor |
| --- | --- |
| Etapa concluída | 0 — Estrutura do projeto |
| Próxima etapa | 1 — Models (entidades e enums) |
| Última atualização | 2026-11-15 |
| Código de domínio | Nenhum. Só `ChamadosApplication` e pastas vazias |

## Contexto

- Projeto de API para gerenciar chamados de suporte técnico de TI.
- Requisitos na documentação técnica v1.0: [`docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt`](docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt).
- Arquitetura detalhada em [`ARCHITECTURE.md`](ARCHITECTURE.md).
- Padrão de código na skill [`padrao-codigo-chamados`](.claude/skills/padrao-codigo-chamados/SKILL.md), derivada da análise do projeto Adopet (Alura), com os erros encontrados nele corrigidos.

## Decisões tomadas

| Data | Decisão | Origem |
| --- | --- | --- |
| 2026-11-15 | Java 21 | Escolha do autor |
| 2026-11-15 | Spring Boot 4.1.2 (versão estável mais recente na criação) | Etapa 0 |
| 2026-11-15 | PostgreSQL + Flyway | Documentação, RNF02 e RNF12 |
| 2026-11-15 | Maven com Maven Wrapper (Maven 3.9.16) | Documentação, seção 18 |
| 2026-11-15 | Pacote raiz `com.sistema.chamados` e pastas da seção 21 | Documentação, seção 21 |
| 2026-11-15 | Starters do Spring Boot 4: `webmvc`, `security`, `data-jpa`, `validation`, `flyway` + `flyway-database-postgresql` | Etapa 0 |
| 2026-11-15 | Credenciais do banco por variáveis de ambiente (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) | Etapa 0 |
| 2026-11-15 | `spring.jpa.hibernate.ddl-auto=validate`: o esquema é criado só pelo Flyway | Etapa 0 |
| 2026-11-15 | Repositório público `masato173/chamados_ti_senai` | Escolha do autor |

### Propostas da Etapa 0 (podem ser revistas)

Definidas para dar ponto de partida; não constam na documentação técnica.

- `@Transactional` no service, não no controller (alinhado à seção 16.3).
- Injeção de dependência por construtor com campos `final`.
- Tratamento de erros com `@RestControllerAdvice` e as exceções `RegraNegocioException` (400), `RecursoNaoEncontradoException` (404) e `ConflitoException` (409).
- Rotas com prefixo `/api` conforme a tabela da seção 6 do `ARCHITECTURE.md`.
- Bloqueio pessimista na contagem de administradores ativos.
- Datas em `OffsetDateTime` para colunas `TIMESTAMPTZ`.

## Decisões pendentes

| Nº | Questão | Precisa estar decidida antes de |
| --- | --- | --- |
| 1 | Autenticação por **sessão** ou **JWT** (seção 18 da documentação) | Etapa 7 |
| 2 | Haverá frontend? Qual tecnologia? Isso influencia a decisão 1 e CORS | Etapa 7 |
| 3 | Um chamado `ABERTO` pode ir direto para `FECHADO`, sem passar por `EM_ANDAMENTO`? | Etapa 5 |
| 4 | Como funciona a reabertura (`CHAMADO_REABERTO` existe, mas não há RF nem regra) | Etapa 5 |
| 5 | No encerramento, o "Responsável" é escolhido no formulário (seção 10.2) ou é o admin autenticado (seção 10.3)? | Etapa 5 |
| 6 | O responsável do chamado precisa ser `ADMIN`? Pode ser alterado depois de `FECHADO`? | Etapa 5 |
| 7 | Quem altera a própria senha: só o admin (seção 8.2) ou o usuário também? | Etapa 5 |
| 8 | Haverá CRUD de categorias ou só a carga inicial? | Etapa 6 |
| 9 | Formato do login: texto livre ou e-mail (`@Email`)? | Etapa 3 |
| 10 | Versão do PostgreSQL usada no desenvolvimento | Etapa 8 |

Observação: o sumário da documentação técnica lista 26 seções; o corpo tem 24 com numeração diferente. As referências deste projeto usam a numeração do corpo.

## Erros a não repetir (lições do Adopet)

Encontrados na análise do projeto Adopet e já cobertos pela skill:

1. Contador declarado dentro do `for`, que zera a cada volta.
2. `catch` de `jakarta.validation.ValidationException` no lugar da exceção de negócio.
3. Mudança de status que não atualiza todos os campos afetados.
4. Notificação enviada ao destinatário errado.
5. `getReferenceById` sem verificar se o registro existe.
6. `findAll()` seguido de filtro em Java.
7. Comparar entidades com `==`.

## Registro das etapas

### Etapa 0 — Estrutura do projeto (2026-11-15)

Criado:

- `pom.xml` com Spring Boot 4.1.2, Java 21 e as dependências da seção 18.
- Maven Wrapper 3.3.4 (modo script, sem `.jar` no repositório) apontando para Maven 3.9.16.
- `ChamadosApplication.java` e `application.properties`.
- Pastas vazias da seção 21 com `.gitkeep`.
- `README.md`, `ARCHITECTURE.md`, `MEMORY.md`, `CLAUDE.md`, `.editorconfig`, `.gitattributes`, `.gitignore`.
- Skill `.claude/skills/padrao-codigo-chamados/SKILL.md`.
- Cópia da documentação técnica em `docs/`.

Não verificado: o build (`./mvnw verify`) não foi executado na criação. Rodar na primeira sessão local.

<!-- Modelo para as próximas etapas:

### Etapa N — Nome (AAAA-MM-DD)

Criado:
- ...

Decisões:
- ...

Pendências:
- ...
-->
