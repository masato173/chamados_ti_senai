package com.sistema.chamados.model;

import com.sistema.chamados.enums.AcaoHistorico;
import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "historico_chamado")
@Immutable
public class HistoricoChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", updatable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", updatable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50, updatable = false)
    private AcaoHistorico acao;

    @Column(name = "campo_alterado", length = 100, updatable = false)
    private String campoAlterado;

    @Column(name = "valor_anterior", columnDefinition = "TEXT", updatable = false)
    private String valorAnterior;

    @Column(name = "valor_novo", columnDefinition = "TEXT", updatable = false)
    private String valorNovo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    protected HistoricoChamado() {}

    public HistoricoChamado(Chamado chamado, Usuario usuario, AcaoHistorico acao, String campoAlterado,
            String valorAnterior, String valorNovo) {
        this.chamado = chamado;
        this.usuario = usuario;
        this.acao = acao;
        this.campoAlterado = campoAlterado;
        this.valorAnterior = valorAnterior;
        this.valorNovo = valorNovo;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public AcaoHistorico getAcao() {
        return acao;
    }

    public String getCampoAlterado() {
        return campoAlterado;
    }

    public String getValorAnterior() {
        return valorAnterior;
    }

    public String getValorNovo() {
        return valorNovo;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoricoChamado outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
