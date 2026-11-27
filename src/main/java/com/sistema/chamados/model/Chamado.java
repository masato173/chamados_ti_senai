package com.sistema.chamados.model;

import com.sistema.chamados.enums.StatusChamado;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "chamados")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", updatable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

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

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @Column(name = "excluido_em")
    private OffsetDateTime excluidoEm;

    @OneToMany(mappedBy = "chamado")
    private List<Atendimento> atendimentos = new ArrayList<>();

    @OneToMany(mappedBy = "chamado")
    private List<HistoricoChamado> historico = new ArrayList<>();

    protected Chamado() {}

    public Chamado(Usuario solicitante, Categoria categoria, String local, String problema, String observacao) {
        this.solicitante = solicitante;
        this.categoria = categoria;
        this.local = local;
        this.problema = problema;
        this.observacao = observacao;
        this.status = StatusChamado.ABERTO;
    }

    @PrePersist
    void aoCriar() {
        this.abertoEm = OffsetDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void excluir() {
        this.excluidoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Usuario getResponsavel() {
        return responsavel;
    }

    public String getLocal() {
        return local;
    }

    public String getProblema() {
        return problema;
    }

    public String getObservacao() {
        return observacao;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public OffsetDateTime getAbertoEm() {
        return abertoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public OffsetDateTime getExcluidoEm() {
        return excluidoEm;
    }

    public List<Atendimento> getAtendimentos() {
        return Collections.unmodifiableList(atendimentos);
    }

    public List<HistoricoChamado> getHistorico() {
        return Collections.unmodifiableList(historico);
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
        return Objects.hashCode(id);
    }

}
