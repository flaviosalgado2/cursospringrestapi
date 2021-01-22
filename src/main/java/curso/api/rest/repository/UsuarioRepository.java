package curso.api.rest.repository;

import org.springframework.data.repository.CrudRepository;

import curso.api.rest.model.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

}
