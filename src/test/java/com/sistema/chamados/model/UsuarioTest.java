package com.sistema.chamados.model;

import com.sistema.chamados.enums.Perfil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class UsuarioTest {

    @Test
    void deveriaCriarUsuarioAtivoESemDataDeExclusao() {
        // ARRANGE
        String nome = "Maria Souza";
        String login = "maria";
        String senhaCriptografada = "senha-ja-criptografada";

        // ACT
        var usuario = new Usuario(nome, login, senhaCriptografada, Perfil.USER);

        // ASSERT
        Assertions.assertEquals(nome, usuario.getNome());
        Assertions.assertEquals(login, usuario.getLogin());
        Assertions.assertEquals(senhaCriptografada, usuario.getSenha());
        Assertions.assertEquals(Perfil.USER, usuario.getPerfil());
        Assertions.assertTrue(usuario.getAtivo());
        Assertions.assertNull(usuario.getExcluidoEm());
    }

    @Test
    void deveriaPreencherCriadoEmAntesDeInserir() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);

        // ACT
        usuario.aoCriar();

        // ASSERT
        Assertions.assertNotNull(usuario.getCriadoEm());
        Assertions.assertNull(usuario.getAtualizadoEm());
    }

    @Test
    void deveriaPreencherAtualizadoEmAntesDeAtualizar() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);

        // ACT
        usuario.aoAtualizar();

        // ASSERT
        Assertions.assertNotNull(usuario.getAtualizadoEm());
    }

    @Test
    void deveriaDesativarEPreencherExcluidoEmAoExcluir() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.ADMIN);

        // ACT
        usuario.excluir();

        // ASSERT
        Assertions.assertFalse(usuario.getAtivo());
        Assertions.assertNotNull(usuario.getExcluidoEm());
    }

    @Test
    void deveriaSerIgualAOutroUsuarioComMesmoId() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
        var outro = new Usuario("Outro Nome", "outro", "outra-senha-criptografada", Perfil.ADMIN);
        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 1L);

        // ACT
        boolean iguais = usuario.equals(outro);

        // ASSERT
        Assertions.assertTrue(iguais);
        Assertions.assertEquals(usuario.hashCode(), outro.hashCode());
    }

    @Test
    void naoDeveriaSerIgualAOutroUsuarioComIdDiferente() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
        var outro = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
        ReflectionTestUtils.setField(usuario, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 2L);

        // ACT
        boolean iguais = usuario.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void naoDeveriaSerIgualAOutroUsuarioQuandoNenhumTemId() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
        var outro = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);

        // ACT
        boolean iguais = usuario.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void deveriaSerIgualAoProxyDoMesmoUsuario() {
        // ARRANGE
        var usuario = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
        ReflectionTestUtils.setField(usuario, "id", 1L);
        // O proxy LAZY do Hibernate é uma subclasse da entidade; a classe anônima simula esse caso.
        Usuario proxy = new Usuario() {
        };
        ReflectionTestUtils.setField(proxy, "id", 1L);

        // ACT
        boolean usuarioIgualAoProxy = usuario.equals(proxy);
        boolean proxyIgualAoUsuario = proxy.equals(usuario);

        // ASSERT
        Assertions.assertTrue(usuarioIgualAoProxy);
        Assertions.assertTrue(proxyIgualAoUsuario);
    }

}
