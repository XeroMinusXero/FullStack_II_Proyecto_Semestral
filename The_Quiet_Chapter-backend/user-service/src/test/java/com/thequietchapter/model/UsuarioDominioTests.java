package com.thequietchapter.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class UsuarioDominioTests {
 @Test void perfilConRolYContrasena(){
  Usuario u=new Usuario("Ana","Perez","ANA@MAIL.CL","12345678","hash-bcrypt-de-prueba");
  assertEquals(RolUsuario.CLIENTE,u.getRol());
  assertEquals("ana@mail.cl",u.getEmail());
  assertEquals("Ana Perez",u.obtenerNombreCompleto());
  assertTrue(u.cumplePoliticaContrasena("unaClave#123"));
  assertFalse(u.cumplePoliticaContrasena("123"));
  assertFalse(u.cumplePoliticaContrasena("sinEspecial123"));
 }
 @Test void libretaNoDuplicaPredeterminada(){
  LibretaDirecciones l=LibretaDirecciones.crear();
  Direccion a=new Direccion("Calle 1","11","Comuna","Region");
  Direccion b=new Direccion("Calle 2","22","Comuna","Region");
  assertTrue(l.agregarDireccion(a));assertTrue(l.agregarDireccion(b));
  assertTrue(a.isPredeterminada());assertFalse(b.isPredeterminada());
  assertEquals(2,l.listarDirecciones().size());
 }
}
