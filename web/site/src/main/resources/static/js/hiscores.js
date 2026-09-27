// Hiscores page data + Alpine component. Every table is fetched live from the selected world's
// `/api/v1/hiscores/*` and `/api/v1/players/*` endpoints - there is no mock dataset here. With no
// world selected, or the selected one offline, every table is left empty.

(function () {
  var API = "/api/v1";

  // Populated by GameData.script() (see Hiscores.kt) as window.VOID_SKILLS/VOID_BOSSES, so the
  // skill/boss lists live in one place (GameData.kt) instead of being duplicated here.
  var SKILLS = window.VOID_SKILLS.map(function (s) { return s.name; });
  var BOSSES = window.VOID_BOSSES;
  var MAX_FLOOR = 60;
  var MODE_TONE = {
    skiller: { bg: "rgba(224,174,60,.14)", fg: "var(--gold-300)", bd: "var(--gold-600)" },
    pure: { bg: "var(--feedback-danger-bg)", fg: "var(--feedback-danger)", bd: "var(--ember-600)" },
  };
  var SEARCH_SORTS = ["level", "rank", "name"];
  var PAGE_FETCH = {
    page: "fetchOverall", skillPage: "fetchSkill", kcPage: "fetchBossKills",
    timePage: "fetchBossTimes", searchPage: "fetchSearch", floorPage: "fetchFloorTimes",
  };
  var FLOOR_FILTERS = { floorSize: "size", floorComplexity: "complexity", floorParty: "partySize" };

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
  function bossGroup(id) {
    var b = BOSSES.filter(function (x) { return x.id === id; })[0];
    return b ? b.group : "world";
  }
  // Five or more is one "mass" record for world bosses, but a dungeoneering party tops out at five.
  function teamLabel(size, group) {
    if (size === 1) return "Solo";
    return size + (size === 5 && group !== "dungeoneering" ? "+" : "") + " players";
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
    if (state.view === "dungeoneering" && state.floor) params.set("floor", String(state.floor));
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
      view: "overall", skill: "Attack", boss: BOSSES.length ? BOSSES[0].id : "", bossGroup: "world", mode: "all", team: "all", query: "",
      page: 0, skillPage: 0, kcPage: 0, timePage: 0, searchPage: 0, floorPage: 0, perPage: 25,
      floor: 1, floorSize: "all", floorComplexity: "all", floorParty: "all",
      nameA: "", nameB: "", profile: "",
      searchQuery: "", searchSort: "level", navDepth: 0,
      combo: null, comboQ: "", comboItems: { a: [], b: [] }, comboTimer: null,
      updatedLabel: "Loading…",
      overallRows: [], overallPager: derivePager(null), overallEyebrow: "",
      skillRows: [], skillPager: derivePager(null), skillEyebrow: "",
      bossKcRows: [], bossKcPager: derivePager(null),
      bossTimeRows: [], bossTimePager: derivePager(null),
      floorRows: [], floorPager: derivePager(null), floorBest: {},
      searchRows: [], searchPager: derivePager(null), searchEyebrow: "",
      compareResult: null,
      profilePlayer: { rank: "—", mode: "", name: "", totalLevel: 0, totalXp: 0, joined: "" },
      profileSkills: [], profileBosses: [], profileFloors: [], profileFloorsCleared: 0,

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var skill = params.get("skill");
        var boss = params.get("boss");
        var player = params.get("player");
        var view = params.get("view");
        var q = params.get("q");
        var sort = params.get("sort");
        var floor = parseInt(params.get("floor"), 10);
        if (floor >= 1 && floor <= MAX_FLOOR) this.floor = floor;
        if (player) this.profile = player;
        if (skill && SKILLS.indexOf(skill) >= 0) this.skill = skill;
        if (boss && BOSSES.some(function (b) { return b.id === boss; })) this.boss = boss;
        if (q) {
          this.query = q;
          this.searchQuery = q;
        }
        if (sort && SEARCH_SORTS.indexOf(sort) >= 0) this.searchSort = sort;
        this.bossGroup = bossGroup(this.boss);
        if (view) {
          this.view = view;
        } else if (q) {
          this.view = "search";
        } else if (boss) {
          this.view = "bosses";
        } else if (floor) {
          this.view = "dungeoneering";
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
          if (s.boss) {
            self.boss = s.boss;
            self.bossGroup = bossGroup(s.boss);
          }
          if (s.floor) self.floor = s.floor;
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
        this.floorRows = []; this.floorPager = derivePager(null); this.floorBest = {};
        this.searchRows = []; this.searchPager = derivePager(null); this.searchEyebrow = "";
        this.compareResult = null;
        this.comboItems = { a: [], b: [] };
        this.profilePlayer = { rank: "—", mode: "", name: this.profile, totalLevel: 0, totalXp: 0, joined: "" };
        this.profileSkills = []; this.profileBosses = []; this.profileFloors = []; this.profileFloorsCleared = 0;
      },

      historyState: function () {
        return {
          view: this.view, skill: this.skill, boss: this.boss, floor: this.floor, profile: this.profile,
          searchQuery: this.searchQuery, searchSort: this.searchSort,
        };
      },

      loadView: function () {
        if (this.view === "overall") this.fetchOverall();
        else if (this.view === "skills") this.fetchSkill();
        else if (this.view === "bosses") {
          this.fetchBossKills();
          this.fetchBossTimes();
        } else if (this.view === "dungeoneering") {
          this.fetchFloorRecords();
          this.fetchFloorTimes();
        } else if (this.view === "compare") this.fetchCompare();
        else if (this.view === "search") this.fetchSearch();
        else if (this.view === "player") this.fetchProfile();
      },

      navigate: function (patch) {
        Object.assign(this, patch);
        if (patch.boss) this.bossGroup = bossGroup(patch.boss);
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
      /** Switches the boss grid between world and Daemonheim bosses, opening the first in the group. */
      selectBossGroup: function (group) {
        if (this.bossGroup === group) return;
        this.bossGroup = group;
        var first = BOSSES.filter(function (b) { return b.group === group; })[0];
        if (first) {
          this.navigate({ boss: first.id, kcPage: 0, timePage: 0, view: "bosses" });
        } else {
          this.bossKcRows = []; this.bossKcPager = derivePager(null);
          this.bossTimeRows = []; this.bossTimePager = derivePager(null);
        }
      },
      setFloorFilter: function (model, value) {
        this[model] = value;
        this.floorPage = 0;
        this.fetchFloorRecords();
        this.fetchFloorTimes();
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
        if (key === "floorPage") return this.floorPager;
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
        if (this.team !== "all") params.set("teamSize", this.team);
        var group = this.bossGroup;
        return getJson(API + "/hiscores/bosses/" + this.boss + "/times?" + params).then(function (data) {
          self.bossTimePager = derivePager(data.pagination);
          self.bossTimeRows = data.items.map(function (row, i) {
            return {
              rank: row.rank, name: row.name, team: teamLabel(row.teamSize, group),
              time: mmss(row.timeSeconds), bg: band(i), rankColor: rankColor(row.rank),
            };
          });
        }).catch(function () {
          self.bossTimeRows = [];
          self.bossTimePager = derivePager(null);
        });
      },

      floorParams: function () {
        var params = new URLSearchParams();
        for (var model in FLOOR_FILTERS) {
          if (this[model] !== "all") params.set(FLOOR_FILTERS[model], this[model]);
        }
        return params;
      },
      get floorFiltered() { return this.floorSize !== "all" || this.floorComplexity !== "all" || this.floorParty !== "all"; },
      get floorEyebrow() {
        var parts = ["FLOOR " + this.floor];
        if (this.floorSize !== "all") parts.push(modeLabel(this.floorSize));
        if (this.floorComplexity !== "all") parts.push("complexity " + this.floorComplexity);
        if (this.floorParty !== "all") parts.push(teamLabel(Number(this.floorParty), "dungeoneering"));
        return parts.join(" · ");
      },

      /** The fastest time per floor, shown under each floor tile. */
      fetchFloorRecords: function () {
        var self = this;
        return getJson(API + "/hiscores/dungeoneering/floors?" + this.floorParams()).then(function (data) {
          var best = {};
          data.items.forEach(function (row) { best[row.floor] = mmss(row.timeSeconds); });
          self.floorBest = best;
        }).catch(function () {
          self.floorBest = {};
        });
      },

      fetchFloorTimes: function () {
        var self = this;
        var params = this.floorParams();
        params.set("page", this.floorPage);
        params.set("pageSize", this.perPage);
        return getJson(API + "/hiscores/dungeoneering/floors/" + this.floor + "?" + params).then(function (data) {
          self.floorPager = derivePager(data.pagination);
          self.floorRows = data.items.map(function (row, i) {
            return {
              key: row.name + ":" + row.size + ":" + row.complexity + ":" + row.partySize,
              rank: row.rank, name: row.name, size: modeLabel(row.size), complexity: row.complexity,
              party: teamLabel(row.partySize, "dungeoneering"), time: mmss(row.timeSeconds),
              bg: band(i), rankColor: rankColor(row.rank),
            };
          });
        }).catch(function () {
          self.floorRows = [];
          self.floorPager = derivePager(null);
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
          // Tolerates worlds running a server from before floor times were tracked
          getJson(API + "/players/" + encoded + "/dungeoneering").catch(function () { return { floorsCleared: 0, items: [] }; }),
        ]).then(function (results) {
          var profile = results[0], skills = results[1], bosses = results[2], floors = results[3];
          self.profileFloors = floors.items.map(window.voidFloorRow);
          self.profileFloorsCleared = floors.floorsCleared;
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
          self.profileFloors = [];
          self.profileFloorsCleared = 0;
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
      get profileFloorsEyebrow() { return this.profileFloorsCleared + " of " + MAX_FLOOR + " floors cleared"; },
      get compareFloorRows() {
        if (!this.compareResult || !this.compareResult.floors) return [];
        return this.compareResult.floors.map(window.voidFloorCompareRow);
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
