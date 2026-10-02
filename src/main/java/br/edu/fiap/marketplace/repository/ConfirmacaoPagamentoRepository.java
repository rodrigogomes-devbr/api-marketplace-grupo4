package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.ConfirmacaoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** TODO adicionar consultas por carrinho e pelo identificador externo do pagamento. */
public interface ConfirmacaoPagamentoRepository
        extends JpaRepository<ConfirmacaoPagamento, Long> {

    Optional<ConfirmacaoPagamento> findByCarrinhoId(Long carrinhoId);
    boolean existsByIdPagamento(String idPagamento);


}
