// Client-side map tile cache for the world map, whichever source Site.REMOTE_MAP_TILES picks.
//
// Neither source lets the browser keep tiles for long: raw.githubusercontent.com only ever sends
// `Cache-Control: max-age=300`, and the site's own web server caches tiles server-side only, so
// every pan back over a tile is another download - and mobile browsers evict their HTTP cache far
// more eagerly than that on top. The tile set practically never changes, so this worker keeps its
// own copy of every tile in the Cache API and answers from it first, only going back to the
// network for tiles it hasn't seen, or quietly in the background once a copy is older than
// REFRESH_MS. After regenerating tiles, bump CACHE_NAME to push them out sooner.
//
// Registered by worldmap.js with the absolute tile base as `?base=` - only requests under it are
// touched, everything else on the site passes straight through. Lives at the site root (not `js/`)
// because a worker can only intercept pages at or below its own path.
//
// Needs `crossOrigin = 'anonymous'` on the tile <img>s (see renderTiles): the remote host allows
// any origin, and a CORS response is readable, whereas a plain no-cors one is opaque - Chrome pads
// every opaque entry to several MiB of storage quota, so a few hundred tiles would exhaust it.
// (Same-origin local tiles are readable either way.)

var CACHE_PREFIX = 'void-map-tiles-';
// Bump to throw away every cached tile, e.g. after the tile set is regenerated with a new layout.
var CACHE_NAME = CACHE_PREFIX + 'v1';
// A cached tile older than this is still served instantly, but re-fetched behind the scenes so a
// regenerated tile set eventually reaches returning visitors without them clearing anything.
var REFRESH_MS = 7 * 24 * 60 * 60 * 1000;
// Upper bound on stored tiles (a few KiB to a few tens of KiB each), oldest written evicted first,
// so wandering the whole map at every zoom can't grow storage without limit on a phone.
var MAX_ENTRIES = 4000;
var TRIM_DELAY_MS = 5000;
var CACHED_AT_HEADER = 'x-void-cached-at';

var TILE_BASE = new URL(self.location.href).searchParams.get('base') || '';

self.addEventListener('install', function () {
  self.skipWaiting();
});

self.addEventListener('activate', function (event) {
  event.waitUntil(
    caches.keys()
      .then(function (names) {
        return Promise.all(names
          .filter(function (name) { return name.indexOf(CACHE_PREFIX) === 0 && name !== CACHE_NAME; })
          .map(function (name) { return caches.delete(name); }));
      })
      // Take over the map page that registered us, so its very first visit is cached too rather
      // than only the one after.
      .then(function () { return self.clients.claim(); })
  );
});

self.addEventListener('fetch', function (event) {
  var request = event.request;
  if (!TILE_BASE || request.method !== 'GET' || request.url.indexOf(TILE_BASE + '/') !== 0) {
    return;
  }
  event.respondWith(caches.open(CACHE_NAME).then(function (cache) {
    return cache.match(request).then(function (cached) {
      if (!cached) {
        return fetchAndStore(cache, request);
      }
      var cachedAt = Number(cached.headers.get(CACHED_AT_HEADER)) || 0;
      if (Date.now() - cachedAt > REFRESH_MS) {
        event.waitUntil(fetchAndStore(cache, request).catch(function () {}));
      }
      return cached;
    });
  }));
});

// Fetches `request` and, if the answer is worth keeping, stores a copy stamped with when it was
// fetched. 404s are kept as well as 200s: most of the pyramid is open ocean the tile set simply has
// no image for, and without caching those every pan over the sea would re-ask for all of them.
// Anything else (5xx, rate limiting) and opaque responses go back to the page as-is, uncached.
function fetchAndStore(cache, request) {
  return fetch(request).then(function (response) {
    if (response.type === 'opaque' || (!response.ok && response.status !== 404)) {
      return response;
    }
    return response.blob().then(function (body) {
      var headers = new Headers(response.headers);
      headers.set(CACHED_AT_HEADER, String(Date.now()));
      var init = { status: response.status, statusText: response.statusText, headers: headers };
      return cache.put(request, new Response(body, init)).then(function () {
        scheduleTrim(cache);
        return new Response(body, init);
      }, function () {
        // Quota exceeded or storage unavailable - still hand the tile over, just not remembered.
        return new Response(body, init);
      });
    });
  });
}

var trimTimer = null;

// Batched rather than run per tile: a single pan can bring in dozens at once. `cache.keys()` comes
// back in insertion order and a refreshed tile is re-inserted at the end, so the front of the
// list is always the longest since last fetched.
function scheduleTrim(cache) {
  if (trimTimer !== null) {
    return;
  }
  trimTimer = setTimeout(function () {
    trimTimer = null;
    cache.keys().then(function (keys) {
      var excess = keys.length - MAX_ENTRIES;
      for (var i = 0; i < excess; i++) {
        cache.delete(keys[i]);
      }
    });
  }, TRIM_DELAY_MS);
}
