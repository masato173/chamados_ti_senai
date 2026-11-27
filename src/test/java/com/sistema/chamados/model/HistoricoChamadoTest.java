package com.sistema.chamados.model;

import com.sistema.chamados.enums.AcaoHistorico;
import com.sistema.chamados.enums.Perfil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class HistoricoChamadoTest {

    private final Usuario administrador = new Usuario("João Lima", "joao", "senha-ja-criptografada", Perfil.ADMIN);
    private final Chamado chamado = new Chamado(administrador, new Categoria(), "Sala 12", "Impressora não liga", null);

    @Test
    void deveriaRegistrarHistoricoComOsDadosInformados() {
        // ARRANGE
        String campoAlterado = "status";
        String valorAnterior = "ABERTO";
        String valorNovo = "EM_ANDAMENTO";

        // ACT
        var historico = new HistoricoChamado(chamado, administrador, AcaoHistorico.STATUS_ALTERADO, campoAlterado,
                valorAnterior, valorNovo);

        // ASSERT
        Assertions.assertSame(chamado, historico.getChamado());
        Assertions.assertSame(administrador, historico.getUsuario());
        Assertions.assertEquals(AcaoHistorico.STATUS_ALTERADO, historico.getAcao());
        Assertions.assertEquals(campoAlterado, historico.getCampoAlterado());
        Assertions.assertEquals(valorAnterior, historico.getValorAnterior());
        Assertions.assertEquals(valorNovo, historico.getValorNovo());
        Assertions.assertNull(historico.getCriadoEm());
    }

    @Test
    void deveriaPreencherCriadoEmAntesDeInserir() {
        // ARRANGE
        var historico = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);

        // ACT
        historico.aoCriar();

        // ASSERT
        Assertions.assertNotNull(historico.getCriadoEm());
    }

    @Test
    void deveriaSerIgualAOutroHistoricoComMesmoId() {
        // ARRANGE
        var historico = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);
        var outro = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_EXCLUIDO, null, null, null);
        ReflectionTestUtils.setField(historico, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 1L);

        // ACT
        boolean iguais = historico.equals(outro);

        // ASSERT
        Assertions.assertTrue(iguais);
        Assertions.assertEquals(historico.hashCode(), outro.hashCode());
    }

    @Test
    void naoDeveriaSerIgualAOutroHistoricoComIdDiferente() {
        // ARRANGE
        var historico = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);
        var outro = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);
        ReflectionTestUtils.setField(historico, "id", 1L);
        ReflectionTestUtils.setField(outro, "id", 2L);

        // ACT
        boolean iguais = historico.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void naoDeveriaSerIgualAOutroHistoricoQuandoNenhumTemId() {
        // ARRANGE
        var historico = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);
        var outro = new HistoricoChamado(chamado, administrador, AcaoHistorico.CHAMADO_ABERTO, null, null, null);

        // ACT
        boolean iguais = historico.equals(outro);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

}
