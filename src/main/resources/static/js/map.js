var LANG = document.documentElement.lang === 'en' ? 'en' : 'es';

var I18N = {
    es: {
        locating: 'Localizando…',
        useLocation: 'Usar mi ubicación',
        yourLocation: 'Tu ubicación',
        howToGet: 'Cómo llegar →',
        errDenied: 'Permiso de ubicación denegado.',
        errUnavail: 'No se pudo obtener tu ubicación.',
        errTimeout: 'La solicitud tardó demasiado.',
        errUnknown: 'Error desconocido.',
        hours: [
            'Lun–Sáb: 5:00am–8:30pm · Dom: 6:00am–8:30pm',
            'Lun–Sáb: 5:00am–8:00pm · Dom: 6:00am–7:00pm',
            'Lun–Jue: 6:00am–8:30pm · Vie–Sáb: 6:00am–9:00pm'
        ]
    },
    en: {
        locating: 'Locating…',
        useLocation: 'Use my location',
        yourLocation: 'Your location',
        howToGet: 'Get directions →',
        errDenied: 'Location permission denied.',
        errUnavail: 'Could not get your location.',
        errTimeout: 'The request timed out.',
        errUnknown: 'Unknown error.',
        hours: [
            'Mon–Sat: 5:00am–8:30pm · Sun: 6:00am–8:30pm',
            'Mon–Sat: 5:00am–8:00pm · Sun: 6:00am–7:00pm',
            'Mon–Thu: 6:00am–8:30pm · Fri–Sat: 6:00am–9:00pm'
        ]
    }
};

var T = I18N[LANG];

var LOCATIONS = [
    {
        id: 'union-city',
        name: 'Union City',
        lat: 40.77817815836558,
        lng: -74.02502107415496,
        addr: '4211 Bergenline Ave',
        fullAddr: '4211 Bergenline Ave, Union City, NJ 07087',
        mapsQuery: 'El Fenix Bakery, 4211 Bergenline Ave, Union City, NJ 07087',
        hours: T.hours[0],
        phone: '(201) 864-2699'
    },
    {
        id: 'west-new-york',
        name: 'West New York',
        lat: 40.791367626875264,
        lng: -74.01460996925532,
        addr: '6132 Bergenline Ave',
        fullAddr: '6132 Bergenline Ave, West New York, NJ 07093',
        mapsQuery: 'El Fenix Bakery, 6132 Bergenline Ave, West New York, NJ 07093',
        hours: T.hours[1],
        phone: '(201) 854-2262'
    },
    {
        id: 'north-bergen',
        name: 'North Bergen',
        lat: 40.803757534292856,
        lng: -74.00530350942387,
        addr: '8133 Bergenline Ave',
        fullAddr: '8133 Bergenline Ave, North Bergen, NJ 07047',
        mapsQuery: 'El Fenix Bakery, 8133 Bergenline Ave, North Bergen, NJ 07047',
        hours: T.hours[2],
        phone: '(201) 994-4060'
    }
];

var fenixMap = null;
var userMarker = null;
var locationMarkers = [];
var mapInitialized = false;

function abrirMapaModal() {
    document.getElementById('mapaModal').style.display = 'flex';
    document.body.style.overflow = 'hidden';
    if (!mapInitialized) {
        setTimeout(function() {
            initMap();
            if (fenixMap) fenixMap.invalidateSize();
        }, 150);
    } else {
        setTimeout(function() { fenixMap.invalidateSize(); }, 150);
    }
}

function cerrarMapaModal() {
    document.getElementById('mapaModal').style.display = 'none';
    document.body.style.overflow = '';
}

function cerrarMapaModalOverlay(e) {
    if (e.target === document.getElementById('mapaModal')) cerrarMapaModal();
}

function initMap() {
    if (mapInitialized) return;
    if (typeof L === 'undefined') return;
    mapInitialized = true;

    fenixMap = L.map('fenixMap', { zoomControl: true, scrollWheelZoom: false })
                .setView([40.791, -74.015], 13);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
        attribution: '&copy; OpenStreetMap &copy; CARTO',
        maxZoom: 19
    }).addTo(fenixMap);

    var goldIcon = L.divIcon({
        className: '',
        html: '<div class="fenix-map-marker"></div>',
        iconSize: [32, 32],
        iconAnchor: [16, 32],
        popupAnchor: [0, -34]
    });

    LOCATIONS.forEach(function(loc) {
        var mapsUrl = 'https://www.google.com/maps/dir/?api=1&destination=' + encodeURIComponent(loc.mapsQuery);
        var popupHtml =
            '<div class="fenix-popup">' +
                '<div class="fenix-popup-title">Fenix Bakery — ' + loc.name + '</div>' +
                '<div class="fenix-popup-addr">📍 ' + loc.fullAddr + '</div>' +
                '<div class="fenix-popup-hours">🕐 ' + loc.hours + '</div>' +
                '<div class="fenix-popup-phone">📞 ' + loc.phone + '</div>' +
                '<a class="fenix-popup-directions" href="' + mapsUrl + '" target="_blank" rel="noopener">' + T.howToGet + '</a>' +
            '</div>';
        var marker = L.marker([loc.lat, loc.lng], { icon: goldIcon })
            .addTo(fenixMap)
            .bindPopup(popupHtml, { maxWidth: 260 });
        locationMarkers.push({ loc: loc, marker: marker });
    });
}

function haversine(lat1, lng1, lat2, lng2) {
    var R = 3958.8;
    var dLat = (lat2 - lat1) * Math.PI / 180;
    var dLng = (lng2 - lng1) * Math.PI / 180;
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLng/2) * Math.sin(dLng/2);
    return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function findNearest() {
    var btn = document.getElementById('btnNearest');
    document.getElementById('nearestError').style.display = 'none';
    btn.disabled = true;
    btn.textContent = T.locating;

    if (!navigator.geolocation) {
        showMapError(T.errUnavail);
        resetBtn();
        return;
    }

    navigator.geolocation.getCurrentPosition(
        function(pos) {
            var lat = pos.coords.latitude;
            var lng = pos.coords.longitude;

            if (userMarker) fenixMap.removeLayer(userMarker);
            var userIcon = L.divIcon({
                className: '',
                html: '<div class="fenix-map-user"></div>',
                iconSize: [16, 16],
                iconAnchor: [8, 8]
            });
            userMarker = L.marker([lat, lng], { icon: userIcon })
                .addTo(fenixMap)
                .bindPopup('<div class="fenix-popup">' + T.yourLocation + '</div>')
                .openPopup();

            var sorted = LOCATIONS.map(function(loc) {
                return Object.assign({}, loc, { dist: haversine(lat, lng, loc.lat, loc.lng) });
            }).sort(function(a, b) { return a.dist - b.dist; });

            var nearest = sorted[0];

            locationMarkers.forEach(function(item) {
                var el = item.marker.getElement();
                if (el) {
                    var dot = el.querySelector('.fenix-map-marker');
                    if (dot) dot.classList.toggle('fenix-map-marker-nearest', item.loc.id === nearest.id);
                }
            });

            var nearestMarker = locationMarkers.find(function(m) { return m.loc.id === nearest.id; });
            if (nearestMarker) nearestMarker.marker.openPopup();

            var bounds = L.latLngBounds([[lat, lng], [nearest.lat, nearest.lng]]).pad(0.3);
            fenixMap.fitBounds(bounds);

            document.getElementById('nearestName').textContent = nearest.name;
            document.getElementById('nearestDist').textContent = nearest.dist < 1
                ? (Math.round(nearest.dist * 5280)) + ' ft'
                : (nearest.dist.toFixed(1)) + ' mi';
            document.getElementById('nearestResult').style.display = 'flex';

            document.querySelectorAll('.contact-loc-compact').forEach(function(el) {
                el.classList.remove('loc-nearest');
            });
            var card = document.getElementById('loc-' + nearest.id);
            if (card) card.classList.add('loc-nearest');

            resetBtn();
        },
        function(err) {
            var msgs = { 1: T.errDenied, 2: T.errUnavail, 3: T.errTimeout };
            showMapError(msgs[err.code] || T.errUnknown);
            resetBtn();
        },
        { timeout: 10000 }
    );
}

function showMapError(msg) {
    document.getElementById('nearestErrorMsg').textContent = msg;
    document.getElementById('nearestError').style.display = 'block';
}

function resetBtn() {
    var btn = document.getElementById('btnNearest');
    btn.disabled = false;
    btn.textContent = T.useLocation;
}

function loadLeafletAndInit() {
    var link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = '/css/leaflet.css';
    document.head.appendChild(link);

    var script = document.createElement('script');
    script.src = '/js/leaflet.js';
    document.body.appendChild(script);
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', loadLeafletAndInit);
} else {
    loadLeafletAndInit();
}

document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') cerrarMapaModal();
});
