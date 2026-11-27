# MEMORY — Sistema de Gerenciamento de Chamados de TI

Memória do projeto: o que já foi decidido, o que falta decidir e onde o desenvolvimento parou. Atualize este arquivo ao fim de cada etapa.

## Estado atual

| Item | Valor |
| --- | --- |
| Etapa concluída | 1 — Models (entidades e enums) |
| Próxima etapa | 2 — DTOs |
| Última atualização | 2026-11-27 |
| Código de domínio | Enums `Perfil`, `StatusChamado`, `AcaoHistorico` e entidades `Usuario`, `Categoria`, `Chamado`, `Atendimento`, `HistoricoChamado`, com testes unitários |
| Atenção | Ainda não há migrations. Até a Etapa 8, `spring-boot:run` deve falhar na validação do esquema (`ddl-auto=validate` sem tabelas). O `./mvnw clean verify` não sobe a aplicação e passa |

## Contexto

- Projeto de API para gerenciar chamados de suporte técnico de TI.
- Requisitos na documentação técnica v1.0: [`docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt`](docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt).
- Arquitetura detalhada em [`ARCHITECTURE.md`](ARCHITECTURE.md).
- Padrão de código na skill [`padrao-codigo-chamados`](.claude/skills/padrao-codigo-chamados/SKILL.md), derivada da análise do projeto Adopet (Alura), com os erros encontrados nele corrigidos.
- Regras absolutas (RA-01 a RA-15) e protocolo de alteração na skill [`protocolo-alteracao-chamados`](.claude/skills/protocolo-alteracao-chamados/SKILL.md).
- Revisão de código na skill [`code-review-chamados`](.claude/skills/code-review-chamados/SKILL.md) e checklist de segurança (S-01 a S-47) na skill [`code-security-chamados`](.claude/skills/code-security-chamados/SKILL.md).

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
| 2026-11-26 | Mapear só os relacionamentos necessários: `@OneToMany` só em `Chamado.atendimentos` (`mappedBy = "chamado"`), porque o detalhe do chamado apresenta os registros de atendimento permitidos ao perfil (§9.3); quais registros o usuário comum vê é a decisão pendente 13. `Usuario` e `Categoria` sem coleções, e `Chamado` sem coleção de histórico. **Diverge da §14.1** (`@OneToMany` em `Usuario`, `Categoria` e `Chamado` → `HistoricoChamado`) e do `ARCHITECTURE.md` §4.1 (histórico em `Chamado`); segue a §14.3 ("evitar relacionamentos bidirecionais desnecessários"). Os chamados de um usuário ou categoria vêm por consulta paginada no repository, e a auditoria de um chamado também, na rota de admin. Corrigido em 2026-11-27: saiu a coleção de histórico | Autor, Etapa 1 |
| 2026-11-26 | `equals` por `instanceof` + `getId()`, com id nulo nunca igual; `hashCode` fixo por classe (`Usuario.class.hashCode()`), que não muda quando o banco gera o id. Substitui o modelo antigo da skill `padrao-codigo-chamados` (`getClass()` + campo), que dá `false` ao comparar a entidade com o proxy `LAZY` dela mesma. `hashCode` corrigido em 2026-11-27 (antes era `Objects.hashCode(id)`) | Autor, Etapa 1 |
| 2026-11-26 | Testes unitários das entidades já na Etapa 1, em `src/test/java/com/sistema/chamados/model/` (pasta nova, fora da árvore da §21) | Autor, Etapa 1 |
| 2026-11-27 | Forma de trabalho: o autor não refaz mais as etapas como exercício. Em cada etapa, o Claude Code implementa, explica cada arquivo novo ou alterado (o que cada anotação ou decisão faz e por quê) e aplica um quiz de 5 questões de múltipla escolha (4 alternativas, uma correta) sobre o porquê das escolhas. Respostas e explicações só depois que o autor responder. Regra incluída no `CLAUDE.md` e na Fase 3 da skill `protocolo-alteracao-chamados` | Autor |

### Propostas da Etapa 0 (podem ser revistas)

Definidas para dar ponto de partida; não constam na documentação técnica.

- `@Transactional` no service, não no controller (alinhado à seção 16.3).
- Injeção de dependência por construtor com campos `final`.
- Tratamento de erros com `@RestControllerAdvice` e as exceções `RegraNegocioException` (400), `RecursoNaoEncontradoException` (404) e `ConflitoException` (409).
- Rotas com prefixo `/api` conforme a tabela da seção 6 do `ARCHITECTURE.md`.
- Bloqueio pessimista na contagem de administradores ativos.
- Datas em `OffsetDateTime` para colunas `TIMESTAMPTZ`.

### Propostas da Etapa 1 (podem ser revistas)

Não constam na documentação técnica.

- `Atendimento` e `HistoricoChamado` só de inserção: `@Immutable` (Hibernate) e `updatable = false` em todas as colunas, sem setters (S-37, RA-04, RA-05).
- `solicitante_id` com `updatable = false`: a §9.4 não lista o solicitante entre os campos que o admin altera.
- `atualizado_em` fica nulo na criação e só é preenchido pelo `@PreUpdate`.
- O getter de `Chamado.atendimentos` devolve lista só de leitura.
- Construtores recebem parâmetros simples, não DTO. O modelo da skill recebe `ChamadoRequestDTO`, que só existe a partir da Etapa 2.
- `Usuario` recebe a senha já criptografada (parâmetro `senhaCriptografada`). Quem codifica é o service, com o `PasswordEncoder`.
- [x] Atualizar o modelo de entidade da skill `padrao-codigo-chamados` com o `equals` adotado. **Feito em 2026-11-27**, junto com o `hashCode` fixo por classe e a regra de asserções do JUnit 5 (`Assertions.xxx`, sem AssertJ).

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
| 11 | Usuário comum que acessa chamado de outro recebe `403` ou `404`? | Etapa 5 |
| 12 | Como é criado o primeiro administrador, sem senha nem hash no repositório público? | Etapa 7 |
| 13 | Quais registros de atendimento o usuário comum vê no detalhe do chamado (seção 9.3)? | Etapa 2 |
| 14 | Tamanho máximo dos campos `TEXT` (`problema`, `observacao`, `solucao`) e da página (`size`) | Etapa 2 (campos) e Etapa 6 (página) |
| 15 | Política mínima de senha (tamanho mínimo; o máximo é 72 bytes, limite do BCrypt) | Etapa 2 |
| 16 | §9.4 "alterar categoria, quando permitido": em que casos a categoria pode mudar? Um chamado `FECHADO` pode ser atualizado? | Etapa 5 |
| 17 | §8.2 "dados permitidos": quais campos do usuário o admin pode atualizar? Trocar o perfil de `ADMIN` para `USER` passa pela proteção do último administrador (§8.4)? | Etapa 2 |
| 18 | Existe desativação de conta separada da exclusão lógica (§8.1 "situação da conta", §8.4 "desativar ou excluir")? Existe reativação? | Etapa 5 |

As decisões 11 a 15 foram levantadas na criação das skills de revisão e segurança (2026-11-19). As decisões 16 a 18 foram levantadas na Etapa 1 (2026-11-26).

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

### Ajuste da Etapa 0 — Skills de processo (2026-11-19)

Criado:

- `.claude/skills/protocolo-alteracao-chamados/SKILL.md`: regras absolutas RA-01 a RA-15, níveis de risco, protocolo em 3 fases, ficha de alteração e relatório final.
- `.claude/skills/code-review-chamados/SKILL.md`: revisão em 7 passadas, severidade e formato do relatório.
- `.claude/skills/code-security-chamados/SKILL.md`: checklist S-01 a S-47 ligado ao OWASP API Top 10 (2023) e testes de segurança obrigatórios.
- `CLAUDE.md` e `README.md` passam a citar as skills.

Decisões:

- Decisões pendentes 11 a 15 adicionadas à tabela.
- Ao implementar a proteção do último administrador (`ARCHITECTURE.md`, seção 5.4), bloquear as linhas dos administradores ativos e verificar o tamanho da lista; o PostgreSQL recusa `FOR UPDATE` em consulta com `count(*)` (skill `code-security-chamados`, item S-35).

Não verificado: build não executado (alteração só de documentação e skills; nenhum código Java mudou).

### Etapa 1 — Models (2026-11-26)

Criado:

- Enums `Perfil`, `StatusChamado` e `AcaoHistorico`, com os valores do `ARCHITECTURE.md` §4.2.
- Entidades `Usuario`, `Categoria`, `Chamado`, `Atendimento` e `HistoricoChamado`, com os campos, tipos e tamanhos da §13.
- Métodos com regra definida na documentação:
  - construtores de criação: `Chamado` começa `ABERTO` (§6.1) e `Usuario` começa ativo (§13.1);
  - `excluir()` em `Usuario` (§8.3) e em `Chamado` (§9.5);
  - datas por `@PrePersist` e `@PreUpdate` (RF09, §14.2).
- Testes unitários em `src/test/java/com/sistema/chamados/model/` (29 casos, sem Spring e sem banco).
- Removidos os `.gitkeep` de `enums/` e `model/`.

Decisões: as três linhas de 2026-11-26 na tabela "Decisões tomadas", incluindo a divergência da §14.1, e as "Propostas da Etapa 1".

Não implementado porque depende de decisão:

- `alterarStatus`, `encerrar` e `reabrir`: decisões 3 e 4.
- `atribuirResponsavel`: decisões 5 e 6.
- `atualizar` do chamado: decisão 16 e o `ChamadoAtualizacaoDTO` (Etapa 2).
- `alterarSenha`, `atualizar`, `desativar` e `reativar` do usuário: decisões 7, 17 e 18.
- Construtor público, `ativar` e `desativar` de `Categoria`: decisão 8.
- As verificações de categoria ativa (§17.2) e do último administrador (§8.4) ficam no service, na Etapa 5. `Usuario.excluir()` não faz a verificação do último administrador.

Verificado:

- `./mvnw clean verify` verde, com os 29 testes passando.
- O metamodelo do Hibernate foi montado sem banco, com um script descartável fora do repositório. O DDL gerado bate com a §23 em colunas, tipos, tamanhos, `NOT NULL` e `UNIQUE`. Nomes de FK, `ON DELETE RESTRICT`, `DEFAULT`, `GENERATED ALWAYS` e o `CHECK` da solução ficam para as migrations da Etapa 8.

Não verificado:

- Validação contra o PostgreSQL real (`ddl-auto=validate`). Depende das migrations da Etapa 8.
- Chamada dos callbacks `@PrePersist` e `@PreUpdate` pelo próprio Hibernate. Os testes chamam os métodos direto; a chamada real fica para os testes de repository.

Pendências para as próximas etapas:

- Etapa 2: nenhum DTO de resposta usa `Usuario.getSenha()` (RA-07, S-21).
- Etapa 4: listagens que leem `atendimentos` usam `@EntityGraph` ou `JOIN FETCH`, para evitar N+1. A auditoria do chamado é lida por consulta paginada no repository de `HistoricoChamado`.
- Etapa 5: o service nunca grava senha ou hash em `valor_anterior`/`valor_novo` (S-33) e busca chamado e usuário ignorando os excluídos antes de chamar `excluir()`.

<!-- Modelo para as próximas etapas:

### Etapa N — Nome (AAAA-MM-DD)

Criado:
- ...

Decisões:
- ...

Pendências:
- ...
-->
