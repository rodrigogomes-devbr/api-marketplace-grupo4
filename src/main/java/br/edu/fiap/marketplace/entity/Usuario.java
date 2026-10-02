package br.edu.fiap.marketplace.entity;

import br.edu.fiap.marketplace.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa a pessoa cadastrada no marketplace.
 *
 * <p>O mapeamento JPA e os construtores já estão prontos. Os comportamentos
 * permanecem como exercício e devem proteger o estado da entidade.</p>
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(nullable = false, length = 100)
    private String senha;

    @Column(nullable = false)
    private boolean ativo;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senha = senhaHash;
        this.ativo = true;
    }

    /** TODO implementar a troca do hash armazenado. */
    public void atualizarSenhaHash(String novoHash) {
        if(novoHash == null || novoHash.isBlank()) {
            throw new RegraNegocioException("Hash de senha inválido");
        }
        this.senha = novoHash;

    }

    /** TODO permitir novamente o uso da conta. */
    public void ativar() {
        this.ativo = true;
    }

    /** TODO impedir login e novas compras. */
    public void desativar() {
        this.ativo = false;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public boolean isAtivo() { return ativo; }
}
