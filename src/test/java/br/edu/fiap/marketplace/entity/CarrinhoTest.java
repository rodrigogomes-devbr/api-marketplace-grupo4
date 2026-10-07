package br.edu.fiap.marketplace.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CarrinhoTest {

    private Carrinho novoCarrinho(int quantidade) {
        Usuario usuario = new Usuario("Teste", "teste@marketplace.com", "hash");
        CatalogoProduto produto = new CatalogoProduto(
                "Teclado mecânico", "Teclado ABNT2", new BigDecimal("299.90"), 10, true);
        return new Carrinho(usuario, produto, quantidade);
    }

    @Test
    void deveCalcularTotalComoPrecoVezesQuantidade() {
        // Arrange
        Carrinho carrinho = novoCarrinho(2);

        // Act
        BigDecimal total = carrinho.calcularTotal();

        // Assert
        assertThat(total).isEqualByComparingTo("599.80");
    }

    @Test
    void naoDeveAlterarQuantidadeDeCarrinhoFinalizado() {
        // Arrange
        Carrinho carrinho = novoCarrinho(2);
        carrinho.finalizar();

        // Act + Assert
        assertThatThrownBy(() -> carrinho.alterarQuantidade(5))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(carrinho.getQuantidade()).isEqualTo(2);
    }
}