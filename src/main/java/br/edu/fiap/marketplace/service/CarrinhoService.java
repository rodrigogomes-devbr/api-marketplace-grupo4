package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.AtualizarQuantidadeRequest;
import br.edu.fiap.marketplace.dto.CarrinhoRequest;
import br.edu.fiap.marketplace.dto.CarrinhoResponse;
import br.edu.fiap.marketplace.entity.Carrinho;
import br.edu.fiap.marketplace.entity.CatalogoProduto;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.exception.RegraNegocioException;
import br.edu.fiap.marketplace.repository.CarrinhoRepository;
import br.edu.fiap.marketplace.repository.CatalogoProdutoRepository;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/** TODO coordenar usuário, produto, estoque, quantidade e estado do carrinho. */
@Service
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CatalogoProdutoRepository catalogoProdutoRepository;

    public CarrinhoService(
            CarrinhoRepository carrinhoRepository,
            UsuarioRepository usuarioRepository,
            CatalogoProdutoRepository catalogoProdutoRepository) {
        this.carrinhoRepository = carrinhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.catalogoProdutoRepository = catalogoProdutoRepository;
    }


    @Transactional
    public CarrinhoResponse criar(CarrinhoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));

        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuário inativo não pode criar carrinho");
        }

        CatalogoProduto produto = catalogoProdutoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));

        if (!produto.isAtivo()) {
            throw new RegraNegocioException("Produto inativo não pode entrar em um carrinho");
        }

        Carrinho carrinho = new Carrinho(usuario, produto, request.quantidade());
        return CarrinhoResponse.de(carrinhoRepository.save(carrinho));
    }

    @Transactional(readOnly = true)
    public CarrinhoResponse buscarPorId(Long id) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado"));
        return CarrinhoResponse.de(carrinho);
    }

    @Transactional(readOnly = true)
    public List<CarrinhoResponse> listarPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }

        return carrinhoRepository.findByUsuarioId(usuarioId).stream()
                .map(CarrinhoResponse::de)
                .toList();
    }

    @Transactional
    public CarrinhoResponse alterarQuantidade(Long id, AtualizarQuantidadeRequest request) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado"));

        carrinho.alterarQuantidade(request.quantidade());

        return CarrinhoResponse.de(carrinhoRepository.save(carrinho));
    }

    @Transactional
    public void cancelar(Long id) {
        Carrinho carrinho = carrinhoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Carrinho não encontrado"));

        carrinho.cancelar();
        carrinhoRepository.save(carrinho);
    }
}




