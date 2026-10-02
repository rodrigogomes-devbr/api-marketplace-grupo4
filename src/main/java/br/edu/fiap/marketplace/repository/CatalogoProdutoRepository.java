package br.edu.fiap.marketplace.repository;

import br.edu.fiap.marketplace.entity.CatalogoProduto;
import org.springframework.data.jpa.repository.JpaRepository;

/** TODO adicionar consultas do catálogo conforme os casos de uso. */
public interface CatalogoProdutoRepository extends JpaRepository<CatalogoProduto, Long> {

    boolean existsByNomeIgnoreCase(String nome);
}
