// Hiscores page data + Alpine component. Every table is fetched live from the selected world's
// `/api/v1/hiscores/*` and `/api/v1/players/*` endpoints - there is no mock dataset here. With no
// world selected, or the selected one offline, every table is left empty.

(function () {
  var API = "/api/v1";

  // Populated by GameData.script() (see Hiscores.kt) as window.VOID_SKILLS/VOID_BOSSES, so the
  // skill/boss lists live in one place (GameData.kt) instead of being duplicated here.
  var SKILLS = window.VOID_SKILLS.map(function (s) { return s.name; });
  var BOSSES = window.VOID_BOSSES;
  var TEAM_SIZE_BY_LABEL = { "Solo": 1, "2 players": 2, "3 players": 3, "4 players": 4 };
  var MODE_TONE = {
    skiller: { bg: "rgba(224,174,60,.14)", fg: "var(--gold-300)", bd: "var(--gold-600)" },
    pure: { bg: "var(--feedback-danger-bg)", fg: "var(--feedback-danger)", bd: "var(--ember-600)" },
  };
  var SEARCH_SORTS = ["level", "rank", "name"];
  var PAGE_FETCH = {
    page: "fetchOverall", skillPage: "fetchSkill", kcPage: "fetchBossKills",
    timePage: "fetchBossTimes", searchPage: "fetchSearch",
  };

  var fmt = window.voidFmt;
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
  function modeLabel(id) { return id ? id.charAt(0).toUpperCase() + id.slice(1) : ""; }
  function skillIcon(id) { return "images/skills/" + id + ".png"; }
  function bossAbbr(name) { return name.split(" ").map(function (w) { return w[0]; }).join("").slice(0, 3).toUpperCase(); }
  // "TzTok-Jad" -> "tz_tok_jad", "King Black Dragon" -> "king_black_dragon", "K'ril Tsutsaroth" -> "kril_tsutsaroth".
  function bossIcon(name) {
    var file = name.replace(/'/g, "").replace(/([a-z])([A-Z])/g, "$1_$2").toLowerCase().replace(/[^a-z0-9]+/g, "_").replace(/^_|_$/g, "");
    return "images/boss/" + file + ".png";
  }
  function formatDate(iso) {
    if (!iso) return "";
    try {
      return new Date(iso).toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric" });
    } catch (e) {
      return "";
    }
  }
  function formatUpdated(iso) {
    try {
      return "Updated " + new Date(iso).toLocaleString("en-US", {
        day: "numeric", month: "long", year: "numeric", hour: "2-digit", minute: "2-digit",
        timeZone: "UTC", timeZoneName: "short",
      });
    } catch (e) {
      return "";
    }
  }

  // Every request goes to the world selected in the navbar (see worlds.js), never this site's own origin.
  var getJson = window.voidWorldJson;

  function band(i) { return i % 2 === 0 ? "var(--surface-panel)" : "var(--umber-850)"; }
  function rankColor(r) {
    return r === 1 ? "var(--gold-400)" : r === 2 ? "var(--gold-300)" : r === 3 ? "var(--gold-200)" :
      r <= 10 ? "var(--parch-200)" : "var(--text-faint)";
  }
  function delta(a, b, unit) {
    if (a === b) return { deltaText: "even", deltaFg: "var(--text-faint)", deltaBg: "transparent", deltaBd: "var(--border-panel)" };
    var d = abbrev(Math.abs(a - b)) + (unit ? " " + unit : "");
    return {
      deltaText: (a > b ? "← " : "") + d + (a > b ? "" : " →"),
      deltaFg: "var(--gold-200)", deltaBg: "rgba(224,174,60,.12)", deltaBd: "var(--gold-600)",
    };
  }
  function decorateRankedRow(row, i) {
    var tone = MODE_TONE[row.mode];
    return {
      rank: row.rank, name: row.name, mode: modeLabel(row.mode), showBadge: !!tone,
      badgeBg: tone ? tone.bg : "", badgeFg: tone ? tone.fg : "", badgeBd: tone ? tone.bd : "",
      totalLevel: fmt(row.totalLevel), totalXp: fmt(row.totalXp),
      bg: band(i), rankColor: rankColor(row.rank),
    };
  }
  function derivePager(pagination) {
    if (!pagination) return { page: 0, pages: 1, label: "No results", prevDisabled: true, nextDisabled: true };
    var from = pagination.total ? pagination.page * pagination.pageSize + 1 : 0;
    var to = Math.min(pagination.total, pagination.page * pagination.pageSize + pagination.pageSize);
    return {
      page: pagination.page, pages: Math.max(1, pagination.totalPages),
      label: pagination.total ? "Showing " + fmt(from) + "–" + fmt(to) + " of " + fmt(pagination.total) : "No results",
      prevDisabled: !pagination.hasPrevious, nextDisabled: !pagination.hasNext,
    };
  }

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

  window.hiscoresApp = function () {
    return {
      view: "overall", skill: "Attack", boss: BOSSES[0].id, mode: "all", team: "All", query: "",
      page: 0, skillPage: 0, kcPage: 0, timePage: 0, searchPage: 0, perPage: 25,
      nameA: "", nameB: "", profile: "",
      searchQuery: "", searchSort: "level", navDepth: 0,
      combo: null, comboQ: "", comboItems: { a: [], b: [] }, comboTimer: null,
      updatedLabel: "Loading…",
      overallRows: [], overallPager: derivePager(null), overallEyebrow: "",
      skillRows: [], skillPager: derivePager(null), skillEyebrow: "",
      bossKcRows: [], bossKcPager: derivePager(null),
      bossTimeRows: [], bossTimePager: derivePager(null),
      searchRows: [], searchPager: derivePager(null), searchEyebrow: "",
      compareResult: null,
      profilePlayer: { rank: "—", mode: "", name: "", totalLevel: 0, totalXp: 0, joined: "" },
      profileSkills: [], profileBosses: [],

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var skill = params.get("skill");
        var boss = params.get("boss");
        var player = params.get("player");
        var view = params.get("view");
        var q = params.get("q");
        var sort = params.get("sort");
        if (player) this.profile = player;
        if (skill && SKILLS.indexOf(skill) >= 0) this.skill = skill;
        if (boss && BOSSES.some(function (b) { return b.id === boss; })) this.boss = boss;
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
            self.loadView();
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
          self.loadView();
        });

        // Loads the current view now, and again from scratch whenever the selected world changes;
        // everything shown belongs to the world it came from, so it's all cleared first.
        window.voidWatchWorld(function (world) {
          self.clearData(world);
          if (world != null) self.loadView();
        });
      },

      clearData: function (world) {
        this.updatedLabel = world != null ? "Loading…" : Alpine.store("world").current != null ? "World offline" : "No world selected";
        this.overallRows = []; this.overallPager = derivePager(null); this.overallEyebrow = "";
        this.skillRows = []; this.skillPager = derivePager(null); this.skillEyebrow = "";
        this.bossKcRows = []; this.bossKcPager = derivePager(null);
        this.bossTimeRows = []; this.bossTimePager = derivePager(null);
        this.searchRows = []; this.searchPager = derivePager(null); this.searchEyebrow = "";
        this.compareResult = null;
        this.comboItems = { a: [], b: [] };
        this.profilePlayer = { rank: "—", mode: "", name: this.profile, totalLevel: 0, totalXp: 0, joined: "" };
        this.profileSkills = []; this.profileBosses = [];
      },

      historyState: function () {
        return {
          view: this.view, skill: this.skill, boss: this.boss, profile: this.profile,
          searchQuery: this.searchQuery, searchSort: this.searchSort,
        };
      },

      loadView: function () {
        if (this.view === "overall") this.fetchOverall();
        else if (this.view === "skills") this.fetchSkill();
        else if (this.view === "bosses") {
          this.fetchBossKills();
          this.fetchBossTimes();
        } else if (this.view === "compare") this.fetchCompare();
        else if (this.view === "search") this.fetchSearch();
        else if (this.view === "player") this.fetchProfile();
      },

      navigate: function (patch) {
        Object.assign(this, patch);
        if (this.view !== "search") this.query = "";
        this.navDepth++;
        var state = this.historyState();
        history.pushState(state, "", urlFor(state));
        this.loadView();
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
      compareThis: function () {
        var patch = { nameA: this.profile, view: "compare" };
        if (this.nameB === this.profile) patch.nameB = "";
        this.navigate(patch);
      },
      search: function () {
        var q = this.query.trim();
        if (!q) return;
        this.searchPage = 0;
        this.navigate({ view: "search", searchQuery: q });
      },
      setSearchSort: function (sort) {
        this.searchSort = sort;
        this.searchPage = 0;
        this.fetchSearch();
      },
      prevPage: function (key) {
        var pager = this.pagerFor(key);
        this[key] = Math.max(0, pager.page - 1);
        this[PAGE_FETCH[key]]();
      },
      nextPage: function (key) {
        var pager = this.pagerFor(key);
        this[key] = Math.min(pager.pages - 1, pager.page + 1);
        this[PAGE_FETCH[key]]();
      },
      pagerFor: function (key) {
        if (key === "page") return this.overallPager;
        if (key === "skillPage") return this.skillPager;
        if (key === "kcPage") return this.bossKcPager;
        if (key === "searchPage") return this.searchPager;
        return this.bossTimePager;
      },
      fmtXp: function (n) { return fmt(n); },

      fetchOverall: function () {
        var self = this;
        var params = new URLSearchParams({ page: this.page, pageSize: this.perPage });
        if (this.mode !== "all") params.set("mode", this.mode);
        if (this.query.trim()) params.set("q", this.query.trim());
        return getJson(API + "/hiscores/overall?" + params).then(function (data) {
          self.overallPager = derivePager(data.pagination);
          self.overallEyebrow = fmt(data.pagination.total) + " accounts" + (self.mode !== "all" ? " · " + modeLabel(self.mode) : "");
          self.overallRows = data.items.map(decorateRankedRow);
          self.updatedLabel = formatUpdated(data.updatedAt);
        }).catch(function () {
          self.overallRows = [];
          self.overallPager = derivePager(null);
        });
      },

      fetchSkill: function () {
        var self = this;
        var params = new URLSearchParams({ page: this.skillPage, pageSize: this.perPage });
        return getJson(API + "/hiscores/skills/" + this.skill.toLowerCase() + "?" + params).then(function (data) {
          self.skillPager = derivePager(data.pagination);
          self.skillEyebrow = data.skillName.toUpperCase() + " · ranked by experience · cap level " + data.maxLevel;
          self.skillRows = data.items.map(function (row, i) {
            return { rank: row.rank, name: row.name, level: row.level, xp: fmt(row.xp), bg: band(i), rankColor: rankColor(row.rank) };
          });
        }).catch(function () {
          self.skillRows = [];
          self.skillPager = derivePager(null);
        });
      },

      fetchBossKills: function () {
        var self = this;
        var params = new URLSearchParams({ page: this.kcPage, pageSize: 10 });
        return getJson(API + "/hiscores/bosses/" + this.boss + "/kills?" + params).then(function (data) {
          self.bossKcPager = derivePager(data.pagination);
          self.bossKcRows = data.items.map(function (row, i) {
            return { rank: row.rank, name: row.name, kc: fmt(row.kills), bg: band(i), rankColor: rankColor(row.rank) };
          });
        }).catch(function () {
          self.bossKcRows = [];
          self.bossKcPager = derivePager(null);
        });
      },

      fetchBossTimes: function () {
        var self = this;
        var params = new URLSearchParams({ page: this.timePage, pageSize: 10 });
        var size = TEAM_SIZE_BY_LABEL[this.team];
        if (size) params.set("teamSize", String(size));
        return getJson(API + "/hiscores/bosses/" + this.boss + "/times?" + params).then(function (data) {
          self.bossTimePager = derivePager(data.pagination);
          self.bossTimeRows = data.items.map(function (row, i) {
            return {
              rank: row.rank, name: row.name, team: row.teamSize === 1 ? "Solo" : row.teamSize + " players",
              time: mmss(row.timeSeconds), bg: band(i), rankColor: rankColor(row.rank),
            };
          });
        }).catch(function () {
          self.bossTimeRows = [];
          self.bossTimePager = derivePager(null);
        });
      },

      fetchSearch: function () {
        var self = this;
        var q = this.searchQuery.trim();
        if (!q) {
          this.searchRows = [];
          this.searchPager = derivePager(null);
          this.searchEyebrow = "";
          return Promise.resolve();
        }
        // The overall endpoint only sorts by experience; level/rank/name sort is applied
        // client-side over one generous page, which is honest at this site's account scale.
        var params = new URLSearchParams({ q: q, page: 0, pageSize: 100 });
        return getJson(API + "/hiscores/overall?" + params).then(function (data) {
          var items = data.items.slice();
          if (self.searchSort === "name") items.sort(function (a, b) { return a.name.localeCompare(b.name); });
          else if (self.searchSort === "rank") items.sort(function (a, b) { return a.rank - b.rank; });
          else items.sort(function (a, b) { return b.totalLevel - a.totalLevel || b.totalXp - a.totalXp; });
          var total = items.length;
          var from = self.searchPage * self.perPage;
          var page = items.slice(from, from + self.perPage);
          self.searchPager = derivePager({
            page: self.searchPage, pageSize: self.perPage, total: total,
            totalPages: Math.max(1, Math.ceil(total / self.perPage)),
            hasPrevious: self.searchPage > 0, hasNext: (self.searchPage + 1) * self.perPage < total,
          });
          self.searchEyebrow = fmt(total) + (total === 1 ? " match" : " matches") + (q ? " for “" + q + "”" : "");
          self.searchRows = page.map(decorateRankedRow);
        }).catch(function () {
          self.searchRows = [];
          self.searchPager = derivePager(null);
        });
      },
      get searchSorted() { return this.searchRows; },

      fetchProfile: function () {
        var self = this;
        var name = this.profile;
        if (!name) return Promise.resolve();
        var encoded = encodeURIComponent(name);
        return Promise.all([
          getJson(API + "/players/" + encoded),
          getJson(API + "/players/" + encoded + "/skills"),
          getJson(API + "/players/" + encoded + "/bosses"),
        ]).then(function (results) {
          var profile = results[0], skills = results[1], bosses = results[2];
          self.profilePlayer = {
            rank: profile.overallRank != null ? fmt(profile.overallRank) : "—",
            mode: modeLabel(profile.mode), name: profile.name,
            totalLevel: profile.totalLevel, totalXp: profile.totalXp,
            joined: profile.joinedAt ? "joined " + formatDate(profile.joinedAt) : "",
          };
          self.profileSkills = skills.items.map(function (s) {
            return { name: s.name, icon: skillIcon(s.name.toLowerCase()), level: s.level, max: s.maxLevel, percent: s.progressPercent };
          });
          self.profileBosses = bosses.items.map(function (b) {
            return {
              key: b.boss, boss: b.name, abbr: bossAbbr(b.name), icon: bossIcon(b.name), kc: fmt(b.kills), best: b.fastestSeconds ? mmss(b.fastestSeconds) : "—",
              rank: b.kills > 0 ? "rank " + fmt(b.rank) : "unranked",
            };
          });
        }).catch(function () {
          self.profilePlayer = { rank: "—", mode: "", name: name, totalLevel: 0, totalXp: 0, joined: "" };
          self.profileSkills = [];
          self.profileBosses = [];
        });
      },

      comboFocus: function (side) {
        this.combo = side;
        this.comboQ = "";
        this.comboSearch(side);
      },
      comboBlur: function () { this.combo = null; this.comboQ = ""; },
      comboSearch: function (side) {
        var self = this;
        var q = this.comboQ.trim();
        var other = side === "a" ? this.nameB : this.nameA;
        clearTimeout(this.comboTimer);
        this.comboTimer = setTimeout(function () {
          var params = new URLSearchParams({ limit: 25 });
          if (q) params.set("q", q);
          getJson(API + "/players/search?" + params).then(function (data) {
            self.comboItems[side] = data.items
              .filter(function (p) { return p.name !== other; })
              .map(function (p) { return { name: p.name, meta: "rank " + fmt(p.rank) + " · " + abbrev(p.totalXp) + " xp" }; });
          }).catch(function () { self.comboItems[side] = []; });
        }, 150);
      },
      pickCombo: function (side, name) {
        var other = side === "a" ? this.nameB : this.nameA;
        if (name === other) return;
        if (side === "a") this.nameA = name; else this.nameB = name;
        this.combo = null;
        this.comboQ = "";
        this.fetchCompare();
      },
      comboValue: function (side) {
        if (this.combo !== side) return side === "a" ? this.nameA : this.nameB;
        return this.comboQ;
      },
      comboResults: function (side) { return this.comboItems[side] || []; },

      fetchCompare: function () {
        var self = this;
        if (!this.nameA || !this.nameB) {
          this.compareResult = null;
          return Promise.resolve();
        }
        var params = new URLSearchParams({ playerA: this.nameA, playerB: this.nameB });
        return getJson(API + "/hiscores/compare?" + params).then(function (data) {
          self.compareResult = data;
        }).catch(function () { self.compareResult = null; });
      },
      get compareReady() { return !!this.compareResult; },
      get compareEyebrow() {
        if (!this.compareReady) return "Pick two players to compare";
        return this.nameA + " vs " + this.nameB;
      },
      get compareBlankText() {
        if (!this.nameA && !this.nameB) return "Search for two players above to compare their stats.";
        return "Search for a second player above to compare.";
      },
      get compareSummary() { return this.compareResult ? this.compareResult.summary : []; },
      get compareRows() {
        if (!this.compareResult) return [];
        return this.compareResult.skills.map(function (r, i) {
          var d = delta(r.a.xp, r.b.xp, "xp");
          return Object.assign({
            skill: r.skillName, icon: skillIcon(r.skill), aLevel: r.a.level, bLevel: r.b.level,
            aXp: fmt(r.a.xp), bXp: fmt(r.b.xp),
            aColor: r.leader === "a" ? "var(--gold-300)" : "var(--text-faint)",
            bColor: r.leader === "b" ? "var(--gold-300)" : "var(--text-faint)",
            bg: band(i),
          }, d);
        });
      },
      get compareBossRows() {
        if (!this.compareResult) return [];
        return this.compareResult.bosses.map(function (r, i) {
          return Object.assign({
            key: r.boss, boss: r.bossName, abbr: bossAbbr(r.bossName), icon: bossIcon(r.bossName), aKc: fmt(r.aKills), bKc: fmt(r.bKills),
            aColor: r.leader === "a" ? "var(--gold-300)" : "var(--text-faint)",
            bColor: r.leader === "b" ? "var(--gold-300)" : "var(--text-faint)",
            bg: band(i),
          }, delta(r.aKills, r.bKills, "kills"));
        });
      },

      get bossName() {
        var b = BOSSES.filter(function (x) { return x.id === this.boss; }, this)[0];
        return b ? b.name : this.boss;
      },
    };
  };
})();
