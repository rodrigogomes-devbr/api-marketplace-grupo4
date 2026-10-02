package br.edu.fiap.marketplace.service;

import br.edu.fiap.marketplace.dto.UsuarioRequest;
import br.edu.fiap.marketplace.dto.UsuarioResponse;
import br.edu.fiap.marketplace.entity.Usuario;
import br.edu.fiap.marketplace.exception.ConflitoNegocioException;
import br.edu.fiap.marketplace.exception.RecursoNaoEncontradoException;
import br.edu.fiap.marketplace.exception.RegraNegocioException;
import br.edu.fiap.marketplace.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioResponse cadastrar(UsuarioRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflitoNegocioException("E-mail já cadastrado.");
        }

        if (request.senha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("Senha excede 72 bytes");
        }

        String senhaHash = encoder.encode(request.senha());
        Usuario usuario = new Usuario(request.nome(), email, senhaHash);

        return UsuarioResponse.de(usuarios.saveAndFlush(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        return UsuarioResponse.de(usuario);
    }
}