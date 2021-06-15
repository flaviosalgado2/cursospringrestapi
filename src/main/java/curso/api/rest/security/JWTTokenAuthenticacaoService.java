package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import curso.example.cursospringrestapi.ApplicationContextLoad;
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
		
		//atualiza token no banco de dados
		ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
		.atualizaTokenUser(JWT, username);

		// evita erro de cors
		liberacaoCors(response);

		response.getWriter().write("{\"Authorization\":\"" + token + "\"}");

	}

	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {

		// pega na requisicao
		String token = request.getHeader(HEADER_STRING);

		try {

			if (token != null) {

				String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();

				String user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
						.getBody().getSubject();

				if (user != null) {

					Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
							.findUserByLogin(user);

					if (usuario != null) {

						if (tokenLimpo.equalsIgnoreCase(usuario.getToken())) {

							return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(),
									usuario.getAuthorities());
						}

					}

				}

			} /* fim da condicao */

		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			try {
				response.getOutputStream()
						.println("Seu TOKEN esta expirado, faca o login ou informe um novo TOKEN para autenticacao");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// evita erro de CORS
		liberacaoCors(response);

		return null; // nao autorizado

	}

	private void liberacaoCors(HttpServletResponse response) {

		if (response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}

		if (response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}

		if (response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}

		if (response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}

	}

}
