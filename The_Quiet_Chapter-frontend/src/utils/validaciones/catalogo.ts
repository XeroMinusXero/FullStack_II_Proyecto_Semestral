import { LIMITES, MENSAJES, reglas, type Errores, soloErrores } from './reglas'

/** Formulario de libro tal como lo escribe el administrador (todo texto, antes de convertir). */
export interface FormularioLibroDatos {
  isbn: string; titulo: string; autores: string; editorial: string; categoria: string; generos: string
  sinopsis: string; formato: string; paginas: string; idioma: string; anioPublicacion: string
  encuadernacion: string; portadaUrl: string; extractoUrl: string; precio: string; stockInicial: string
}

/** Separa "Autor A, Autor B" en una lista limpia. */
export const lista = (v: string) => v.split(',').map((x) => x.trim()).filter(Boolean)

/** HU-046/047 (adminCatalogo.html). `esNuevo` exige stock inicial, que solo existe al registrar. */
export function validarLibro(f: FormularioLibroDatos, esNuevo: boolean): Errores<FormularioLibroDatos> {
  const anio = Number(f.anioPublicacion)
  return soloErrores({
    isbn: reglas.isbn(f.isbn) ? undefined : 'ISBN de 10 a 17 caracteres (dígitos, guiones o X)',
    titulo: !reglas.requerido(f.titulo) ? MENSAJES.requerido : !reglas.maximo(f.titulo, 255) ? MENSAJES.maximo(255) : undefined,
    autores: lista(f.autores).length ? undefined : 'Indica al menos un autor',
    editorial: reglas.requerido(f.editorial) ? undefined : MENSAJES.requerido,
    categoria: reglas.requerido(f.categoria) ? undefined : MENSAJES.requerido,
    precio: Number(f.precio) > 0 ? undefined : 'El precio debe ser mayor a 0',
    stockInicial: !esNuevo || reglas.entero(f.stockInicial) ? undefined : 'Stock entero mayor o igual a 0',
    paginas: !f.paginas || reglas.entero(f.paginas) ? undefined : 'Número entero mayor o igual a 0',
    anioPublicacion: !f.anioPublicacion || (Number.isInteger(anio) && anio >= 1000 && anio <= new Date().getFullYear() + 1)
      ? undefined : 'Año inválido',
  })
}

/** HU-048: precio en CLP, IVA incluido. */
export function validarPrecio(valor: string): string | undefined {
  return Number(valor) > 0 ? undefined : 'El precio debe ser mayor a 0'
}

/** HU-050: ingreso de unidades (entero positivo). */
export function validarIngresoStock(cantidad: string): string | undefined {
  return reglas.entero(cantidad, 1) ? undefined : 'Ingresa una cantidad entera mayor a 0'
}

export interface DatosAjusteStock { nuevoStock: string; motivo: string }
/** HU-051: ajuste manual con motivo obligatorio. */
export function validarAjusteStock(f: DatosAjusteStock): Errores<DatosAjusteStock> {
  return soloErrores({
    nuevoStock: reglas.entero(f.nuevoStock) ? undefined : 'Stock entero mayor o igual a 0',
    motivo: f.motivo.trim().length < LIMITES.motivoAjusteMin ? MENSAJES.minimo(LIMITES.motivoAjusteMin)
      : !reglas.maximo(f.motivo, LIMITES.motivoAjusteMax) ? MENSAJES.maximo(LIMITES.motivoAjusteMax) : undefined,
  })
}

/** HU-005: rango de precio del catalogo. Devuelve el mensaje de error o undefined. */
export function validarRangoPrecio(minimo: string, maximo: string): string | undefined {
  if ((minimo && !reglas.numero(minimo)) || (maximo && !reglas.numero(maximo))) return 'Ingresa montos mayores o iguales a 0'
  if (minimo && maximo && Number(minimo) > Number(maximo)) return 'El precio mínimo no puede ser superior al máximo'
}

/** HU-009: biografia de autor. */
export function validarBiografia(texto: string): string | undefined {
  return reglas.maximo(texto, LIMITES.biografia) ? undefined : MENSAJES.maximo(LIMITES.biografia)
}
