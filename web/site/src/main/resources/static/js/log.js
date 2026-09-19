// Adventurer's log page data + Alpine component. Every panel is fetched live from the real
// `/api/v1/players/*` and `/api/v1/hiscores/*` endpoints (see `HiscoresRoutes.kt`) - the only
// synthesized piece left is the xp-history chart's day-by-day distribution, since the server
// doesn't keep daily xp snapshots yet; its totals still add up to the account's real per-skill xp.

(function () {
  var API = "/api/v1";

  var BAND = ["var(--surface-panel)", "var(--umber-850)"];
  var TONE_BY_EVENT_TYPE = { skill: "gold", quest: "info", combat: "danger", account: "success" };
  var TONE_BY_DIFFICULTY = { novice: "success", intermediate: "info", experienced: "warning", master: "danger" };

  // How many days of daily xp-gain history to synthesize per profile, and how many trailing
  // days of it each chart range toggle shows.
  var HISTORY_DAYS = 730;
  var RANGE_DAYS = { week: 7, month: 30, year: 365, all: HISTORY_DAYS };
  var RANGE_LABEL = { week: "last 7 days", month: "last 30 days", year: "last 365 days", all: "all time" };

  // Populated below from window.VOID_SKILLS (see GameData.kt's `skillColors`), so the fixed
  // per-skill colours used by the xp chart live in one place instead of being duplicated here.
  var SKILL_COLORS = {};
  window.VOID_SKILLS.forEach(function (s) { SKILL_COLORS[s.name] = s.color; });
  var OTHER_COLOR = "#5a646b";

  function getJson(url) {
    return fetch(url).then(function (response) {
      if (!response.ok) {
        throw new Error("Request to " + url + " failed: " + response.status);
      }
      return response.json();
    });
  }

  function rng(seed) {
    var a = seed >>> 0;
    return function () {
      a += 0x6D2B79F5;
      var t = a;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  function hashSeed(name) {
    var h = 0;
    for (var i = 0; i < name.length; i++) h = (Math.imul(h, 31) + name.charCodeAt(i)) | 0;
    return h >>> 0;
  }

  var fmt = window.voidFmt;
  function abbrevXp(n) {
    if (n >= 1e9) return (n / 1e9).toFixed(2) + "B";
    if (n >= 1e6) return (n / 1e6).toFixed(1) + "M";
    if (n >= 1e3) return (n / 1e3).toFixed(1) + "K";
    return String(Math.round(n));
  }
  function shortXp(n) {
    var a = Math.abs(n);
    if (a >= 1e6) return (n / 1e6).toFixed(1) + "M";
    if (a >= 1e3) return (n / 1e3).toFixed(1) + "K";
    return String(Math.round(n));
  }
  function xpStamp(t, range) {
    var d = new Date(t);
    var D = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
    var M = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    if (range === "week") return D[d.getUTCDay()] + " " + d.getUTCDate() + " " + M[d.getUTCMonth()];
    if (range === "month") return d.getUTCDate() + " " + M[d.getUTCMonth()];
    return M[d.getUTCMonth()] + " " + d.getUTCFullYear();
  }
  function mmss(sec) {
    var total = Math.round(sec);
    var m = Math.floor(total / 60), s = total % 60;
    return m + ":" + String(s).padStart(2, "0");
  }
  function shortDate(iso) {
    if (!iso) return "—";
    try {
      return new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
    } catch (e) {
      return "—";
    }
  }
  function longDateTime(iso) {
    if (!iso) return "";
    try {
      return new Date(iso).toLocaleString("en-GB", { day: "numeric", month: "long", year: "numeric", hour: "numeric", minute: "2-digit" });
    } catch (e) {
      return "";
    }
  }
  function modeLabel(id) { return id ? id.charAt(0).toUpperCase() + id.slice(1) : ""; }
  function capitalize(s) { return s ? s.charAt(0).toUpperCase() + s.slice(1) : s; }
  function band(i) { return BAND[i % 2]; }
  function bossAbbr(name) { return name.split(" ").map(function (w) { return w[0]; }).join("").slice(0, 3).toUpperCase(); }

  // Synthesizes daily xp gains per skill over the last HISTORY_DAYS days: each skill trains in a
  // handful of random "active" windows on the timeline, gaining xp on most (not all) days within
  // them, scaled so the total roughly matches the skill's real, already-earned xp. There's no
  // record of when that xp was actually earned, so the day-by-day shape here is illustrative only.
  function buildXpHistory(skills, r) {
    var days = HISTORY_DAYS;
    var today = Date.now();
    var day = 86400000;
    var config = skills.map(function (sk) {
      var windowCount = 3 + Math.floor(r() * 5);
      var windows = [];
      for (var i = 0; i < windowCount; i++) {
        var start = Math.floor(r() * days);
        windows.push({ start: start, end: Math.min(days, start + 3 + Math.floor(r() * 21)) });
      }
      return { name: sk.name, windows: windows, rate: Math.max(300, sk.xp / (days * 0.12)) };
    });
    var history = [];
    for (var d = 0; d < days; d++) {
      var gains = {};
      for (var i = 0; i < config.length; i++) {
        var cfg = config[i];
        var active = cfg.windows.some(function (w) { return d >= w.start && d < w.end; });
        if (active && r() > 0.3) gains[cfg.name] = Math.round(cfg.rate * (0.4 + r() * 1.3));
      }
      history.push({ t: today - (days - 1 - d) * day, gains: gains });
    }
    return history;
  }

  function urlFor(view, profileName) {
    if (view === "profile") return "?player=" + encodeURIComponent(profileName);
    return window.location.pathname;
  }

  function buildSkillRow(s) {
    return {
      name: s.name, max: s.maxLevel, level: s.level, xp: s.xp,
      xpLabel: fmt(s.xp), rankLabel: s.rank ? "rank " + fmt(s.rank) : "unranked",
      icon: "images/skills/" + s.name.toLowerCase() + ".png",
    };
  }

  function buildQuestRow(q, i) {
    return {
      name: q.name,
      difficulty: capitalize(q.difficulty),
      tone: TONE_BY_DIFFICULTY[q.difficulty] || "info",
      duration: "—",
      date: shortDate(q.completedAt),
      band: band(i),
    };
  }

  function buildBossRow(b, i) {
    return {
      name: b.name, abbr: bossAbbr(b.name), kills: fmt(b.kills),
      fastest: b.fastestSeconds != null ? mmss(b.fastestSeconds) : "—",
      last: "—",
      band: band(i),
    };
  }

  function buildEventRow(e, i) {
    return {
      kind: capitalize(e.type),
      tone: TONE_BY_EVENT_TYPE[e.type] || "info",
      text: e.text,
      description: "",
      date: shortDate(e.occurredAt),
      exact: longDateTime(e.occurredAt),
      band: band(i),
    };
  }

  // Builds the `profile` object every panel in the template reads from a name plus the raw
  // responses of the five per-player endpoints (see `HiscoresRoutes.kt`'s `/players/{name}*`).
  function buildProfile(name, player, skillsResp, bossesResp, questsResp, eventsResp) {
    var skills = skillsResp.items.map(buildSkillRow);
    var completedQuests = questsResp.items
      .filter(function (q) { return q.status === "complete"; })
      .map(buildQuestRow);
    var bosses = bossesResp.items
      .slice()
      .sort(function (a, b) { return b.kills - a.kills; })
      .map(buildBossRow);
    var events = eventsResp.items.map(buildEventRow);
    var r = rng(hashSeed(name));

    return {
      name: player.name,
      rights: player.rights,
      mode: modeLabel(player.mode),
      joined: shortDate(player.joinedAt),
      totalLevel: player.totalLevel,
      combat: player.combatLevel,
      totalXpLabel: abbrevXp(player.totalXp),
      questPoints: player.questPoints,
      questPointsMax: player.questPointsMax,
      skills: skills,
      maxedCount: player.maxedSkills,
      xpHistory: buildXpHistory(skills, r),
      events: events,
      quests: completedQuests,
      questTotal: questsResp.total,
      bosses: bosses,
      bossKills: player.bossKills,
      milestones: player.milestones,
    };
  }

  var EMPTY_PROFILE = {
    name: "", rights: "none", mode: "", joined: "—", totalLevel: 0, combat: 0, totalXpLabel: "0",
    questPoints: 0, questPointsMax: 1, skills: [], maxedCount: 0,
    xpHistory: [{ t: Date.now(), gains: {} }],
    events: [], quests: [], questTotal: 0, bosses: [], bossKills: 0, milestones: [],
  };

  window.logApp = function () {
    return {
      view: "overview",
      profileName: "",
      query: "",
      filter: "All",
      sort: "level",
      xpRange: "month",
      xpHover: null,
      xpZoom: null,
      xpDragging: false,
      xpDragStart: null,
      xpDragEnd: null,

      profiles: {},
      topPlayers: [],
      searchResults: [],

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var player = params.get("player");
        if (player) {
          this.profileName = player;
          this.view = "profile";
        }
        history.replaceState({ view: this.view, profileName: this.profileName }, "", urlFor(this.view, this.profileName));

        if (this.view === "profile") {
          this.loadProfile(this.profileName);
        }
        this.refreshTopPlayers();
        this.refreshSearch();

        var self = this;
        window.addEventListener("popstate", function (e) {
          var s = e.state;
          if (!s) {
            self.view = "overview";
            return;
          }
          self.profileName = s.profileName;
          self.view = s.view;
          if (self.view === "profile") self.loadProfile(self.profileName);
        });
        this.$watch("query", function () { self.refreshSearch(); });
      },

      navigate: function (view, profileName) {
        this.view = view;
        if (profileName !== undefined) this.profileName = profileName;
        if (view === "profile") this.loadProfile(this.profileName);
        history.pushState({ view: view, profileName: this.profileName }, "", urlFor(view, this.profileName));
      },

      loadProfile: function (name) {
        if (!name || this.profiles[name]) return;
        var self = this;
        var base = API + "/players/" + encodeURIComponent(name);
        Promise.all([
          getJson(base),
          getJson(base + "/skills"),
          getJson(base + "/bosses"),
          getJson(base + "/quests?status=all"),
          getJson(base + "/events?pageSize=30"),
        ]).then(function (results) {
          self.profiles[name] = buildProfile(name, results[0], results[1], results[2], results[3], results[4]);
        }).catch(function () {
          self.profiles[name] = null;
        });
      },

      get profile() { return this.profiles[this.profileName] || EMPTY_PROFILE; },
      get profileMissing() { return this.profiles[this.profileName] === null; },
      pick: function (name) { this.query = ""; this.navigate("profile", name); },
      backToOverview: function () { this.navigate("overview"); },

      refreshTopPlayers: function () {
        var self = this;
        getJson(API + "/hiscores/overall?pageSize=12").then(function (data) {
          self.topPlayers = data.items.map(function (row) {
            return { name: row.name, mode: modeLabel(row.mode), total: fmt(row.totalLevel) };
          });
        }).catch(function () { self.topPlayers = []; });
      },
      get overviewPlayers() { return this.topPlayers; },

      refreshSearch: function () {
        var self = this;
        var params = new URLSearchParams();
        var q = this.query.trim();
        if (q) params.set("q", q);
        params.set("limit", "8");
        getJson(API + "/players/search?" + params.toString()).then(function (data) {
          self.searchResults = data.items.map(function (p) {
            return { name: p.name, meta: modeLabel(p.mode) + (p.rank ? " · rank " + fmt(p.rank) : ""), total: fmt(p.totalLevel) };
          });
        }).catch(function () { self.searchResults = []; });
      },
      get results() { return this.searchResults; },
      get noResults() { return this.query.trim().length > 0 && this.searchResults.length === 0; },

      get filterTabs() {
        var self = this;
        return ["All", "Skill", "Quest", "Combat", "Account"].map(function (label) {
          return {
            label: label,
            active: self.filter === label,
            onClick: function () { self.filter = label; },
          };
        });
      },
      get visibleEvents() {
        if (this.profile.events.length === 0) {
          return [{
            kind: "", tone: "info", text: "No recent events",
            description: "I don't have any recent events yet. I need to do more adventuring.",
            date: "", exact: "", band: band(0),
          }];
        }
        var filter = this.filter;
        return this.profile.events.filter(function (e) { return filter === "All" || e.kind === filter; });
      },

      get sortTabs() {
        var self = this;
        return [["normal", "Skill order"], ["level", "By level"], ["alphabetical", "A–Z"]].map(function (t) {
          return { key: t[0], label: t[1], active: self.sort === t[0], onClick: function () { self.sort = t[0]; } };
        });
      },
      get sortedSkills() {
        var sort = this.sort;
        if (sort === "normal") return this.profile.skills.slice();
        return this.profile.skills.slice().sort(function (a, b) {
          return sort === "alphabetical" ? a.name.localeCompare(b.name) : b.level - a.level;
        });
      },

      // Length/offset (into the full xp-history array) of the window currently on screen — either
      // a preset range's trailing N days, or a drag-selected zoom slice.
      xpWindowLength: function () {
        return this.xpZoom ? this.xpZoom.end - this.xpZoom.start + 1 : RANGE_DAYS[this.xpRange];
      },
      xpWindowOffset: function () {
        return this.xpZoom ? this.xpZoom.start : this.profile.xpHistory.length - RANGE_DAYS[this.xpRange];
      },
      xpIndexAt: function (e) {
        var r = e.currentTarget.getBoundingClientRect();
        var vb = ((e.clientX - r.left) / r.width) * 920;
        var n = this.xpWindowLength();
        var step = (920 - 66 - 12) / Math.max(1, n - 1);
        return Math.max(0, Math.min(n - 1, Math.round((vb - 66) / step)));
      },
      onXpChartDown: function (e) {
        var i = this.xpIndexAt(e);
        this.xpDragging = true;
        this.xpDragStart = i;
        this.xpDragEnd = i;
      },
      onXpChartMove: function (e) {
        var i = this.xpIndexAt(e);
        if (this.xpDragging) {
          this.xpDragEnd = i;
        } else if (i !== this.xpHover) {
          this.xpHover = i;
        }
      },
      // Bound with .window so a drag that ends outside the chart still zooms — the mouse doesn't
      // have to be released back over the SVG.
      onXpChartUp: function () {
        if (!this.xpDragging) return;
        var a = Math.min(this.xpDragStart, this.xpDragEnd);
        var b = Math.max(this.xpDragStart, this.xpDragEnd);
        this.xpDragging = false;
        this.xpDragStart = null;
        this.xpDragEnd = null;
        if (b - a >= 1) {
          var offset = this.xpWindowOffset();
          this.xpZoom = { start: offset + a, end: offset + b };
          this.xpHover = null;
        }
      },
      onXpChartLeave: function () { this.xpHover = null; },
      resetXpZoom: function () { this.xpZoom = null; this.xpHover = null; },

      get xpRangeTabs() {
        var self = this;
        return [["week", "Week"], ["month", "Month"], ["year", "Year"], ["all", "All"]].map(function (t) {
          return {
            key: t[0], label: t[1], active: !self.xpZoom && self.xpRange === t[0],
            onClick: function () { self.xpRange = t[0]; self.xpHover = null; self.xpZoom = null; },
          };
        });
      },
      get xpChartData() {
        var range = this.xpRange;
        var full = this.profile.xpHistory;
        var zoom = this.xpZoom;
        var pts = zoom ? full.slice(zoom.start, zoom.end + 1) : full.slice(Math.max(0, full.length - RANGE_DAYS[range]));
        var m = pts.length;

        // Every skill that gained xp anywhere in the visible range gets its own stacked layer in
        // its fixed colour (SKILL_COLORS covers all 25, so nothing needs bucketing into a generic
        // "Other" band — that grey catch-all was swallowing most of the chart on wider ranges,
        // where more skills contribute at least a little). Order largest-total-first so the
        // biggest bands sit at the bottom.
        var totals = {};
        pts.forEach(function (day) {
          Object.keys(day.gains).forEach(function (name) { totals[name] = (totals[name] || 0) + day.gains[name]; });
        });
        var layerNames = Object.keys(totals).sort(function (a, b) { return totals[b] - totals[a]; });

        var stacks = pts.map(function (day) {
          var cum = 0, tops = [];
          layerNames.forEach(function (name) {
            cum += day.gains[name] || 0;
            tops.push(cum);
          });
          return tops;
        });
        var maxTotal = stacks.reduce(function (mx, s) { return Math.max(mx, s[s.length - 1] || 0); }, 0) || 1;

        var W = 920, PL = 66, PR = 12, PT = 14, PB = 34, H = 300;
        function X(i) { return PL + (i * (W - PL - PR)) / Math.max(1, m - 1); }
        function Y(v) { return PT + (1 - v / maxTotal) * (H - PT - PB); }

        var layers = layerNames.map(function (name, li) {
          var top = stacks.map(function (s, i) { return (i ? "L" : "M") + X(i).toFixed(1) + " " + Y(s[li]).toFixed(1); }).join(" ");
          var bottom = stacks.slice().reverse().map(function (s, i) {
            var idx = m - 1 - i;
            var v = li ? s[li - 1] : 0;
            return "L" + X(idx).toFixed(1) + " " + Y(v).toFixed(1);
          }).join(" ");
          return { name: name, color: SKILL_COLORS[name] || OTHER_COLOR, d: top + " " + bottom + " Z" };
        });
        var layersSvg = layers.map(function (l) {
          return '<path d="' + l.d + '" style="fill:' + l.color + ';opacity:.88"></path>';
        }).join("");

        var grid = [0, 0.25, 0.5, 0.75, 1].map(function (f) {
          var y = PT + f * (H - PT - PB);
          return { y: y.toFixed(1), top: ((y / H) * 100).toFixed(2) + "%", label: shortXp(maxTotal - f * maxTotal) };
        });
        var gridSvg = grid.map(function (g) {
          return '<line x1="66" x2="908" y1="' + g.y + '" y2="' + g.y + '" style="stroke:var(--umber-700);stroke-width:1"></line>';
        }).join("");
        var xlabels = [0, 0.25, 0.5, 0.75, 1].map(function (f) {
          var i = Math.round(f * (m - 1));
          return { left: ((X(i) / W) * 100).toFixed(2) + "%", label: pts.length ? xpStamp(pts[i].t, range) : "" };
        });

        var hoverIndex = this.xpHover !== null ? Math.min(m - 1, this.xpHover) : m - 1;
        var hoverDay = pts[hoverIndex] || { t: Date.now(), gains: {} };
        var breakdown = Object.keys(hoverDay.gains).map(function (name) {
          return { name: name, xpLabel: fmt(hoverDay.gains[name]), color: SKILL_COLORS[name] || OTHER_COLOR };
        }).sort(function (a, b) { return parseInt(b.xpLabel.replace(/,/g, "")) - parseInt(a.xpLabel.replace(/,/g, "")); });
        var dayTotal = breakdown.reduce(function (s, b) { return s + parseInt(b.xpLabel.replace(/,/g, "")); }, 0);
        var hoverDotsSvg = layers.map(function (l, li) {
          return '<circle cx="' + X(hoverIndex).toFixed(1) + '" cy="' + Y(stacks[hoverIndex] ? stacks[hoverIndex][li] : 0).toFixed(1) + '" r="2.5" style="fill:' + l.color + '"></circle>';
        }).join("");

        var selectionSvg = "";
        if (this.xpDragging && this.xpDragStart !== null && this.xpDragEnd !== null && this.xpDragStart !== this.xpDragEnd) {
          var sa = X(Math.min(this.xpDragStart, this.xpDragEnd));
          var sb = X(Math.max(this.xpDragStart, this.xpDragEnd));
          selectionSvg = '<rect x="' + sa.toFixed(1) + '" y="' + PT + '" width="' + (sb - sa).toFixed(1) + '" height="' + (H - PT - PB) +
            '" style="fill:rgba(240,198,103,.14);stroke:var(--gold-400);stroke-width:1"></rect>';
        }

        return {
          eyebrow: zoom ? (pts.length ? xpStamp(pts[0].t, "year") + " – " + xpStamp(pts[m - 1].t, "year") : "") : RANGE_LABEL[range],
          zoomed: !!zoom,
          legend: layers.map(function (l) { return { name: l.name, color: l.color }; }),
          layersSvg: layersSvg, gridSvg: gridSvg, grid: grid, xlabels: xlabels,
          hovering: this.xpHover !== null && !this.xpDragging, hx: X(hoverIndex).toFixed(1),
          hoverLeft: ((X(hoverIndex) / W) * 100).toFixed(2) + "%",
          hoverDotsSvg: hoverDotsSvg,
          stamp: pts.length ? xpStamp(hoverDay.t, range) : "",
          dayTotal: fmt(dayTotal) + " xp",
          breakdown: breakdown,
          selectionSvg: selectionSvg,
          dragging: this.xpDragging && this.xpDragStart !== null && this.xpDragEnd !== null && this.xpDragStart !== this.xpDragEnd,
        };
      },
    };
  };
})();
