// ── Navbar scroll effect ─────────────────────────
window.addEventListener('scroll', () => {
    const nav = document.querySelector('.navbar');
    if (nav) nav.classList.toggle('scrolled', window.scrollY > 40);
});

// ── Nav toggle mobile ─────────────────────────────
function toggleNav() {
    const links = document.querySelector('.nav-links');
    if (links) links.classList.toggle('open');
}

// ── Dropdown Locaciones — funciona con clic ───────
document.addEventListener('DOMContentLoaded', () => {

    const dropdowns = document.querySelectorAll('.dropdown');

    function closeAllDropdowns() {
        document.querySelectorAll('.dropdown.open').forEach(d => d.classList.remove('open'));
    }

    dropdowns.forEach((dropdown) => {
        const toggle = dropdown.querySelector('.dropdown-toggle');
        const menu   = dropdown.querySelector('.dropdown-menu');

        if (toggle && menu) {
            // Abrir/cerrar con clic en el toggle
            toggle.addEventListener('click', (e) => {
                e.preventDefault();
                const isOpen = dropdown.classList.contains('open');
                closeAllDropdowns();
                if (!isOpen) dropdown.classList.add('open');
            });
        }
    });

    // Cerrar si se hace clic fuera de cualquier dropdown
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.dropdown')) closeAllDropdowns();
    });

    // ── Animaciones scroll ────────────────────────
    if (!('IntersectionObserver' in window)) {
        document.querySelectorAll('.reveal, .reveal-item').forEach(el => el.classList.add('visible'));
        return;
    }

    // ── Animación de entrada del hero al cargar ──────
    // Pequeño delay para asegurar que el DOM está listo
    requestAnimationFrame(() => {
        setTimeout(() => {
            document.querySelectorAll('.hero-anim, .hero-right-anim').forEach(el => {
                el.classList.add('loaded');
            });
        }, 80);
    });

    // ── Animaciones al hacer scroll (secciones bajo el hero) ──
    const sectionObs = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                sectionObs.unobserve(entry.target);
            }
        });
    }, { threshold: 0.08 });

    document.querySelectorAll('.reveal').forEach(el => sectionObs.observe(el));

    const itemObs = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const siblings = Array.from(entry.target.parentElement.querySelectorAll('.reveal-item'));
                const idx = siblings.indexOf(entry.target);
                setTimeout(() => entry.target.classList.add('visible'), idx * 100);
                itemObs.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.reveal-item').forEach(el => itemObs.observe(el));
});

// ── Formulario contacto ───────────────────────────
function enviarForm(e) {
    e.preventDefault();
    const form = document.querySelector('.contact-form');
    const ok   = document.getElementById('form-ok');
    if (form) form.style.display = 'none';
    if (ok)   ok.style.display   = 'block';
}

// ── Filtro y ordenación del menú ──────────────────
let originalOrder = [];
let currentCat = 'all';

function filterMenu(cat) {
    currentCat = cat;
    const cards = document.querySelectorAll('.menu-product-card');
    let visible = 0;
    cards.forEach(card => {
        const match = cat === 'all' || card.dataset.cat === cat;
        card.classList.toggle('hidden', !match);
        if (match) visible++;
    });
    const empty = document.getElementById('menuEmpty');
    if (empty) empty.style.display = visible === 0 ? 'block' : 'none';
}

function filterByCat(cat) {
    filterMenu(cat);
}

// Usado por el <select> de categoría de cakes en móvil: filtra y
// actualiza también el estado "active" de los botones pill de desktop
function filterMenuAndSyncPills(cat) {
    filterMenu(cat);
    document.querySelectorAll('.filter-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.cat === cat);
    });
    document.querySelectorAll('.menu-cat-select').forEach(sel => { sel.value = cat; });
}

function parsePrice(card) {
    // Extrae el número del precio: "$1.50" → 1.50, "Desde $45" → 45
    const priceEl = card.querySelector('.mpc-price');
    if (!priceEl) return 0;
    const match = priceEl.textContent.replace(',','.').match(/[\d.]+/);
    return match ? parseFloat(match[0]) : 0;
}

function sortMenu(val) {
    const grid = document.getElementById('menuGrid');
    if (!grid) return;

    const cards = Array.from(grid.querySelectorAll('.menu-product-card'));

    if (val === 'recommended') {
        originalOrder.forEach(card => grid.appendChild(card));
    } else {
        cards.sort((a, b) => {
            if (val === 'popular') {
                const pa = a.dataset.popular === 'true' ? 1 : 0;
                const pb = b.dataset.popular === 'true' ? 1 : 0;
                return pb - pa;
            }
            if (val === 'price-high') return parsePrice(b) - parsePrice(a);
            if (val === 'price-low')  return parsePrice(a) - parsePrice(b);
            return 0;
        });
        cards.forEach(c => grid.appendChild(c));
    }
    filterMenu(currentCat);
}

// Inicializar menú
document.addEventListener('DOMContentLoaded', () => {
    const grid = document.getElementById('menuGrid');
    if (grid) {
        // Guardar orden original
        originalOrder = Array.from(grid.querySelectorAll('.menu-product-card'));
    }

    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            filterMenu(btn.dataset.cat);
            document.querySelectorAll('.menu-cat-select').forEach(sel => { sel.value = btn.dataset.cat; });
        });
    });
});

// ── Carrusel del hero ─────────────────────────────
let carouselIndex = 0;
let carouselTimer = null;
const CAROUSEL_INTERVAL = 4000;

function carouselInit() {
    const slides = document.querySelectorAll('.carousel-slide');
    const dotsContainer = document.getElementById('carouselDots');
    if (!slides.length || !dotsContainer) return;

    // Crear dots
    slides.forEach((_, i) => {
        const dot = document.createElement('button');
        dot.className = 'carousel-dot' + (i === 0 ? ' active' : '');
        dot.setAttribute('aria-label', 'Slide ' + (i+1));
        dot.onclick = () => carouselGoTo(i);
        dotsContainer.appendChild(dot);
    });

    carouselStart();
}

function carouselGoTo(idx) {
    const slides = document.querySelectorAll('.carousel-slide');
    const dots   = document.querySelectorAll('.carousel-dot');
    if (!slides.length) return;

    slides[carouselIndex].classList.remove('active');
    if (dots[carouselIndex]) dots[carouselIndex].classList.remove('active');

    carouselIndex = (idx + slides.length) % slides.length;

    slides[carouselIndex].classList.add('active');
    if (dots[carouselIndex]) dots[carouselIndex].classList.add('active');
}

function carouselNext() { clearInterval(carouselTimer); carouselGoTo(carouselIndex + 1); carouselStart(); }
function carouselPrev() { clearInterval(carouselTimer); carouselGoTo(carouselIndex - 1); carouselStart(); }

function carouselStart() {
    carouselTimer = setInterval(() => carouselGoTo(carouselIndex + 1), CAROUSEL_INTERVAL);
}

document.addEventListener('DOMContentLoaded', carouselInit);

// ── Carrusel de origen (provincias de Cuba — nosotros.html) ──
let originIndex = 0;
let originTimer = null;
const ORIGIN_CAROUSEL_INTERVAL = 3500;

function originCarouselInit() {
    const slides = document.querySelectorAll('.origin-slide');
    if (!slides.length) return;

    updateOriginCaption();
    originCarouselStart();
}

function originCarouselGoTo(idx) {
    const slides = document.querySelectorAll('.origin-slide');
    if (!slides.length) return;

    slides[originIndex].classList.remove('active');
    originIndex = (idx + slides.length) % slides.length;
    slides[originIndex].classList.add('active');

    updateOriginCaption();
}

function updateOriginCaption() {
    const slides = document.querySelectorAll('.origin-slide');
    const caption = document.getElementById('originCaption');
    if (!slides.length || !caption) return;
    caption.textContent = slides[originIndex].getAttribute('data-caption') || '';
}

function originCarouselNext() { clearInterval(originTimer); originCarouselGoTo(originIndex + 1); originCarouselStart(); }
function originCarouselPrev() { clearInterval(originTimer); originCarouselGoTo(originIndex - 1); originCarouselStart(); }

function originCarouselStart() {
    originTimer = setInterval(() => originCarouselGoTo(originIndex + 1), ORIGIN_CAROUSEL_INTERVAL);
}

document.addEventListener('DOMContentLoaded', originCarouselInit);

// ── Carrusel del equipo (nosotros.html) ──
let teamIndex = 0;
let teamTimer = null;
const TEAM_CAROUSEL_INTERVAL = 4000;

function teamCarouselInit() {
    const slides = document.querySelectorAll('.team-slide');
    const dotsWrap = document.getElementById('teamDots');
    if (!slides.length || !dotsWrap) return;

    dotsWrap.innerHTML = '';
    slides.forEach((_, i) => {
        const dot = document.createElement('button');
        dot.className = 'team-dot' + (i === 0 ? ' active' : '');
        dot.setAttribute('aria-label', 'Foto ' + (i + 1));
        dot.onclick = () => { clearInterval(teamTimer); teamCarouselGoTo(i); teamCarouselStart(); };
        dotsWrap.appendChild(dot);
    });

    teamCarouselStart();
}

function teamCarouselGoTo(idx) {
    const slides = document.querySelectorAll('.team-slide');
    const dots = document.querySelectorAll('.team-dot');
    if (!slides.length) return;

    slides[teamIndex].classList.remove('active');
    if (dots[teamIndex]) dots[teamIndex].classList.remove('active');

    teamIndex = (idx + slides.length) % slides.length;

    slides[teamIndex].classList.add('active');
    if (dots[teamIndex]) dots[teamIndex].classList.add('active');
}

function teamCarouselNext() { clearInterval(teamTimer); teamCarouselGoTo(teamIndex + 1); teamCarouselStart(); }
function teamCarouselPrev() { clearInterval(teamTimer); teamCarouselGoTo(teamIndex - 1); teamCarouselStart(); }

function teamCarouselStart() {
    teamTimer = setInterval(() => teamCarouselGoTo(teamIndex + 1), TEAM_CAROUSEL_INTERVAL);
}

document.addEventListener('DOMContentLoaded', teamCarouselInit);
