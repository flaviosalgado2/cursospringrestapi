package curso.api.rest.security;

import org.apache.catalina.authenticator.SpnegoAuthenticator.AuthenticateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import curso.api.rest.service.ImplementacaoUserDetailsService;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter{
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	//configura solicitacaoes de acesso via http
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		//ativando protecao contra usuarios nao validados por token
		http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
		
		//ativando permissao de acesso a pagina inicial
		.disable().authorizeRequests().antMatchers("/").permitAll()
		.antMatchers("/index").permitAll()
		
		//redireciona para deslogar
		.anyRequest().authenticated().and().logout().logoutSuccessUrl("/index")
		
		//mapeia URL do logout e invalida usuaio
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		
		//filtra requisicoes de login para autenticacao
		.and().addFilterBefore(new JWTLoginFilter("/login", authenticationManager()), UsernamePasswordAuthenticationFilter.class)
		
		//filtra demais requisicoes para verificar a presencao do token jwt no header http
		.addFilterBefore(new JwtApiAutenticacaoFilter(), UsernamePasswordAuthenticationFilter.class);
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		
		//service que consulta usuario no banco
		auth.userDetailsService(implementacaoUserDetailsService)
		//padro de codificacao de senha
		.passwordEncoder(new BCryptPasswordEncoder());
	}
}
