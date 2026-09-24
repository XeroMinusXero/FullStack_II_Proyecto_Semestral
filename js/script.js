const ES_INDEX = !location.pathname.includes('/pages/');

function getParam(nombre) {
    return new URLSearchParams(location.search).get(nombre);
}

function paginaActual() {
    const path = location.pathname.split('/').pop() || 'index.html';
    return path.replace(/\.html?$/i, '').toLowerCase();
}

function urlPagina(nombre, params = {}) {
    const nombreNorm = String(nombre).replace(/\.html?$/i, '').toLowerCase();
    let base;
    if (nombreNorm === 'index') {
        base = ES_INDEX ? 'index.html' : '../index.html';
    } else {
        base = ES_INDEX ? 'pages/' + nombreNorm + '.html' : nombreNorm + '.html';
    }
    const qs = new URLSearchParams(params).toString();
    return qs ? base + '?' + qs : base;
}

function cerrarSesion(e) {
    if (e) e.preventDefault();
    location.href = ES_INDEX ? 'index.html' : '../index.html';
}

const V = {
    requerido: v => v.trim().length > 0,
    email: v => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v),
    telefono: v => /^[+]?[\d\s\-()]{8,15}$/.test(v),
    soloNumeros: v => /^\d+$/.test(v),
    min: (v, n) => v.trim().length >= n,
    max: (v, n) => v.trim().length <= n,
    numeroPositivo: v => !isNaN(v) && Number(v) > 0,
    numeroNoNegativo: v => !isNaN(v) && Number(v) >= 0,
    fechaValida: v => !isNaN(Date.parse(v)),
    isbn: v => /^[\d\-]{10,17}$/.test(v),
    passwordFuerte: v => v.length >= 8 && /[!@#$%^&*(),.?":{}|<>]/.test(v),
    soloLetras: v => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ\s'\-]+$/.test(v),
    textoLibre: v => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ0-9\s.,;:¡!¿?'"()\-]+$/.test(v),
    sinNumeros: v => !/\d/.test(v),
    direccion: v => /^[a-záéíóúñüA-ZÁÉÍÓÚÑÜ0-9\s.,#'\-/]+$/.test(v)
};

function marcarError(input, mensaje) {
    const campo = input.closest('.campo'); if (!campo) return;
    campo.classList.add('error');
    const msg = campo.querySelector('.mensaje-error');
    if (msg) msg.textContent = mensaje;
}
function limpiarError(input) {
    const campo = input.closest('.campo'); if (campo) campo.classList.remove('error');
}
function limpiarFormulario(form) {
    form.querySelectorAll('.campo').forEach(c => c.classList.remove('error'));
}

function validarCampo(input) {
    const regla = input.dataset.validar; if (!regla) return true;
    const v = input.value; const opcional = input.dataset.opcional === 'true';
    limpiarError(input);
    if (opcional && v.trim() === '') return true;

    if (regla.includes('requerido') && !V.requerido(v)) { marcarError(input, 'Este campo es obligatorio'); return false; }
    if (regla.includes('email') && !V.email(v)) { marcarError(input, 'Ingrese un correo válido'); return false; }
    if (regla.includes('telefono') && !V.telefono(v)) { marcarError(input, 'Ingrese un teléfono válido'); return false; }
    if (regla.includes('passwordFuerte') && !V.passwordFuerte(v)) { marcarError(input, 'Mínimo 8 caracteres y un carácter especial'); return false; }
    if (regla.includes('isbn') && !V.isbn(v)) { marcarError(input, 'ISBN inválido'); return false; }
    if (regla.includes('numeroPositivo') && !V.numeroPositivo(v)) { marcarError(input, 'Debe ser un número mayor a 0'); return false; }
    if (regla.includes('numeroNoNegativo') && !V.numeroNoNegativo(v)) { marcarError(input, 'No puede ser negativo'); return false; }
    if (regla.includes('soloLetras') && !V.soloLetras(v)) { marcarError(input, 'Solo se permiten letras, espacios y guiones'); return false; }
    if (regla.includes('sinNumeros') && !V.sinNumeros(v)) { marcarError(input, 'No se permiten números'); return false; }
    if (regla.includes('textoLibre') && !V.textoLibre(v)) { marcarError(input, 'Contiene caracteres no permitidos'); return false; }
    if (regla.includes('direccion') && !V.direccion(v)) { marcarError(input, 'Dirección inválida'); return false; }
    if (regla.includes('soloNumeros') && !V.soloNumeros(v)) { marcarError(input, 'Solo números'); return false; }

    const min = input.dataset.min;
    if (min && !V.min(v, Number(min))) { marcarError(input, `Mínimo ${min} caracteres`); return false; }
    const max = input.dataset.max;
    if (max && !V.max(v, Number(max))) { marcarError(input, `Máximo ${max} caracteres`); return false; }
    return true;
}

const PAGINAS_ADMIN = ['admin', 'admincatalogo', 'adminpedidos', 'admindashboard'];
const PAGINAS_CLIENTE = ['miperfil', 'mispedidos', 'misdirecciones'];
const PAGINAS_CHECKOUT = ['checkoutdireccion', 'checkoutenvio', 'checkoutpago', 'checkoutresumen'];

function aplicarGuards() {
    const rol = getParam('rol');
    const pagina = paginaActual();

    if (PAGINAS_ADMIN.includes(pagina) && rol !== 'admin') {
        location.href = urlPagina('login', { redir: pagina, motivo: 'admin' });
        return false;
    }
    if (PAGINAS_CLIENTE.includes(pagina) && rol !== 'cliente') {
        location.href = urlPagina('login', { redir: pagina });
        return false;
    }
    if (PAGINAS_CHECKOUT.includes(pagina) && rol !== 'cliente') {
        location.href = urlPagina('login', { redir: pagina });
        return false;
    }
    return true;
}

function configurarNavegacion() {
    const nav = document.querySelector('header nav');
    if (!nav) return;

    const rol = getParam('rol');

    nav.querySelectorAll('a').forEach(a => {
        const href = a.getAttribute('href') || '';
        if (href.includes('login.html') || href.includes('registro.html') ||
            href.includes('admin.html') || href.includes('miPerfil.html') ||
            a.classList.contains('nav-sesion')) {
            a.remove();
        }
    });

    const bloque = document.createElement('span');
    bloque.className = 'nav-sesion';
    bloque.style.cssText = 'display:flex;gap:6px;align-items:center;';

    if (!rol) {
        bloque.innerHTML = `
        <a href="${urlPagina('login')}">Ingresar</a>
        <a href="${urlPagina('registro')}">Registrarse</a>
    `;
    } else if (rol === 'admin') {
        bloque.innerHTML = `
        <a href="${urlPagina('admin', { rol: 'admin' })}" style="color:var(--dorado);font-weight:700;">⚙ Panel Admin</a>
        <a href="#" onclick="cerrarSesion(event)">Salir</a>
    `;
    } else {
        bloque.innerHTML = `
        <a href="${urlPagina('miPerfil', { rol: 'cliente' })}">👤 Mi Perfil</a>
        <a href="#" onclick="cerrarSesion(event)">Salir</a>
    `;
    }
    nav.appendChild(bloque);

    if (rol) {
        document.querySelectorAll('a[href]').forEach(a => {
            const href = a.getAttribute('href');
            if (!href) return;
            if (href.startsWith('#') || href.startsWith('http') ||
                href.startsWith('mailto:') || href.startsWith('javascript:')) return;
            if (!href.includes('.html')) return;
            if (href.includes('login.html') || href.includes('registro.html')) return;

            const [path, query] = href.split('?');
            const params = new URLSearchParams(query || '');
            if (!params.has('rol')) params.set('rol', rol);
            const qs = params.toString();
            a.setAttribute('href', qs ? path + '?' + qs : path);
        });
    }
}

function configurarAcciones() {

    document.addEventListener('click', (e) => {
        const el = e.target.closest('[data-agregar]');
        if (!el) return;
        e.preventDefault();

        const rol = getParam('rol');
        if (rol === 'admin') {
            alert('El administrador no puede realizar compras.');
            return;
        }

        const textoOriginal = el.textContent;
        el.textContent = '✓ Agregado al carrito';
        el.disabled = true;
        setTimeout(() => {
            el.textContent = textoOriginal;
            el.disabled = false;
        }, 1200);
    });

    document.addEventListener('click', (e) => {
        const el = e.target.closest('[data-protegido]');
        if (!el) return;
        e.preventDefault();

        const rol = getParam('rol');
        const destino = el.dataset.destino;

        if (!rol) {
            const params = {
                redir: paginaActual(),
                accion: el.dataset.accion || 'comprar'
            };
            if (destino) params.destino = destino;
            location.href = urlPagina('login', params);
            return;
        }

        if (rol === 'admin') {
            alert('El administrador no puede realizar compras ni agregar a favoritos.');
            return;
        }

        if (destino) {
            location.href = urlPagina(destino, { rol: 'cliente' });
            return;
        }

        const textoOriginal = el.textContent;
        el.textContent = '✓ Listo';
        el.disabled = true;
        setTimeout(() => {
            el.textContent = textoOriginal;
            el.disabled = false;
        }, 800);
    });
}

document.addEventListener('DOMContentLoaded', () => {

    if (!aplicarGuards()) return;

    configurarNavegacion();
    configurarAcciones();

    const pasosSiguiente = [
        { desde: 'checkoutenvio', selector: 'a[href^="checkoutPago.html"]' },
        { desde: 'checkoutpago', selector: 'a[href^="checkoutResumen.html"]' },
        { desde: 'checkoutresumen', selector: 'a[href^="confirmacion.html"]' }
    ];
    const pasoActual = paginaActual();
    const regla = pasosSiguiente.find(p => p.desde === pasoActual);
    if (regla) {
        const link = document.querySelector(regla.selector);
        if (link) {
            const qs = location.search.startsWith('?') ? location.search.slice(1) : location.search;
            const base = link.getAttribute('href').split('?')[0];
            link.setAttribute('href', qs ? base + '?' + qs : base);
        }
    }

    document.querySelectorAll('[data-validar]').forEach(input => {
        input.addEventListener('blur', () => validarCampo(input));
        input.addEventListener('input', () => {
            const campo = input.closest('.campo');
            if (campo && campo.classList.contains('error')) validarCampo(input);
        });
    });

    document.querySelectorAll('form[data-formulario]').forEach(form => {
        form.addEventListener('submit', e => {
            e.preventDefault();
            limpiarFormulario(form);
            let ok = true;
            form.querySelectorAll('[data-validar]').forEach(input => { if (!validarCampo(input)) ok = false; });

            const tipo = form.dataset.formulario;

            if (tipo === 'login') {
                if (!ok) return;
                const email = form.querySelector('#email').value.trim().toLowerCase();
                const redir = getParam('redir') || 'index';
                const accion = getParam('accion');
                const destino = getParam('destino');

                if (email === 'admin@gmail.com') {
                    location.href = urlPagina('admin', { rol: 'admin' });
                    return;
                }

                const params = { rol: 'cliente' };
                if (accion) params.accion = accion;
                const paginaDestino = destino || redir;
                location.href = urlPagina(paginaDestino, params);
                return;
            }

            if (tipo === 'registro') {
                const p1 = form.querySelector('#password');
                const p2 = form.querySelector('#password2');
                if (p1.value !== p2.value) { marcarError(p2, 'Las contraseñas no coinciden'); ok = false; }
                if (!form.querySelector('#terminos').checked) { alert('Debe aceptar los términos y condiciones'); ok = false; }
                if (!ok) return;
                location.href = urlPagina('index', { rol: 'cliente' });
                return;
            }

            if (tipo === 'checkout-direccion') {
                const radioNueva = form.querySelector('input[name="dir"][value="nueva"]');
                const usaNueva = radioNueva && radioNueva.checked;

                if (usaNueva) {
                    ['calle', 'numero', 'comuna', 'region'].forEach(id => {
                        const input = form.querySelector('#' + id);
                        if (!input) return;
                        const opcionalPrevio = input.dataset.opcional;
                        input.dataset.opcional = 'false';
                        if (!validarCampo(input)) ok = false;
                        if (opcionalPrevio !== undefined) input.dataset.opcional = opcionalPrevio;
                    });

                    if (!ok) {
                        const pe = form.querySelector('.campo.error input');
                        if (pe) pe.focus();
                        return;
                    }
                } else {
                    const radioSel = form.querySelector('input[name="dir"]:checked');
                    if (!radioSel) {
                        alert('Debe seleccionar una dirección o usar una nueva.');
                        return;
                    }
                }

                let direccionFinal = '';
                if (usaNueva) {
                    const calle = form.querySelector('#calle').value.trim();
                    const numero = form.querySelector('#numero').value.trim();
                    const comuna = form.querySelector('#comuna').value.trim();
                    const region = form.querySelector('#region').value.trim();
                    direccionFinal = `${calle} ${numero}, ${comuna}, ${region}`.replace(/\s+/g, ' ').trim();
                } else {
                    const sel = form.querySelector('input[name="dir"]:checked');
                    direccionFinal = sel.closest('label').querySelector('span').textContent.trim();
                }

                const params = { rol: 'cliente', direccion: direccionFinal };
                location.href = urlPagina('checkoutEnvio', params);
                return;
            }

            if (tipo === 'perfil' && ok) { mostrarMensaje(form, 'Datos guardados correctamente'); return; }
            if (tipo === 'direccion' && ok) { mostrarMensaje(form, 'Dirección guardada correctamente'); return; }
            if (tipo === 'admin-libro' && ok) { mostrarMensaje(form, 'Libro registrado correctamente'); return; }
            if (tipo === 'cupon' && ok) { mostrarMensaje(form, 'Cupón registrado correctamente'); return; }
            if (tipo === 'boletin' && ok) { mostrarMensaje(form, 'Boletín guardado como borrador'); return; }

            if (!ok) {
                const pe = form.querySelector('.campo.error input,.campo.error select,.campo.error textarea');
                if (pe) pe.focus();
            }
        });
    });

    document.querySelectorAll('.control-cantidad').forEach(ctrl => {
        const input = ctrl.querySelector('input');
        const menos = ctrl.querySelector('.menos');
        const mas = ctrl.querySelector('.mas');
        if (menos) menos.addEventListener('click', () => {
            let v = Number(input.value) - 1; if (v < 0) v = 0; input.value = v;
        });
        if (mas) mas.addEventListener('click', () => { input.value = Number(input.value) + 1; });
    });

    document.querySelectorAll('.tabs').forEach(tabs => {
        tabs.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', () => {
                tabs.querySelectorAll('button').forEach(b => b.classList.remove('activo'));
                btn.classList.add('activo');
            });
        });
    });

    document.querySelectorAll('.paginacion').forEach(pag => {
        pag.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', () => {
                pag.querySelectorAll('button').forEach(b => b.classList.remove('activo'));
                btn.classList.add('activo');
            });
        });
    });
});

function mostrarMensaje(form, mensaje) {
    let box = form.querySelector('.mensaje-exito-dinamico');
    if (!box) {
        box = document.createElement('div');
        box.className = 'mensaje-exito mensaje-exito-dinamico';
        form.prepend(box);
    }
    box.textContent = mensaje;
    setTimeout(() => box.remove(), 3500);
}