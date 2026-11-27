package com.sistema.chamados.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CategoriaTest {

    @Test
    void deveriaSerIgualAOutraCategoriaComMesmoId() {
        // ARRANGE
        var categoria = new Categoria();
        var outra = new Categoria();
        ReflectionTestUtils.setField(categoria, "id", 1L);
        ReflectionTestUtils.setField(outra, "id", 1L);

        // ACT
        boolean iguais = categoria.equals(outra);

        // ASSERT
        Assertions.assertTrue(iguais);
        Assertions.assertEquals(categoria.hashCode(), outra.hashCode());
    }

    @Test
    void naoDeveriaSerIgualAOutraCategoriaComIdDiferente() {
        // ARRANGE
        var categoria = new Categoria();
        var outra = new Categoria();
        ReflectionTestUtils.setField(categoria, "id", 1L);
        ReflectionTestUtils.setField(outra, "id", 2L);

        // ACT
        boolean iguais = categoria.equals(outra);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

    @Test
    void naoDeveriaSerIgualAOutraCategoriaQuandoNenhumaTemId() {
        // ARRANGE
        var categoria = new Categoria();
        var outra = new Categoria();

        // ACT
        boolean iguais = categoria.equals(outra);

        // ASSERT
        Assertions.assertFalse(iguais);
    }

}
