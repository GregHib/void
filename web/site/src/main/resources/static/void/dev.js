// Developer panel data + Alpine components. The dashboard runs a small self-contained
// simulation (CPU/heap/tick/population random walks, ticking on an interval) so the live
// charts and console have something to animate without a server round-trip; the player
// workbench ships a mock account roster so search, selection and the variables/moderation
// filters have real data to operate on. Mirrors the mock-data-in-JS pattern in hiscores.js.

(function () {
  function clamp(v, min, max) { return Math.min(max, Math.max(min, v)); }
  function walk(v, step, min, max) { return clamp(v + (Math.random() - 0.5) * 2 * step, min, max); }
  function fmt(n) { return Math.round(n).toLocaleString('en-US'); }
  function clockNow() { return new Date().toTimeString().slice(0, 8); }

  // Mirrors components/Badge.kt's BadgeTone palette, for badges whose tone depends on
  // reactive data (the selected player's state) rather than being fixed at render time.
  window.DEV_BADGE_TONE = {
    Neutral: { bg: 'var(--umber-700)', fg: 'var(--parch-200)', bd: 'var(--border-strong)' },
    Gold: { bg: 'rgba(224,174,60,.14)', fg: 'var(--gold-300)', bd: 'var(--gold-600)' },
    Success: { bg: 'var(--feedback-success-bg)', fg: 'var(--feedback-success)', bd: 'var(--moss-600)' },
    Warning: { bg: 'var(--feedback-warning-bg)', fg: 'var(--feedback-warning)', bd: 'var(--gold-700)' },
    Danger: { bg: 'var(--feedback-danger-bg)', fg: 'var(--feedback-danger)', bd: 'var(--ember-600)' },
    Info: { bg: 'var(--feedback-info-bg)', fg: 'var(--feedback-info)', bd: 'var(--steel-600)' },
  };

  // Builds an SVG polyline path for [values] (scaled to min..max) across a 0..[width] by
  // 0..[height] viewBox, y-flipped so higher values plot nearer the top.
  function linePath(values, min, max, width, height) {
    if (!values.length) return '';
    var n = values.length, stepX = n > 1 ? width / (n - 1) : 0, span = (max - min) || 1;
    var out = '';
    for (var i = 0; i < n; i++) {
      var t = clamp((values[i] - min) / span, 0, 1);
      var y = (height - t * height).toFixed(1);
      out += (i === 0 ? 'M' : 'L') + (i * stepX).toFixed(1) + ' ' + y + ' ';
    }
    return out.trim();
  }

  // Same as [linePath] but closed down to the baseline, for a filled area under the line.
  function areaPath(values, min, max, width, height) {
    var line = linePath(values, min, max, width, height);
    if (!line) return '';
    var n = values.length, stepX = n > 1 ? width / (n - 1) : 0;
    var lastX = ((n - 1) * stepX).toFixed(1);
    return line + ' L' + lastX + ' ' + height + ' L0 ' + height + ' Z';
  }

  function push90(arr, v) {
    var out = arr.concat([v]);
    return out.length > 90 ? out.slice(out.length - 90) : out;
  }

  // ---------------------------------------------------------------- dashboard

  window.devDashboardApp = function () {
    return {
      live: true,
      consoleOpen: true,
      world: 9,
      cpu: [], heap: [], tickMs: [], pop: [], logins: [], log: [], cmd: '', hist: [], histIdx: -1,
      errorOpen: false, copied: false,
      errorSel: { level: '', tone: 'Neutral', time: '', text: '', meta: '' },
      hover: { chart: null, index: -1, x: 0, pct: 0 },

      init: function () {
        var cpu = [], heap = [], tick = [], pop = [], logins = [];
        var c = 34, h = 6.1, p = 1284;
        for (var i = 0; i < 90; i++) {
          c = walk(c, 5, 16, 78); cpu.push(c);
          h = walk(h, 0.22, 3.6, 9.8); heap.push(h);
          tick.push(this.sampleTick());
          p = walk(p, 11, 980, 1560); pop.push(Math.round(p));
          logins.push(14 + Math.round(Math.random() * 24));
        }
        this.cpu = cpu; this.heap = heap; this.tickMs = tick; this.pop = pop; this.logins = logins;
        this.log = [
          ['info', 'void-server 0.41.2 · revision 231 · commit 9f1c3ad'],
          ['ok', '[boot] world 9 listening on 43594 — 2,000 npc spawns, 41 regions'],
          ['info', '[auth] staff session opened for rotce (administrator)'],
          ['ok', '[persistence] connection pool ready — 12 connections'],
          ['warn', '[world 9] tick overrun 241 ms — npc respawn batch exceeded budget'],
          ['info', 'type help for the command list'],
        ].map(function (e) { return { level: e[0], text: e[1], time: clockNow() }; });
        var self = this;
        this.timer = setInterval(function () { if (self.live) self.step(); }, 1000);
      },

      sampleTick: function () {
        var spike = Math.random() < 0.06;
        return spike ? 110 + Math.random() * 170 : 32 + Math.random() * 22;
      },

      step: function () {
        this.cpu = push90(this.cpu, walk(this.cpu[this.cpu.length - 1] || 34, 5, 16, 78));
        this.heap = push90(this.heap, walk(this.heap[this.heap.length - 1] || 6, 0.22, 3.6, 9.8));
        this.tickMs = push90(this.tickMs, this.sampleTick());
        this.pop = push90(this.pop, Math.round(walk(this.pop[this.pop.length - 1] || 1284, 11, 980, 1560)));
        this.logins = push90(this.logins, 14 + Math.round(Math.random() * 24));
        if (Math.random() < 0.12) {
          this.appendLog(Math.random() < 0.25 ? 'warn' : 'info', this.ambientLine());
        }
      },

      ambientLine: function () {
        var lines = [
          '[world 9] npc respawn batch complete — 2,000 spawns in 41 ms',
          '[persistence] flushed 184 dirty accounts in 62 ms',
          '[login] player connected from world 12 · revision 231',
          '[world 9] tick overrun 214 ms — pathfinder queue depth 1,204',
          '[cache] index 2 re-read — 41 archives, 0 mismatches',
          '[gc] young collection 8 ms — heap ' + this.heapNow + ' / 12.0 GB',
          '[grand exchange] 1,204 offers matched this minute',
        ];
        return lines[Math.floor(Math.random() * lines.length)];
      },

      appendLog: function (level, text) {
        this.log = push90(this.log, { level: level, text: text, time: clockNow() });
        var self = this;
        this.$nextTick(function () {
          if (self.$refs.logEl) self.$refs.logEl.scrollTop = self.$refs.logEl.scrollHeight;
        });
      },

      clearLog: function () { this.log = []; },
      toggleConsole: function () { this.consoleOpen = !this.consoleOpen; },

      // Tracks mouse position over a chart as an index into its data array, snapped to the
      // nearest sample so the hover line lands on an actual plotted point rather than drifting
      // continuously with the cursor.
      onChartHover: function (event, chart, n) {
        var rect = event.currentTarget.getBoundingClientRect();
        var frac = rect.width ? clamp((event.clientX - rect.left) / rect.width, 0, 1) : 0;
        var idx = n > 1 ? Math.round(frac * (n - 1)) : 0;
        var stepX = n > 1 ? 300 / (n - 1) : 0;
        this.hover = { chart: chart, index: idx, x: idx * stepX, pct: n > 1 ? (idx / (n - 1)) * 100 : 0 };
      },
      onChartLeave: function () { this.hover.chart = null; },

      openError: function (level, tone, time, text, meta) {
        this.errorSel = { level: level, tone: tone, time: time, text: text, meta: meta };
        this.errorOpen = true;
      },
      copyError: function () {
        var e = this.errorSel, self = this;
        var payload = '[' + e.level + '] ' + e.time + ' — ' + e.text + ' (' + e.meta + ')';
        navigator.clipboard.writeText(payload).catch(function () {}).then(function () {
          self.copied = true;
          setTimeout(function () { self.copied = false; }, 1500);
        });
      },

      runCmd: function () {
        var text = this.cmd.trim();
        if (!text) return;
        this.appendLog('info', '> ' + text);
        this.hist.push(text);
        this.histIdx = this.hist.length;
        var word = text.split(/\s+/)[0].toLowerCase();
        if (word === 'help') {
          this.appendLog('ok', 'commands: help, clear, world <n>, kick <player>, broadcast <message>');
        } else if (word === 'clear') {
          this.clearLog();
        } else if (word === 'world') {
          this.appendLog('ok', 'switched console target to ' + (text.split(/\s+/)[1] || 'world 9'));
        } else if (word === 'kick') {
          this.appendLog('warn', (text.split(/\s+/)[1] || 'player') + ' disconnected by staff action');
        } else if (word === 'broadcast') {
          this.appendLog('ok', 'broadcast sent: "' + text.slice(text.indexOf(' ') + 1) + '"');
        } else {
          this.appendLog('warn', 'unknown command "' + word + '" — type help for the list');
        }
        this.cmd = '';
      },

      onCmdKey: function (event) {
        if (event.key === 'Enter') { this.runCmd(); return; }
        if (event.key === 'ArrowUp') {
          if (this.histIdx > 0) { this.histIdx--; this.cmd = this.hist[this.histIdx] || ''; }
          event.preventDefault();
        } else if (event.key === 'ArrowDown') {
          if (this.histIdx < this.hist.length) { this.histIdx++; this.cmd = this.hist[this.histIdx] || ''; }
          event.preventDefault();
        }
      },

      get cpuNow() { return Math.round(this.cpu[this.cpu.length - 1] || 0); },
      get heapNow() { return (this.heap[this.heap.length - 1] || 0).toFixed(1); },
      get rssNow() { return ((this.heap[this.heap.length - 1] || 0) + 1.4).toFixed(1); },
      get cpuPath() { return linePath(this.cpu, 0, 100, 300, 100); },
      get heapPath() { return linePath(this.heap, 0, 12, 300, 100); },
      get heapArea() { return areaPath(this.heap, 0, 12, 300, 100); },
      get hoverCpuLabel() {
        var i = this.hover.index;
        return Math.round(this.cpu[i] || 0) + '% CPU · ' + (this.heap[i] || 0).toFixed(1) + ' GB heap';
      },

      get tickNow() { return Math.round(this.tickMs[this.tickMs.length - 1] || 0) + ' ms'; },
      get tickAvg() {
        var arr = this.tickMs;
        return arr.length ? Math.round(arr.reduce(function (a, b) { return a + b; }, 0) / arr.length) + ' ms' : '0 ms';
      },
      get tickP95() {
        var sorted = this.tickMs.slice().sort(function (a, b) { return a - b; });
        var idx = Math.floor(sorted.length * 0.95);
        return sorted.length ? Math.round(sorted[Math.min(idx, sorted.length - 1)]) + ' ms' : '0 ms';
      },
      get tickMax() { return Math.round(this.tickMs.length ? Math.max.apply(null, this.tickMs) : 0) + ' ms'; },
      get tickOverruns() { return this.tickMs.filter(function (v) { return v > 200; }).length; },
      get tickPath() { return linePath(this.tickMs, 0, 300, 300, 100); },
      get tickArea() { return areaPath(this.tickMs, 0, 300, 300, 100); },
      get hoverTickLabel() { return Math.round(this.tickMs[this.hover.index] || 0) + ' ms'; },

      get popNow() { return fmt(this.pop[this.pop.length - 1] || 0); },
      get popPeak() { return fmt(this.pop.length ? Math.max.apply(null, this.pop) : 0); },
      get loginsNow() { return this.logins[this.logins.length - 1] || 0; },
      get popPath() { return linePath(this.pop, 900, 1600, 300, 100); },
      get popArea() { return areaPath(this.pop, 900, 1600, 300, 100); },
      get hoverPopLabel() {
        var i = this.hover.index;
        return fmt(this.pop[i] || 0) + ' online · ' + (this.logins[i] || 0) + ' logins/min';
      },
      // A single SVG path of little bar rectangles, rather than a `<template x-for>` of <rect>s —
      // Alpine's template cloning doesn't handle a <template> nested inside foreign (SVG) content.
      get loginBarsPath() {
        var arr = this.logins, n = arr.length, w = n > 1 ? 300 / n : 0, d = '';
        arr.forEach(function (v, i) {
          var h = clamp((v / 40) * 60, 2, 60), x = i * w, y = 100 - h, bw = Math.max(1, w - 1);
          d += 'M' + x.toFixed(1) + ' ' + y.toFixed(1) + 'h' + bw.toFixed(1) + 'v' + h.toFixed(1) + 'h' + (-bw).toFixed(1) + 'Z';
        });
        return d;
      },
    };
  };

  // ---------------------------------------------------------------- players

  var SKILL_NAMES = [
    'Attack', 'Strength', 'Defence', 'Ranged', 'Prayer', 'Magic', 'Runecrafting', 'Construction',
    'Hitpoints', 'Agility', 'Herblore', 'Thieving', 'Crafting', 'Fletching', 'Slayer', 'Hunter',
    'Mining', 'Smithing', 'Fishing', 'Cooking', 'Firemaking', 'Woodcutting', 'Farming',
  ];

  function skillSet(base, spread) {
    var out = [];
    SKILL_NAMES.forEach(function (name, i) {
      var v = clamp(Math.round(base + Math.sin(i * 1.7 + base) * spread), 1, 99);
      var rank = Math.max(1, Math.round(340000 - v * 3300 - Math.abs(Math.sin(i * 2.3 + base)) * 9000));
      out.push({ name: name, level: v, rank: rank });
    });
    return out;
  }

  function equipmentFor(id) {
    var sets = {
      'power-spark': [
        ['Head', 'Void mage helm'], ['Cape', 'Cooking cape (t)'], ['Amulet', 'Amulet of fury'],
        ['Weapon', 'Abyssal whip'], ['Body', 'Void knight top'], ['Shield', 'Dragon defender'],
        ['Legs', 'Void knight robe'], ['Hands', 'Barrows gloves'], ['Feet', 'Ranger boots'],
        ['Ring', 'Ring of wealth'], ['Ammunition', 'Rune arrow × 480'],
      ],
      'rotce': [
        ['Head', '—'], ['Cape', 'Staff of office'], ['Amulet', '—'], ['Weapon', 'Staff of office'],
        ['Body', 'Dev robe top'], ['Shield', '—'], ['Legs', 'Dev robe bottom'], ['Hands', '—'],
        ['Feet', '—'], ['Ring', '—'], ['Ammunition', '—'],
      ],
    };
    return (sets[id] || sets['power-spark']).map(function (e) { return { slot: e[0], item: e[1] }; });
  }

  function inventoryFor(seed) {
    var out = [];
    for (var i = 0; i < 28; i++) {
      var empty = (i * 7 + seed) % 3 === 0;
      out.push({ empty: empty, qty: empty ? 0 : 1 + ((i * 53 + seed * 11) % 4000) });
    }
    return out;
  }

  function bankFor(id) {
    if (id === 'rotce') {
      return [
        { item: 'Coins', qty: '999,999,999', value: '—' },
        { item: 'Test bond', qty: '99', value: '—' },
      ];
    }
    return [
      { item: 'Coins', qty: '1,912,447', value: '—' },
      { item: 'Nature rune', qty: '48,220', value: '10.3M gp' },
      { item: 'Abyssal whip', qty: '2', value: '5.1M gp' },
      { item: 'Shark', qty: '1,140', value: '1.3M gp' },
      { item: 'Rune bar', qty: '860', value: '10.9M gp' },
      { item: 'Dragon bones', qty: '2,411', value: '6.7M gp' },
      { item: 'Magic logs', qty: '740', value: '0.8M gp' },
    ];
  }

  function variablesFor(id) {
    var base = [
      { key: 'varbit.4607', label: 'tutorial stage', value: '12', type: 'varbit', scope: 'Account', updated: '2d ago' },
      { key: 'varbit.8063', label: 'slayer task streak', value: '41', type: 'varbit', scope: 'Account', updated: '18m ago' },
      { key: 'varp.101', label: 'quest points', value: '218', type: 'varp', scope: 'Account', updated: '3h ago' },
      { key: 'attr.slayer_task', label: 'Greater demon', value: '107 left', type: 'string', scope: 'Session', updated: '18m ago' },
      { key: 'attr.run_energy', label: 'run energy', value: '88', type: 'int', scope: 'Session', updated: '4s ago' },
      { key: 'attr.special_energy', label: 'special attack', value: '100', type: 'int', scope: 'Session', updated: '1m ago' },
      { key: 'config.xp_rate', label: 'xp multiplier', value: '5.0', type: 'double', scope: 'World', updated: 'on boot' },
      { key: 'config.pvp_enabled', label: 'pvp toggle', value: 'false', type: 'boolean', scope: 'World', updated: 'on boot' },
      { key: 'flag.muted', label: 'chat mute', value: 'false', type: 'boolean', scope: 'Account', updated: '—' },
      { key: 'flag.jailed', label: 'jail flag', value: 'false', type: 'boolean', scope: 'Account', updated: '—' },
      { key: 'pref.menu_swap', label: 'menu entry swapper', value: 'on', type: 'string', scope: 'Account', updated: '7d ago' },
      { key: 'stat.deaths', label: 'deaths total', value: '37', type: 'int', scope: 'Account', updated: '6d ago' },
      { key: 'session.ping', label: 'client ping', value: '38', type: 'int', scope: 'Session', updated: 'live' },
    ];
    if (id === 'rotce') {
      base = base.concat([{ key: 'flag.staff', label: 'staff account', value: 'true', type: 'boolean', scope: 'Account', updated: '1y ago' }]);
    }
    return base;
  }

  function activityFor(id) {
    if (id === 'sablewisp') {
      return [{ time: '14:08:22', action: 'Logout', detail: 'Session ended cleanly · no pending trade' }];
    }
    return [
      {
        time: '14:22:07', action: 'Trade offer created', detail: '4,200 × nature rune @ 214 gp · Grand Exchange slot 1',
        expand: ['Offer value: 898,800 gp', 'GE tax (1%): 8,988 gp', 'Matched: 0 / 4,200 so far', 'Offer id: 88213-1'],
      },
      { time: '14:18:44', action: 'Slayer task advanced', detail: 'Greater demon 112 → 107 · streak 41' },
      { time: '14:11:02', action: 'Teleported', detail: 'Varrock teleport · 3183, 3436, 0' },
      {
        time: '13:58:31', action: 'Item withdrawn', detail: '2 × abyssal whip from bank tab 3',
        expand: ['Estimated value: 5,148,200 gp', 'Bank tab: 3 (PvM gear)', 'Withdraw mode: note'],
      },
      { time: '13:44:12', action: 'Level gained', detail: 'Cooking 88 → 89 · 4,470,110 xp' },
      { time: '13:19:03', action: 'Login', detail: 'Client 0.41.2 · 89.44.12.— · revision 231' },
    ];
  }

  function auditFor(id) {
    return [
      { action: 'Mute lifted', reason: 'Appeal accepted · first offence', staff: 'kilnfast', time: '2 Sept 2026' },
      { action: 'Muted 24h', reason: 'Offensive language in public chat', staff: 'kilnfast', time: '1 Sept 2026' },
      { action: id === 'rotce' ? 'Staff role granted' : 'Name change approved', reason: id === 'rotce' ? 'Promoted to administrator' : 'Old name: PowerSpark2', staff: 'rotce', time: '14 Aug 2026' },
      { action: 'Account created', reason: '—', staff: 'system', time: '11 Mar 2024' },
    ];
  }

  var CHAT_TONE = { Public: 'var(--text-muted)', Clan: 'var(--gold-300)', Private: 'var(--steel-500)', Game: 'var(--moss-500)' };

  function chatFor(id) {
    if (id === 'sablewisp') return [];
    return [
      { time: '14:22:41', channel: 'Public', text: 'selling nature runes 214 each, 4k in stock' },
      { time: '14:21:08', channel: 'Clan', text: 'anyone up for a demon trip after ge run' },
      { time: '14:19:50', channel: 'Private', text: 'to Kilnfast: thanks for sorting the mute appeal' },
      { time: '14:17:32', channel: 'Game', text: 'Your slayer task is now 107 greater demons.' },
      { time: '14:12:10', channel: 'Public', text: 'ge prices still lagging behind the update' },
      { time: '14:08:45', channel: 'Clan', text: 'world 9 felt laggy for a minute there' },
      { time: '13:59:04', channel: 'Public', text: 'buying rune bars 12.7k' },
    ].map(function (c) { return Object.assign({ tint: CHAT_TONE[c.channel] }, c); });
  }

  function makePlayer(p) {
    var skills = skillSet(p.skillBase, p.skillSpread);
    var byName = {};
    skills.forEach(function (s) { byName[s.name] = s.level; });
    var totalLevel = skills.reduce(function (t, s) { return t + s.level; }, 0);
    return Object.assign({}, p, {
      skills: skills,
      totalLevel: totalLevel,
      combatSkills: ['Attack', 'Strength', 'Defence', 'Hitpoints'].map(function (n) { return { name: n, level: byName[n] }; }),
      combatFacts: [
        { k: 'Combat level', v: String(Math.round(3 + totalLevel / 8)) },
        { k: 'Wilderness level', v: p.wilderness || '—' },
        { k: 'Special energy', v: '100%' },
        { k: 'Prayer points', v: byName.Prayer + ' / ' + byName.Prayer },
      ],
      equipment: equipmentFor(p.id),
      inventory: inventoryFor(p.seed),
      bank: bankFor(p.id),
      variables: variablesFor(p.id),
      activityLog: activityFor(p.id),
      audit: auditFor(p.id),
      chat: chatFor(p.id),
    });
  }

  var PLAYERS = [
    makePlayer({
      id: 'power-spark', name: 'Power Spark', world: 9, state: 'Online', tone: 'Success', rank: 'Member',
      account: '1,284,551', region: 'Varrock · West bank', meta: '4h 12m', ip: '89.44.12.— masked',
      activity: 'Trading · Grand Exchange', detail: 'Offer 1 of 3 pending · 4,200 nature runes at 214 gp',
      xpRate: '41,200 xp/h', xpPct: 64, x: 3183, y: 3436, z: 0, skillBase: 68, skillSpread: 16, seed: 1,
    }),
    makePlayer({
      id: 'rotce', name: 'rotce', world: 30, state: 'Online', tone: 'Success', rank: 'Administrator',
      account: '1', region: 'Developer world · Lumbridge', meta: '19h 02m', ip: '10.0.0.— internal',
      activity: 'Idle · scripted test rig', detail: 'Attached debugger · packet trace on opcode 41',
      xpRate: '0 xp/h', xpPct: 0, x: 3222, y: 3218, z: 0, skillBase: 99, skillSpread: 0, seed: 2,
    }),
    makePlayer({
      id: 'lumbriwick', name: 'Lumbriwick', world: 12, state: 'Online', tone: 'Success', rank: 'Member',
      account: '1,301,882', region: 'Wilderness · level 24', meta: '52m', ip: '77.21.9.— masked',
      activity: 'Combat · fighting Greater demon', detail: 'Skulled · 3 kills this trip · risk 1.2M gp',
      xpRate: '88,400 xp/h', xpPct: 31, x: 3094, y: 3711, z: 0, wilderness: '24', skillBase: 60, skillSpread: 22, seed: 3,
    }),
    makePlayer({
      id: 'graveltoe', name: 'Graveltoe', world: 9, state: 'Restarting', tone: 'Warning', rank: 'Free',
      account: '1,299,004', region: 'Mining guild · Falador', meta: '2h 41m', ip: '212.5.78.— masked',
      activity: 'Mining · coal rocks', detail: 'Auto-retaliate off · inventory 26/28',
      xpRate: '22,900 xp/h', xpPct: 47, x: 3021, y: 9740, z: 0, skillBase: 42, skillSpread: 12, seed: 4,
    }),
    makePlayer({
      id: 'sablewisp', name: 'Sablewisp', world: 18, state: 'Offline', tone: 'Danger', rank: 'Member',
      account: '1,240,117', region: 'Last seen · Ardougne market', meta: '—', ip: '95.60.31.— masked',
      activity: 'Logged out 14 min ago', detail: 'Session ended cleanly · no pending trade',
      xpRate: '—', xpPct: 0, x: 2655, y: 3283, z: 0, skillBase: 55, skillSpread: 18, seed: 5,
    }),
    makePlayer({
      id: 'kilnfast', name: 'Kilnfast', world: 9, state: 'Online', tone: 'Success', rank: 'Moderator',
      account: '884,220', region: 'Karamja · volcano', meta: '6h 05m', ip: '88.19.44.— masked',
      activity: 'Skilling · smithing rune bars', detail: 'Furnace queue 84 bars · 12 min remaining',
      xpRate: '63,700 xp/h', xpPct: 78, x: 2857, y: 3168, z: 0, skillBase: 74, skillSpread: 10, seed: 6,
    }),
  ];

  window.devPlayersApp = function () {
    return {
      query: '', selected: 'power-spark', ptab: 'skills',
      varFilter: '', varScope: 'All scopes', bankFilter: '',
      modReason: 'Offensive language', modDuration: '48 hours', modNote: '',
      tpX: '', tpY: '', tpZ: '',

      init: function () { this.syncTeleport(); },
      syncTeleport: function () {
        var p = this.player;
        this.tpX = String(p.x); this.tpY = String(p.y); this.tpZ = String(p.z);
      },
      select: function (id) { this.selected = id; this.syncTeleport(); },
      searchEnter: function () {
        var top = this.filtered[0];
        if (top) this.select(top.id);
      },

      get filtered() {
        var q = this.query.trim().toLowerCase();
        return PLAYERS.filter(function (p) {
          return !q || p.name.toLowerCase().indexOf(q) >= 0 || p.id.indexOf(q) >= 0 ||
            p.account.toLowerCase().indexOf(q) >= 0 || p.ip.toLowerCase().indexOf(q) >= 0 ||
            String(p.world).indexOf(q) >= 0;
        });
      },
      get player() {
        var self = this;
        var direct = PLAYERS.filter(function (p) { return p.id === self.selected; })[0];
        if (direct) return direct;
        return this.filtered[0] || PLAYERS[0];
      },
      get resultRows() {
        var self = this;
        return this.filtered.map(function (p) {
          return { id: p.id, name: p.name, state: p.state, tone: p.tone, meta: 'world ' + p.world + ' · ' + p.meta, selected: p.id === self.player.id };
        });
      },

      get filteredVariables() {
        var self = this, q = this.varFilter.trim().toLowerCase(), scope = this.varScope;
        return this.player.variables.filter(function (v) {
          return (scope === 'All scopes' || v.scope === scope) &&
            (!q || v.key.toLowerCase().indexOf(q) >= 0 || v.label.toLowerCase().indexOf(q) >= 0);
        });
      },
      resetVarFilter: function () { this.varFilter = ''; this.varScope = 'All scopes'; },

      get filteredBank() {
        var q = this.bankFilter.trim().toLowerCase();
        return this.player.bank.filter(function (b) {
          return !q || b.item.toLowerCase().indexOf(q) >= 0;
        });
      },

      get inventoryUsed() { return this.player.inventory.filter(function (i) { return !i.empty; }).length; },
      get locationRows() {
        var p = this.player;
        return [
          { k: 'Region', v: p.region },
          { k: 'Coordinates', v: p.x + ', ' + p.y + ', plane ' + p.z },
          { k: 'World', v: p.world + ' · voidmmo-eu-1' },
          { k: 'IP', v: p.ip },
          { k: 'Session', v: p.meta },
        ];
      },
    };
  };
})();
