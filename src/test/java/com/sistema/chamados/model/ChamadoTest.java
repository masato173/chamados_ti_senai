package com.sistema.chamados.model;

import com.sistema.chamados.enums.AcaoHistorico;
import com.sistema.chamados.enums.Perfil;
import com.sistema.chamados.enums.StatusChamado;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ChamadoTest {

    private final Usuario solicitante = new Usuario("Maria Souza", "maria", "senha-ja-criptografada", Perfil.USER);
    private final Categoria categoria = new Categoria();

    @Test
    void deveriaAbrirChamadoComStatusAberto() {
        // ARRANGE
        String local = "Sala 12";
        String problema = "Impressora não liga";
        String observacao = "Desde ontem";

        // ACT
        var chamado = new Chamado(solicitante, categoria, local, problema, observacao);

        // ASSERT
        Assertions.assertSame(solicitante, chamado.getSolicitante());
        Assertions.assertSame(categoria, chamado.getCategoria());
        Assertions.assertEquals(local, chamado.getLocal());
        Assertions.assertEquals(problema, chamado.getProblema());
        Assertions.assertEquals(observacao, chamado.getObservacao());
        Assertions.assertEquals(StatusChamado.ABERTO, chamado.getStatus());
        Assertions.assertNull(chamado.getResponsavel());
        Assertions.assertNull(chamado.getExcluidoEm());
        Assertions.assertTrue(chamado.getAtendimentos().isEmpty());
        Assertions.assertTrue(chamado.getHistorico().isEmpty());
    }

    @Test
    void deveriaPreencherAbertoEmAntesDeInserir() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);

        // ACT
        chamado.aoCriar();

        // ASSERT
        Assertions.assertNotNull(chamado.getAbertoEm());
        Assertions.assertNull(chamado.getAtualizadoEm());
    }

    @Test
    void deveriaPreencherAtualizadoEmAntesDeAtualizar() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);

        // ACT
        chamado.aoAtualizar();

        // ASSERT
        Assertions.assertNotNull(chamado.getAtualizadoEm());
    }

    @Test
    void deveriaPreencherExcluidoEmAoExcluir() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);

        // ACT
        chamado.excluir();

        // ASSERT
        Assertions.assertNotNull(chamado.getExcluidoEm());
    }

    @Test
    void naoDeveriaPermitirAlterarAtendimentosPelaLista() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        var atendimento = new Atendimento(chamado, solicitante, null, "Cabo trocado");

        // ACT
        var atendimentos = chamado.getAtendimentos();

        // ASSERT
        Assertions.assertThrows(UnsupportedOperationException.class, () -> atendimentos.add(atendimento));
    }

    @Test
    void naoDeveriaPermitirAlterarHistoricoPelaLista() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        var historico = new HistoricoChamado(chamado, solicitante, AcaoHistorico.CHAMADO_ABERTO, null, null, null);

        // ACT
        var registros = chamado.getHistorico();

        // ASSERT
        Assertions.assertThrows(UnsupportedOperationException.class, () -> registros.add(historico));
    }

    @Test
    void deveriaSerIgualAOutroChamadoComMesmoId() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        var outro = new Chamado(solicitante, categoria, "Sala 30", "Sem internet", null);
        ReflectionTestUtils.setField(chamado, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 1L);

        // ACT
        boolean iguais = chamado.equals(outro);

        // ASSERT
        Assertions.assertTrue(iguais);
        Assertions.assertEquals(chamado.hashCode(), outro.hashCode());
    }

    @Test
    void naoDeveriaSerIgualAOutroChamadoComIdDiferente() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        var outro = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        ReflectionTestUtils.setField(chamado, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 2L);

        // ACT
        boolean iguais = chamado.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void naoDeveriaSerIgualAOutroChamadoQuandoNenhumTemId() {
        // ARRANGE
        var chamado = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);
        var outro = new Chamado(solicitante, categoria, "Sala 12", "Impressora não liga", null);

        // ACT
        boolean iguais = chamado.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

}
