---
name: padrao-codigo-chamados
description: Padrão de código Java/Spring Boot do Sistema de Chamados de TI. Use ao criar, revisar ou refatorar qualquer classe, DTO, migration ou teste deste projeto.
---

# Padrão de código — Sistema de Chamados de TI

Base: padrão medido no projeto Adopet (Alura), com os erros dele corrigidos e adaptado às regras da documentação técnica deste projeto (`docs/`). Quando a documentação técnica pedir algo diferente, ela prevalece.

## Como aplicar

1. **Escrever:** siga as seções 1 a 8 e copie a forma dos modelos da seção 6.
2. **Revisar:** percorra o checklist da seção 10 e aponte arquivo e linha de cada violação.
3. **Refatorar:** corrija primeiro os itens da seção 9 (mudam comportamento), depois estilo.
4. Responda em português (pt-BR).

## 1. Formatação

- 4 espaços por nível, nunca TAB. Continuação com 8 espaços.
- LF, UTF-8 sem BOM, sem espaço no fim da linha, quebra de linha no fim do arquivo.
- Máximo de 120 colunas.
- Chave de abertura na mesma linha: `metodo() {`.
- `} else {`, `} else if (...) {`, `} catch (...) {` na linha da chave de fechamento, com espaço.
- `if`, `for` e `while` sempre com chaves. Exceção: `if (this == o) return true;` em `equals`.
- Espaço entre palavra-chave e parêntese: `if (`, `for (`, `catch (`.
- Espaço dos dois lados dos operadores, inclusive `+`: `"Chamado " + id + " encerrado"`.
- Uma linha em branco entre membros da classe, após a `{` da classe e antes da `}` final.
- Dentro do método, linha em branco separando consulta, verificação e gravação.
- Uma anotação por linha.
- Encadeamento longo quebrado antes do ponto, um método por linha.
- Record com anotações: um componente por bloco, anotações acima do tipo.

## 2. Arquivo e imports

Ordem: `package` → imports `com`/`jakarta`/`org` em ordem alfabética → `java.*` → `import static` → anotações → tipo → campos → construtores → métodos.

- Curinga `*` só com 5 ou mais classes do mesmo pacote.
- Nenhum import sem uso.

## 3. Sintaxe e tipos

- DTO sempre `record`.
- Entidades usam wrappers (`Long`, `Boolean`); variáveis locais de cálculo usam primitivos.
- IDs `Long`. Colunas `TIMESTAMPTZ` como `OffsetDateTime`.
- Valor decimal exato com `BigDecimal`.
- Coleções declaradas pela interface (`List<...>`).
- Campos sempre `private`. Dependências injetadas `private final`.
- `@Override` em todo método sobrescrito ou implementado.
- Sem Lombok.
- Literal `long` com `L` maiúsculo.
- `var` só em testes.
- Mensagens para o usuário em português, com acentos.

## 4. Nomenclatura

| Elemento | Regra | Exemplo |
| --- | --- | --- |
| Pacote raiz | `com.sistema.chamados` | — |
| Classe, record, enum | PascalCase | `ChamadoService`, `StatusChamado` |
| Método | camelCase, verbo no infinitivo | `abrir`, `registrarAtendimento` |
| Booleano | frase afirmativa | `categoriaAtiva`, `ultimoAdmin` |
| Constante de enum | MAIÚSCULAS_COM_SUBLINHADO | `EM_ANDAMENTO` |
| Controller / Service / Repository | `<Entidade>Controller` etc. | `ChamadoRepository` |
| DTO de entrada | `<Entidade>RequestDTO` ou `<Entidade><Ação>DTO` | `ChamadoRequestDTO`, `ChamadoAtualizacaoDTO` |
| DTO de saída | `<Entidade>ResponseDTO` | `ChamadoResponseDTO` |
| DTO de autenticação | `LoginRequestDTO`, `LoginResponseDTO` | — |
| Exceção | `<Nome>Exception` | `RecursoNaoEncontradoException` |
| Parâmetro DTO | `dto` | `abrir(ChamadoRequestDTO dto)` |
| Rota | `/api/<recurso no plural>` | `/api/chamados` |
| Tabela | plural, minúsculas | `chamados`, `historico_chamado` |
| Coluna | snake_case | `aberto_em`, `responsavel_id` |
| FK | `fk_<entidade>_<relação>` | `fk_chamado_solicitante` |
| CHECK | `ck_<entidade>_<campo>` | `ck_chamado_status` |
| Índice | `idx_<tabela>_<coluna>` | `idx_chamados_status` |
| Migration | `V<n>__<acao>_<tabela>.sql` | `V3__create_chamados.sql` |
| Classe de teste | `<Classe>Test` | `ChamadoServiceTest` |
| Método de teste | `deveria...` / `naoDeveria...` | `deveriaAbrirChamadoComStatusAberto` |

Identificadores em português sem acento nem cedilha (`Atendimento`, `observacao`). Sufixos técnicos em inglês.

## 5. Pacotes

| Pacote | Conteúdo |
| --- | --- |
| `config` | `SecurityConfig`, `PasswordEncoder`, configurações |
| `controller` | `AuthController`, `UsuarioController`, `ChamadoController`, `AtendimentoController` |
| `service` | Regras de negócio, auditoria, usuário autenticado |
| `repository` | Interfaces `JpaRepository` |
| `model` | Entidades JPA |
| `dto/auth`, `dto/usuario`, `dto/chamado`, `dto/atendimento` | Records |
| `validation` | Validadores e anotações próprias |
| `enums` | `Perfil`, `StatusChamado`, `AcaoHistorico` |
| `exception` | Exceções de negócio e `GlobalExceptionHandler` |

## 6. Modelos por camada

### Controller

Só recebe, valida e devolve. Sem `try/catch`: erros vão para o handler global.

```java
@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ChamadoResponseDTO> abrir(@RequestBody @Valid ChamadoRequestDTO dto,
            UriComponentsBuilder uriBuilder) {
        ChamadoResponseDTO chamado = service.abrir(dto);
        URI uri = uriBuilder.path("/api/chamados/{id}").buildAndExpand(chamado.id()).toUri();
        return ResponseEntity.created(uri).body(chamado);
    }

    @GetMapping
    public ResponseEntity<Page<ChamadoResponseDTO>> listar(ChamadoFiltroDTO filtro,
            @PageableDefault(size = 10, sort = "abertoEm", direction = Sort.Direction.DESC) Pageable paginacao) {
        return ResponseEntity.ok(service.listar(filtro, paginacao));
    }

}
```

- Criação devolve `201 Created` com `Location`.
- Listagem sempre paginada.
- Restrição por perfil com `@PreAuthorize("hasRole('ADMIN')")` ou na `SecurityConfig`.

### Service

```java
@Service
public class ChamadoService {

    private final ChamadoRepository repository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioAutenticadoService usuarioAutenticado;
    private final HistoricoService historico;

    public ChamadoService(ChamadoRepository repository, CategoriaRepository categoriaRepository,
            UsuarioAutenticadoService usuarioAutenticado, HistoricoService historico) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.historico = historico;
    }

    @Transactional
    public ChamadoResponseDTO abrir(ChamadoRequestDTO dto) {
        Usuario solicitante = usuarioAutenticado.obter();
        Categoria categoria = categoriaRepository
                .findByIdAndAtivaTrue(dto.categoriaId())
                .orElseThrow(() -> new RegraNegocioException("Categoria inexistente ou inativa."));

        Chamado chamado = repository.save(new Chamado(solicitante, categoria, dto));

        historico.registrar(chamado, solicitante, AcaoHistorico.CHAMADO_ABERTO);
        return new ChamadoResponseDTO(chamado);
    }

}
```

- `@Transactional` em todo método que grava; `@Transactional(readOnly = true)` nas leituras.
- Buscar com `findById(...).orElseThrow(() -> new RecursoNaoEncontradoException(...))`. Nunca `getReferenceById` sem checar.
- Usuário autenticado sempre via `SecurityContext`, nunca pelo DTO.
- Usuário comum: filtrar sempre pelo `solicitante` autenticado.
- Toda alteração relevante chama a auditoria com valor anterior e novo valor.

### Entidade

```java
@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false, length = 150)
    private String local;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String problema;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusChamado status;

    @Column(name = "aberto_em", nullable = false, updatable = false)
    private OffsetDateTime abertoEm;

    @Column(name = "excluido_em")
    private OffsetDateTime excluidoEm;

    protected Chamado() {}

    public Chamado(Usuario solicitante, Categoria categoria, ChamadoRequestDTO dto) {
        this.solicitante = solicitante;
        this.categoria = categoria;
        this.local = dto.local();
        this.problema = dto.problema();
        this.observacao = dto.observacao();
        this.status = StatusChamado.ABERTO;
    }

    @PrePersist
    void aoCriar() {
        this.abertoEm = OffsetDateTime.now();
    }

    public void encerrar() {
        if (this.status == StatusChamado.FECHADO) {
            throw new RegraNegocioException("O chamado já está fechado.");
        }
        this.status = StatusChamado.FECHADO;
    }

    public void excluir() {
        this.excluidoEm = OffsetDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chamado outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Chamado.class.hashCode();
    }

}
```

- `@ManyToOne(fetch = FetchType.LAZY)` com `@JoinColumn` explícito.
- Enum com `@Enumerated(EnumType.STRING)`.
- Construtor vazio `protected` + construtor de criação que define valores iniciais.
- Estado muda por métodos de negócio (`encerrar`, `excluir`, `atribuirResponsavel`), não por setters. Cada método atualiza **todos** os campos afetados.
- Datas com `@PrePersist` e `@PreUpdate`.
- Sem `CascadeType.REMOVE`. Sem exclusão física.
- `equals` só pelo `id`, com `instanceof` e `getId()`. Assim aceita o proxy `LAZY` do Hibernate, que é uma subclasse da entidade com o campo `id` vazio. Id nulo nunca é igual: duas entidades ainda não salvas são registros diferentes.
- `hashCode` fixo por classe (`Chamado.class.hashCode()`): não muda quando o banco gera o id e é o mesmo para o proxy.
- Comparar entidades com `equals`, nunca `==`.

### Repository

```java
public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    Optional<Chamado> findByIdAndExcluidoEmIsNull(Long id);

    Page<Chamado> findBySolicitanteIdAndExcluidoEmIsNull(Long solicitanteId, Pageable paginacao);

}
```

- Consultas derivadas ou `@Query`. Nunca `findAll()` seguido de filtro em Java.
- Filtros combinados com `Specification`.
- Toda consulta de negócio ignora registros com `excluido_em` preenchido.
- `@EntityGraph` ou `JOIN FETCH` para evitar N+1.

### DTO

```java
public record ChamadoRequestDTO(
        @NotBlank
        @Size(max = 150)
        String local,
        @NotNull
        Long categoriaId,
        @NotBlank
        String problema,
        String observacao) {
}
```

```java
public record ChamadoResponseDTO(Long id, String solicitante, String categoria, StatusChamado status,
        OffsetDateTime abertoEm) {

    public ChamadoResponseDTO(Chamado chamado) {
        this(chamado.getId(), chamado.getSolicitante().getNome(), chamado.getCategoria().getNome(),
                chamado.getStatus(), chamado.getAbertoEm());
    }

}
```

- Entrada: Bean Validation com tamanhos iguais aos da tabela.
- Saída: nunca contém senha nem entidade JPA.

### Exceções

- `RegraNegocioException` → 400, `RecursoNaoEncontradoException` → 404, `ConflitoException` → 409.
- Um `GlobalExceptionHandler` com `@RestControllerAdvice` converte tudo no formato de erro do `ARCHITECTURE.md`.
- A resposta de erro nunca expõe senha, SQL ou stack trace.

## 7. Migrations (Flyway + PostgreSQL)

- Uma migration por tabela, na ordem das dependências; carga de dados em migration própria.
- Palavras-chave SQL em MAIÚSCULAS, nomes em minúsculas, uma coluna por linha, 4 espaços.
- `GENERATED ALWAYS AS IDENTITY` para IDs; `TIMESTAMPTZ` para datas.
- Toda FK com nome e `ON DELETE RESTRICT`; `CHECK` para enums e textos obrigatórios.
- Índices nas FKs e nas colunas de filtro.
- Migration já aplicada nunca é editada: crie uma nova.

```sql
CREATE TABLE categorias (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);
```

## 8. Testes

- JUnit 5 + Mockito, no mesmo pacote da classe testada.
- Corpo com `// ARRANGE`, `// ACT`, `// ASSERT`.
- Asserções do JUnit 5 com a classe qualificada: `Assertions.assertEquals`, `Assertions.assertTrue`, `Assertions.assertThrows` etc. (`import org.junit.jupiter.api.Assertions;`). Sem AssertJ (`assertThat`).
- Unitário: `@ExtendWith(MockitoExtension.class)`, `@InjectMocks`, `@Mock`, `given(...).willReturn(...)`, `then(mock).should()`.
- Controller: `@WebMvcTest` + `MockMvc` + `@MockitoBean`; segurança com `@WithMockUser(roles = "ADMIN")`.
- Repository: `@DataJpaTest`.
- Toda regra tem um caso que passa e um que falha.
- Testes de permissão: usuário comum recebe 403 em rota de admin e não vê chamado de terceiro.

## 9. Erros que não podem acontecer

1. Variável acumuladora declarada dentro do laço.
2. Capturar exceção diferente da lançada (ex.: `ValidationException` no lugar da exceção de negócio).
3. Mudança de estado que esquece um campo afetado.
4. Mensagem ou notificação enviada a quem não é o destinatário.
5. `getReferenceById` sem verificar existência.
6. `findAll()` + filtro em Java.
7. Comparar entidades com `==`.
8. Solicitante ou autor vindo do corpo da requisição.
9. Exclusão física de chamado, atendimento ou histórico.
10. Entidade JPA ou senha na resposta da API.
11. Atendimento novo sobrescrevendo o anterior.
12. Erros de digitação em nomes de método e mensagens.

## 10. Checklist de revisão

- [ ] Formatação da seção 1 (4 espaços, 120 colunas, chaves, espaços em operadores)
- [ ] Imports organizados e sem uso desnecessário
- [ ] `@Override` em todo método sobrescrito
- [ ] Nomes conforme a seção 4
- [ ] Controller sem regra e sem `try/catch`; criação devolve 201
- [ ] Service com `@Transactional` e `findById(...).orElseThrow(...)`
- [ ] Usuário autenticado obtido do contexto de segurança
- [ ] Permissão aplicada no backend
- [ ] Auditoria registrada nas alterações
- [ ] Exclusão lógica e consultas ignorando excluídos
- [ ] Entidade com métodos de negócio completos, `equals` por `id` (`instanceof` + `getId()`) e `hashCode` fixo por classe
- [ ] Repository sem `findAll()` + filtro; listagens paginadas
- [ ] DTO `record` com Bean Validation; resposta sem entidade nem senha
- [ ] Migration nova (nunca editar aplicada), com FK nomeada, `CHECK` e índice
- [ ] Testes com caso válido e inválido, asserções `Assertions.xxx` do JUnit 5 (sem AssertJ)
- [ ] Nenhum item da seção 9
