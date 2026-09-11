// Grand Exchange page data + Alpine component. A self-contained mock dataset stands in for the
// exchange API: deterministic pseudo-random prices/volume/history so the page has something
// realistic to browse, search, filter and chart without a server round-trip.

(function () {
  var RAW = [
    ["Emberfang blade", "Weapons", 1284000, 41200, 8, true, "Still warm to the touch."],
    ["Warden's greataxe", "Weapons", 8420000, 6100, 5, true, "Two hands, one swing."],
    ["Hollowpoint crossbow", "Weapons", 447000, 22800, 40, true, "Loaded and unhappy about it."],
    ["Thornwood shortbow", "Weapons", 61500, 96400, 125, false, "Cut from a tree that fought back."],
    ["Gravemaker spear", "Weapons", 2140000, 9800, 10, true, "The haft is colder than the head."],
    ["Runic platebody", "Armour", 3860000, 15400, 10, true, "Heavy, and proud of it."],
    ["Duskweave hood", "Armour", 128000, 54300, 70, true, "Keeps the rain and the questions out."],
    ["Gilded kiteshield", "Armour", 5210000, 4200, 5, true, "More gold than sense."],
    ["Bonecarved greaves", "Armour", 742000, 18900, 25, true, "Somebody else no longer needs these."],
    ["Tidewalker boots", "Armour", 316000, 27600, 40, true, "Dry feet in every swamp."],
    ["Void rune", "Runes", 412, 18400000, 25000, false, "A hole in the world, palm-sized."],
    ["Ember rune", "Runes", 96, 41200000, 25000, false, "Warm in the pouch."],
    ["Tidal rune", "Runes", 74, 38900000, 25000, false, "Faintly damp."],
    ["Grove rune", "Runes", 58, 22700000, 25000, false, "Smells of cut grass."],
    ["Runic catalyst", "Runes", 3480, 2140000, 10000, true, "Binds the others together."],
    ["Ashroot brew", "Consumables", 2870, 1240000, 2000, false, "Tastes like a chimney. Works, though."],
    ["Silverfin steak", "Consumables", 1140, 3480000, 10000, false, "Cooked exactly once."],
    ["Moonpetal tonic", "Consumables", 9420, 684000, 2000, true, "Restores what the dungeon took."],
    ["Ranger's draught", "Consumables", 14800, 412000, 2000, true, "Steadies the hand for four minutes."],
    ["Ironbark log", "Resources", 428, 8940000, 25000, false, "Denser than it looks."],
    ["Starforged bar", "Resources", 18600, 947000, 10000, true, "Cooled in open sky."],
    ["Deepvein ore", "Resources", 1240, 4210000, 25000, false, "Mined from under the water table."],
    ["Spindlethread", "Resources", 342, 6180000, 25000, false, "One strand holds a full pack."],
    ["Warden's sigil", "Curios", 486000000, 94, 2, true, "Nine of these exist. Eight are accounted for."],
    ["Hollow key", "Curios", 74200000, 610, 2, true, "Opens something. Nobody agrees what."],
    ["Cracked orb", "Curios", 12400000, 2840, 5, true, "The crack is part of the design."],
  ];

  var CAT_BORDER = {
    Weapons: "var(--ember-600)", Armour: "var(--border-strong)", Runes: "var(--steel-600)",
    Consumables: "var(--moss-600)", Resources: "var(--border-strong)", Curios: "var(--gold-500)",
  };
  var CAT_CODE = {
    Weapons: "WPN", Armour: "ARM", Runes: "RUN", Consumables: "POT", Resources: "RES", Curios: "CUR",
  };
  var CAT_BLURB = {
    Weapons: "Members can trade this weapon freely. Prices track the top three PvP worlds most closely.",
    Armour: "Degrades on use. Repair costs are excluded from the guide price.",
    Runes: "Bulk commodity. The buy limit resets every four hours per account.",
    Consumables: "Consumed on use, so supply is entirely production-side.",
    Resources: "Raw input for several skills. Volume follows server population.",
    Curios: "Rare drop. Fewer than a thousand trades have ever been recorded.",
  };

  var TFS = {
    "24H": { n: 48, v: 0.010, m: 1, ms: 1800000, k: 11 },
    "7D": { n: 56, v: 0.014, m: 2.3, ms: 10800000, k: 23 },
    "30D": { n: 60, v: 0.020, m: 3.6, ms: 43200000, k: 37 },
    "1Y": { n: 52, v: 0.034, m: 6.2, ms: 604800000, k: 53 },
    All: { n: 60, v: 0.046, m: 9.4, ms: 2592000000, k: 71 },
  };
  var NOW = Date.UTC(2026, 8, 10, 12, 0, 0);

  function rng(seed) {
    var a = seed >>> 0;
    return function () {
      a = (a + 0x6D2B79F5) >>> 0;
      var t = Math.imul(a ^ (a >>> 15), 1 | a);
      t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  function fmt(n) { return Math.round(n).toLocaleString("en-US"); }
  function gp(n) { return fmt(n) + " gp"; }
  function short(n) {
    var a = Math.abs(n);
    if (a >= 1e9) return (n / 1e9).toFixed(2) + "b";
    if (a >= 1e6) return (n / 1e6).toFixed(2) + "m";
    if (a >= 1e3) return (n / 1e3).toFixed(1) + "k";
    return String(Math.round(n));
  }
  function pct(d) { return (d >= 0 ? "+" : "−") + (Math.abs(d) * 100).toFixed(2) + "%"; }
  function col(d) { return d >= 0 ? "var(--moss-500)" : "var(--ember-500)"; }

  function stamp(t, tf) {
    var d = new Date(t);
    var M = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var D = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    var hh = String(d.getUTCHours()).padStart(2, "0") + ":" + String(d.getUTCMinutes()).padStart(2, "0");
    if (tf === "24H") return hh + " UTC";
    if (tf === "7D") return D[d.getUTCDay()] + " " + hh;
    if (tf === "30D") return d.getUTCDate() + " " + M[d.getUTCMonth()];
    return M[d.getUTCMonth()] + " " + String(d.getUTCFullYear()).slice(2);
  }

  function buildItems() {
    return RAW.map(function (r) {
      var name = r[0], cat = r[1], base = r[2], vol = r[3], limit = r[4], members = r[5], examine = r[6];
      var h = 2166136261;
      for (var i = 0; i < name.length; i++) { h ^= name.charCodeAt(i); h = Math.imul(h, 16777619) >>> 0; }
      var rnd = rng(h);
      rnd();
      var trend = (rnd() - 0.44) * 0.34;
      return {
        id: name.toLowerCase().replace(/[^a-z0-9]+/g, "-"),
        name: name, cat: cat, base: base, vol: vol, limit: limit, members: members, examine: examine,
        trend: trend, seed: h, border: CAT_BORDER[cat], code: CAT_CODE[cat], desc: CAT_BLURB[cat],
      };
    });
  }

  var ITEMS = buildItems();

  window.exchangeApp = function () {
    return {
      page: "home", q: "", cat: "All", sort: "vol", id: "warden-s-sigil", tf: "24H", hover: null, drift: {},

      priceOf: function (it) { return Math.max(1, Math.round(it.base * (1 + (this.drift[it.id] || 0)))); },
      deltaOf: function (it) { return it.trend + (this.drift[it.id] || 0); },
      gp: gp, short: short, pct: pct, col: col,

      init: function () {
        var self = this;
        this.syncFromHash();
        this.hashListener = function () { self.syncFromHash(); };
        window.addEventListener("popstate", this.hashListener);
        this.timer = setInterval(function () {
          var d = Object.assign({}, self.drift);
          for (var i = 0; i < 7; i++) {
            var it = ITEMS[Math.floor(Math.random() * ITEMS.length)];
            var nv = (d[it.id] || 0) + (Math.random() - 0.5) * 0.008;
            d[it.id] = Math.max(-0.06, Math.min(0.06, nv));
          }
          self.drift = d;
        }, 2500);
      },
      destroy: function () {
        clearInterval(this.timer);
        window.removeEventListener("popstate", this.hashListener);
      },

      // Pushes a history entry per in-page navigation so the browser back/forward buttons step
      // through home/search/item states instead of leaving the page on the first back press.
      syncFromHash: function () {
        var h = window.location.hash.replace(/^#/, "");
        if (h.indexOf("item/") === 0) {
          this.id = decodeURIComponent(h.slice(5));
          this.page = "item";
        } else if (h === "search") {
          this.page = "search";
        } else {
          this.page = "home";
        }
        this.hover = null;
      },

      series: function (it) {
        var cfg = TFS[this.tf], n = cfg.n;
        var rnd = rng(it.seed + cfg.k);
        var price = this.priceOf(it);
        var w = [], x = 0;
        for (var i = 0; i < n; i++) { x = x * 0.9 + (rnd() - 0.5) * 2 * cfg.v; w.push(x); }
        var t = it.trend * cfg.m;
        var pts = [];
        for (var j = 0; j < n; j++) {
          var mid = Math.max(1, price * (1 + w[j]) * (1 + t * (j / (n - 1) - 1)));
          var sp = 0.008 + 0.02 * Math.abs(w[j]);
          pts.push({
            t: NOW - (n - 1 - j) * cfg.ms,
            buy: Math.round(mid * (1 + sp)),
            sell: Math.round(mid * (1 - sp)),
            vol: Math.round((it.vol / n) * (0.4 + rnd() * 1.6) * (this.tf === "24H" ? 1 : cfg.ms / 1800000)),
          });
        }
        pts[n - 1].buy = Math.round(price * 1.009);
        pts[n - 1].sell = Math.round(price * 0.991);
        return pts;
      },

      onChartMove: function (e) {
        var r = e.currentTarget.getBoundingClientRect();
        var vb = ((e.clientX - r.left) / r.width) * 920;
        var n = TFS[this.tf].n;
        var step = (920 - 66 - 12) / (n - 1);
        var i = Math.max(0, Math.min(n - 1, Math.round((vb - 66) / step)));
        if (i !== this.hover) this.hover = i;
      },
      onChartLeave: function () { this.hover = null; },

      open: function (id) {
        this.id = id; this.page = "item"; this.hover = null; window.scrollTo(0, 0);
        history.pushState(null, "", "#item/" + id);
      },
      goHome: function () { this.page = "home"; history.pushState(null, "", "#"); },
      goSearch: function () { this.page = "search"; history.pushState(null, "", "#search"); },

      row: function (it, kind) {
        var self = this, d = this.deltaOf(it);
        return {
          id: it.id, name: it.name, cat: it.cat, code: it.code, border: it.border,
          m1: kind === "vol" ? short(it.vol) : kind === "price" ? gp(this.priceOf(it)) : pct(d),
          m2: kind === "vol" ? gp(this.priceOf(it)) : kind === "price" ? short(it.vol) + " traded" : gp(this.priceOf(it)),
          m1Color: kind === "delta" ? col(d) : "var(--text-strong)",
        };
      },

      get traded() { return ITEMS.reduce(function (s, it) { return s + it.vol * it.base; }, 0); },
      get marketIndex() {
        var self = this;
        return ITEMS.reduce(function (s, it) { return s + self.deltaOf(it); }, 0) / ITEMS.length;
      },
      get summary() {
        var index = this.marketIndex;
        return [
          { label: "Items tracked", value: String(ITEMS.length), note: "across 34 worlds", color: "var(--text-faint)" },
          { label: "Value traded · 24h", value: short(this.traded) + " gp", note: "1,284,902 trades settled", color: "var(--text-faint)" },
          { label: "Market index", value: pct(index), note: "mean price move today", color: col(index) },
          { label: "Last sample", value: "5m ago", note: "next sample 12:05 UTC", color: "var(--text-faint)" },
        ];
      },
      get byVol() { return ITEMS.slice().sort(function (a, b) { return b.vol * b.base - a.vol * a.base; }); },
      get byDelta() { var self = this; return ITEMS.slice().sort(function (a, b) { return self.deltaOf(b) - self.deltaOf(a); }); },
      get byPrice() { var self = this; return ITEMS.slice().sort(function (a, b) { return self.priceOf(b) - self.priceOf(a); }); },
      get topVolume() { var self = this; return this.byVol.slice(0, 6).map(function (x) { return self.row(x, "vol"); }); },
      get risers() { var self = this; return this.byDelta.slice(0, 6).map(function (x) { return self.row(x, "delta"); }); },
      get fallers() { var self = this; return this.byDelta.slice(-6).reverse().map(function (x) { return self.row(x, "delta"); }); },
      get mostExpensive() { var self = this; return this.byPrice.slice(0, 6).map(function (x) { return self.row(x, "price"); }); },

      get filtered() {
        var self = this, q = this.q.trim().toLowerCase();
        return ITEMS.filter(function (it) { return (self.cat === "All" || it.cat === self.cat) && (!q || it.name.toLowerCase().indexOf(q) >= 0); });
      },
      get sorted() {
        var self = this;
        return this.filtered.slice().sort(function (a, b) {
          if (self.sort === "vol") return b.vol * b.base - a.vol * a.base;
          if (self.sort === "price") return self.priceOf(b) - self.priceOf(a);
          if (self.sort === "gain") return self.deltaOf(b) - self.deltaOf(a);
          if (self.sort === "loss") return self.deltaOf(a) - self.deltaOf(b);
          return a.name.localeCompare(b.name);
        });
      },
      get results() {
        var self = this;
        return this.sorted.map(function (x) {
          var d = self.deltaOf(x);
          return {
            id: x.id, name: x.name, examine: x.examine, code: x.code, border: x.border,
            price: gp(self.priceOf(x)), delta: pct(d), deltaColor: col(d),
            vol: short(x.vol), limit: fmt(x.limit),
          };
        });
      },
      get item() {
        var it = ITEMS.filter(function (x) { return x.id === this.id; }, this)[0] || ITEMS[0];
        var price = this.priceOf(it), d = this.deltaOf(it);
        return {
          name: it.name, cat: it.cat, code: it.code, border: it.border, desc: it.desc, examine: it.examine,
          price: gp(price), delta: pct(d), deltaColor: col(d),
          memberLabel: it.members ? "Members" : "Free",
          memberBg: it.members ? "rgba(224,174,60,.14)" : "var(--umber-700)",
          memberColor: it.members ? "var(--gold-300)" : "var(--parch-200)",
          memberBorder: it.members ? "var(--gold-600)" : "var(--border-strong)",
        };
      },
      get itemRaw() { return ITEMS.filter(function (x) { return x.id === this.id; }, this)[0] || ITEMS[0]; },
      get chartData() {
        var it = this.itemRaw, pts = this.series(it), n = pts.length;
        var W = 920, PL = 66, PR = 12, PT = 14, PB = 34, H = 300;
        var lo = Infinity, hi = -Infinity, vhi = 0;
        pts.forEach(function (p) { lo = Math.min(lo, p.sell); hi = Math.max(hi, p.buy); vhi = Math.max(vhi, p.vol); });
        var pad = (hi - lo) * 0.14 || hi * 0.05;
        lo -= pad; hi += pad;
        function X(i) { return PL + (i * (W - PL - PR)) / (n - 1); }
        function Y(v) { return PT + (1 - (v - lo) / (hi - lo)) * (H - PT - PB); }
        function path(key) {
          return pts.map(function (p, i) { return (i ? "L" : "M") + X(i).toFixed(1) + " " + Y(p[key]).toFixed(1); }).join(" ");
        }
        var band = path("buy") + " " + pts.slice().reverse().map(function (p, i) { return "L" + X(n - 1 - i).toFixed(1) + " " + Y(p.sell).toFixed(1); }).join(" ") + " Z";
        var self = this;
        var grid = [0, 0.25, 0.5, 0.75, 1].map(function (f) {
          var y = PT + f * (H - PT - PB);
          return { y: y.toFixed(1), top: ((y / H) * 100).toFixed(2) + "%", label: short(hi - f * (hi - lo)) };
        });
        var xlabels = [0, 0.25, 0.5, 0.75, 1].map(function (f) {
          var i = Math.round(f * (n - 1));
          return { left: ((X(i) / W) * 100).toFixed(2) + "%", label: stamp(pts[i].t, self.tf) };
        });
        var bw = Math.max(2, (W - PL - PR) / n - 2);
        var bars = pts.map(function (p, i) {
          var h = Math.max(1, (p.vol / vhi) * 58);
          return { x: (X(i) - bw / 2).toFixed(1), y: (66 - h).toFixed(1), w: bw.toFixed(1), h: h.toFixed(1) };
        });
        var hv = self.hover !== null ? pts[self.hover] : pts[n - 1];
        // Alpine's `x-for` can't clone SVG shapes out of a `<template>` nested inside an `<svg>` —
        // the HTML parser reads the template's contents in the HTML namespace, so `<line>`/`<rect>`
        // never become real SVG elements. Building the markup here and binding it with `x-html` on
        // a `<g>` sidesteps that: `Element.innerHTML` parses in the element's own (SVG) namespace.
        var gridSvg = grid.map(function (g) {
          return '<line x1="66" x2="908" y1="' + g.y + '" y2="' + g.y + '" style="stroke:var(--umber-700);stroke-width:1"></line>';
        }).join("");
        var barsSvg = bars.map(function (b, i) {
          var active = self.hover === i;
          return '<rect x="' + b.x + '" y="' + b.y + '" width="' + b.w + '" height="' + b.h + '" style="fill:' + (active ? "var(--gold-300)" : "var(--umber-500)") + '"></rect>';
        }).join("");
        var hyBuy = self.hover !== null ? Y(pts[self.hover].buy) : 0;
        var hySell = self.hover !== null ? Y(pts[self.hover].sell) : 0;
        return {
          eyebrow: self.tf === "All" ? "since launch" : "last " + self.tf,
          stamp: stamp(hv.t, self.tf), buy: gp(hv.buy), sell: gp(hv.sell), vol: short(hv.vol),
          grid: grid, xlabels: xlabels, bars: bars, gridSvg: gridSvg, barsSvg: barsSvg,
          buyPath: path("buy"), sellPath: path("sell"), band: band,
          hovering: self.hover !== null,
          hx: self.hover !== null ? X(self.hover).toFixed(1) : 0,
          hyBuy: hyBuy.toFixed(1), hySell: hySell.toFixed(1),
          hoverLeft: (((self.hover !== null ? X(self.hover) : 0) / W) * 100).toFixed(2) + "%",
          hoverTop: ((Math.min(hyBuy, hySell) / H) * 100).toFixed(2) + "%",
          margin: hv.buy - hv.sell,
        };
      },
      get stats() {
        var it = this.itemRaw, price = this.priceOf(it), chart = this.chartData;
        var alch = Math.round(price * 0.6), low = Math.round(price * 0.4);
        var tax = Math.min(5000000, Math.round(price * 0.02));
        return [
          { label: "Buy limit", value: fmt(it.limit), note: "per 4 hours" },
          { label: "Margin", value: gp(chart.margin), note: "buy minus sell" },
          { label: "Tax", value: gp(tax), note: "2%, capped at 5m" },
          { label: "Daily volume", value: short(it.vol), note: "units, 24h mean" },
          { label: "High alchemy", value: gp(alch), note: "nature rune not included" },
          { label: "Low alchemy", value: gp(low), note: "fire runes only" },
          { label: "Shop value", value: gp(Math.round(price * 0.65)), note: "general store base" },
          { label: "Members", value: it.members ? "Yes" : "No", note: it.members ? "members worlds only" : "all worlds" },
        ];
      },
      get related() {
        var self = this, it = this.itemRaw;
        return ITEMS.filter(function (x) { return x.cat === it.cat && x.id !== it.id; }).slice(0, 4).map(function (x) {
          var d = self.deltaOf(x);
          return { id: x.id, name: x.name, code: x.code, border: x.border, delta: pct(d), deltaColor: col(d) };
        });
      },
    };
  };
})();
