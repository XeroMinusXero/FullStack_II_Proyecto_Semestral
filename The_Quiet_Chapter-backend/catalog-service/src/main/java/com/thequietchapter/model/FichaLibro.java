package com.thequietchapter.model;

import jakarta.persistence.*;

/**
 * Composicion DC-01: la ficha se almacena junto con el libro (sin tabla
 * propia).
 */
@Embeddable
public class FichaLibro {
    @Column(nullable = false, columnDefinition = "TEXT")
    private String sinopsis = "";
    @Column(nullable = false, length = 80)
    private String formato = "";
    private int paginas;
    @Column(nullable = false, length = 80)
    private String idioma = "";
    @Column(name = "anio_publicacion")
    private int anioPublicacion;
    @Column(nullable = false, length = 80)
    private String encuadernacion = "";
    @Column(name = "portada_url", nullable = false, columnDefinition = "TEXT")
    private String portadaUrl = "";
    @Column(name = "extracto_url", nullable = false, columnDefinition = "TEXT")
    private String extractoUrl = "";

    public FichaLibro() {
    }

    public FichaLibro(String sinopsis, String formato, int paginas, String idioma,
            int anioPublicacion, String encuadernacion, String portadaUrl, String extractoUrl) {
        if (paginas < 0 || anioPublicacion < 0)
            throw new IllegalArgumentException("Ficha invalida");
        this.sinopsis = texto(sinopsis);
        this.formato = texto(formato);
        this.paginas = paginas;
        this.idioma = texto(idioma);
        this.anioPublicacion = anioPublicacion;
        this.encuadernacion = texto(encuadernacion);
        this.portadaUrl = texto(portadaUrl);
        this.extractoUrl = texto(extractoUrl);
    }

    private static String texto(String value) {
        return value == null ? "" : value.strip();
    }

    public void actualizarInformacion(FichaLibro nuevaFicha) {
        if (nuevaFicha == null)
            throw new IllegalArgumentException("Ficha requerida");
        sinopsis = nuevaFicha.sinopsis;
        formato = nuevaFicha.formato;
        paginas = nuevaFicha.paginas;
        idioma = nuevaFicha.idioma;
        anioPublicacion = nuevaFicha.anioPublicacion;
        encuadernacion = nuevaFicha.encuadernacion;
        portadaUrl = nuevaFicha.portadaUrl;
        extractoUrl = nuevaFicha.extractoUrl;
    }

    public void actualizarPortada(String url) {
        portadaUrl = texto(url);
    }

    public void actualizarExtracto(String url) {
        extractoUrl = texto(url);
    }

    public boolean tieneExtracto() {
        return !extractoUrl.isBlank();
    }

    public String getSinopsis() {
        return sinopsis;
    }

    public String getFormato() {
        return formato;
    }

    public int getPaginas() {
        return paginas;
    }

    public String getIdioma() {
        return idioma;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public String getEncuadernacion() {
        return encuadernacion;
    }

    public String getPortadaUrl() {
        return portadaUrl;
    }

    public String getExtractoUrl() {
        return extractoUrl;
    }
}
