package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repository.UsuarioRepository;

@RestController
@RequestMapping(value = "/usuario")
public class IndexController {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@GetMapping(value = "v1/{id}", produces = "application/json", headers = "X-API-Version=v1")
	public ResponseEntity<UsuarioDTO> initV1(@PathVariable(value = "id") Long id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		System.out.println("versao 1");

		return new ResponseEntity<UsuarioDTO>(new UsuarioDTO(usuario.get()), HttpStatus.OK);

	}

	@GetMapping(value = "v2/{id}", produces = "application/json", headers = "X-API-Version=v1")
	public ResponseEntity<Usuario> initV2(@PathVariable(value = "id") Long id) {

		Optional<Usuario> usuario = usuarioRepository.findById(id);

		System.out.println("versao 2");

		return new ResponseEntity(usuario.get(), HttpStatus.OK);

	}

	// teste de cache em consultas lentas
	@GetMapping(value = "/", produces = "application/json")
	// @Cacheable("cacheusuarios")
	@CacheEvict(value = "cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuario() throws InterruptedException {

		List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();

		// simula uma consulta lenta
		// Thread.sleep(6000);

		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}
	

	//consulta de usuario por nome
	@GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<List<Usuario>> usuarioPorNome(@PathVariable("nome") String nome) throws InterruptedException {

		List<Usuario> list = (List<Usuario>) usuarioRepository.findUserByNome(nome);

		return new ResponseEntity<List<Usuario>>(list, HttpStatus.OK);
	}

	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) {

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}

		try {

			// consumindo API externa
			URL url = new URL("https://viacep.com.br/ws/" + usuario.getCep() + "/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

			String cep = "";
			StringBuilder jsonCep = new StringBuilder();

			while ((cep = br.readLine()) != null) {
				jsonCep.append(cep);
			}

			System.out.println(jsonCep.toString());

			Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);

			usuario.setSenha(userAux.getCep());
			usuario.setLogradouro(userAux.getLogradouro());
			usuario.setComplemento(userAux.getComplemento());
			usuario.setBairro(userAux.getBairro());
			usuario.setLocalidade(userAux.getLocalidade());
			usuario.setUf(userAux.getUf());

		} catch (Exception e) {
		}

		// criptografando senha
		String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografada);

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}

	// atualizando usuario
	@PutMapping(value = "/")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {

		for (int pos = 0; pos < usuario.getTelefones().size(); pos++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}

		// atualizar senha
		// se a senha dada pra atualizar for diferente da que esta no banco
		Usuario userTemporario = usuarioRepository.findUserByLogin(usuario.getLogin());

		if (!userTemporario.getSenha().equals(usuario.getSenha())) {// senhas diferentes
			String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhacriptografada);
		}

		Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}

	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity<Usuario> cadastrarvenda(@PathVariable Long iduser, @PathVariable Long idvenda) {

		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("id user: " + iduser + "idvenda: " + idvenda, HttpStatus.OK);
	}

	@PutMapping(value = "/{iduser}/idvenda/{idvenda}")
	public ResponseEntity<Usuario> updateVenda(@PathVariable Long iduser, @PathVariable Long idvenda) {

		// Usuario usuarioSalvo = usuarioRepository.save(usuario);

		return new ResponseEntity("Venda Atualizada!", HttpStatus.OK);
	}

	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {
		usuarioRepository.deleteById(id);

		return "ok";
	}
}
