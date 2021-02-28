package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.cursospringrestapi.ApplicationContextLoad;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAuthenticacaoService {

	/* Tempo de expiracao do token em ms */
	private static final long EXPIRATION_TIME = 172812345;

	private static final String SECRET = "SenhaExtremamenteSecreta";

	// prefixo padrao de token
	private static final String TOKEN_PREFIX = "Bearer";

	private static final String HEADER_STRING = "Authorization";

	public void addAuthentication(HttpServletResponse response, String username) throws IOException {

		String JWT = Jwts.builder().setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();

		String token = TOKEN_PREFIX + " " + JWT;

		response.addHeader(HEADER_STRING, token);

		response.getWriter().write("{\"Authorization\":\"" + token + "\"}");

	}

	public Authentication getAuthentication(HttpServletRequest request) {

		String token = request.getHeader(HEADER_STRING);

		if (token != null) {

			String user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody()
					.getSubject();

			if (user != null) {

				Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class) 
						.findUserByLogin(user);

				if (usuario != null) {

					return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
							usuario.getAuthorities());

				}

			}

		}

		return null; // nao autorizado

	}

}
