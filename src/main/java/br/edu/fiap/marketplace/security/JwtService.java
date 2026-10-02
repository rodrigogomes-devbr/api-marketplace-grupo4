package br.edu.fiap.marketplace.security;

import br.edu.fiap.marketplace.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final long expiracaoSegundos;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.expiracao-segundos:900}") long expiracaoSegundos) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.expiracaoSegundos = expiracaoSegundos;
    }

    public TokenGerado gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plusSeconds(expiracaoSegundos);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nome", usuario.getNome())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new TokenGerado(token, expiracaoSegundos, expiraEm);
    }

    public record TokenGerado(String accessToken, long expiresIn, Instant expiresAt) {
    }
}