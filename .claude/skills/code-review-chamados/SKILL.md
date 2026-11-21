---
name: code-review-chamados
description: Code review do Sistema de Chamados de TI (chamados_ti_senai, Java/Spring Boot). Use ao revisar diff, PR, arquivo ou entrega do Claude Code neste projeto.
---

# Code review — Sistema de Chamados de TI

Revisão de código do projeto `masato173/chamados_ti_senai` (Java 21, Spring Boot 4.1.2, Spring Security, Spring Data JPA, PostgreSQL, Flyway). Responda em português (pt-BR).

A revisão aponta problemas reais, com arquivo, linha e cenário de falha. Não altera o código por conta própria: corrige só se o autor pedir, seguindo `protocolo-alteracao-chamados`.

## 1. Antes de revisar

1. Carregar `protocolo-alteracao-chamados` (regras RA-01 a RA-15) e `padrao-codigo-chamados` (seções 9 e 10). Se não estiverem disponíveis, usar o `CLAUDE.md` do repositório.
2. Ler o `MEMORY.md`: etapa atual, decisões tomadas e pendentes.
3. Delimitar o escopo:

```bash
git status
git diff --stat main...HEAD          # branch ou PR
git diff main...HEAD                 # alterações da branch
git diff                             # mudanças ainda não commitadas
```

4. Ler cada arquivo alterado **inteiro**, não só o trecho do diff, e as classes que ele chama.
5. Rodar o build e anotar o resultado:

```bash
./mvnw clean verify                  # Windows: .\mvnw.cmd clean verify
```

Se não der para rodar (sem PostgreSQL, sem rede), registrar "build não executado" no relatório.

## 2. Passadas da revisão

Faça as passadas nesta ordem. Uma não substitui a outra.

### Passada 1 — Regras absolutas (bloqueante)

Conferir RA-01 a RA-15 em cada arquivo. Pontos que mais escapam:

- `solicitanteId`, `usuarioId`, `autorId` ou o responsável do atendimento chegando em DTO de entrada (RA-01).
- Chamado buscado só pelo `id`, sem o solicitante, quando quem pede é usuário comum (RA-02).
- `deleteById`, `delete`, `CascadeType.REMOVE` ou `orphanRemoval = true` em entidade com histórico (RA-03).
- Método de service que altera chamado sem chamar a auditoria, ou auditoria sem valor anterior e novo (RA-04).
- Atendimento atualizado em vez de inserido; encerramento sem solução ou fora de transação (RA-05).
- Migration existente alterada. Conferir com o comando abaixo: status `M`, `R` ou `D` é violação de RA-09.

```bash
git diff --name-status main...HEAD -- src/main/resources/db/migration/
```

- Classe de etapa futura entrando antes da hora (RA-11).

### Passada 2 — Comportamento e regras de negócio

- Status: `ABERTO → EM_ANDAMENTO → FECHADO` é o fluxo da documentação (§2). `ABERTO → FECHADO` direto e a reabertura dependem das decisões pendentes 3 e 4. Chamado fechado não é encerrado de novo (§7.2).
- Cada método de negócio da entidade atualiza todos os campos afetados (status, responsável, `atualizado_em`).
- O service confere o que a §17.2 pede: usuário existe, chamado existe e não está excluído, categoria ativa, responsável válido e ativo, status permitido, solução no encerramento, último admin.
- Consultas de negócio ignoram registros com `excluido_em` preenchido.
- Ações de auditoria batem com o enum `AcaoHistorico` e com a lista da §16.1.
- Exceção de negócio estende `RuntimeException`. Exceção checked não desfaz a transação no Spring.
- `try/catch` que engole exceção dentro de método `@Transactional` grava estado parcial.
- Com `spring.jpa.open-in-view=false`, a conversão entidade → DTO que toca relação `LAZY` acontece dentro do método `@Transactional` do service. Feita no controller, gera `LazyInitializationException`.
- Erros da seção 9 do `padrao-codigo-chamados`: contador dentro do laço, exceção errada no `catch`, `getReferenceById` sem checar, `findAll()` + filtro em Java, `==` entre entidades.

### Passada 3 — Segurança

Se o diff toca `config/`, login, `Usuario`, senha, controller, DTO, `@Query`/`Specification`, `GlobalExceptionHandler`, `application.properties` ou `pom.xml`, carregar `code-security-chamados` e aplicar o checklist dela. Os achados entram neste mesmo relatório.

### Passada 4 — Persistência e migrations

- Mapeamento da entidade igual à migration. Com `ddl-auto=validate`, tipo ou nome de coluna diferente derruba a aplicação na inicialização (`TIMESTAMPTZ` ↔ `OffsetDateTime`, `TEXT` ↔ `columnDefinition = "TEXT"`, tamanhos de `VARCHAR`).
- Migration nova com o próximo número livre, FK nomeada com `ON DELETE RESTRICT`, `CHECK` dos enums, índice nas FKs e nas colunas de filtro.
- `@ManyToOne(fetch = FetchType.LAZY)`; listagens com `@EntityGraph` ou `JOIN FETCH`, sem N+1.
- Listagem sempre paginada (`Pageable` → `Page`).

### Passada 5 — Testes

- Toda regra nova tem um caso que passa e um que falha.
- Cada rota nova tem teste de permissão: sem login → `401`; usuário comum em rota de admin → `403`.
- Teste de service verifica a auditoria (`then(historico).should()...`) e confere os campos salvos com `ArgumentCaptor`.
- Nenhum teste removido, marcado com `@Disabled` ou com asserção afrouxada (RA-13).
- Nome `deveria...` / `naoDeveria...` e corpo com `// ARRANGE`, `// ACT`, `// ASSERT`.

### Passada 6 — Padrão de código

Checklist da seção 10 do `padrao-codigo-chamados`. Achado de estilo nunca é bloqueante sozinho.

### Passada 7 — Documentação

- `MEMORY.md` atualizado: estado, registro da etapa, decisões e propostas.
- Etapa marcada no `README.md` quando concluída.
- Endpoint novo ou alterado refletido na tabela do `ARCHITECTURE.md`.
- Mensagem de commit no formato `etapa-N: verbo ...`.

## 3. Severidade

| Nível | Quando usar | Efeito no veredito |
| --- | --- | --- |
| BLOQUEANTE | Viola regra absoluta, quebra build ou teste, perde dado, abre falha de segurança (CRÍTICA ou ALTA em `code-security-chamados`), comportamento errado comprovado | Reprova |
| IMPORTANTE | Bug provável, regra de negócio incompleta, falta teste para regra nova, N+1, divergência da documentação | Aprova com ressalvas |
| SUGESTÃO | Legibilidade, padrão de código, simplificação | Não afeta |

## 4. Verificar antes de reportar

Cada achado precisa de:

- **Arquivo e linha** reais.
- **Cenário de falha concreto**: entrada → resultado errado. Ex.: "o usuário 7 faz `GET /api/chamados/12`, que é do usuário 3, e recebe `200` com o chamado".
- **Evidência**: trecho lido, teste executado ou comando rodado.

Marque **CONFIRMADO** quando o cenário foi executado (teste ou requisição) e **PLAUSÍVEL** quando veio só da leitura. Sem evidência, o achado não entra. Item que depende de decisão pendente vai para "Decisões pendentes", não para achados. Gosto pessoal não é bug.

## 5. Formato do relatório

````markdown
# Revisão — <escopo: branch, PR ou arquivos>

**Veredito:** APROVADO / APROVADO COM RESSALVAS / REPROVADO
**Etapa:** N — nome | **Build:** verde / falhou / não executado

## Achados

| # | Severidade | Regra | Arquivo:linha | Resumo |
| --- | --- | --- | --- | --- |
| 1 | BLOQUEANTE | RA-02 | `service/ChamadoService.java:48` | Usuário comum lê chamado de terceiro |

### 1. <título curto>
- **Problema:**
- **Cenário de falha:**
- **Evidência:** CONFIRMADO / PLAUSÍVEL —
- **Correção:**
  ```java
  // trecho corrigido
  ```

## Verificado
-

## Não verificado
-

## Decisões pendentes que afetam este código
-

## Mensagem pronta para o Claude Code
> Corrija os achados 1 a N desta revisão seguindo `protocolo-alteracao-chamados` e `padrao-codigo-chamados`. Não altere nada fora desses pontos. Ao terminar, rode `./mvnw clean verify` e entregue o relatório da alteração.
> <lista dos achados com arquivo:linha e correção esperada>
````

Sem achados: veredito APROVADO, com as seções "Verificado" e "Não verificado" preenchidas.
