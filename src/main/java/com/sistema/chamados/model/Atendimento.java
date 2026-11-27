package com.sistema.chamados.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "atendimentos")
@Immutable
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", updatable = false)
    private Chamado chamado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", updatable = false)
    private Usuario responsavel;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String observacao;

    @Column(nullable = false, columnDefinition = "TEXT", updatable = false)
    private String solucao;

    @Column(name = "realizado_em", nullable = false, updatable = false)
    private OffsetDateTime realizadoEm;

    protected Atendimento() {}

    public Atendimento(Chamado chamado, Usuario responsavel, String observacao, String solucao) {
        this.chamado = chamado;
        this.responsavel = responsavel;
        this.observacao = observacao;
        this.solucao = solucao;
    }

    @PrePersist
    void aoCriar() {
        this.realizadoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getSolucao() {
        return solucao;
    }

    public OffsetDateTime getRealizadoEm() {
        return realizadoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Atendimento outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
