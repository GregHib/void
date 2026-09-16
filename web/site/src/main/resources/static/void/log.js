// Adventurer's log page data + Alpine component. A self-contained mock dataset stands in for
// the profile API: one deterministic account per name (seeded from the name itself) so the log
// has something realistic to browse and the "Find a log" search has other accounts to switch to.

(function () {
  // Populated by GameData.script() (see Hiscores.kt/AdventurersLog.kt) as window.VOID_SKILLS, so
  // the real skill list lives in one place (GameData.kt) instead of being duplicated here.
  var SKILLS = window.VOID_SKILLS.map(function (s) { return [s.name, s.max]; });
  // Fictional boss roster for this page's mock profile data - unrelated to the real hiscores
  // bosses in GameData.kt, so it stays hardcoded here.
  var BOSSES = [
    ["Ashen Wyrm", 214], ["Gravelord Thane", 332], ["The Hollow King", 488], ["Sunken Leviathan", 276],
    ["Mother of Blades", 191], ["Warden of Cinders", 405], ["Rot-Priest Malgrim", 148], ["Frostbound Colossus", 560],
    ["Twin Serpents of Ord", 233], ["Blightmaw", 127],
  ];
  var QUESTS = [
    ["The Sunken Archive", "Master", "danger", 278], ["Ashes of Ord", "Experienced", "warning", 124],
    ["The Hollow Road", "Experienced", "warning", 96], ["Warden's Gambit", "Master", "danger", 168],
    ["Rot in the Rafters", "Intermediate", "info", 58], ["Blightmaw's Bargain", "Intermediate", "info", 71],
    ["The Pale Choir", "Experienced", "warning", 89], ["Verdant Horror", "Intermediate", "info", 47],
    ["A Thane's Debt", "Novice", "success", 22], ["Cinders of Home", "Novice", "success", 15],
    ["The Long Dig", "Experienced", "warning", 84], ["Colossus Waking", "Master", "danger", 312],
    ["Twin-Fanged", "Intermediate", "info", 63], ["First Light", "Novice", "success", 9],
  ];
  var PLAYER_NAMES = [
    ["Thornwake", "World 9 · PvP"], ["Brackwater", "World 9"], ["Corvid Ash", "World 12"],
    ["Duskfen", "World 3"], ["Emberhollow", "World 9"], ["Verdigris", "World 18"],
    ["Mournvale", "World 24"], ["Sable Kest", "World 9"], ["Rooksbane", "World 12"],
    ["Ashgrave", "World 3"], ["Wyrmden", "World 18"], ["Cindermoor", "World 9"],
  ];
  // Persistent guild rosters. A player not listed here is clanless (profile.clan is null).
  var CLANS = [
    { name: "Ashen Compact", members: ["Thornwake", "Brackwater", "Corvid Ash", "Duskfen", "Emberhollow"] },
    { name: "Verdant Bastion", members: ["Verdigris", "Mournvale", "Sable Kest"] },
    { name: "Rookery", members: ["Rooksbane", "Ashgrave", "Wyrmden", "Cindermoor"] },
  ];
  var BAND = ["var(--surface-panel)", "var(--umber-850)"];
  var TODAY = Date.UTC(2026, 8, 9);
  var DAY = 86400000;

  // How many days of daily xp-gain history to synthesize per profile, and how many trailing
  // days of it each chart range toggle shows.
  var HISTORY_DAYS = 730;
  var RANGE_DAYS = { week: 7, month: 30, year: 365, all: HISTORY_DAYS };
  var RANGE_LABEL = { week: "last 7 days", month: "last 30 days", year: "last 365 days", all: "all time" };

  // Fixed per-skill colour, so a skill is always the same colour on the chart no matter which
  // other skills it's stacked alongside or how the top-5-plus-"Other" bucketing shakes out.
  var SKILL_COLORS = {
    Attack: "#c2493a",
    Defence: "#7d9db0",
    Strength: "#dd9a2b",
    Constitution: "#d1495c",
    Ranged: "#7fae4f",
    Prayer: "#f0c667",
    Magic: "#8b6bc4",
    Cooking: "#e08a3c",
    Woodcutting: "#6b8e4e",
    Fletching: "#c9a66b",
    Fishing: "#5b8fb0",
    Firemaking: "#e0663c",
    Crafting: "#b06bb0",
    Smithing: "#a0a8ad",
    Mining: "#7a6a57",
    Herblore: "#4f9e6e",
    Agility: "#4fb0a8",
    Thieving: "#6b4e8e",
    Slayer: "#8e2f2f",
    Farming: "#6a9e3f",
    Runecrafting: "#3f9ea0",
    Hunter: "#9e7a4f",
    Construction: "#71542c",
    Summoning: "#7a5ea8",
    Dungeoneering: "#c2a34a",
  };
  var OTHER_COLOR = "#5a646b";

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
  function abbrevXp(n) { return (n / 1e6).toFixed(1) + "M"; }
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
  function dateAgo(days) { return new Date(TODAY - days * DAY).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
  function exactTime(days, r) {
    var t = TODAY - days * DAY + Math.floor(r() * DAY);
    return new Date(t).toLocaleString("en-GB", {
      day: "numeric", month: "long", year: "numeric", hour: "numeric", minute: "2-digit",
    });
  }
  function band(i) { return BAND[i % 2]; }
  function skillIcon(name) { return "void/images/skills/" + name.toLowerCase() + ".png"; }
  function bossAbbr(name) { return name.split(" ").map(function (w) { return w[0]; }).join("").slice(0, 3).toUpperCase(); }
  function clanFor(name) {
    for (var i = 0; i < CLANS.length; i++) {
      if (CLANS[i].members.indexOf(name) >= 0) return CLANS[i].name;
    }
    return null;
  }
  function clanByName(name) {
    for (var i = 0; i < CLANS.length; i++) {
      if (CLANS[i].name === name) return CLANS[i];
    }
    return CLANS[0];
  }

  // Synthesizes daily xp gains per skill over the last HISTORY_DAYS days: each skill trains in a
  // handful of random "active" windows on the timeline, gaining xp on most (not all) days within
  // them, roughly scaled to how much total xp that skill has ended up with.
  function buildXpHistory(skills, r) {
    var days = HISTORY_DAYS;
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
      history.push({ t: TODAY - (days - 1 - d) * DAY, gains: gains });
    }
    return history;
  }

  function urlFor(view, profileName, clanName) {
    if (view === "profile") return "?player=" + encodeURIComponent(profileName);
    if (view === "clan") return "?clan=" + encodeURIComponent(clanName);
    return window.location.pathname;
  }

  function buildProfile(name) {
    var r = rng(hashSeed(name));
    var strength = 0.45 + r() * 0.6;
    var member = r() > 0.15;

    var skills = SKILLS.map(function (s, i) {
      var capXp = XPT[s[1] + 1];
      var xp = Math.max(1200, Math.min(capXp, Math.round(capXp * Math.min(1, strength * (0.5 + r() * 0.85)))));
      var level = levelFromXp(xp, s[1]);
      return {
        name: s[0], max: s[1], level: level, xp: xp,
        xpLabel: fmt(xp), rankLabel: "rank " + fmt(1200 + i * 941 + (s[1] - level) * 1800),
        icon: skillIcon(s[0]),
      };
    });
    var totalLevel = skills.reduce(function (s, k) { return s + k.level; }, 0);
    var totalXp = skills.reduce(function (s, k) { return s + k.xp; }, 0);
    var maxedCount = skills.filter(function (k) { return k.level >= k.max; }).length;

    var questCount = Math.round(QUESTS.length * Math.min(1, strength * 0.9 + r() * 0.2));
    var completedQuests = QUESTS.slice(0, questCount).map(function (q, i) {
      var minutes = Math.round(q[3] * (0.7 + r() * 0.6));
      return {
        name: q[0], difficulty: q[1], tone: q[2],
        duration: minutes >= 60 ? Math.floor(minutes / 60) + " h " + String(minutes % 60).padStart(2, "0") + " m" : minutes + " m",
        date: dateAgo(6 + i * (17 + Math.floor(r() * 10))),
        band: band(i),
      };
    });
    var questPoints = Math.round(questCount * 1.56);

    var bosses = BOSSES.map(function (b, i) {
      var kc = Math.round(Math.max(0, 900 * strength * (0.1 + r() * 1.1)) / (1 + i * 0.12));
      var fastest = kc > 0 ? b[1] * (0.5 + r() * 0.4) : 0;
      return {
        name: b[0], abbr: bossAbbr(b[0]), kills: fmt(kc),
        fastest: kc > 0 ? mmss(fastest) : "—",
        last: kc > 0 ? dateAgo(1 + Math.floor(r() * 30)) : "—",
        band: band(i),
      };
    }).sort(function (a, b) { return parseInt(b.kills.replace(/,/g, "")) - parseInt(a.kills.replace(/,/g, "")); });
    var bossKills = bosses.reduce(function (s, b) { return s + (parseInt(b.kills.replace(/,/g, "")) || 0); }, 0);

    var topSkill = skills.slice().sort(function (a, b) { return b.level - a.level; })[0];
    var topBoss = bosses[0];
    var events = [];
    if (maxedCount > 0) {
      var maxedSkill = skills.filter(function (k) { return k.level >= k.max; })[0].name;
      events.push({ kind: "Skill", tone: "gold", text: "Reached level 99 " + maxedSkill + ".", description: "Joined the ranks of the elite in " + maxedSkill + ", reaching the maximum level of 99." });
    }
    events.push({ kind: "Skill", tone: "gold", text: "Total level passed " + (Math.floor(totalLevel / 100) * 100) + ".", description: "Combined level across all skills crossed a new milestone." });
    if (completedQuests.length > 0) {
      var q0 = completedQuests[0];
      events.push({ kind: "Quest", tone: "info", text: "Completed " + q0.name + ". +" + Math.round(q0.name.length / 3) + " quest points.", description: "Finished the " + q0.difficulty.toLowerCase() + " quest \"" + q0.name + "\" in " + q0.duration + "." });
    }
    if (topBoss.kills !== "0") {
      events.push({ kind: "Combat", tone: "danger", text: "First kill: " + topBoss.name + ", solo.", description: "Defeated " + topBoss.name + " unassisted for the first time." });
    }
    events.push({ kind: "Account", tone: "success", text: member ? "Membership renewed for 12 months." : "Playing on a free account.", description: member ? "Subscription extended, unlocking members-only areas, skills and quests." : "Currently playing without a membership subscription." });
    if (completedQuests.length > 1) {
      events.push({ kind: "Quest", tone: "info", text: "Completed " + completedQuests[1].name + ".", description: "Finished the " + completedQuests[1].difficulty.toLowerCase() + " quest \"" + completedQuests[1].name + "\" in " + completedQuests[1].duration + "." });
    }
    events.push({ kind: "Skill", tone: "gold", text: topSkill.name + " reached level " + topSkill.level + ".", description: "Highest trained skill continues to climb." });
    var clanName = clanFor(name);
    if (clanName) {
      events.push({ kind: "Account", tone: "success", text: "Joined the clan " + clanName + " as a member.", description: "Became a member of " + clanName + "." });
    }
    events = events.map(function (e, i) {
      var days = 2 + i * (3 + Math.floor(r() * 5));
      return {
        kind: e.kind, tone: e.tone, text: e.text,
        description: e.description || "",
        date: dateAgo(days),
        exact: exactTime(days, r),
        band: band(i),
      };
    });

    var joinedDaysAgo = 200 + Math.floor(r() * 900);
    var xpHistory = buildXpHistory(skills, r);

    return {
      name: name,
      member: member,
      clan: clanFor(name),
      world: 3 + Math.floor(r() * 40),
      mode: r() > 0.7 ? "Skill total" : "PvP",
      joined: dateAgo(joinedDaysAgo),
      totalLevel: totalLevel,
      totalXpLabel: abbrevXp(totalXp),
      combat: 3 + Math.round((skills[0].level + skills[1].level + skills[2].level + skills[3].level * 1.33 + skills[4].level + skills[5].level + skills[6].level) / 8),
      questPoints: questPoints,
      questPointsMax: Math.round(QUESTS.length * 1.56),
      skills: skills,
      maxedCount: maxedCount,
      xpHistory: xpHistory,
      events: events,
      quests: completedQuests,
      questTotal: QUESTS.length,
      bosses: bosses,
      bossKills: bossKills,
      milestones: [
        { label: "Skills at 99", value: maxedCount + " of " + SKILLS.length },
        { label: "Quests complete", value: completedQuests.length + " of " + QUESTS.length },
        { label: "Bosses defeated", value: fmt(bossKills) },
        { label: "Time played", value: fmt(Math.round(totalXp / 42000)) + " h" },
        { label: "Last seen", value: events.length ? events[0].date : "—" },
      ],
    };
  }

  var PROFILES = {};
  function profileFor(name) {
    if (!PROFILES[name]) PROFILES[name] = buildProfile(name);
    return PROFILES[name];
  }

  window.logApp = function () {
    return {
      view: "overview",
      profileName: "Thornwake",
      clanName: CLANS[0].name,
      query: "",
      filter: "All",
      sort: "level",
      xpRange: "month",
      xpHover: null,
      xpZoom: null,
      xpDragging: false,
      xpDragStart: null,
      xpDragEnd: null,

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var player = params.get("player");
        var clan = params.get("clan");
        if (player) {
          this.profileName = player;
          this.view = "profile";
        } else if (clan) {
          this.clanName = clan;
          this.view = "clan";
        }
        history.replaceState(
          { view: this.view, profileName: this.profileName, clanName: this.clanName },
          "",
          urlFor(this.view, this.profileName, this.clanName),
        );

        var self = this;
        window.addEventListener("popstate", function (e) {
          var s = e.state;
          if (!s) {
            self.view = "overview";
            return;
          }
          self.profileName = s.profileName;
          self.clanName = s.clanName;
          self.view = s.view;
        });
      },

      navigate: function (view, profileName, clanName) {
        this.view = view;
        if (profileName !== undefined) this.profileName = profileName;
        if (clanName !== undefined) this.clanName = clanName;
        history.pushState(
          { view: view, profileName: this.profileName, clanName: this.clanName },
          "",
          urlFor(view, this.profileName, this.clanName),
        );
      },

      get profile() { return profileFor(this.profileName); },
      pick: function (name) { this.query = ""; this.navigate("profile", name); },
      pickClan: function (name) { this.navigate("clan", undefined, name); },
      backToOverview: function () { this.navigate("overview"); },

      get overviewClans() {
        return CLANS.map(function (c) {
          var members = c.members.map(profileFor);
          var combinedLevel = members.reduce(function (s, p) { return s + p.totalLevel; }, 0);
          return {
            name: c.name,
            members: c.members.length,
            combinedLevel: fmt(combinedLevel),
            averageLevel: fmt(Math.round(combinedLevel / members.length)),
          };
        }).sort(function (a, b) { return parseInt(b.combinedLevel.replace(/,/g, "")) - parseInt(a.combinedLevel.replace(/,/g, "")); });
      },
      get overviewPlayers() {
        return PLAYER_NAMES.map(function (p) {
          var prof = profileFor(p[0]);
          return { name: p[0], meta: p[1], clan: prof.clan, total: fmt(prof.totalLevel) };
        }).sort(function (a, b) { return parseInt(b.total.replace(/,/g, "")) - parseInt(a.total.replace(/,/g, "")); });
      },

      get clan() { return clanByName(this.clanName); },
      get clanMembers() {
        return this.clan.members.map(function (name) {
          var p = profileFor(name);
          return { name: name, totalLevel: p.totalLevel, totalLevelLabel: fmt(p.totalLevel), combat: p.combat, member: p.member };
        }).sort(function (a, b) { return b.totalLevel - a.totalLevel; });
      },
      get clanStats() {
        var members = this.clanMembers;
        var combinedLevel = members.reduce(function (s, m) { return s + m.totalLevel; }, 0);
        var combinedCombat = members.reduce(function (s, m) { return s + m.combat; }, 0);
        return {
          members: members.length,
          combinedLevel: fmt(combinedLevel),
          averageLevel: fmt(Math.round(combinedLevel / members.length)),
          averageCombat: Math.round(combinedCombat / members.length),
        };
      },

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
        var pts = zoom ? full.slice(zoom.start, zoom.end + 1) : full.slice(full.length - RANGE_DAYS[range]);
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
          return { left: ((X(i) / W) * 100).toFixed(2) + "%", label: xpStamp(pts[i].t, range) };
        });

        var hoverIndex = this.xpHover !== null ? Math.min(m - 1, this.xpHover) : m - 1;
        var hoverDay = pts[hoverIndex];
        var breakdown = Object.keys(hoverDay.gains).map(function (name) {
          return { name: name, xpLabel: fmt(hoverDay.gains[name]), color: SKILL_COLORS[name] || OTHER_COLOR };
        }).sort(function (a, b) { return parseInt(b.xpLabel.replace(/,/g, "")) - parseInt(a.xpLabel.replace(/,/g, "")); });
        var dayTotal = breakdown.reduce(function (s, b) { return s + parseInt(b.xpLabel.replace(/,/g, "")); }, 0);
        var hoverDotsSvg = layers.map(function (l, li) {
          return '<circle cx="' + X(hoverIndex).toFixed(1) + '" cy="' + Y(stacks[hoverIndex][li]).toFixed(1) + '" r="2.5" style="fill:' + l.color + '"></circle>';
        }).join("");

        var selectionSvg = "";
        if (this.xpDragging && this.xpDragStart !== null && this.xpDragEnd !== null && this.xpDragStart !== this.xpDragEnd) {
          var sa = X(Math.min(this.xpDragStart, this.xpDragEnd));
          var sb = X(Math.max(this.xpDragStart, this.xpDragEnd));
          selectionSvg = '<rect x="' + sa.toFixed(1) + '" y="' + PT + '" width="' + (sb - sa).toFixed(1) + '" height="' + (H - PT - PB) +
            '" style="fill:rgba(240,198,103,.14);stroke:var(--gold-400);stroke-width:1"></rect>';
        }

        return {
          eyebrow: zoom ? (xpStamp(pts[0].t, "year") + " – " + xpStamp(pts[m - 1].t, "year")) : RANGE_LABEL[range],
          zoomed: !!zoom,
          legend: layers.map(function (l) { return { name: l.name, color: l.color }; }),
          layersSvg: layersSvg, gridSvg: gridSvg, grid: grid, xlabels: xlabels,
          hovering: this.xpHover !== null && !this.xpDragging, hx: X(hoverIndex).toFixed(1),
          hoverLeft: ((X(hoverIndex) / W) * 100).toFixed(2) + "%",
          hoverDotsSvg: hoverDotsSvg,
          stamp: xpStamp(hoverDay.t, range),
          dayTotal: fmt(dayTotal) + " xp",
          breakdown: breakdown,
          selectionSvg: selectionSvg,
          dragging: this.xpDragging && this.xpDragStart !== null && this.xpDragEnd !== null && this.xpDragStart !== this.xpDragEnd,
        };
      },

      get results() {
        var q = this.query.trim().toLowerCase();
        var list = q ? PLAYER_NAMES.filter(function (p) { return p[0].toLowerCase().indexOf(q) >= 0; }) : PLAYER_NAMES.slice(0, 6);
        return list.map(function (p) {
          var prof = profileFor(p[0]);
          return { name: p[0], meta: p[1], total: fmt(prof.totalLevel) };
        });
      },
      get noResults() { return this.query.trim().length > 0 && this.results.length === 0; },
    };
  };
})();
