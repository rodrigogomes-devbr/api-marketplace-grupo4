package br.edu.fiap.marketplace.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CatalogoProdutoTest {

    private CatalogoProduto novoProduto(int estoque) {
        return new CatalogoProduto(
                "Teclado mecânico", "Teclado ABNT2", new BigDecimal("299.90"), estoque, true);
    }

    @Test
    void deveBaixarEstoqueQuandoHaSaldo() {
        CatalogoProduto produto = novoProduto(10);

        produto.baixarEstoque(3);

        assertThat(produto.getEstoque()).isEqualTo(7);
    }

    @Test
    void naoDeveBaixarEstoqueMaiorQueOSaldo() {
        CatalogoProduto produto = novoProduto(5);

        assertThatThrownBy(() -> produto.baixarEstoque(6))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(produto.getEstoque()).isEqualTo(5);
    }

    @Test
    void naoDeveAceitarPrecoZero() {
        CatalogoProduto produto = novoProduto(5);

        assertThatThrownBy(() -> produto.alterarPreco(BigDecimal.ZERO))
                .isInstanceOf(RegraNegocioException.class);
    }
}