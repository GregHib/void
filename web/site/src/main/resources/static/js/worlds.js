// Live state for the world list (navbar dropdown + full worlds page) and the visitor's own saved
// custom servers. `worlds.json` (see WorldTable.kt's loadWorlds()) only holds each world's static
// description; everything else comes straight from each world's own web server (see
// WorldsRoutes.kt): `/api/v1/info` for what changes while it runs — status, players, capacity,
// xp/drop rates, uptime — polled slowly, and `/api/v1/ping`, an empty response polled often and
// timed here in the browser, so reachability stays fresh and the latency is the visitor's own.
// World results land in the `worlds` Alpine store, which WorldMenu.kt/WorldTable.kt bind to.
var VOID_CUSTOM_WORLDS_KEY = 'void-custom-worlds';
var VOID_INFO_REFRESH_MS = 60000;
var VOID_PING_REFRESH_MS = 10000;
var VOID_REQUEST_TIMEOUT_MS = 5000;

var VOID_WORLD_UNKNOWN = {
  status: 'Checking',
  ping: null,
  players: null,
  capacity: null,
  xpRate: null,
  dropRate: null,
  uptimeSeconds: null,
};

// Per world number: `undefined` until the first answer, `null` once a request has failed.
var voidWorldList = [];
var voidWorldInfo = {};
var voidWorldPing = {};

document.addEventListener('alpine:init', function () {
  Alpine.store('worlds', {
    live: {},
    updatedAt: null,
    get: function (number) {
      return this.live[number] || VOID_WORLD_UNKNOWN;
    },
    totalPlayers: function () {
      var live = this.live;
      return Object.keys(live).reduce(function (sum, key) { return sum + (live[key].players || 0); }, 0);
    },
    up: function () {
      var live = this.live;
      return Object.keys(live).filter(function (key) { return live[key].status !== 'Offline'; }).length;
    },
  });
});

function voidFormatNumber(value) {
  return value == null ? '—' : Number(value).toLocaleString('en-GB');
}

function voidFormatRate(rate) {
  return rate == null ? '—' : rate + '×';
}

function voidFormatUptime(seconds) {
  if (seconds == null) {
    return '—';
  }
  var days = Math.floor(seconds / 86400);
  var hours = Math.floor((seconds % 86400) / 3600);
  var minutes = Math.floor((seconds % 3600) / 60);
  var pad = function (n) { return n < 10 ? '0' + n : String(n); };
  return days > 0 ? days + 'd ' + pad(hours) + 'h' : hours + 'h ' + pad(minutes) + 'm';
}

window.voidFormatNumber = voidFormatNumber;
window.voidFormatRate = voidFormatRate;
window.voidFormatUptime = voidFormatUptime;

// A web server's base URL without trailing slashes, assuming http:// when no scheme was given;
// null when it isn't a usable URL.
function voidWebBase(address) {
  var value = String(address || '').trim().replace(/\/+$/, '');
  if (!value) {
    return null;
  }
  if (!/^https?:\/\//i.test(value)) {
    value = 'http://' + value;
  }
  try {
    new URL(value);
  } catch (e) {
    return null;
  }
  return value;
}

function voidFetchWithTimeout(url) {
  var controller = typeof AbortController === 'function' ? new AbortController() : null;
  var timer = controller ? setTimeout(function () { controller.abort(); }, VOID_REQUEST_TIMEOUT_MS) : null;
  return fetch(url, { cache: 'no-store', signal: controller ? controller.signal : undefined })
    .finally(function () { clearTimeout(timer); });
}

// Resolves to the info object, or null if the world didn't answer.
function voidFetchInfo(web) {
  var base = voidWebBase(web);
  if (!base) {
    return Promise.resolve(null);
  }
  return voidFetchWithTimeout(base + '/api/v1/info')
    .then(function (res) { return res.ok ? res.json() : null; })
    .catch(function () { return null; });
}

// Resolves to the round trip in ms, or null if the world didn't answer.
function voidPing(web) {
  var base = voidWebBase(web);
  if (!base) {
    return Promise.resolve(null);
  }
  var start = performance.now();
  return voidFetchWithTimeout(base + '/api/v1/ping')
    .then(function (res) { return res.ok ? Math.round(performance.now() - start) : null; })
    .catch(function () { return null; });
}

// The latest ping is the freshest word on reachability, so a failed one means Offline even if
// the last info said otherwise; before any ping has come back, the info request stands in for it.
function voidResolveWorld(info, ping) {
  var reachable = ping !== undefined ? ping !== null : info !== undefined ? info !== null : undefined;
  var live = Object.assign({}, VOID_WORLD_UNKNOWN, info || {});
  live.status = reachable === undefined ? 'Checking' : !reachable ? 'Offline' : info ? info.status : 'Online';
  live.ping = ping == null ? null : ping;
  return live;
}

function voidPublishWorlds() {
  var live = {};
  voidWorldList.forEach(function (world) {
    live[world.number] = voidResolveWorld(voidWorldInfo[world.number], voidWorldPing[world.number]);
  });
  Alpine.store('worlds').live = live;
}

function voidRefreshInfo() {
  return Promise.all(voidWorldList.map(function (world) {
    return voidFetchInfo(world.web).then(function (info) { voidWorldInfo[world.number] = info; });
  })).then(function () {
    voidPublishWorlds();
    Alpine.store('worlds').updatedAt = new Date();
  });
}

function voidRefreshPings() {
  var worlds = Promise.all(voidWorldList.map(function (world) {
    return voidPing(world.web).then(function (ms) { voidWorldPing[world.number] = ms; });
  })).then(voidPublishWorlds);
  return Promise.all([worlds, voidPingCustomWorlds()]);
}

// `worlds.json`, fetched once however many callers ask — the world list on page start-up, and
// voidWorldWeb, which the world map can reach first (it boots before `alpine:initialized`).
// Settles to an empty list if the file can't be loaded.
var voidWorldsLoaded = null;

// Resolved against this script's own URL (it lives at `<site root>/js/worlds.js`) rather than
// the domain root, since GitHub Pages project sites are served from a subpath
// (`<user>.github.io/<repo>/`) and pages sit at different depths below it.
var VOID_WORLDS_URL = new URL('../worlds.json', (document.currentScript && document.currentScript.src) || document.baseURI).href;

function voidLoadWorldList() {
  if (!voidWorldsLoaded) {
    voidWorldsLoaded = fetch(VOID_WORLDS_URL)
      .then(function (res) { return res.ok ? res.json() : []; })
      .catch(function () { return []; })
      .then(function (worlds) {
        voidWorldList = worlds;
        return worlds;
      });
  }
  return voidWorldsLoaded;
}

function voidStartWorlds() {
  voidLoadWorldList().then(function () {
    voidRefreshInfo();
    voidRefreshPings();
    setInterval(voidRefreshInfo, VOID_INFO_REFRESH_MS);
    setInterval(voidRefreshPings, VOID_PING_REFRESH_MS);
  });
}

// Resolves to world `number`'s web server base URL (see voidWebBase), or null when it isn't in
// `worlds.json` or has no usable address.
window.voidWorldWeb = function (number) {
  return voidLoadWorldList().then(function (worlds) {
    var world = worlds.find(function (entry) { return entry.number === number; });
    return world ? voidWebBase(world.web) : null;
  });
};

// The selected world while it can be asked for data: its number, or null when no world is
// selected or the selected one is known to be Offline. `Checking` counts as available, so a page
// doesn't wait on the first ping before loading; a world that's really down just fails the fetch.
function voidAvailableWorld() {
  var world = Alpine.store('world').current;
  if (world == null) {
    return null;
  }
  return Alpine.store('worlds').get(world).status === 'Offline' ? null : world;
}

window.voidAvailableWorld = voidAvailableWorld;

// Calls `callback(world)` straight away and again whenever the available world (see
// voidAvailableWorld) changes — a switch, a disconnect, or the selected world going offline or
// coming back — with null when there's nothing to load from. Pages clear their data and, given a
// world, reload it from there. The callback runs outside the effect so whatever it reads doesn't
// become a dependency of it.
window.voidWatchWorld = function (callback) {
  var last;
  Alpine.effect(function () {
    var world = voidAvailableWorld();
    if (world === last) {
      return;
    }
    last = world;
    Promise.resolve().then(function () { callback(world); });
  });
};

// Resolves to the JSON at `path` (e.g. `/api/v1/hiscores/overall?page=0`) on the selected world's
// web server. Rejects when no world is available, the world has no web address, the request fails,
// or the selection changes before the reply lands, so a slow answer from the previous world can't
// fill a page that has since moved to another one.
window.voidWorldJson = function (path) {
  var world = voidAvailableWorld();
  if (world == null) {
    return Promise.reject(new Error('No world available'));
  }
  return window.voidWorldWeb(world)
    .then(function (base) {
      if (!base) {
        throw new Error('No web address for world ' + world);
      }
      return fetch(base + path);
    })
    .then(function (response) {
      if (!response.ok) {
        throw new Error('Request to ' + path + ' failed: ' + response.status);
      }
      return response.json();
    })
    .then(function (data) {
      if (voidAvailableWorld() !== world) {
        throw new Error('World changed from ' + world);
      }
      return data;
    });
};

// --- Custom servers: name/web address the visitor typed in themselves, kept in this browser only ---

function voidGetCustomWorlds() {
  try {
    var raw = localStorage.getItem(VOID_CUSTOM_WORLDS_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch (e) {
    return [];
  }
}

function voidSaveCustomWorlds(list) {
  try {
    localStorage.setItem(VOID_CUSTOM_WORLDS_KEY, JSON.stringify(list));
  } catch (e) {
    // Private browsing / storage disabled — the list still renders for this view.
  }
}

window.voidAddCustomWorld = function (name, address) {
  if (!voidWebBase(address)) {
    return false;
  }
  var list = voidGetCustomWorlds();
  list.push({ id: 'custom-' + Date.now(), name: (name || '').trim() || address.trim(), address: address.trim() });
  voidSaveCustomWorlds(list);
  voidRenderCustomWorlds();
  voidPingCustomWorlds();
  return true;
};

window.voidRemoveCustomWorld = function (id) {
  voidSaveCustomWorlds(voidGetCustomWorlds().filter(function (entry) { return entry.id !== id; }));
  voidRenderCustomWorlds();
};

function voidEscapeHtml(value) {
  var div = document.createElement('div');
  div.textContent = value == null ? '' : String(value);
  return div.innerHTML;
}

function voidCustomRowHtml(entry) {
  return (
    '<div style="display:flex;align-items:center;gap:var(--space-5);' +
    'padding:var(--space-4) var(--space-5);background:var(--surface-inset);border:1px solid var(--border-panel);' +
    'border-radius:var(--radius-sm)">' +
    '<div style="flex:1;min-width:0">' +
    '<div style="font:var(--weight-semibold) var(--text-sm)/1.3 var(--font-ui);color:var(--parch-50);' +
    'white-space:nowrap;overflow:hidden;text-overflow:ellipsis">' + voidEscapeHtml(entry.name) + '</div>' +
    '<div style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);margin-top:2px">' +
    voidEscapeHtml(entry.address) + '</div>' +
    '</div>' +
    '<span id="wc-status-' + entry.id + '" style="font:var(--type-label);letter-spacing:var(--tracking-caps);' +
    'text-transform:uppercase;color:var(--text-faint);white-space:nowrap">Checking…</span>' +
    '<span id="wc-ping-' + entry.id + '" style="font:var(--type-code);font-size:var(--text-2xs);' +
    'color:var(--text-muted);min-width:44px;text-align:right"></span>' +
    '<button type="button" aria-label="Remove server" onclick="voidRemoveCustomWorld(\'' + entry.id + '\')" ' +
    'class="void-btn void-btn-ghost" style="height:28px;padding:0 12px;border-radius:var(--radius-sm)">Remove</button>' +
    '</div>'
  );
}

function voidRenderCustomWorlds() {
  var container = document.getElementById('void-custom-worlds');
  if (!container) {
    return;
  }
  var list = voidGetCustomWorlds();
  if (list.length === 0) {
    container.innerHTML = '<p style="margin:0;font:var(--type-body-sm);color:var(--text-faint)">' +
      'No custom servers saved yet.</p>';
    return;
  }
  container.innerHTML = list.map(voidCustomRowHtml).join('');
}

function voidPingCustomWorlds() {
  if (!document.getElementById('void-custom-worlds')) {
    return Promise.resolve();
  }
  return Promise.all(voidGetCustomWorlds().map(function (entry) {
    return voidPing(entry.address).then(function (ms) {
      var online = ms !== null;
      var statusEl = document.getElementById('wc-status-' + entry.id);
      if (statusEl) {
        statusEl.textContent = online ? 'Online' : 'Offline';
        statusEl.style.color = online ? 'var(--feedback-success)' : 'var(--feedback-danger)';
      }
      var pingEl = document.getElementById('wc-ping-' + entry.id);
      if (pingEl) {
        pingEl.textContent = online ? ms + 'ms' : '—';
      }
    });
  }));
}

document.addEventListener('DOMContentLoaded', voidRenderCustomWorlds);

// alpine:initialized so the `worlds` store exists by the time results come back.
document.addEventListener('alpine:initialized', voidStartWorlds);
