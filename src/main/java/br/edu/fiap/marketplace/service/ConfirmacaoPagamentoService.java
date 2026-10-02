package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoRequest;
import br.edu.fiap.marketplace.dto.ConfirmacaoPagamentoResponse;
import br.edu.fiap.marketplace.entity.Carrinho;
import br.edu.fiap.marketplace.entity.ConfirmacaoPagamento;
import br.edu.fiap.marketplace.entity.StatusCarrinho;
import br.edu.fiap.marketplace.entity.StatusPagamento;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.ConflitoNegocioException;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.exception.RegraNegocioException;
import br.edu.fiap.marketplace.repository.CarrinhoRepository;
import br.edu.fiap.marketplace.repository.ConfirmacaoPagamentoRepository;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ConfirmacaoPagamentoService {

    private final ConfirmacaoPagamentoRepository pagamentoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioRepository usuarioRepository;

    public ConfirmacaoPagamentoService(
            ConfirmacaoPagamentoRepository pagamentoRepository,
            CarrinhoRepository carrinhoRepository,
            UsuarioRepository usuarioRepository) {

        this.pagamentoRepository = pagamentoRepository;
        this.carrinhoRepository = carrinhoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ConfirmacaoPagamentoResponse criar(ConfirmacaoPagamentoRequest request) {

        Carrinho carrinho = carrinhoRepository.findById(request.carrinhoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Carrinho não encontrado."));

        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado."));

        if (!carrinho.getUsuario().getId().equals(usuario.getId())) {
            throw new RegraNegocioException(
                    "Usuário informado não é o dono do carrinho.");
        }

        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new RegraNegocioException(
                    "Apenas carrinho aberto pode receber um pagamento.");
        }

        if (pagamentoRepository.findByCarrinhoId(carrinho.getId()).isPresent()) {
            throw new ConflitoNegocioException(
                    "O carrinho já possui uma confirmação de pagamento.");
        }

        if (pagamentoRepository.existsByIdPagamento(request.idPagamento())) {
            throw new ConflitoNegocioException(
                    "ID do pagamento já utilizado.");
        }

        BigDecimal valorPago = carrinho.calcularTotal();

        ConfirmacaoPagamento pagamento = new ConfirmacaoPagamento(
                carrinho,
                usuario,
                request.idPagamento(),
                valorPago);

        ConfirmacaoPagamento salvo = pagamentoRepository.save(pagamento);

        return ConfirmacaoPagamentoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public ConfirmacaoPagamentoResponse consultar(Long id) {

        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pagamento não encontrado."));

        return ConfirmacaoPagamentoResponse.de(pagamento);
    }

    @Transactional
    public ConfirmacaoPagamentoResponse aprovar(Long id) {

        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pagamento não encontrado."));

        if (pagamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException(
                    "Pagamento já foi processado anteriormente.");
        }

        Carrinho carrinho = pagamento.getCarrinho();

        if (carrinho.getStatus() != StatusCarrinho.ABERTO) {
            throw new RegraNegocioException(
                    "Apenas carrinho aberto pode ter o pagamento aprovado.");
        }

        if (pagamento.getValorPago().compareTo(carrinho.calcularTotal()) != 0) {
            throw new RegraNegocioException(
                    "Valor pago não corresponde ao total atual do carrinho.");
        }

        carrinho.getProduto().baixarEstoque(carrinho.getQuantidade());

        pagamento.aprovar();

        return ConfirmacaoPagamentoResponse.de(pagamento);
    }

    @Transactional
    public ConfirmacaoPagamentoResponse recusar(Long id) {

        ConfirmacaoPagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Pagamento não encontrado."));

        pagamento.recusar();

        return ConfirmacaoPagamentoResponse.de(pagamento);
    }
}