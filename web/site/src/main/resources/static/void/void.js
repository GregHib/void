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
