// Developer panel data + Alpine components. The dashboard runs a small self-contained
// simulation (CPU/heap/tick/population random walks, ticking on an interval) so the live
// charts and console have something to animate without a server round-trip - that telemetry
// only exists inside a running game server's JVM, which this static site has no channel into.
// The player workbench is real: it's fetched live from `/api/v1/dev/players/*`, the same way
// hiscores.js drives the hiscores page. Those endpoints only see what's been saved to disk,
// so there's no live online/offline state, position, chat or moderation history - see the
// DevService class doc for why.

(function () {
  function clamp(v, min, max) { return Math.min(max, Math.max(min, v)); }
  function walk(v, step, min, max) { return clamp(v + (Math.random() - 0.5) * 2 * step, min, max); }
  var fmt = window.voidFmt;
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

  var API = '/api/v1';

  function getJson(url) {
    return fetch(url).then(function (response) {
      if (!response.ok) throw new Error('Request to ' + url + ' failed: ' + response.status);
      return response.json();
    });
  }

  var RIGHTS_LABEL = { admin: 'Administrator', mod: 'Moderator', none: 'Member' };
  var RIGHTS_TONE = { admin: 'Gold', mod: 'Info', none: 'Neutral' };

  var fmt = window.voidFmt;
  function formatDate(iso) {
    if (!iso) return null;
    try {
      return new Date(iso).toLocaleString('en-US', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (e) { return null; }
  }

  function emptyPlayer(name) {
    return {
      name: name || '', rights: 'none', tone: 'Neutral', state: 'No live session', rank: 'Member',
      account: name || '', world: '—',
      combatSkills: [], combatFacts: [], skills: [],
      equipment: [], inventory: [], bank: [], variables: [],
      activityLog: [], audit: [], chat: [],
      activity: 'No live session data', detail: 'This panel only reflects the account’s last save to disk.',
      xpRate: '—', xpPct: 0, x: 0, y: 0, z: 0,
    };
  }

  window.devPlayersApp = function () {
    return {
      query: '', selectedName: '', ptab: 'skills',
      varFilter: '', varScope: 'All scopes', bankFilter: '',
      modReason: 'Offensive language', modDuration: '48 hours', modNote: '',
      tpX: '', tpY: '', tpZ: '',
      resultRows: [], player: emptyPlayer(''), loading: false,

      init: function () {
        var params = new URLSearchParams(window.location.search);
        var player = params.get('player');
        var self = this;
        this.fetchSearch('').then(function () {
          var top = self.resultRows.filter(function (r) { return player && r.name.toLowerCase() === player.toLowerCase(); })[0] || self.resultRows[0];
          if (top) self.select(top.name);
        });
      },

      fetchSearch: function (q) {
        var self = this;
        var params = new URLSearchParams({ limit: 30 });
        if (q) params.set('q', q);
        return getJson(API + '/dev/players/search?' + params).then(function (data) {
          self.resultRows = data.items.map(function (row) {
            return {
              id: row.name, name: row.name, meta: row.meta,
              state: RIGHTS_LABEL[row.rights] || 'Member', tone: RIGHTS_TONE[row.rights] || 'Neutral',
              selected: row.name === self.player.name,
            };
          });
        }).catch(function () { self.resultRows = []; });
      },

      search: function () { this.fetchSearch(this.query.trim()); },
      searchEnter: function () {
        var self = this;
        this.fetchSearch(this.query.trim()).then(function () {
          var top = self.resultRows[0];
          if (top) self.select(top.name);
        });
      },

      select: function (name) {
        var self = this;
        this.selectedName = name;
        this.resultRows = this.resultRows.map(function (r) { return Object.assign({}, r, { selected: r.name === name }); });
        this.loading = true;
        var encoded = encodeURIComponent(name);
        return Promise.all([
          getJson(API + '/dev/players/' + encoded),
          getJson(API + '/dev/players/' + encoded + '/skills'),
          getJson(API + '/dev/players/' + encoded + '/inventories'),
          getJson(API + '/dev/players/' + encoded + '/variables'),
          getJson(API + '/dev/players/' + encoded + '/events?pageSize=30'),
        ]).then(function (results) {
          var overview = results[0], skills = results[1], inventories = results[2], variables = results[3], events = results[4];
          var byName = {};
          skills.items.forEach(function (s) { byName[s.name] = s.level; });
          self.player = {
            name: overview.name, rights: overview.rights,
            tone: 'Neutral', state: 'No live session', rank: RIGHTS_LABEL[overview.rights] || 'Member',
            account: overview.name, world: '—',
            combatSkills: ['Attack', 'Strength', 'Defence', 'Constitution'].map(function (n) {
              return { name: n === 'Constitution' ? 'Hitpoints' : n, level: byName[n] || 1 };
            }),
            combatFacts: [
              { k: 'Combat level', v: String(overview.combatLevel) },
              { k: 'Quest points', v: String(overview.questPoints) },
              { k: 'Boss kills', v: fmt(overview.bossKills) },
              { k: 'Playtime', v: overview.playtimeHours.toFixed(1) + ' h' },
            ],
            skills: skills.items,
            equipment: inventories.equipment,
            inventory: Array.from({ length: inventories.inventorySize }, function (_, idx) {
              var stack = inventories.inventory.filter(function (i) { return i.slot === idx; })[0];
              return stack ? { empty: false, qty: stack.amount, name: stack.name } : { empty: true, qty: 0 };
            }),
            bank: inventories.bank.map(function (b) { return { item: b.name, qty: fmt(b.amount), value: '—' }; }),
            variables: variables.items.map(function (v) { return { key: v.key, label: v.type, value: v.value, type: v.type, scope: 'Account', updated: '—' }; }),
            activityLog: events.items.map(function (e) {
              return { time: formatDate(e.occurredAt) || e.occurredAt, action: e.title, detail: e.description || '' };
            }),
            audit: [],
            chat: [],
            activity: 'No live session data',
            detail: 'This panel only reflects the account’s last save to disk — there is no live connection to a running game world.',
            xpRate: '—', xpPct: 0,
            x: overview.tile.x, y: overview.tile.y, z: overview.tile.plane,
          };
          self.loading = false;
          self.syncTeleport();
        }).catch(function () {
          self.player = emptyPlayer(name);
          self.loading = false;
        });
      },

      syncTeleport: function () {
        var p = this.player;
        this.tpX = String(p.x); this.tpY = String(p.y); this.tpZ = String(p.z);
      },

      get filteredVariables() {
        var self = this, q = this.varFilter.trim().toLowerCase(), scope = this.varScope;
        return this.player.variables.filter(function (v) {
          return (scope === 'All scopes' || v.scope === scope) &&
            (!q || v.key.toLowerCase().indexOf(q) >= 0);
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
          { k: 'Tile', v: p.x + ', ' + p.y + ', plane ' + p.z },
          { k: 'World', v: p.world },
          { k: 'Session', v: 'No live session data' },
        ];
      },
    };
  };
})();
