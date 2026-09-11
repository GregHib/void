// Merges a handful of style declarations onto an element without touching the rest of its
// inline `style` attribute — Alpine's `x-bind:style` string form replaces the whole attribute,
// which would wipe the static layout styles components set at render time.
window.voidBx = function (el, on, whenTrue, whenFalse) {
  var decls = on ? whenTrue : (whenFalse || '');
  decls.split(';').forEach(function (rule) {
    var i = rule.indexOf(':');
    if (i > 0) {
      el.style.setProperty(rule.slice(0, i).trim(), rule.slice(i + 1).trim());
    }
  });
};

// The connected world: read/written to localStorage so it survives navigating between pages.
// worldMenuData() backs the navbar's quick-switch dropdown (see WorldMenu.kt); playApp() backs
// the play page (see Play.kt). Both redirect to play.html?world=N on a fresh pick so the loading
// screen and the URL stay in sync — the dropdown only does this when already on the play page.
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

window.worldMenuData = function () {
  return {
    world: voidGetWorld(),
    open: false,
    select: function (number) {
      voidSetWorld(number);
      this.world = number;
      this.open = false;
      if (/(^|\/)play\.html$/.test(window.location.pathname)) {
        window.location.href = 'play.html?world=' + number;
      }
    },
    disconnect: function () {
      voidClearWorld();
      this.world = null;
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
    world: null,
    init: function () {
      var fromQuery = new URLSearchParams(window.location.search).get('world');
      if (fromQuery) {
        var number = parseInt(fromQuery, 10);
        voidSetWorld(number);
        this.world = number;
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
