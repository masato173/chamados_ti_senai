package com.sistema.chamados.model;

import com.sistema.chamados.enums.Perfil;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String login;

    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em")
    private OffsetDateTime atualizadoEm;

    @Column(name = "excluido_em")
    private OffsetDateTime excluidoEm;

    protected Usuario() {}

    public Usuario(String nome, String login, String senhaCriptografada, Perfil perfil) {
        this.nome = nome;
        this.login = login;
        this.senha = senhaCriptografada;
        this.perfil = perfil;
        this.ativo = true;
    }

    @PrePersist
    void aoCriar() {
        this.criadoEm = OffsetDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = OffsetDateTime.now();
    }

    public void excluir() {
        this.ativo = false;
        this.excluidoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public OffsetDateTime getExcluidoEm() {
        return excluidoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario outro)) {
            return false;
        }
        return id != null && id.equals(outro.getId());
    }

    @Override
    public int hashCode() {
        return Usuario.class.hashCode();
    }

}
