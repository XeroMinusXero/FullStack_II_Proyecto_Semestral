package com.thequietchapter.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Entidad POO DC-01; sus reglas de negocio siguen dentro del objeto. */
@Entity
@Table(name = "libros", uniqueConstraints = @UniqueConstraint(columnNames = "isbn"))
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String isbn;
    @Column(nullable = false, length = 255)
    private String titulo;
    @Column(nullable = false, precision = 13, scale = 2)
    private BigDecimal precio;
    @Column(nullable = false)
    private boolean activo;
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    /** Composicion 1:1 de Libro con su ficha; las columnas viven en libros. */
    @Embedded
    private FichaLibro ficha = new FichaLibro();

    /** Inventario es el unico lugar donde se mantiene el stock. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "inventario_id", nullable = false, unique = true)
    private Inventario inventario;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "editorial_id", nullable = false)
    private Editorial editorial;

    @ManyToMany(fetch = FetchType.EAGER)
    @org.hibernate.annotations.BatchSize(size = 50)
    @JoinTable(name = "libro_autores", joinColumns = @JoinColumn(name = "libro_id"), inverseJoinColumns = @JoinColumn(name = "autor_id"))
    private Set<Autor> autores = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @org.hibernate.annotations.BatchSize(size = 50)
    @JoinTable(name = "libro_generos", joinColumns = @JoinColumn(name = "libro_id"), inverseJoinColumns = @JoinColumn(name = "genero_id"))
    private Set<Genero> generos = new LinkedHashSet<>();

    protected Libro() {
    } // JPA necesita constructor sin argumentos.

    public Libro(String isbn, String titulo, BigDecimal precio, Inventario inventario) {
        validarIdentificacion(titulo, isbn);
        validarPrecio(precio);
        if (inventario == null)
            throw new IllegalArgumentException("Un libro requiere inventario");
        this.isbn = isbn.strip();
        this.titulo = titulo.strip();
        this.precio = precio;
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
        this.inventario = inventario;
    }

    public void actualizarIdentificacion(String titulo, String isbn) {
        validarIdentificacion(titulo, isbn);
        this.titulo = titulo.strip();
        this.isbn = isbn.strip();
    }

    public void actualizarPrecio(BigDecimal precio) {
        validarPrecio(precio);
        this.precio = precio;
    }

    public void cambiarEstado(boolean activo) {
        this.activo = activo;
    }

    public void asignarCategoria(Categoria categoria) {
        if (categoria == null)
            throw new IllegalArgumentException("Categoria requerida");
        this.categoria = categoria;
    }

    public void asignarEditorial(Editorial editorial) {
        if (editorial == null)
            throw new IllegalArgumentException("Editorial requerida");
        this.editorial = editorial;
    }

    public void reemplazarAutores(List<Autor> autores) {
        if (autores == null || autores.isEmpty() || autores.stream().anyMatch(a -> a == null))
            throw new IllegalArgumentException("Se requiere al menos un autor");
        this.autores.clear();
        this.autores.addAll(autores);
    }

    public void reemplazarGeneros(List<Genero> generos) {
        if (generos == null || generos.stream().anyMatch(g -> g == null))
            throw new IllegalArgumentException("Generos invalidos");
        this.generos.clear();
        this.generos.addAll(generos);
    }

    private static void validarIdentificacion(String titulo, String isbn) {
        if (titulo == null || titulo.isBlank() || isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("Titulo e ISBN obligatorios");
    }

    private static void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() <= 0)
            throw new IllegalArgumentException("Precio debe ser positivo");
    }

    public Long getId() {
        return id;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public FichaLibro getFicha() {
        return ficha;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public Set<Autor> getAutores() {
        return Set.copyOf(autores);
    }

    public Set<Genero> getGeneros() {
        return Set.copyOf(generos);
    }
}
