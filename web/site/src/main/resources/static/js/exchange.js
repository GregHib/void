// Grand Exchange page data + Alpine component. Every panel is fetched live from the real
// `/api/v1/exchange/*` endpoints (see `ExchangeRoutes.kt`) - market summary, highlights, item
// search, item detail, price history and related items are all backed by real item definitions
// and `Storage.priceHistory()`. Most items have no recorded trades yet (history only exists for
// items that have actually been bought/sold on the Grand Exchange), so their guide price falls
// back to the item's configured/shop value and their chart is empty until someone trades them.

(function () {
  var API = "/api/v1/exchange";

  // Maps the site's category chip labels (baked into `Exchange.kt`) to the API's category ids.
  var CATEGORY_ID = {
    All: "all", Weapons: "weapons", Armour: "armour", Runes: "runes",
    Consumables: "consumables", Resources: "resources", Curios: "curios",
  };
  var TIMEFRAME_ID = { "24H": "24h", "7D": "7d", "30D": "30d", "1Y": "1y", All: "all" };

  function getJson(url) {
    return fetch(url).then(function (response) {
      if (!response.ok) {
        throw new Error("Request to " + url + " failed: " + response.status);
      }
      return response.json();
    });
  }

  var fmt = window.voidFmt;
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

  function stamp(iso, tf) {
    var d = new Date(iso);
    var M = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var D = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    var hh = String(d.getUTCHours()).padStart(2, "0") + ":" + String(d.getUTCMinutes()).padStart(2, "0");
    if (tf === "24H") return hh + " UTC";
    if (tf === "7D") return D[d.getUTCDay()] + " " + hh;
    if (tf === "30D") return d.getUTCDate() + " " + M[d.getUTCMonth()];
    return M[d.getUTCMonth()] + " " + String(d.getUTCFullYear()).slice(2);
  }

  // Coloured category border/code, mirroring the chips baked into `Exchange.kt` - kept here since
  // the API's `categoryCode` already gives the three-letter tile code, only the border colour
  // (a CSS variable) needs a client-side lookup.
  var CAT_BORDER = {
    weapons: "var(--ember-600)", armour: "var(--border-strong)", runes: "var(--steel-600)",
    consumables: "var(--moss-600)", resources: "var(--border-strong)", curios: "var(--gold-500)",
  };

  function itemRow(it, kind) {
    var d = it.delta24h;
    return {
      id: it.id, name: it.name, cat: it.categoryName, code: it.categoryCode, border: CAT_BORDER[it.category] || "var(--border-strong)",
      m1: kind === "vol" ? short(it.volume24h) : kind === "price" ? gp(it.price) : pct(d),
      m2: kind === "vol" ? gp(it.price) : kind === "price" ? short(it.volume24h) + " traded" : gp(it.price),
      m1Color: kind === "delta" ? col(d) : "var(--text-strong)",
    };
  }

  function searchRow(it) {
    var d = it.delta24h;
    return {
      id: it.id, name: it.name, examine: it.examine, code: it.categoryCode, border: CAT_BORDER[it.category] || "var(--border-strong)",
      price: gp(it.price), delta: pct(d), deltaColor: col(d),
      vol: short(it.volume24h), limit: it.buyLimit != null ? fmt(it.buyLimit) : "None",
    };
  }

  function relatedRow(it) {
    var d = it.delta24h;
    return { id: it.id, name: it.name, code: it.categoryCode, border: CAT_BORDER[it.category] || "var(--border-strong)", delta: pct(d), deltaColor: col(d) };
  }

  function itemView(it) {
    var d = it.delta24h;
    return {
      name: it.name, cat: it.categoryName, code: it.categoryCode, border: CAT_BORDER[it.category] || "var(--border-strong)",
      examine: it.examine, price: gp(it.price), delta: pct(d), deltaColor: col(d),
      memberLabel: it.members ? "Members" : "Free",
      memberBg: it.members ? "rgba(224,174,60,.14)" : "var(--umber-700)",
      memberColor: it.members ? "var(--gold-300)" : "var(--parch-200)",
      memberBorder: it.members ? "var(--gold-600)" : "var(--border-strong)",
    };
  }

  function itemStats(it) {
    return [
      { label: "Buy limit", value: it.buyLimit != null ? fmt(it.buyLimit) : "None", note: it.buyLimitWindowHours ? "per " + it.buyLimitWindowHours + " hours" : "" },
      { label: "Margin", value: gp(it.margin), note: "buy minus sell" },
      { label: "Tax", value: gp(it.tax), note: "2%, capped at 5m" },
      { label: "Daily volume", value: short(it.volume24h), note: "units, 24h mean" },
      { label: "High alchemy", value: gp(it.highAlchemy), note: "nature rune not included" },
      { label: "Low alchemy", value: gp(it.lowAlchemy), note: "fire runes only" },
      { label: "Shop value", value: gp(it.shopValue), note: "general store base" },
      { label: "Members", value: it.members ? "Yes" : "No", note: it.members ? "members worlds only" : "all worlds" },
    ];
  }

  var EMPTY_ITEM = {
    name: "", cat: "", code: "", border: "var(--border-strong)", examine: "",
    price: "0 gp", delta: "+0.00%", deltaColor: "var(--moss-500)",
    memberLabel: "Free", memberBg: "var(--umber-700)", memberColor: "var(--parch-200)", memberBorder: "var(--border-strong)",
  };

  window.exchangeApp = function () {
    return {
      page: "home", q: "", cat: "All", sort: "vol", id: "", tf: "24H", hover: null,

      trackedItems: 0,
      summaryTiles: [],
      topVolume: [], risers: [], fallers: [], mostExpensive: [],
      searchResults: [],
      items: {}, // itemId -> ItemDetail, cached across navigation
      historyPoints: {}, // "itemId:timeframe" -> PricePoint[]
      relatedItems: [],

      init: function () {
        var self = this;
        this.syncFromHash();
        this.hashListener = function () { self.syncFromHash(); };
        window.addEventListener("popstate", this.hashListener);
        this.loadHome();
        this.$watch("q", function () { if (self.page === "search") self.loadSearch(); });
        this.$watch("cat", function () { if (self.page === "search") self.loadSearch(); });
        this.$watch("sort", function () { if (self.page === "search") self.loadSearch(); });
        this.$watch("tf", function () { if (self.page === "item") self.loadHistory(); });
      },
      destroy: function () {
        window.removeEventListener("popstate", this.hashListener);
      },

      loadHome: function () {
        var self = this;
        getJson(API + "/summary").then(function (data) {
          self.trackedItems = data.trackedItems;
          self.summaryTiles = [
            { label: "Items tracked", value: String(data.trackedItems), note: "grand exchange listings", color: "var(--text-faint)" },
            { label: "Value traded · 24h", value: short(data.valueTraded24h) + " gp", note: fmt(data.tradesSettled24h) + " units traded", color: "var(--text-faint)" },
            { label: "Market index", value: pct(data.marketIndex), note: "mean price move today", color: col(data.marketIndex) },
            { label: "Last sample", value: "just now", note: "live from the server", color: "var(--text-faint)" },
          ];
        }).catch(function () { self.summaryTiles = []; });
        getJson(API + "/highlights?limit=6").then(function (data) {
          self.topVolume = data.topVolume.map(function (x) { return itemRow(x, "vol"); });
          self.risers = data.risers.map(function (x) { return itemRow(x, "delta"); });
          self.fallers = data.fallers.slice().reverse().map(function (x) { return itemRow(x, "delta"); });
          self.mostExpensive = data.mostExpensive.map(function (x) { return itemRow(x, "price"); });
        }).catch(function () {
          self.topVolume = []; self.risers = []; self.fallers = []; self.mostExpensive = [];
        });
      },
      get summary() { return this.summaryTiles; },

      loadSearch: function () {
        var self = this;
        var params = new URLSearchParams();
        var q = this.q.trim();
        if (q) params.set("q", q);
        params.set("category", CATEGORY_ID[this.cat] || "all");
        params.set("sort", this.sort === "vol" ? "volume" : this.sort);
        params.set("pageSize", "100");
        getJson(API + "/items?" + params.toString()).then(function (data) {
          self.searchResults = data.items.map(searchRow);
        }).catch(function () { self.searchResults = []; });
      },
      get results() { return this.searchResults; },

      loadItem: function (id) {
        var self = this;
        return getJson(API + "/items/" + encodeURIComponent(id)).then(function (data) {
          self.items[id] = data;
        }).catch(function () {
          self.items[id] = null;
        });
      },
      loadHistory: function () {
        var self = this, id = this.id, tf = this.tf;
        var key = id + ":" + tf;
        if (this.historyPoints[key]) return;
        getJson(API + "/items/" + encodeURIComponent(id) + "/history?timeframe=" + TIMEFRAME_ID[tf]).then(function (data) {
          self.historyPoints[key] = data.points;
        }).catch(function () { self.historyPoints[key] = []; });
      },
      loadRelated: function (id) {
        var self = this;
        getJson(API + "/items/" + encodeURIComponent(id) + "/related?limit=4").then(function (data) {
          self.relatedItems = data.items.map(relatedRow);
        }).catch(function () { self.relatedItems = []; });
      },

      // Pushes a history entry per in-page navigation so the browser back/forward buttons step
      // through home/search/item states instead of leaving the page on the first back press.
      syncFromHash: function () {
        var h = window.location.hash.replace(/^#/, "");
        var wasSearch = this.page === "search";
        if (h.indexOf("item/") === 0) {
          this.id = decodeURIComponent(h.slice(5));
          this.page = "item";
          this.enterItem(this.id);
        } else if (h === "search") {
          this.page = "search";
          this.loadSearch();
        } else {
          this.page = "home";
        }
        if (wasSearch && this.page !== "search") this.q = "";
        this.hover = null;
      },

      enterItem: function (id) {
        var self = this;
        this.hover = null;
        if (!this.items[id]) this.loadItem(id);
        this.loadHistory();
        this.loadRelated(id);
      },

      onChartMove: function (e) {
        var pts = this.rawHistory;
        if (pts.length < 2) return;
        var r = e.currentTarget.getBoundingClientRect();
        var vb = ((e.clientX - r.left) / r.width) * 920;
        var step = (920 - 66 - 12) / (pts.length - 1);
        var i = Math.max(0, Math.min(pts.length - 1, Math.round((vb - 66) / step)));
        if (i !== this.hover) this.hover = i;
      },
      onChartLeave: function () { this.hover = null; },

      open: function (id) {
        var wasSearch = this.page === "search";
        this.id = id; this.page = "item"; this.hover = null; window.scrollTo(0, 0);
        if (wasSearch) this.q = "";
        this.enterItem(id);
        history.pushState(null, "", "#item/" + id);
      },
      goHome: function () {
        var wasSearch = this.page === "search";
        this.page = "home";
        if (wasSearch) this.q = "";
        history.pushState(null, "", "#");
      },
      goSearch: function () { this.page = "search"; this.loadSearch(); history.pushState(null, "", "#search"); },

      get itemRaw() { return this.items[this.id]; },
      get itemMissing() { return this.itemRaw === null; },
      get item() { return this.itemRaw ? itemView(this.itemRaw) : EMPTY_ITEM; },
      get stats() { return this.itemRaw ? itemStats(this.itemRaw) : []; },
      get related() { return this.relatedItems; },

      get rawHistory() { return this.historyPoints[this.id + ":" + this.tf] || []; },
      get chartData() {
        var pts = this.rawHistory, n = pts.length;
        var it = this.itemRaw;
        if (n === 0) {
          var price = it ? it.price : 0;
          return {
            eyebrow: this.tf === "All" ? "since launch" : "last " + this.tf,
            stamp: "No trades yet", buy: gp(price), sell: gp(price), vol: "0",
            grid: [], xlabels: [], gridSvg: "", barsSvg: "", buyPath: "", sellPath: "", band: "",
            hovering: false, hx: 0, hyBuy: 0, hySell: 0, hoverLeft: "0%", hoverTop: "0%", margin: 0,
          };
        }
        var W = 920, PL = 66, PR = 12, PT = 14, PB = 34, H = 300;
        var lo = Infinity, hi = -Infinity, vhi = 0;
        pts.forEach(function (p) { lo = Math.min(lo, p.sell); hi = Math.max(hi, p.buy); vhi = Math.max(vhi, p.volume); });
        var pad = (hi - lo) * 0.14 || hi * 0.05 || 1;
        lo -= pad; hi += pad;
        function X(i) { return PL + (i * (W - PL - PR)) / Math.max(1, n - 1); }
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
          return { left: ((X(i) / W) * 100).toFixed(2) + "%", label: stamp(pts[i].at, self.tf) };
        });
        var bw = Math.max(2, (W - PL - PR) / n - 2);
        var bars = pts.map(function (p, i) {
          var h = vhi > 0 ? Math.max(1, (p.volume / vhi) * 58) : 1;
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
          stamp: stamp(hv.at, self.tf), buy: gp(hv.buy), sell: gp(hv.sell), vol: short(hv.volume),
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
    };
  };
})();
