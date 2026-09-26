// Merges a handful of style declarations onto an element without touching the rest of its
// inline `style` attribute — Alpine's `x-bind:style` string form replaces the whole attribute,
// which would wipe the static layout styles components set at render time.
// Shared number formatter used by every page's Alpine app (dev/exchange/hiscores/log) for
// counts, xp, gp and kill-counts — kept here so they all format the same way from one place.
window.voidFmt = function (n) {
  return Math.round(n).toLocaleString('en-US');
};

window.voidBx = function (el, on, whenTrue, whenFalse) {
  var decls = on ? whenTrue : (whenFalse || '');
  decls.split(';').forEach(function (rule) {
    var i = rule.indexOf(':');
    if (i > 0) {
      el.style.setProperty(rule.slice(0, i).trim(), rule.slice(i + 1).trim());
    }
  });
};

// The connected world: read/written to localStorage so it survives navigating between pages, and
// mirrored into an Alpine store (below) so every component on the *same* page — the navbar's
// worldMenu and a page-body picker are separate x-data components — reacts the instant either one
// changes it, with no reload needed. worldMenuData() backs the navbar's quick-switch dropdown (see
// WorldMenu.kt); playApp() backs the play page (see Play.kt). Both redirect to play.html?world=N on
// a fresh pick so the loading screen and the URL stay in sync — the dropdown only does this when
// already on the play page.
var VOID_WORLD_KEY = 'void-world';

function voidGetWorld() {
  try {
    var raw = localStorage.getItem(VOID_WORLD_KEY);
    return raw ? parseInt(raw, 10) : null;
  } catch (e) {
    return null;
  }
}

function voidSetWorld(number) {
  try {
    localStorage.setItem(VOID_WORLD_KEY, String(number));
  } catch (e) {
    // Private browsing / storage disabled — selection still applies for this view.
  }
}

function voidClearWorld() {
  try {
    localStorage.removeItem(VOID_WORLD_KEY);
  } catch (e) {
    // Private browsing / storage disabled — the clear still applies for this view.
  }
}

// Whether the play page's nav bar is tucked away to give the client the whole window.
var VOID_NAV_HIDDEN_KEY = 'void-play-nav-hidden';

function voidGetNavHidden() {
  try {
    return localStorage.getItem(VOID_NAV_HIDDEN_KEY) === 'true';
  } catch (e) {
    return false;
  }
}

document.addEventListener('alpine:init', function () {
  Alpine.store('world', { current: voidGetWorld() });
  // Only honoured while a world is loaded (see Play.kt), so the world picker always keeps its nav.
  Alpine.store('playNav', {
    hidden: voidGetNavHidden(),
    setHidden: function (hidden) {
      this.hidden = hidden;
      try {
        localStorage.setItem(VOID_NAV_HIDDEN_KEY, String(hidden));
      } catch (e) {
        // Private browsing / storage disabled — the nav bar still hides for this view.
      }
    },
  });
});

// The size the client should render at: `#client`'s area. Starts at the game's minimum so a
// client started while `#client` has no size yet (a hidden or collapsed tab) never sees a 0x0
// window, which it can't recover from.
var voidClientView = { width: 765, height: 503 };

// Recomputes voidClientView from `#client`. The client sizes itself from
// window.innerWidth/innerHeight on load and on every window `resize` (and has no other hook), so
// voidClientViewport() points those at voidClientView and a synthetic `resize` tells it to pick
// the new size up.
function voidClientLayout() {
  var client = document.getElementById('client');
  if (!client || !window.voidClientViewportInstalled) {
    return;
  }
  var width = client.clientWidth;
  var height = client.clientHeight;
  if (width <= 0 || height <= 0) {
    return;
  }
  voidClientView = { width: width, height: height };
  window.dispatchEvent(new Event('resize'));
  // The client sizes its game canvas from its root element's size as last seen by a
  // ResizeObserver, which hasn't caught up with the resize above yet — so nudge it again once
  // the frame's observers have run.
  requestAnimationFrame(function () {
    setTimeout(function () { window.dispatchEvent(new Event('resize')); }, 0);
  });
}

function voidClientViewport() {
  if (window.voidClientViewportInstalled) {
    return;
  }
  window.voidClientViewportInstalled = true;
  Object.defineProperty(window, 'innerWidth', { configurable: true, get: function () { return voidClientView.width; } });
  Object.defineProperty(window, 'innerHeight', { configurable: true, get: function () { return voidClientView.height; } });
  new ResizeObserver(voidClientLayout).observe(document.getElementById('client'));
  voidClientLayout();
}

window.worldMenuData = function () {
  return {
    open: false,
    get world() {
      return Alpine.store('world').current;
    },
    select: function (number) {
      voidSetWorld(number);
      Alpine.store('world').current = number;
      this.open = false;
      if (/(^|\/)play\.html$/.test(window.location.pathname)) {
        window.location.href = 'play.html?world=' + number;
      }
    },
    disconnect: function () {
      voidClearWorld();
      Alpine.store('world').current = null;
      this.open = false;
      // Play.html reads ?world= before localStorage, so a plain redirect there is needed —
      // otherwise the stale query string would just reconnect it on load.
      if (/(^|\/)play\.html$/.test(window.location.pathname)) {
        window.location.href = 'play.html';
      }
    },
  };
};

window.playApp = function () {
  return {
    status: 'Loading world ...',
    get world() {
      return Alpine.store('world').current;
    },
    init: function () {
      var fromQuery = new URLSearchParams(window.location.search).get('world');
      if (fromQuery) {
        var number = parseInt(fromQuery, 10);
        voidSetWorld(number);
        Alpine.store('world').current = number;
        this.status = 'Loading world ' + number + ' ...';
        this.launch(number);
        return;
      }
      var saved = voidGetWorld();
      if (saved) {
        window.location.href = 'play.html?world=' + saved;
      }
    },
    select: function (number) {
      window.location.href = 'play.html?world=' + number;
    },
    // Loads the web client from world `number`'s web server, which serves it under `/play/` and
    // proxies its websocket at `/proxy` through to the game server.
    launch: function (number) {
      // Two clients on one page would both run their game loops (and both log in).
      if (window.voidClientLaunched) {
        return;
      }
      window.voidClientLaunched = true;
      var self = this;
      window.voidWorldWeb(number).then(function (base) {
        if (!base) {
          self.status = 'World ' + number + ' has no web address to play from.';
          return;
        }
        window.CONFIG = { url: base.replace(/^http/i, 'ws') + '/proxy' };
        voidClientViewport();
        var script = document.createElement('script');
        script.src = base + '/play/void-client.js';
        script.onload = function () { self.status = null; };
        script.onerror = function () { self.status = 'Couldn\'t load the client from world ' + number + '.'; };
        document.body.appendChild(script);
      });
    },
  };
};

// Docs code-tab groups (see CodeTabs in Markdown.kt): every `.code-tabs` block on the page is
// switched to the same language at once, so picking "JSON" in one config example flips every
// other example that also has a JSON tab. The choice is remembered per-browser.
window.voidApplyCodeTabLang = function (lang) {
  document.querySelectorAll('.code-tabs').forEach(function (group) {
    var matched = false;
    group.querySelectorAll('.code-tab-panel').forEach(function (panel) {
      var on = panel.getAttribute('data-lang') === lang;
      panel.hidden = !on;
      if (on) {
        matched = true;
      }
    });
    if (!matched) {
      return;
    }
    group.querySelectorAll('.code-tab-btn').forEach(function (btn) {
      btn.classList.toggle('active', btn.getAttribute('data-lang') === lang);
    });
  });
};

window.voidSwitchCodeTab = function (btn) {
  var lang = btn.getAttribute('data-lang');
  try {
    localStorage.setItem('void-code-lang', lang);
  } catch (e) {
    // Private browsing / storage disabled — the switch still applies for this view.
  }
  window.voidApplyCodeTabLang(lang);
};

document.addEventListener('DOMContentLoaded', function () {
  var saved = null;
  try {
    saved = localStorage.getItem('void-code-lang');
  } catch (e) {
    // Ignore — falls back to each group's server-rendered default tab.
  }
  if (saved) {
    window.voidApplyCodeTabLang(saved);
  }
});

// Copy-to-clipboard button on rendered fenced code blocks (see CodeFenceGeneratingProvider in
// Markdown.kt). Reads the sibling <pre><code> as plain text so highlighting spans aren't copied.
window.voidCopyCode = function (btn) {
  var code = btn.parentElement.querySelector('pre code');
  if (!code) {
    return;
  }
  var text = code.textContent;
  var onCopied = function () {
    var original = btn.textContent;
    btn.textContent = 'Copied!';
    btn.classList.add('copied');
    setTimeout(function () {
      btn.textContent = original;
      btn.classList.remove('copied');
    }, 1500);
  };
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(text).then(onCopied, function () {
      // Clipboard permission denied — leave the button as-is rather than claim success.
    });
    return;
  }
  var textarea = document.createElement('textarea');
  textarea.value = text;
  textarea.style.position = 'fixed';
  textarea.style.opacity = '0';
  document.body.appendChild(textarea);
  textarea.select();
  try {
    document.execCommand('copy');
    onCopied();
  } catch (e) {
    // Unsupported — nothing more we can do.
  }
  document.body.removeChild(textarea);
};
