// Adventurer's log page data + Alpine component. A self-contained mock dataset stands in for
// the profile API: one deterministic account per name (seeded from the name itself) so the log
// has something realistic to browse and the "Find a log" search has other accounts to switch to.

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
  function mmss(sec) {
    var total = Math.round(sec);
    var m = Math.floor(total / 60), s = total % 60;
    return m + ":" + String(s).padStart(2, "0");
  }
  function dateAgo(days) { return new Date(TODAY - days * DAY).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }); }
  function band(i) { return BAND[i % 2]; }
  function skillIcon(name) { return "void/images/skills/" + (name === "Constitution" ? "hitpoints" : name.toLowerCase()) + ".png"; }
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
      events.push({ kind: "Skill", tone: "gold", text: "Reached level 99 " + skills.filter(function (k) { return k.level >= k.max; })[0].name + "." });
    }
    events.push({ kind: "Skill", tone: "gold", text: "Total level passed " + (Math.floor(totalLevel / 100) * 100) + "." });
    if (completedQuests.length > 0) {
      var q0 = completedQuests[0];
      events.push({ kind: "Quest", tone: "info", text: "Completed " + q0.name + ". +" + Math.round(q0.name.length / 3) + " quest points." });
    }
    if (topBoss.kills !== "0") {
      events.push({ kind: "Combat", tone: "danger", text: "First kill: " + topBoss.name + ", solo." });
    }
    events.push({ kind: "Account", tone: "success", text: member ? "Membership renewed for 12 months." : "Playing on a free account." });
    if (completedQuests.length > 1) {
      events.push({ kind: "Quest", tone: "info", text: "Completed " + completedQuests[1].name + "." });
    }
    events.push({ kind: "Skill", tone: "gold", text: topSkill.name + " reached level " + topSkill.level + "." });
    var clanName = clanFor(name);
    if (clanName) {
      events.push({ kind: "Account", tone: "success", text: "Joined the clan " + clanName + " as a member." });
    }
    events = events.map(function (e, i) {
      return {
        kind: e.kind, tone: e.tone, text: e.text,
        date: dateAgo(2 + i * (3 + Math.floor(r() * 5))),
        band: band(i),
      };
    });

    var joinedDaysAgo = 200 + Math.floor(r() * 900);

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
