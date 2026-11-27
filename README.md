# Sistema de Gerenciamento de Chamados de TI

API REST para abrir, acompanhar e encerrar chamados de suporte técnico de TI, com controle de acesso por perfil, histórico de atendimentos e auditoria das operações.

> **Status:** Etapa 1 concluída — enums e entidades JPA criados, com testes unitários. A API só sobe depois das migrations da Etapa 8.
> O desenvolvimento segue as etapas descritas em [Ordem de desenvolvimento](#ordem-de-desenvolvimento).

## Sumário

- [Objetivo](#objetivo)
- [Funcionalidades previstas](#funcionalidades-previstas)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Ordem de desenvolvimento](#ordem-de-desenvolvimento)
- [Documentação do projeto](#documentação-do-projeto)
- [Desenvolvimento com Claude Code](#desenvolvimento-com-claude-code)

## Objetivo

Centralizar as solicitações de suporte técnico de TI, substituindo controles manuais e informações dispersas por um processo organizado e rastreável:

- O **usuário comum** abre chamados e acompanha os próprios.
- O **administrador** gerencia usuários, atribui responsáveis, registra atendimentos e encerra chamados.
- Toda alteração relevante fica registrada em auditoria.

## Funcionalidades previstas

| Área | Funcionalidades |
| --- | --- |
| Autenticação | Login, perfis `ROLE_USER` e `ROLE_ADMIN`, senhas com BCrypt |
| Usuários | Cadastro, atualização, exclusão lógica, proteção do último administrador |
| Chamados | Abertura com solicitante automático, consulta, detalhes, atualização, exclusão lógica |
| Atendimentos | Registro de solução, encerramento, histórico preservado |
| Consultas | Filtros (ID, solicitante, categoria, status, responsável, período) e paginação |
| Auditoria | Histórico com usuário, ação, campo, valor anterior e novo valor |

Requisitos completos (RF01 a RF24 e RNF01 a RNF12) em [`docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt`](docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt).

## Tecnologias

| Tecnologia | Versão | Uso |
| --- | --- | --- |
| Java | 21 | Linguagem |
| Spring Boot | 4.1.2 | Base da aplicação |
| Spring Web MVC | via Boot | API REST |
| Spring Security | via Boot | Autenticação, autorização e BCrypt |
| Spring Data JPA / Hibernate | via Boot | Persistência |
| Bean Validation | via Boot | Validação de entrada |
| PostgreSQL | a definir | Banco de dados |
| Flyway | via Boot | Versionamento do banco |
| Maven Wrapper | Maven 3.9.16 | Build (não precisa instalar Maven) |
| JUnit 5 + Mockito | via Boot | Testes |

## Pré-requisitos

- JDK 21 instalado (`java -version`).
- PostgreSQL rodando localmente, com um banco chamado `chamados_ti`.
- Git.

Criar o banco (uma vez):

```sql
CREATE DATABASE chamados_ti;
```

## Como executar

As credenciais do banco vêm de variáveis de ambiente. Sem elas, o projeto usa os valores padrão de `application.properties` (`postgres` / `postgres` em `localhost:5432/chamados_ti`).

**Windows (PowerShell)**

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/chamados_ti"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua_senha"
.\mvnw.cmd spring-boot:run
```

**Linux / macOS**

```bash
export DB_URL=jdbc:postgresql://localhost:5432/chamados_ti
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha
./mvnw spring-boot:run
```

Outros comandos:

| Comando | O que faz |
| --- | --- |
| `./mvnw clean verify` | Compila e roda os testes |
| `./mvnw test` | Roda só os testes |
| `./mvnw spring-boot:run` | Sobe a API em `http://localhost:8080` |

> Enquanto a Etapa 7 (segurança) não for implementada, o Spring Security protege tudo com o usuário `user` e uma senha gerada, exibida no console ao iniciar.

## Estrutura de pastas

```
chamados_ti_senai/
├── .claude/skills/                                   # skills do Claude Code
│   ├── protocolo-alteracao-chamados/SKILL.md         # regras absolutas e protocolo de alteração
│   ├── padrao-codigo-chamados/SKILL.md               # padrão de código
│   ├── code-review-chamados/SKILL.md                 # revisão de código
│   └── code-security-chamados/SKILL.md               # checklist de segurança
├── docs/                                             # documentação técnica original
├── src/
│   ├── main/
│   │   ├── java/com/sistema/chamados/
│   │   │   ├── ChamadosApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/{auth,usuario,chamado,atendimento}/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── validation/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   └── test/java/com/sistema/chamados/{controller,service,repository,integration,model}/
├── ARCHITECTURE.md
├── CLAUDE.md
├── MEMORY.md
├── README.md
├── mvnw, mvnw.cmd, .mvn/
└── pom.xml
```

As pastas vazias têm um arquivo `.gitkeep` só para o Git guardá-las. Ele pode ser apagado quando a pasta receber a primeira classe.

## Ordem de desenvolvimento

- [x] **Etapa 0** — Estrutura do projeto, dependências e documentação
- [x] **Etapa 1** — Models (entidades e enums)
- [ ] **Etapa 2** — DTOs
- [ ] **Etapa 3** — Validações
- [ ] **Etapa 4** — Repositories
- [ ] **Etapa 5** — Services (regras de negócio, auditoria, paginação)
- [ ] **Etapa 6** — Controllers
- [ ] **Etapa 7** — Config e Security
- [ ] **Etapa 8** — Banco de dados (migrations definitivas e carga de categorias)
- [ ] **Etapa 9** — Testes
- [ ] **Etapa 10** — Estrutura definitiva

Cada etapa é implementada, revisada e registrada em [`MEMORY.md`](MEMORY.md) antes de avançar.

## Documentação do projeto

| Arquivo | Conteúdo |
| --- | --- |
| [`ARCHITECTURE.md`](ARCHITECTURE.md) | Camadas, pacotes, modelo de dados, fluxos, segurança e endpoints planejados |
| [`MEMORY.md`](MEMORY.md) | Decisões tomadas, decisões pendentes, status de cada etapa |
| [`CLAUDE.md`](CLAUDE.md) | Instruções para o Claude Code trabalhar no projeto |
| [`.claude/skills/protocolo-alteracao-chamados/SKILL.md`](.claude/skills/protocolo-alteracao-chamados/SKILL.md) | Regras absolutas (RA-01 a RA-15) e protocolo de alteração |
| [`.claude/skills/padrao-codigo-chamados/SKILL.md`](.claude/skills/padrao-codigo-chamados/SKILL.md) | Padrão de código Java do projeto |
| [`.claude/skills/code-review-chamados/SKILL.md`](.claude/skills/code-review-chamados/SKILL.md) | Roteiro e formato de revisão de código |
| [`.claude/skills/code-security-chamados/SKILL.md`](.claude/skills/code-security-chamados/SKILL.md) | Checklist de segurança (S-01 a S-47) e testes obrigatórios |
| [`docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt`](docs/Documentacao_Tecnica_Sistema_Chamados_TI.txt) | Documentação técnica original, versão 1.0 |

## Desenvolvimento com Claude Code

Na raiz do projeto:

```bash
claude
```

O Claude Code lê o `CLAUDE.md` automaticamente e, por ele, a arquitetura, a memória e as skills do projeto. Para começar a próxima etapa:

```
Leia o MEMORY.md e implemente a Etapa 1 (Models) seguindo o protocolo-alteracao-chamados, o ARCHITECTURE.md e a skill padrao-codigo-chamados.
```

Para revisar uma entrega:

```
Revise as alterações desta branch com a skill code-review-chamados.
```

Para uma auditoria de segurança:

```
Faça uma revisão de segurança do projeto com a skill code-security-chamados.
```

## Autor

Fernando Masato ([@masato173](https://github.com/masato173)).
