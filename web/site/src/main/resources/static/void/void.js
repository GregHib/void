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
