package com.sistema.chamados.model;

import com.sistema.chamados.enums.Perfil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AtendimentoTest {

    private final Usuario solicitante = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
    private final Usuario administrador = new Usuario("João Lima", "joao", "senha-ja-criptografada", Perfil.ADMIN);
    private final Chamado chamado = new Chamado(solicitante, new Categoria(), "Sala 12", "Impressora não liga", null);

    @Test
    void deveriaRegistrarAtendimentoComOsDadosInformados() {
        // ARRANGE
        String observacao = "Cabo solto";
        String solucao = "Cabo de energia reconectado";

        // ACT
        var atendimento = new Atendimento(chamado, administrador, observacao, solucao);

        // ASSERT
        Assertions.assertSame(chamado, atendimento.getChamado());
        Assertions.assertSame(administrador, atendimento.getResponsavel());
        Assertions.assertEquals(observacao, atendimento.getObservacao());
        Assertions.assertEquals(solucao, atendimento.getSolucao());
        Assertions.assertNull(atendimento.getRealizadoEm());
    }

    @Test
    void deveriaPreencherRealizadoEmAntesDeInserir() {
        // ARRANGE
        var atendimento = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");

        // ACT
        atendimento.aoCriar();

        // ASSERT
        Assertions.assertNotNull(atendimento.getRealizadoEm());
    }

    @Test
    void deveriaSerIgualAOutroAtendimentoComMesmoId() {
        // ARRANGE
        var atendimento = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");
        var outro = new Atendimento(chamado, administrador, null, "Toner trocado");
        ReflectionTestUtils.setField(atendimento, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 1L);

        // ACT
        boolean iguais = atendimento.equals(outro);

        // ASSERT
        Assertions.assertTrue(iguais);
        Assertions.assertEquals(atendimento.hashCode(), outro.hashCode());
    }

    @Test
    void naoDeveriaSerIgualAOutroAtendimentoComIdDiferente() {
        // ARRANGE
        var atendimento = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");
        var outro = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");
        ReflectionTestUtils.setField(atendimento, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 2L);

        // ACT
        boolean iguais = atendimento.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void naoDeveriaSerIgualAOutroAtendimentoQuandoNenhumTemId() {
        // ARRANGE
        var atendimento = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");
        var outro = new Atendimento(chamado, administrador, null, "Cabo de energia reconectado");

        // ACT
        boolean iguais = atendimento.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

}
