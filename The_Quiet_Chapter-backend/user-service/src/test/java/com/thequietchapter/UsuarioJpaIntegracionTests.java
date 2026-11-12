package com.thequietchapter;
import com.thequietchapter.service.UsuarioService;
import com.thequietchapter.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest @Transactional
public class UsuarioJpaIntegracionTests {
 @Autowired UsuarioService usuarios;
 @Test void registroLoginYDireccionesPersistentes(){
  PerfilRespuesta nuevo=usuarios.registrar(new RegistroSolicitud("Rosa","Mora","rosa@example.cl","987654321","ClaveFuerte#123"));
  assertNotNull(nuevo.id());assertEquals("CLIENTE",nuevo.rol());
  TokenRespuesta sesion=usuarios.login(new LoginSolicitud("rosa@example.cl","ClaveFuerte#123"));
  String bearer="Bearer "+sesion.accessToken();
  assertEquals(nuevo.id(),usuarios.perfil(bearer).id());
  DireccionRespuesta d=usuarios.agregarDireccion(bearer,new DireccionSolicitud("Calle Central","77","Temuco","Araucania"));
  assertNotNull(d.id());assertTrue(d.predeterminada());
  assertEquals(1,usuarios.direcciones(bearer).size());
  usuarios.cambiarContrasena(bearer,new CambiarContrasenaSolicitud("ClaveFuerte#123","OtraClave#456"));
  assertThrows(ResponseStatusException.class,()->usuarios.login(new LoginSolicitud("rosa@example.cl","ClaveFuerte#123")));
  assertNotNull(usuarios.login(new LoginSolicitud("rosa@example.cl","OtraClave#456")).accessToken());
 }
 @Test void tokenFaltanteNoAccedeAPerfil(){assertThrows(ResponseStatusException.class,()->usuarios.perfil("sin-token"));}
}
