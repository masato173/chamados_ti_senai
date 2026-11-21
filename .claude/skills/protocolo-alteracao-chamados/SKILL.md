---
name: protocolo-alteracao-chamados
description: Regras absolutas e protocolo de alteração do Sistema de Chamados de TI. Use antes de criar, alterar ou apagar qualquer arquivo do projeto chamados_ti_senai.
---

# Regras absolutas e protocolo de alteração — Sistema de Chamados de TI

Projeto: `masato173/chamados_ti_senai` — API REST em Java 21 + Spring Boot 4.1.2 + Spring Security + Spring Data JPA + PostgreSQL + Flyway.

O repositório é **público**: tudo que entra no Git fica visível para qualquer pessoa.

Esta skill vale para toda alteração: código, migration, teste, `pom.xml`, `application.properties` e documentação. Responda sempre em português (pt-BR).

Skills irmãs:

- `padrao-codigo-chamados` — como escrever (formatação, nomes, camadas).
- `code-review-chamados` — como revisar.
- `code-security-chamados` — como auditar segurança.

## 1. Regra suprema — não inventar

Fontes da verdade, em ordem de prioridade:

1. `docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt` (v1.0). Use a numeração do corpo, não a do sumário.
2. `MEMORY.md` — etapa atual, decisões tomadas e pendentes.
3. `ARCHITECTURE.md`.
4. Skill `padrao-codigo-chamados`.
5. O código que já existe no repositório.

Regras:

- Antes de afirmar algo sobre o projeto, leia o arquivo. Se não leu, diga "não verifiquei".
- Nunca invente requisito, campo, rota, status HTTP, transição de status ou mensagem. Se faltar, pergunte.
- Documentação e `ARCHITECTURE.md` divergem: siga a documentação e registre a diferença no `MEMORY.md`.
- Regra nova que não vem da documentação entra no `MEMORY.md` como **proposta**.
- O estado do projeto muda: leia o `MEMORY.md` em vez de confiar na memória da conversa.

## 2. Regras absolutas

Nenhuma alteração pode violar estas regras. Violação encontrada em revisão é sempre BLOQUEANTE. Cite pelo código (ex.: "viola RA-03").

### Domínio

| Código | Regra | Origem |
| --- | --- | --- |
| RA-01 | Solicitante, autor da operação, responsável pelo atendimento e autor da auditoria vêm do usuário autenticado (`SecurityContext`). Nunca do corpo, da URL ou de parâmetro da requisição. | RF07, §6.1, §10.3, §13.4 |
| RA-02 | Autorização aplicada no backend em toda rota. Usuário comum só acessa os próprios chamados, e o filtro é feito na consulta ao banco. | §5, §9.2, §15, RF24 |
| RA-03 | Exclusão sempre lógica (`excluido_em` / `ativo = false`). Nunca `DELETE` físico de usuários, chamados, atendimentos ou histórico. Sem `CascadeType.REMOVE`; FKs com `ON DELETE RESTRICT`. | RF04, RF15, §14.3, §22 |
| RA-04 | Toda alteração relevante grava `historico_chamado` (quem, ação, campo, valor anterior, valor novo, data). Registro de auditoria nunca é alterado nem apagado. | RF23, §16.1 |
| RA-05 | Atendimento novo nunca sobrescreve o anterior. Encerramento exige solução e grava atendimento, status e auditoria na mesma transação. Chamado fechado só volta por reabertura autorizada. | §7.2, §10.3, §16.3 |
| RA-06 | O último administrador ativo nunca é desativado nem excluído. A verificação é transacional e com bloqueio. | §8.4 |
| RA-07 | Senha só com BCrypt, em fluxo próprio. Nunca aparece em resposta, log, auditoria, mensagem de erro ou `toString`. | RNF04, §8.2 |
| RA-08 | Entidade JPA nunca entra nem sai da API: sempre DTO `record`. | RNF11, §22 |

### Banco e configuração

| Código | Regra | Origem |
| --- | --- | --- |
| RA-09 | O esquema muda só por migration Flyway nova. Migration já aplicada nunca é editada, renomeada ou apagada. `spring.jpa.hibernate.ddl-auto` fica em `validate`. | RNF12, MEMORY.md |
| RA-10 | Nenhum segredo no repositório: senha, token, chave JWT, hash de senha real, `.env`. Credenciais só por variável de ambiente. | MEMORY.md, repositório público |

### Processo

| Código | Regra | Origem |
| --- | --- | --- |
| RA-11 | Uma etapa por vez, na ordem do README. Não adiantar código de etapa futura. | CLAUDE.md |
| RA-12 | Decisão pendente do `MEMORY.md` não é decidida por conta própria: pare e pergunte. | CLAUDE.md |
| RA-13 | Só está pronto com `./mvnw clean verify` verde. Proibido `@Disabled`, `-DskipTests`, apagar teste ou afrouxar asserção para o build passar. | CLAUDE.md |
| RA-14 | Contrato da API (rota, campo de DTO, status HTTP, formato de erro) não muda em silêncio: toda mudança é anunciada e registrada. | Proposta |
| RA-15 | Sem `commit`, `push`, `reset`, `rebase` ou `push --force` sem pedido explícito. Ao terminar, sugerir a mensagem de commit. | CLAUDE.md |

## 3. Prioridades

Quando dois objetivos conflitam, o de cima ganha:

1. Regras absolutas, segurança e integridade dos dados
2. Conformidade com a documentação técnica
3. Correção comprovada por teste
4. Simplicidade: implementar só o que o sistema precisa (§22)
5. Padrão de código e legibilidade
6. Desempenho
7. Velocidade de entrega

Se a própria documentação pedir algo que fere uma regra absoluta, pare e pergunte (seção 6).

## 4. Classificar a alteração

| Nível | Exemplos | Exige |
| --- | --- | --- |
| 1 — Baixo | Documentação, teste novo, formatação, renomear variável local | Fases 1 e 3 resumidas |
| 2 — Médio | Classe ou método novo da etapa atual, regra de negócio, endpoint novo, consulta nova | Ficha de alteração na resposta, antes do código |
| 3 — Alto | `SecurityConfig`, login, `Usuario`/senha, migration, entidade, auditoria, exclusão, último admin, contrato da API, `pom.xml`, `application.properties` | Ficha + **esperar aprovação** antes de editar + skill `code-security-chamados` |

Na dúvida entre dois níveis, use o mais alto.

## 5. Protocolo de alteração

### Fase 1 — Antes de editar

1. Ler o `MEMORY.md`: etapa atual, decisões tomadas e pendentes.
2. Ler as seções da documentação e do `ARCHITECTURE.md` ligadas à tarefa.
3. Confirmar que a tarefa é da etapa atual (RA-11). Se não for, avisar e perguntar.
4. Listar as decisões pendentes envolvidas. Se houver alguma, parar e perguntar (RA-12).
5. Ler por inteiro cada arquivo que será alterado e as classes que o chamam.
6. Fazer a análise de impacto: o que muda, o que não muda, contrato da API, esquema do banco, testes afetados.
7. Definir os critérios de aceite, ligados aos itens da §20 da documentação.
8. Planejar os testes. Regra nova começa pelo teste que falha (RED → GREEN → REFACTOR).
9. Planejar a reversão: `git revert` do commit; para o esquema, migration nova que desfaz a anterior.

Ficha de alteração (níveis 2 e 3):

````markdown
## Ficha de alteração
- **Objetivo:**
- **Etapa:** N — nome
- **Nível de risco:** 1 / 2 / 3 — motivo
- **Requisitos:** RFxx, RNFxx, §x.y
- **Arquivos criados ou alterados:**
- **Arquivos que não serão tocados:**
- **Regras absolutas envolvidas:** RA-xx
- **Decisões pendentes envolvidas:** nenhuma / nº x — pergunta
- **Critérios de aceite:**
- **Testes:** novos / existentes afetados
- **Reversão:**
````

### Fase 2 — Durante

- Alteração mínima, um assunto por vez. Refatoração fora do escopo vira sugestão, não código.
- Escrever conforme `padrao-codigo-chamados`.
- Migration é sempre arquivo novo `V<n>__<acao>_<tabela>.sql`, com o próximo número livre.
- Problema fora do escopo (bug, divergência da documentação): anotar e informar. Não corrigir "de carona".
- Não apagar código ou teste existente sem justificar na resposta.
- Apagar o `.gitkeep` da pasta que recebe a primeira classe.
- Se o plano mudar no meio (nível ou arquivos), parar e apresentar a ficha atualizada.

### Fase 3 — Depois

1. Rodar `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Se não der para rodar, escrever "build não executado". Nunca supor que passa.
2. Autorrevisar com `code-review-chamados`. Nível 3 também com `code-security-chamados`.
3. Atualizar o `MEMORY.md`: estado atual, registro da etapa, decisões novas e propostas.
4. Marcar a etapa no `README.md` quando concluída.
5. Sugerir a mensagem de commit: `etapa-N: <verbo no imperativo> ...`.
6. Entregar o relatório:

````markdown
## Relatório da alteração
- **O que mudou:** arquivo → resumo
- **Por quê:** requisito ou pedido atendido
- **Como verificar:** comando, teste ou requisição
- **Build:** verde / falhou / não executado
- **Não verificado:**
- **Pendências e propostas registradas no MEMORY.md:**
- **Commit sugerido:** `etapa-N: ...`
````

## 6. Pedido que fere uma regra absoluta

1. Parar antes de editar.
2. Dizer qual RA seria violada e o risco concreto.
3. Apresentar dois caminhos: o pedido como está e a alternativa que respeita a regra.
4. Esperar a decisão.
5. Se o autor confirmar o desvio, executar e registrar no `MEMORY.md`: data, regra, motivo e quem decidiu.

## 7. Quando parar e perguntar

- A tarefa depende de decisão pendente do `MEMORY.md`.
- A documentação não diz como fazer e a escolha muda comportamento ou contrato.
- A tarefa é de outra etapa.
- O build quebra por motivo fora do escopo.
- A correção exigiria editar migration aplicada.
- Há segredo no repositório. Avisar na hora: o segredo precisa ser trocado, não só apagado do Git.

Decisões que ainda não estão no `MEMORY.md` e precisam ser feitas antes da etapa correspondente. Pergunte e registre como pendência:

- Status devolvido quando o usuário comum acessa chamado de outro: `403` ou `404`.
- Como é criado o primeiro administrador, sem senha nem hash no repositório público.
- Quais registros de atendimento o usuário comum vê no detalhe do chamado (§9.3).
- Tamanho máximo dos campos `TEXT` (`problema`, `observacao`, `solucao`) e da página (`size`).
- Política mínima de senha (tamanho mínimo).
