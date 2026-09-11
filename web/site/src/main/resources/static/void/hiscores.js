// Hiscores page data + Alpine component. A self-contained mock dataset stands in for the
// hiscores API: deterministic pseudo-random players so the page has something realistic to
// search, sort, filter, compare and paginate without a server round-trip.

(function () {
  var SKILLS = [
    ["Attack", 99], ["Defence", 99], ["Strength", 99], ["Constitution", 99], ["Ranged", 99],
    ["Prayer", 99], ["Magic", 99], ["Cooking", 99], ["Woodcutting", 99], ["Fletching", 99],
    ["Fishing", 99], ["Firemaking", 99], ["Crafting", 99], ["Smithing", 99], ["Mining", 99],
    ["Herblore", 99], ["Agility", 99], ["Thieving", 99], ["Slayer", 99], ["Farming", 99],
    ["Runecrafting", 99], ["Hunter", 99], ["Construction", 99], ["Summoning", 99], ["Dungeoneering", 120],
  ];
  var BOSSES = [
    ["Ashen Wyrm", 214], ["Gravelord Thane", 332], ["The Hollow King", 488], ["Sunken Leviathan", 276],
    ["Mother of Blades", 191], ["Warden of Cinders", 405], ["Rot-Priest Malgrim", 148], ["Frostbound Colossus", 560],
    ["Twin Serpents of Ord", 233], ["Blightmaw", 127], ["The Pale Choir", 372], ["Verdant Horror", 302],
  ];
  var MODES = [["Standard", null], ["Ironman", "info"], ["Hardcore", "danger"], ["Ultimate", "gold"]];
  var TEAMS = ["All", "Solo", "2 players", "3 players", "4 players"];
  var PRE = ["Zeph", "Kald", "Mourn", "Vex", "Thal", "Ordin", "Brack", "Sable", "Cind", "Grim", "Halden", "Iron", "Jarl", "Kestrel", "Lorn", "Mirek", "Nyx", "Osric", "Pyre", "Quill", "Ruvan", "Skarn", "Torv", "Ulric", "Varr", "Wren", "Yorick", "Zarn", "Ashen", "Brine"];
  var SUF = ["", "wyrd", "ic", "ara", "os", "ley", "thorn", "stone", "mir", "ax", "en", "iss", "ord", "ric", "vale"];

  var TONE = {
    info: { bg: "var(--feedback-info-bg)", fg: "var(--feedback-info)", bd: "var(--steel-600)" },
    danger: { bg: "var(--feedback-danger-bg)", fg: "var(--feedback-danger)", bd: "var(--ember-600)" },
    gold: { bg: "rgba(224,174,60,.14)", fg: "var(--gold-300)", bd: "var(--gold-600)" },
  };

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

  function xpTable() {
    var t = [0, 0], acc = 0;
    for (var l = 1; l <= 125; l++) {
      acc += Math.floor(l + 300 * Math.pow(2, l / 7));
      t[l + 1] = Math.floor(acc / 4);
    }
    return t;
  }
  var XPT = xpTable();

  function levelFromXp(xp, max) {
    var l = 1;
    while (l < max && XPT[l + 1] <= xp) l++;
    return l;
  }

  function fmt(n) { return Math.round(n).toLocaleString("en-US"); }
  function abbrev(n) {
    if (n >= 1e9) return (n / 1e9).toFixed(2) + "B";
    if (n >= 1e6) return (n / 1e6).toFixed(1) + "M";
    if (n >= 1e3) return (n / 1e3).toFixed(1) + "K";
    return String(Math.round(n));
  }
  function mmss(sec) {
    var m = Math.floor(sec / 60), s = Math.round(sec % 60);
    return m + ":" + String(s).padStart(2, "0");
  }

  function buildPlayers() {
    var out = [];
    for (var i = 0; i < 60; i++) {
      var r = rng(i * 9176 + 13);
      var name = PRE[i % PRE.length] + SUF[(i * 7) % SUF.length] + (i >= PRE.length ? String(60 + i) : "");
      var mode = i % 13 === 4 ? MODES[2] : i % 7 === 3 ? MODES[1] : i % 29 === 11 ? MODES[3] : MODES[0];
      var strength = Math.pow(0.985, i) * (0.9 + r() * 0.14);
      var skills = {}, totalXp = 0, totalLevel = 0;
      SKILLS.forEach(function (s) {
        var capXp = XPT[s[1] + 1];
        var xp = Math.max(1200, Math.min(capXp, Math.round(capXp * Math.min(1.12, strength * (0.55 + r() * 0.75)))));
        var lvl = levelFromXp(xp, s[1]);
        skills[s[0]] = { xp: xp, level: lvl };
        totalXp += xp;
        totalLevel += lvl;
      });
      var bosses = {};
      BOSSES.forEach(function (b, j) {
        var kc = Math.round(Math.max(0, (1400 * strength) * (0.15 + r() * 0.95)) / (1 + j * 0.08));
        bosses[b[0]] = { kc: kc, best: kc > 0 ? b[1] * (0.82 + r() * 0.6) : null };
      });
      out.push({
        name: name, mode: mode[0], modeTone: mode[1], skills: skills, totalXp: totalXp, totalLevel: totalLevel,
        bosses: bosses, joined: "joined " + (2024 + (i % 3)), world: "world " + (1 + (i * 7) % 42),
      });
    }
    out.sort(function (a, b) { return b.totalXp - a.totalXp; });
    out.forEach(function (p, i) { p.rank = i + 1; });
    return out;
  }

  var PLAYERS = buildPlayers();

  function band(i) { return i % 2 === 0 ? "var(--surface-panel)" : "var(--umber-850)"; }
  function rankColor(r) {
    return r === 1 ? "var(--gold-400)" : r === 2 ? "var(--gold-300)" : r === 3 ? "var(--gold-200)" :
      r <= 10 ? "var(--parch-200)" : "var(--text-faint)";
  }
  function skillIcon(name) { return "void/images/skills/" + (name === "Constitution" ? "hitpoints" : name.toLowerCase()) + ".png"; }
  function byName(n) { return PLAYERS.filter(function (p) { return p.name === n; })[0] || PLAYERS[0]; }
  function delta(a, b, unit) {
    if (a === b) return { deltaText: "even", deltaFg: "var(--text-faint)", deltaBg: "transparent", deltaBd: "var(--border-panel)" };
    var d = abbrev(Math.abs(a - b)) + (unit ? " " + unit : "");
    return {
      deltaText: (a > b ? "← " : "") + d + (a > b ? "" : " →"),
      deltaFg: "var(--gold-200)", deltaBg: "rgba(224,174,60,.12)", deltaBd: "var(--gold-600)",
    };
  }

  var SEARCH_SORTS = ["level", "rank", "name"];

  function urlFor(state) {
    var params = new URLSearchParams();
    if (state.view && state.view !== "overall") params.set("view", state.view);
    if (state.view === "skills" && state.skill) params.set("skill", state.skill);
    if (state.view === "bosses" && state.boss) params.set("boss", state.boss);
    if (state.view === "player" && state.profile) params.set("player", state.profile);
    if (state.view === "search") {
      if (state.searchQuery) params.set("q", state.searchQuery);
      if (state.searchSort && state.searchSort !== "level") params.set("sort", state.searchSort);
    }
    var qs = params.toString();
    return window.location.pathname + (qs ? "?" + qs : "");
  }

  function pageInfo(total, per, page) {
    var pages = Math.max(1, Math.ceil(total / per)), p = Math.min(page, pages - 1);
    return {
      page: p, pages: pages, start: p * per, end: p * per + per,
      label: total ? "Showing " + fmt(p * per + 1) + "–" + fmt(Math.min(total, p * per + per)) + " of " + fmt(total) : "No results",
      prevDisabled: p === 0, nextDisabled: p >= pages - 1,
    };
  }

  window.HISCORES_SKILLS = SKILLS;
  window.HISCORES_BOSSES = BOSSES;
  window.HISCORES_TEAMS = TEAMS;

  window.hiscoresApp = function () {
    return {
      view: "overall", skill: "Attack", boss: BOSSES[0][0], mode: "All", team: "All", query: "",
      page: 0, skillPage: 0, kcPage: 0, timePage: 0, searchPage: 0, perPage: 25,
      nameA: "", nameB: "", profile: PLAYERS[0].name,
      searchQuery: "", searchSort: "level", navDepth: 0,
      combo: null, comboQ: "",

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var skill = params.get("skill");
        var boss = params.get("boss");
        var player = params.get("player");
        var view = params.get("view");
        var q = params.get("q");
        var sort = params.get("sort");
        if (player) this.profile = player;
        if (skill && SKILLS.some(function (s) { return s[0] === skill; })) this.skill = skill;
        if (boss && BOSSES.some(function (b) { return b[0] === boss; })) this.boss = boss;
        if (q) {
          this.query = q;
          this.searchQuery = q;
        }
        if (sort && SEARCH_SORTS.indexOf(sort) >= 0) this.searchSort = sort;
        if (view) {
          this.view = view;
        } else if (q) {
          this.view = "search";
        } else if (boss) {
          this.view = "bosses";
        } else if (skill) {
          this.view = "skills";
        } else if (player) {
          this.view = "player";
        }

        var state = this.historyState();
        history.replaceState(state, "", urlFor(state));

        var self = this;
        window.addEventListener("popstate", function (e) {
          self.navDepth = Math.max(0, self.navDepth - 1);
          var s = e.state;
          if (!s) {
            self.view = "overall";
            return;
          }
          if (s.skill) self.skill = s.skill;
          if (s.boss) self.boss = s.boss;
          if (s.profile) self.profile = s.profile;
          if (s.searchQuery !== undefined) {
            self.searchQuery = s.searchQuery;
            self.query = s.searchQuery;
          }
          if (s.searchSort) self.searchSort = s.searchSort;
          self.view = s.view || "overall";
          if (self.view !== "search") self.query = "";
        });
      },

      historyState: function () {
        return {
          view: this.view, skill: this.skill, boss: this.boss, profile: this.profile,
          searchQuery: this.searchQuery, searchSort: this.searchSort,
        };
      },

      navigate: function (patch) {
        Object.assign(this, patch);
        if (this.view !== "search") this.query = "";
        this.navDepth++;
        var state = this.historyState();
        history.pushState(state, "", urlFor(state));
      },

      open: function (name) { this.navigate({ profile: name, view: "player" }); },
      /** Returns to wherever the visitor came from (search results, a leaderboard, …) rather than always the overall view. */
      back: function () {
        if (this.navDepth > 0) {
          this.navDepth--;
          history.back();
        } else {
          this.navigate({ view: "overall" });
        }
      },
      compareThis: function () { this.navigate({ nameA: this.profile, view: "compare" }); },
      search: function () {
        var q = this.query.trim();
        if (!q) return;
        this.searchPage = 0;
        this.navigate({ view: "search", searchQuery: q });
      },
      setSearchSort: function (sort) {
        this.searchSort = sort;
        this.searchPage = 0;
      },
      prevPage: function (key) { var info = this.pagerFor(key); this[key] = Math.max(0, info.page - 1); },
      nextPage: function (key) { var info = this.pagerFor(key); this[key] = Math.min(info.pages - 1, info.page + 1); },
      pagerFor: function (key) {
        if (key === "page") return this.overallPager;
        if (key === "skillPage") return this.skillPager;
        if (key === "kcPage") return this.bossKcPager;
        if (key === "searchPage") return this.searchPager;
        return this.bossTimePager;
      },
      fmtXp: function (n) { return fmt(n); },

      comboFocus: function (side) { this.combo = side; this.comboQ = ""; },
      comboBlur: function () { this.combo = null; this.comboQ = ""; },
      pickCombo: function (side, name) {
        if (side === "a") this.nameA = name; else this.nameB = name;
        this.combo = null;
        this.comboQ = "";
      },
      comboValue: function (side) {
        if (this.combo !== side) return side === "a" ? this.nameA : this.nameB;
        return this.comboQ;
      },
      comboResults: function (side) {
        if (this.combo !== side) return [];
        var q = this.comboQ.trim().toLowerCase();
        return PLAYERS.filter(function (p) { return !q || p.name.toLowerCase().indexOf(q) >= 0; })
          .slice(0, 25)
          .map(function (p) { return { name: p.name, meta: "rank " + fmt(p.rank) + " · " + abbrev(p.totalXp) + " xp" }; });
      },

      get pool() {
        var self = this;
        return PLAYERS.filter(function (p) { return self.mode === "All" || p.mode === self.mode; });
      },
      get overallEyebrow() {
        return fmt(this.pool.length) + " accounts" + (this.mode !== "All" ? " · " + this.mode : "");
      },
      get overallPager() { return pageInfo(this.pool.length, this.perPage, this.page); },
      get overallRows() {
        var self = this, p = this.overallPager;
        return this.pool.slice(p.start, p.end).map(function (pl, i) {
          var tone = pl.modeTone ? TONE[pl.modeTone] : null;
          return {
            rank: pl.rank, name: pl.name, mode: pl.mode, showBadge: !!tone,
            badgeBg: tone ? tone.bg : "", badgeFg: tone ? tone.fg : "", badgeBd: tone ? tone.bd : "",
            totalLevel: fmt(pl.totalLevel), totalXp: self.fmtXp(pl.totalXp),
            bg: band(i), rankColor: rankColor(pl.rank),
          };
        });
      },

      get searchPool() {
        var q = this.searchQuery.trim().toLowerCase();
        if (!q) return [];
        return PLAYERS.filter(function (p) { return p.name.toLowerCase().indexOf(q) >= 0; });
      },
      get searchSorted() {
        var sort = this.searchSort, list = this.searchPool.slice();
        if (sort === "name") list.sort(function (a, b) { return a.name.localeCompare(b.name); });
        else if (sort === "rank") list.sort(function (a, b) { return a.rank - b.rank; });
        else list.sort(function (a, b) { return b.totalLevel - a.totalLevel || b.totalXp - a.totalXp; });
        return list;
      },
      get searchEyebrow() {
        var n = this.searchSorted.length, q = this.searchQuery.trim();
        return fmt(n) + (n === 1 ? " match" : " matches") + (q ? " for “" + q + "”" : "");
      },
      get searchPager() { return pageInfo(this.searchSorted.length, this.perPage, this.searchPage); },
      get searchRows() {
        var self = this, p = this.searchPager;
        return this.searchSorted.slice(p.start, p.end).map(function (pl, i) {
          var tone = pl.modeTone ? TONE[pl.modeTone] : null;
          return {
            rank: pl.rank, name: pl.name, mode: pl.mode, showBadge: !!tone,
            badgeBg: tone ? tone.bg : "", badgeFg: tone ? tone.fg : "", badgeBd: tone ? tone.bd : "",
            totalLevel: fmt(pl.totalLevel), totalXp: self.fmtXp(pl.totalXp),
            bg: band(i), rankColor: rankColor(pl.rank),
          };
        });
      },

      get skillMax() { return (SKILLS.filter(function (s) { return s[0] === this.skill; }, this)[0] || SKILLS[0])[1]; },
      get skillEyebrow() { return this.skill.toUpperCase() + " · ranked by experience · cap level " + this.skillMax; },
      get skillPager() {
        return pageInfo(PLAYERS.length, this.perPage, this.skillPage);
      },
      get skillRows() {
        var self = this, skill = this.skill, p = this.skillPager;
        var sorted = PLAYERS.slice().sort(function (a, b) { return b.skills[skill].xp - a.skills[skill].xp; });
        return sorted.slice(p.start, p.end).map(function (pl, i) {
          var rank = p.start + i + 1;
          return { rank: rank, name: pl.name, level: pl.skills[skill].level, xp: self.fmtXp(pl.skills[skill].xp), bg: band(i), rankColor: rankColor(rank) };
        });
      },

      get compareA() { return this.nameA ? byName(this.nameA) : null; },
      get compareB() { return this.nameB ? byName(this.nameB) : null; },
      get compareReady() { return !!(this.compareA && this.compareB); },
      get compareEyebrow() {
        if (!this.compareReady) return "Pick two players to compare";
        return this.compareA.name + " vs " + this.compareB.name;
      },
      get compareBlankText() {
        if (!this.nameA && !this.nameB) return "Search for two players above to compare their stats.";
        return "Search for a second player above to compare.";
      },
      get compareRows() {
        if (!this.compareReady) return [];
        var A = this.compareA, B = this.compareB;
        return SKILLS.map(function (s, i) {
          var a = A.skills[s[0]], b = B.skills[s[0]];
          var win = a.xp === b.xp ? 0 : a.xp > b.xp ? -1 : 1;
          var d = delta(a.xp, b.xp, "xp");
          return Object.assign({
            skill: s[0], icon: skillIcon(s[0]), aLevel: a.level, bLevel: b.level, aXp: fmt(a.xp), bXp: fmt(b.xp),
            aColor: win < 0 ? "var(--gold-300)" : "var(--text-faint)", bColor: win > 0 ? "var(--gold-300)" : "var(--text-faint)",
            bg: band(i),
          }, d);
        });
      },
      get compareBossRows() {
        if (!this.compareReady) return [];
        var A = this.compareA, B = this.compareB;
        return BOSSES.map(function (b, i) {
          var a = A.bosses[b[0]].kc, c = B.bosses[b[0]].kc;
          return Object.assign({
            boss: b[0], aKc: fmt(a), bKc: fmt(c),
            aColor: a >= c ? "var(--gold-300)" : "var(--text-faint)", bColor: c >= a ? "var(--gold-300)" : "var(--text-faint)",
            bg: band(i),
          }, delta(a, c, "kills"));
        });
      },
      get compareSummary() {
        if (!this.compareReady) return [];
        var A = this.compareA, B = this.compareB;
        var bossKcA = BOSSES.reduce(function (t, b) { return t + A.bosses[b[0]].kc; }, 0);
        var bossKcB = BOSSES.reduce(function (t, b) { return t + B.bosses[b[0]].kc; }, 0);
        var skillWins = this.compareRows.filter(function (r) { return r.aColor === "var(--gold-300)"; }).length;
        return [
          { label: "TOTAL EXPERIENCE", leader: A.totalXp >= B.totalXp ? A.name : B.name, detail: "+" + fmt(Math.abs(A.totalXp - B.totalXp)) + " xp" },
          { label: "TOTAL LEVEL", leader: A.totalLevel >= B.totalLevel ? A.name : B.name, detail: fmt(A.totalLevel) + " · " + fmt(B.totalLevel) },
          { label: "SKILLS AHEAD", leader: skillWins >= 13 ? A.name : B.name, detail: skillWins + " · " + (25 - skillWins) + " of 25" },
          { label: "BOSS KILLS", leader: bossKcA >= bossKcB ? A.name : B.name, detail: fmt(bossKcA) + " · " + fmt(bossKcB) },
        ];
      },

      get bossBase() { return (BOSSES.filter(function (b) { return b[0] === this.boss; }, this)[0] || BOSSES[0])[1]; },
      get bossKcPager() { return pageInfo(PLAYERS.length, 10, this.kcPage); },
      get bossKcRows() {
        var boss = this.boss, p = this.bossKcPager;
        var sorted = PLAYERS.slice().sort(function (a, b) { return b.bosses[boss].kc - a.bosses[boss].kc; });
        return sorted.slice(p.start, p.end).map(function (pl, i) {
          var rank = p.start + i + 1;
          return { rank: rank, name: pl.name, kc: fmt(pl.bosses[boss].kc), bg: band(i), rankColor: rankColor(rank) };
        });
      },
      get bossTimeAll() {
        var base = this.bossBase;
        return PLAYERS.slice(0, 60).map(function (pl, i) {
          var size = 1 + ((i * 5 + base) % 4);
          return { name: pl.name, size: size, team: size === 1 ? "Solo" : size + " players", seconds: base * (0.52 + i * 0.03) / (1 + (size - 1) * 0.16) };
        }).sort(function (a, b) { return a.seconds - b.seconds; });
      },
      get bossTimePager() {
        var team = this.team;
        var filtered = this.bossTimeAll.filter(function (e) { return team === "All" || e.team === team; });
        return pageInfo(filtered.length, 10, this.timePage);
      },
      get bossTimeRows() {
        var team = this.team, p = this.bossTimePager;
        var filtered = this.bossTimeAll.filter(function (e) { return team === "All" || e.team === team; });
        return filtered.slice(p.start, p.end).map(function (e, i) {
          var rank = p.start + i + 1;
          return { rank: rank, name: e.name, team: e.team, time: mmss(e.seconds), bg: band(i), rankColor: rankColor(rank) };
        });
      },

      get profilePlayer() { return byName(this.profile); },
      get profileSkills() {
        var P = this.profilePlayer;
        return SKILLS.map(function (s) {
          var level = P.skills[s[0]].level, max = s[1];
          return { name: s[0], icon: skillIcon(s[0]), level: level, max: max, percent: Math.round((level * 100) / max) };
        });
      },
      get profileBosses() {
        var P = this.profilePlayer;
        return BOSSES.map(function (b, i) {
          var e = P.bosses[b[0]];
          return {
            boss: b[0], kc: fmt(e.kc), best: e.best ? mmss(e.best) : "—",
            rank: e.kc > 0 ? "rank " + fmt(1 + ((i * 37 + P.rank * 11) % 9000)) : "unranked", bg: band(i),
          };
        });
      },
    };
  };
})();
