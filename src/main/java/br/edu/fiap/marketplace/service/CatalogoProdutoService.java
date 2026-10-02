package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.CatalogoProdutoRequest;
import br.edu.fiap.marketplace.dto.CatalogoProdutoResponse;
import br.edu.fiap.marketplace.entity.CatalogoProduto;
import br.edu.fiap.marketplace.exception.ConflitoNegocioException;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.repository.CatalogoProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CatalogoProdutoService {

    private final CatalogoProdutoRepository repository;

    public CatalogoProdutoService(CatalogoProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CatalogoProdutoResponse cadastrar(CatalogoProdutoRequest request) {

        if (repository.existsByNomeIgnoreCase(request.nome())) {
            throw new ConflitoNegocioException("Já existe um produto com esse nome.");
        }

        CatalogoProduto produto = new CatalogoProduto(
                request.nome(),
                request.descricao(),
                request.preco(),
                request.estoque(),
                request.ativo()
        );

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<CatalogoProdutoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(CatalogoProdutoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogoProdutoResponse buscarPorId(Long id) {
        return CatalogoProdutoResponse.de(buscarProduto(id));
    }

    @Transactional
    public CatalogoProdutoResponse atualizar(Long id, CatalogoProdutoRequest request) {

        CatalogoProduto produto = buscarProduto(id);

        produto.alterarPreco(request.preco());

        if (request.ativo()) {
            produto.ativar();
        } else {
            produto.desativar();
        }

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public CatalogoProdutoResponse alterarPreco(Long id, BigDecimal novoPreco) {

        CatalogoProduto produto = buscarProduto(id);

        produto.alterarPreco(novoPreco);

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public CatalogoProdutoResponse baixarEstoque(Long id, Integer quantidade) {

        CatalogoProduto produto = buscarProduto(id);

        produto.baixarEstoque(quantidade);

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public CatalogoProdutoResponse reporEstoque(Long id, Integer quantidade) {

        CatalogoProduto produto = buscarProduto(id);

        produto.reporEstoque(quantidade);

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public CatalogoProdutoResponse ativar(Long id) {

        CatalogoProduto produto = buscarProduto(id);

        produto.ativar();

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    @Transactional
    public CatalogoProdutoResponse desativar(Long id) {

        CatalogoProduto produto = buscarProduto(id);

        produto.desativar();

        return CatalogoProdutoResponse.de(repository.save(produto));
    }

    private CatalogoProduto buscarProduto(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado."));
    }
}