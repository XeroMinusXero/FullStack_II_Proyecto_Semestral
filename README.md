<div align="center">

# The Quiet Chapter

### Librería online — Prototipo visual interactivo

**FullStack II · Proyecto Semestral**

_Una experiencia de librería inspirada en el silencio, la lectura y el placer de descubrir una nueva historia._

</div>

---

## Sobre el proyecto

**The Quiet Chapter** es el prototipo de una librería online desarrollado para el proyecto semestral de FullStack II.

La propuesta busca representar una experiencia de compra de libros de principio a fin, manteniendo una identidad visual editorial basada en tonos café oscuro, negro, crema y detalles cálidos.

El prototipo contempla tanto la experiencia pública de navegación como los principales recorridos asociados a una cuenta de cliente y a la administración de la librería.

---

## Experiencia principal

El recorrido del usuario está planteado alrededor de cuatro momentos:

```text
Descubrir
   ↓
Explorar el catálogo
   ↓
Conocer un libro
   ↓
Agregar al carrito
   ↓
Iniciar compra
   ↓
Dirección → Envío → Pago → Resumen
   ↓
Confirmación
```

También se contemplan experiencias complementarias como:

- Catálogo y búsqueda de libros.
- Filtros y ordenamiento.
- Detalle de un libro.
- Ofertas.
- Favoritos.
- Carrito de compra.
- Registro e inicio de sesión.
- Perfil del cliente.
- Direcciones guardadas.
- Historial de pedidos.
- Flujo de checkout.
- Panel administrativo.
- Gestión de catálogo e inventario.
- Administración de pedidos.
- Reportes y analítica.

---

## Identidad visual

La interfaz utiliza una estética de librería contemporánea, evitando una apariencia excesivamente tecnológica.

| Elemento | Dirección visual |
|---|---|
| **Paleta** | Café oscuro, negro carbón, crema, beige y acentos dorados/cobre |
| **Tipografía** | Serif editorial para títulos + sans-serif para información y controles |
| **Componentes** | Tarjetas, botones redondeados, formularios, indicadores y paneles |
| **Sensación** | Cálida, sobria, editorial y orientada a la lectura |
| **Responsive** | Adaptación para escritorio y dispositivos móviles |

---

## Flujos destacados

### Visitante → Cliente

El visitante puede recorrer la librería y explorar su catálogo sin necesidad de iniciar sesión.

Cuando una acción requiere una cuenta, el flujo conduce al usuario hacia el inicio de sesión o registro antes de continuar con la experiencia correspondiente.

### Acceso administrativo

El área administrativa no forma parte de la navegación pública principal. El acceso se produce mediante el flujo de autenticación y el rol correspondiente.

### Checkout

El proceso de compra está dividido visualmente en etapas para que el usuario pueda reconocer en qué punto del proceso se encuentra:

**Dirección → Envío → Pago → Confirmación**

Los datos necesarios para continuar entre determinadas etapas se trasladan mediante parámetros de navegación, permitiendo representar el recorrido completo dentro del prototipo.

---

## Estructura del proyecto

```text
FullStack_II_Proyecto_Semestral/
│
├── Documentos/
│   └── Plantilla_Gestion_Requerimientos_Scrum.xlsx
│
├── The_Quiet_Chapter/
│   ├── Index.html
│   ├── css/
│   │   └── styles.css
│   ├── js/
│   │   └── script.js
│   ├── img/
│   │   └── Portadas de libros
│   └── pages/
│       ├── Catálogo
│       ├── Detalle de libro
│       ├── Carrito
│       ├── Checkout
│       ├── Cuenta de cliente
│       └── Administración
│
└── README.md
```

---

## Alcance actual

Esta versión corresponde al **prototipo visual e interactivo** del proyecto.

Las validaciones de campos de texto forman parte de la interacción funcional disponible en esta etapa. El resto de los recorridos busca representar visualmente cómo funcionaría la experiencia final de la librería.

La intención de esta versión es validar principalmente:

- La arquitectura visual de las pantallas.
- La navegación entre las distintas áreas.
- La experiencia del usuario.
- La coherencia del flujo de compra.
- La adaptación a distintos tamaños de pantalla.
- La presentación de las funcionalidades definidas para el proyecto.

---

## Áreas del prototipo

| Área | Estado en el prototipo |
|---|---|
| Inicio | Disponible |
| Catálogo | Disponible |
| Detalle de libro | Disponible |
| Ofertas | Disponible |
| Favoritos | Disponible |
| Carrito | Disponible |
| Registro | Disponible |
| Inicio de sesión | Disponible |
| Perfil | Disponible |
| Direcciones | Disponible |
| Pedidos | Disponible |
| Checkout | Disponible |
| Confirmación | Disponible |
| Administración | Disponible |
| Reportes | Disponible |

---

## Nombre

> **The Quiet Chapter**
>
> Porque cada libro guarda un capítulo que todavía no conocemos.

<div align="center">

**FullStack II · 2026**

</div>
