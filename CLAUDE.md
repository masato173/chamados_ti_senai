# CLAUDE.md — Sistema de Gerenciamento de Chamados de TI

API REST em Java 21 + Spring Boot 4.1.2 + PostgreSQL para abrir, acompanhar e encerrar chamados de suporte de TI.

## Antes de qualquer tarefa

1. Leia [`MEMORY.md`](MEMORY.md): etapa atual, decisões tomadas e pendentes.
2. Leia [`ARCHITECTURE.md`](ARCHITECTURE.md): camadas, modelo de dados, fluxos, endpoints.
3. Para escrever ou revisar Java, use a skill `padrao-codigo-chamados` (`.claude/skills/padrao-codigo-chamados/SKILL.md`).
4. Requisitos completos: [`docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt`](docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt).

## Regras de trabalho

- Responda e escreva documentação em português (pt-BR).
- Trabalhe **uma etapa por vez**, na ordem do README (Etapas 1 a 10). Não adiante código de etapas futuras.
- Antes de implementar algo que depende de uma decisão pendente do `MEMORY.md`, pergunte.
- Se a documentação técnica e o `ARCHITECTURE.md` divergirem, siga a documentação e registre a diferença no `MEMORY.md`.
- Não invente requisito. Se faltar informação, pergunte.
- Ao concluir uma etapa:
    1. Rode `./mvnw clean verify` (no Windows, `.\mvnw.cmd clean verify`).
    2. Atualize o `MEMORY.md` (estado atual + registro da etapa).
    3. Marque a etapa no `README.md`.
    4. Sugira a mensagem de commit.
- Apague o `.gitkeep` de uma pasta quando ela receber a primeira classe.

## Comandos

| Comando | Uso |
| --- | --- |
| `./mvnw clean verify` | Compila e roda os testes |
| `./mvnw test -Dtest=NomeDaClasseTest` | Roda um teste |
| `./mvnw spring-boot:run` | Sobe a API (precisa do PostgreSQL) |

Variáveis do banco: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

## Regras do domínio que não podem ser quebradas

- Solicitante e autor das operações vêm do usuário autenticado, nunca do corpo da requisição.
- Exclusão sempre lógica (`excluido_em`); nunca apagar chamados, atendimentos ou auditoria.
- Usuário comum só vê os próprios chamados; a regra é aplicada no backend.
- Encerramento exige solução; atendimento novo nunca sobrescreve o anterior.
- O último administrador ativo não pode ser desativado.
- Toda alteração relevante gera registro em `historico_chamado`.
- Senha só com BCrypt e nunca retornada pela API.
- Entidades JPA nunca saem nas respostas: sempre DTO.

## Commits

Mensagens em português, no imperativo, com prefixo da etapa. Exemplo: `etapa-1: cria entidades Usuario e Categoria`.
