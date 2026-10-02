package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** TODO adicionar consultas por usuário e status do carrinho. */
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    List<Carrinho> findByUsuarioId(Long usuarioId);
}
