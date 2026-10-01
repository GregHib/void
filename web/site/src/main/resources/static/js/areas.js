// Area polygon editor for the world map (see WorldMap.kt's "Areas" panels).
//
// Areas live in `*.areas.toml` files under `data/` (read by the game's Areas.load): one `[name]`
// section per area with `x` and `y` arrays of tile coordinates, an optional `level` (left out, the
// area covers every level), optional `tags`, and any other keys (a `hint`, params) the content
// reads. Two points make a rectangle between them, three or more a polygon, both inclusive of their
// edges. (One point is a one-tile rectangle.)
//
// The page is built with every area the server loads baked in (`window.VOID_AREAS`) for the polygon
// layer and search to draw from. Loading files swaps that for what's in them, editable, and saved
// back to the same files (or downloaded, in a browser that can't write them) — the same way the nav
// graph editor (navgraph.js) works, with mapedit.js holding what the two share and deciding which
// one the pointer goes to while both are showing.
//
// As there, a save edits the file's own text rather than re-serialising it: `parseAreaFile` splits
// it into one chunk per section (taking the comment lines directly above a header with it) and
// remembers where each key's value sits, and `areaText` rewrites only the values that changed.
// Untouched sections, comments, key order and extra keys survive a save as they were.
(function () {
  var T = window.VoidToml;
  // Screen-space hit radius, in CSS pixels, for picking a vertex or edge midpoint handle.
  var HANDLE_HIT_PX = 8;
  // A press that travels further than this before release is a pan, not a click.
  var CLICK_SLOP_PX = 4;
  // Edges shorter than this on screen get no midpoint handle — it'd sit on top of the vertices.
  var MID_MIN_PX = 28;
  var UNDO_LIMIT = 100;
  // The keys the editor manages itself, in the order the existing files write them. Everything
  // else in a section is an "extra", edited as a raw key/value pair.
  var SHAPE_KEYS = ['x', 'y', 'level', 'tags'];
  var NAME_RE = /^[A-Za-z0-9_-]+$/;

  // --- Parsing -------------------------------------------------------------------------------

  // The top-level lines of an areas file: `[name]` headers, `key = value` pairs (a bracketed value
  // may run over several lines, and a trailing comment stays with its pair), comments and blank
  // lines — each with its span in `text`.
  function scanLines(text) {
    var out = [];
    var i = 0;
    while (i < text.length) {
      var lineEnd = text.indexOf('\n', i);
      lineEnd = lineEnd === -1 ? text.length : lineEnd + 1;
      var line = text.slice(i, lineEnd);
      var trimmed = line.trim();
      var lead = line.length - line.replace(/^[ \t]+/, '').length;
      if (trimmed === '' || trimmed[0] === '#') {
        out.push({ type: trimmed === '' ? 'blank' : 'comment', start: i, end: lineEnd });
        i = lineEnd;
        continue;
      }
      if (trimmed[0] === '[') {
        var header = /^\[[ \t]*("(?:[^"\\]|\\.)*"|[^\]\s]+)[ \t]*\]/.exec(trimmed);
        if (!header) {
          throw new Error('Unreadable section header: ' + trimmed.slice(0, 60));
        }
        var nameStart = i + lead + header[0].indexOf(header[1]);
        out.push({ type: 'header', name: T.stringValue(header[1]), start: i, end: lineEnd, nameStart: nameStart, nameEnd: nameStart + header[1].length });
        i = lineEnd;
        continue;
      }
      var pair = /^([A-Za-z0-9_-]+|"[^"]*")[ \t]*=[ \t]*/.exec(line.slice(lead));
      if (!pair) {
        throw new Error('Unreadable line: ' + trimmed.slice(0, 60));
      }
      var valueStart = i + lead + pair[0].length;
      var valueEnd;
      var c = text[valueStart];
      if (c === '[' || c === '{') {
        valueEnd = T.matchBracket(text, valueStart);
      } else if (c === '"' || c === "'") {
        valueEnd = T.skipString(text, valueStart);
      } else {
        valueEnd = valueStart;
        while (valueEnd < text.length && !/[\r\n#]/.test(text[valueEnd])) {
          valueEnd++;
        }
        while (valueEnd > valueStart && /[ \t]/.test(text[valueEnd - 1])) {
          valueEnd--;
        }
      }
      var end = text.indexOf('\n', valueEnd);
      end = end === -1 ? text.length : end + 1;
      out.push({ type: 'pair', key: T.stringValue(pair[1]), start: i, end: end, valueStart: valueStart, valueEnd: valueEnd });
      i = end;
    }
    return out;
  }

  function ints(raw, what) {
    return T.splitArray(raw).map(function (v) {
      var n = parseInt(v, 10);
      if (isNaN(n)) {
        throw new Error(what + ': "' + v + '" isn\'t a number');
      }
      return n;
    });
  }

  // The part of an area the editor changes — also what an undo checkpoint copies.
  function stateOf(area) {
    return {
      name: area.name,
      x: area.x.slice(),
      y: area.y.slice(),
      level: area.level,
      tags: area.tags.slice(),
      extras: area.extras.map(function (e) {
        return { key: e.key, value: e.value };
      }),
    };
  }

  function applyState(area, state) {
    area.name = state.name;
    area.x = state.x.slice();
    area.y = state.y.slice();
    area.level = state.level;
    area.tags = state.tags.slice();
    area.extras = state.extras.map(function (e) {
      return { key: e.key, value: e.value };
    });
  }

  // `pre` is whatever comes before the first section; each area keeps its own chunk of the text
  // (its header up to the next area's chunk) with every span in it relative to that chunk.
  function parseAreaFile(text) {
    var lines = scanLines(text);
    var eol = text.indexOf('\r\n') !== -1 ? '\r\n' : '\n';
    var headers = [];
    lines.forEach(function (line, i) {
      if (line.type === 'header') {
        headers.push(i);
      }
    });
    // A comment block sitting directly on a header (no blank line between) is about that area, so
    // it moves or goes with it.
    var starts = headers.map(function (h) {
      var k = h;
      while (k > 0 && lines[k - 1].type === 'comment') {
        k--;
      }
      return k;
    });
    var areas = [];
    for (var n = 0; n < headers.length; n++) {
      var base = lines[starts[n]].start;
      var chunkEnd = n + 1 < headers.length ? lines[starts[n + 1]].start : text.length;
      var header = lines[headers[n]];
      var pairs = [];
      var last = n + 1 < headers.length ? starts[n + 1] : lines.length;
      for (var l = headers[n] + 1; l < last; l++) {
        if (lines[l].type === 'pair') {
          var p = lines[l];
          pairs.push({ key: p.key, start: p.start - base, end: p.end - base, valueStart: p.valueStart - base, valueEnd: p.valueEnd - base });
        }
      }
      var chunk = text.slice(base, chunkEnd);
      var area = {
        text: chunk,
        headerEnd: header.end - base,
        nameSpan: { start: header.nameStart - base, end: header.nameEnd - base },
        pairs: pairs,
        name: header.name,
        x: [],
        y: [],
        level: null,
        tags: [],
        extras: [],
        added: false,
      };
      pairs.forEach(function (pair) {
        var raw = chunk.slice(pair.valueStart, pair.valueEnd);
        if (pair.key === 'x' || pair.key === 'y') {
          area[pair.key] = ints(raw, header.name + '.' + pair.key);
        } else if (pair.key === 'level') {
          area.level = parseInt(raw, 10);
        } else if (pair.key === 'tags') {
          area.tags = T.splitArray(raw).map(T.stringValue);
        } else {
          area.extras.push({ key: pair.key, value: T.displayValue(T.oneLine(raw)) });
        }
      });
      // Like Areas.load: up to two x values is a rectangle over both arrays' min and max, whatever
      // y's length; more is a polygon, which needs a y for every x.
      if (!area.x.length || !area.y.length || (area.x.length > 2 && area.x.length !== area.y.length)) {
        throw new Error('[' + header.name + '] needs x and y arrays of the same length');
      }
      area.orig = stateOf(area);
      areas.push(area);
    }
    return { pre: text.slice(0, headers.length ? lines[starts[0]].start : text.length), areas: areas, eol: eol };
  }

  // --- Serialising ---------------------------------------------------------------------------

  function formatName(name) {
    return NAME_RE.test(name) ? name : JSON.stringify(name);
  }

  function formatValue(key, state) {
    if (key === 'x' || key === 'y') {
      return '[' + state[key].join(', ') + ']';
    }
    if (key === 'level') {
      return String(state.level);
    }
    return '[' + state.tags.map(function (tag) {
      return JSON.stringify(tag);
    }).join(', ') + ']';
  }

  function hasKey(key, state) {
    return key === 'level' ? state.level !== null : key === 'tags' ? state.tags.length > 0 : true;
  }

  function sameValue(key, a, b) {
    return JSON.stringify(a[key]) === JSON.stringify(b[key]);
  }

  // A brand-new section, written the way the existing files are: shape keys first, then extras.
  // `serializeAreaFile` puts the blank line before it.
  function freshText(state, eol) {
    var text = '[' + formatName(state.name) + ']' + eol;
    SHAPE_KEYS.forEach(function (key) {
      if (hasKey(key, state)) {
        text += key + ' = ' + formatValue(key, state) + eol;
      }
    });
    state.extras.forEach(function (extra) {
      if (extra.key) {
        text += extra.key + ' = ' + T.tomlValue(extra.value) + eol;
      }
    });
    return text;
  }

  // The area's chunk with only what changed rewritten: a renamed header, a changed value replaced
  // in place, a removed key's line dropped and an added key's line inserted where it belongs.
  function areaText(area, eol) {
    var now = stateOf(area);
    if (area.added) {
      return freshText(now, eol);
    }
    var orig = area.orig;
    if (JSON.stringify(now) === JSON.stringify(orig)) {
      return area.text;
    }
    var text = area.text;
    var ops = [];
    function insert(at, line) {
      ops.push({ start: at, end: at, text: line + eol, insert: true });
    }
    if (now.name !== orig.name) {
      ops.push({ start: area.nameSpan.start, end: area.nameSpan.end, text: formatName(now.name) });
    }
    function pairFor(key) {
      return area.pairs.filter(function (p) {
        return p.key === key;
      })[0];
    }
    var at = area.headerEnd;
    SHAPE_KEYS.forEach(function (key) {
      var pair = pairFor(key);
      var has = hasKey(key, now);
      if (pair) {
        if (!has) {
          ops.push({ start: pair.start, end: pair.end, text: '' });
        } else if (!sameValue(key, now, orig)) {
          ops.push({ start: pair.valueStart, end: pair.valueEnd, text: formatValue(key, now) });
        }
        at = Math.max(at, pair.end);
      } else if (has) {
        insert(at, key + ' = ' + formatValue(key, now));
      }
    });
    // Extras are matched up by key: one whose key is gone is removed, one with a new key added
    // after the last line of the section.
    var seen = {};
    var lastPairEnd = area.pairs.reduce(function (end, p) {
      return Math.max(end, p.end);
    }, area.headerEnd);
    area.pairs.forEach(function (pair) {
      if (SHAPE_KEYS.indexOf(pair.key) !== -1 || seen[pair.key]) {
        return;
      }
      seen[pair.key] = true;
      var before = orig.extras.filter(function (e) {
        return e.key === pair.key;
      })[0];
      var after = now.extras.filter(function (e) {
        return e.key === pair.key;
      })[0];
      if (!after) {
        ops.push({ start: pair.start, end: pair.end, text: '' });
      } else if (!before || before.value !== after.value) {
        ops.push({ start: pair.valueStart, end: pair.valueEnd, text: T.tomlValue(after.value) });
      }
    });
    now.extras.forEach(function (extra) {
      if (extra.key && !seen[extra.key]) {
        seen[extra.key] = true;
        insert(lastPairEnd, extra.key + ' = ' + T.tomlValue(extra.value));
      }
    });
    // Applied back to front so earlier spans stay valid. At the same position a replacement or
    // removal goes before an insertion (so the insertion isn't swallowed by it), and insertions at
    // the same point keep the order they were made in.
    ops.forEach(function (op, i) {
      op.i = i;
      if (op.insert) {
        // A line inserted after one without a line break (the last line of the file) needs one of
        // its own first — judged by the line that'll actually precede it, past any being removed.
        var at = op.start;
        for (var k = 0; k < ops.length; k++) {
          if (!ops[k].insert && !ops[k].text && ops[k].end === at && ops[k].start < at) {
            at = ops[k].start;
            k = -1;
          }
        }
        if (at > 0 && text[at - 1] !== '\n') {
          op.text = eol + op.text;
        }
      }
    });
    ops.sort(function (a, b) {
      return b.start - a.start || b.end - a.end || b.i - a.i;
    });
    ops.forEach(function (op) {
      text = text.slice(0, op.start) + op.text + text.slice(op.end);
    });
    return text;
  }

  function serializeAreaFile(file) {
    var out = file.pre;
    file.areas.forEach(function (area) {
      if (area.added && out) {
        if (!/\n$/.test(out)) {
          out += file.eol;
        }
        if (!/\n[ \t]*\r?\n$/.test(out)) {
          out += file.eol;
        }
      }
      out += areaText(area, file.eol);
    });
    return out;
  }

  // --- Geometry ------------------------------------------------------------------------------

  function isRect(area) {
    return area.x.length <= 2;
  }

  function bounds(xs, ys) {
    return { minX: Math.min.apply(null, xs), maxX: Math.max.apply(null, xs), minY: Math.min.apply(null, ys), maxY: Math.max.apply(null, ys) };
  }

  // The points drawn and dragged: a rectangle's four corners (south-west first, anticlockwise, the
  // same order WorldMap.kt expands the baked ones in), or a polygon's own vertices.
  function vertices(area) {
    if (isRect(area)) {
      var b = bounds(area.x, area.y);
      return [
        { x: b.minX, y: b.minY },
        { x: b.maxX, y: b.minY },
        { x: b.maxX, y: b.maxY },
        { x: b.minX, y: b.maxY },
      ];
    }
    return area.x.map(function (x, i) {
      return { x: x, y: area.y[i] };
    });
  }

  function toPolygon(area) {
    var v = vertices(area);
    area.x = v.map(function (p) {
      return p.x;
    });
    area.y = v.map(function (p) {
      return p.y;
    });
  }

  // Moves vertex `index` to `tile`, returning its index afterwards. A rectangle stays one: the
  // dragged corner moves and the opposite one stays put, which can flip which corner it is.
  function moveVertex(area, index, tile) {
    if (!isRect(area)) {
      area.x[index] = tile.x;
      area.y[index] = tile.y;
      return index;
    }
    var v = vertices(area);
    var opposite = v[(index + 2) % 4];
    var wasMaxX = index === 1 || index === 2;
    var wasMaxY = index === 2 || index === 3;
    area.x = [Math.min(tile.x, opposite.x), Math.max(tile.x, opposite.x)];
    area.y = [Math.min(tile.y, opposite.y), Math.max(tile.y, opposite.y)];
    var maxX = tile.x === opposite.x ? wasMaxX : tile.x > opposite.x;
    var maxY = tile.y === opposite.y ? wasMaxY : tile.y > opposite.y;
    return maxY ? (maxX ? 2 : 3) : maxX ? 1 : 0;
  }

  // The server's own test (Polygon.pointInPolygon), integer division and all, so the editor agrees
  // with the game about exactly which tiles are in: inside, or anywhere on an edge.
  function contains(xs, ys, x, y) {
    var inside = false;
    for (var i = 0, j = xs.length - 1; i < xs.length; j = i++) {
      var dxl = xs[j] - xs[i];
      var dyl = ys[j] - ys[i];
      if (ys[i] > y !== ys[j] > y && x < Math.trunc((dxl * (y - ys[i])) / dyl) + xs[i]) {
        inside = !inside;
      }
      if ((x - xs[i]) * dyl - (y - ys[i]) * dxl === 0) {
        if (Math.abs(dxl) >= Math.abs(dyl)) {
          if (x >= Math.min(xs[i], xs[j]) && x <= Math.max(xs[i], xs[j])) {
            return true;
          }
        } else if (y >= Math.min(ys[i], ys[j]) && y <= Math.max(ys[i], ys[j])) {
          return true;
        }
      }
    }
    return inside;
  }

  window.VoidAreas = { parse: parseAreaFile, serialize: serializeAreaFile, contains: contains };

  // --- Alpine methods --------------------------------------------------------------------------

  // Mixed into `worldMapApp()`'s data object. As with the nav graph, the files and undo history live
  // out here in the closure rather than on the (Alpine-proxied) object — file handles can't be
  // called through a Proxy, and object identity is how the selection is tracked — and the panels
  // only read the plain summaries `syncAreaState` copies across.
  window.areaEditorMethods = function () {
    var ar = {
      files: [],
      // What the polygon layer, hover readout and search draw from once files are loaded:
      // `{ name, minLevel, maxLevel, x, y }` like `window.VOID_AREAS`, rebuilt on every edit.
      shapes: [],
      selected: null,
      // Index into the selected area's `vertices` of the selected vertex, or -1.
      vertex: -1,
      selSig: null,
      undo: [],
      press: null,
      drag: null,
      // Tiles of the area being drawn, or null when not drawing.
      draft: null,
      hover: null,
      panKey: false,
    };

    return {
      areaLoaded: false,
      areaPanelOpen: true,
      areaEditPanelOpen: true,
      areaFiles: [],
      // The file new areas go into. Follows the selection.
      areaTarget: 0,
      areaDirty: false,
      areaCanUndo: false,
      areaStats: '',
      areaError: '',
      areaNotice: '',
      // The Area panel's copy of the selected area (null when none), and every tag in use, for the
      // tag field's suggestions.
      areaSel: null,
      areaTags: [],
      // Points placed so far while drawing a new area, or -1 when not drawing.
      areaDrawing: -1,

      areaBoot: function () {
        var self = this;
        window.addEventListener('keydown', function (e) {
          self.areaKeyDown(e);
        });
        window.addEventListener('keyup', function (e) {
          if (e.key === ' ' && ar.panKey) {
            e.preventDefault();
            ar.panKey = false;
            self.areaUpdateCursor(null);
          }
        });
        window.addEventListener('blur', function () {
          ar.panKey = false;
        });
        window.addEventListener('beforeunload', function (e) {
          if (self.areaDirty) {
            e.preventDefault();
            e.returnValue = '';
          }
        });
      },

      // What the polygon layer draws: the loaded files' areas, or the ones baked into the page.
      areaShapes: function () {
        return this.areaLoaded ? ar.shapes : window.VOID_AREAS || [];
      },

      // --- Files ---

      // Opening a file that's already open (by path) replaces it rather than doubling its areas.
      areaAddFiles: function (loaded) {
        var errors = [];
        loaded.forEach(function (entry) {
          var parsed;
          try {
            parsed = parseAreaFile(entry.text);
          } catch (e) {
            errors.push(entry.name + ': ' + e.message);
            return;
          }
          parsed.name = entry.name;
          parsed.path = entry.path;
          parsed.handle = entry.handle;
          parsed.dirty = false;
          var existing = ar.files.findIndex(function (file) {
            return file.path === entry.path;
          });
          if (existing === -1) {
            ar.files.push(parsed);
          } else {
            ar.files[existing] = parsed;
          }
        });
        this.areaError = errors.join('\n');
        this.areaNotice = '';
        ar.selected = null;
        ar.undo = [];
        if (ar.files.length) {
          this.showAreaPolygons = true;
        }
        this.areaRebuild();
      },

      closeAreaFiles: function () {
        if (this.areaDirty && !window.confirm('Discard unsaved area changes?')) {
          return;
        }
        ar.files = [];
        ar.selected = null;
        ar.draft = null;
        ar.undo = [];
        this.showAreaPolygons = false;
        this.areaError = '';
        this.areaNotice = '';
        this.areaTarget = 0;
        this.areaRebuild();
      },

      // Closes just the selected file, leaving the others open.
      closeAreaFile: function () {
        var file = ar.files[this.areaTarget];
        if (!file) {
          return;
        }
        if (file.dirty && !window.confirm('Discard unsaved changes to ' + file.name + '?')) {
          return;
        }
        ar.files.splice(this.areaTarget, 1);
        ar.selected = null;
        ar.draft = null;
        ar.undo = [];
        this.areaError = '';
        this.areaNotice = '';
        this.areaTarget = 0;
        if (!ar.files.length) {
          this.showAreaPolygons = false;
        }
        this.areaRebuild();
      },

      areaPickFile: function (index) {
        this.areaTarget = index;
      },

      // Adds an empty `*.areas.toml` — see `VoidMapFiles.create`.
      areaNewFile: function () {
        var self = this;
        VoidMapFiles.create('area', '')
          .then(function (created) {
            if (!created) {
              return;
            }
            if (ar.files.some(function (file) {
              return file.path === created.name;
            })) {
              self.areaError = created.name + ' is already open.';
              return;
            }
            var parsed = parseAreaFile('');
            parsed.name = created.name;
            parsed.path = created.name;
            parsed.handle = created.handle;
            parsed.dirty = created.dirty;
            ar.files.push(parsed);
            ar.undo = [];
            self.areaError = created.warning;
            self.showAreaPolygons = true;
            self.setMapEditor('area');
            self.areaTarget = ar.files.length - 1;
            self.areaRebuild();
          })
          .catch(function (e) {
            self.areaError = String(e.message || e);
          });
      },

      // One file at a time, for the same reason as `saveNavFiles`: a second permission prompt while
      // the first is showing is rejected outright.
      saveAreaFiles: function () {
        this.areaSave(null);
      },

      // Just the file new areas go into (the highlighted row).
      saveAreaFile: function () {
        this.areaSave(ar.files[this.areaTarget] || null);
      },

      // `only`: the one file to write, or null for every changed file.
      areaSave: function (only) {
        var self = this;
        var written = [];
        var downloaded = [];
        var chain = Promise.resolve();
        var selectedName = ar.selected ? ar.selected.name : null;
        ar.files.forEach(function (file) {
          if (!file.dirty || (only && file !== only)) {
            return;
          }
          chain = chain.then(function () {
            var text = serializeAreaFile(file);
            return VoidMapFiles.write(file, text).then(function () {
              (file.handle ? written : downloaded).push(file.name);
              // Re-parse what was written so the next save diffs against it, not the original.
              var parsed = parseAreaFile(text);
              file.pre = parsed.pre;
              file.areas = parsed.areas;
              file.dirty = false;
            });
          });
        });
        this.areaNotice = '';
        chain
          .then(function () {
            self.areaError = '';
            ar.undo = [];
          })
          .catch(function (e) {
            self.areaError = 'Save failed: ' + (e.message || e);
          })
          .then(function () {
            var notice = [];
            if (written.length) {
              notice.push('Saved ' + written.join(', ') + '.');
            }
            if (downloaded.length) {
              notice.push('Downloaded ' + downloaded.join(', ') + ' — this browser can\'t write to the opened files, so copy the download over the original.');
            }
            self.areaNotice = notice.join(' ');
            // Saved files hold new area objects; keep the same area selected.
            ar.selected = selectedName === null ? null : self.areaNamed(selectedName);
            self.areaRebuild();
          });
      },

      // --- Model ---

      areaNamed: function (name) {
        for (var f = 0; f < ar.files.length; f++) {
          for (var i = 0; i < ar.files[f].areas.length; i++) {
            if (ar.files[f].areas[i].name === name) {
              return ar.files[f].areas[i];
            }
          }
        }
        return null;
      },

      areaRebuild: function () {
        var shapes = [];
        var tags = {};
        var count = 0;
        ar.files.forEach(function (file, f) {
          file.areas.forEach(function (area) {
            area.file = f;
            count++;
            var v = vertices(area);
            shapes.push({
              name: area.name,
              minLevel: area.level === null ? 0 : area.level,
              maxLevel: area.level === null ? 3 : area.level,
              x: v.map(function (p) {
                return p.x;
              }),
              y: v.map(function (p) {
                return p.y;
              }),
              area: area,
            });
            area.tags.forEach(function (tag) {
              tags[tag] = true;
            });
          });
        });
        ar.shapes = shapes;
        if (ar.selected && !this.areaExists(ar.selected)) {
          ar.selected = null;
        }
        this.areaTags = Object.keys(tags).sort();
        this.areaStats = ar.files.length ? count + ' areas' : '';
        // The search index is built from the areas being drawn.
        this._searchEntries = null;
        this.syncAreaState();
        this.scheduleRender();
      },

      areaExists: function (area) {
        var file = ar.files[area.file];
        return !!file && file.areas.indexOf(area) !== -1;
      },

      syncAreaState: function () {
        this.areaLoaded = ar.files.length > 0;
        this.areaFiles = ar.files.map(function (file, i) {
          return { index: i, name: file.name, path: file.path, dirty: file.dirty, count: file.areas.length, writable: !!file.handle };
        });
        this.areaDirty = ar.files.some(function (file) {
          return file.dirty;
        });
        this.areaCanUndo = ar.undo.length > 0;
        this.areaDrawing = ar.draft ? ar.draft.length : -1;
        if (this.areaTarget >= ar.files.length) {
          this.areaTarget = 0;
        }
        var area = ar.selected;
        if (area !== ar.selSig) {
          // A newly picked area makes the file it lives in the target.
          ar.selSig = area;
          ar.vertex = -1;
          if (area) {
            this.areaTarget = area.file;
          }
        }
        if (!area) {
          this.areaSel = null;
          return;
        }
        var v = vertices(area);
        if (ar.vertex >= v.length) {
          ar.vertex = -1;
        }
        var b = bounds(area.x, area.y);
        var clash = ar.shapes.filter(function (shape) {
          return shape.name === area.name;
        }).length > 1;
        this.areaSel = {
          name: area.name,
          file: ar.files[area.file] ? ar.files[area.file].name : '',
          shape: isRect(area) ? 'Rectangle' : 'Polygon · ' + area.x.length + ' points',
          bounds: b.minX + ', ' + b.minY + ' → ' + b.maxX + ', ' + b.maxY,
          level: area.level === null ? '' : String(area.level),
          tags: area.tags.slice(),
          extras: area.extras.map(function (e) {
            return { key: e.key, value: e.value };
          }),
          clash: clash,
          vertex: ar.vertex >= 0 ? v[ar.vertex].x + ', ' + v[ar.vertex].y : '',
          canDeleteVertex: ar.vertex >= 0 && !isRect(area) && area.x.length > 3,
        };
      },

      areaCheckpoint: function () {
        ar.undo.push({
          files: ar.files.map(function (file) {
            return {
              dirty: file.dirty,
              areas: file.areas.map(function (area) {
                return { area: area, state: stateOf(area) };
              }),
            };
          }),
          draft: ar.draft ? ar.draft.slice() : null,
          selected: ar.selected,
          target: this.areaTarget,
        });
        if (ar.undo.length > UNDO_LIMIT) {
          ar.undo.shift();
        }
      },

      areaUndo: function () {
        var state = ar.undo.pop();
        if (!state) {
          return;
        }
        for (var f = 0; f < state.files.length && f < ar.files.length; f++) {
          ar.files[f].dirty = state.files[f].dirty;
          ar.files[f].areas = state.files[f].areas.map(function (entry) {
            applyState(entry.area, entry.state);
            return entry.area;
          });
        }
        ar.draft = state.draft;
        ar.selected = state.selected;
        this.areaTarget = state.target;
        this.areaRebuild();
      },

      // Every change to an area goes through here: an undo checkpoint, the change, and the file
      // marked unsaved — unless the change turned out to change nothing.
      areaEdit: function (change) {
        var area = ar.selected;
        if (!area) {
          return;
        }
        var before = JSON.stringify(stateOf(area));
        this.areaCheckpoint();
        change.call(this, area);
        if (JSON.stringify(stateOf(area)) === before) {
          ar.undo.pop();
          this.syncAreaState();
          return;
        }
        ar.files[area.file].dirty = true;
        this.areaRebuild();
      },

      areaSelect: function (area) {
        ar.selected = area;
        ar.vertex = -1;
        this.syncAreaState();
        this.scheduleRender();
      },

      // Picking an area in the search pane while the editor has files loaded selects it too.
      areaSelectNamed: function (name) {
        if (this.activeEditor() !== 'area') {
          return;
        }
        var area = this.areaNamed(name);
        if (area) {
          this.areaSelect(area);
        }
      },

      areaFrameSelected: function () {
        var area = ar.selected;
        if (!area) {
          return;
        }
        var b = bounds(area.x, area.y);
        var level = area.level === null ? this.level : area.level;
        this.focusOn((b.minX + b.maxX) / 2 + 0.5, (b.minY + b.maxY) / 2 + 0.5, level, this.zoomFitting(b.maxX - b.minX + 1, b.maxY - b.minY + 1));
      },

      // --- The Area panel ---

      areaRename: function (value) {
        var name = value.trim();
        var area = ar.selected;
        if (!area || name === area.name) {
          return;
        }
        if (!NAME_RE.test(name)) {
          this.areaError = 'Names can only use letters, digits, _ and -.';
          this.syncAreaState();
          return;
        }
        if (this.areaNamed(name)) {
          this.areaError = 'An area called ' + name + ' is already open.';
          this.syncAreaState();
          return;
        }
        var baked = (window.VOID_AREAS || []).some(function (shape) {
          return shape.name === name;
        });
        this.areaError = baked ? name + ' already exists in a file that isn\'t loaded — the server keeps whichever it loads last.' : '';
        this.areaEdit(function (a) {
          a.name = name;
        });
      },

      areaSetLevel: function (value) {
        this.areaEdit(function (a) {
          a.level = value === '' ? null : parseInt(value, 10);
        });
      },

      // Takes one tag or several (separated by commas or spaces).
      areaAddTag: function (value) {
        var tags = value.split(/[\s,]+/).filter(function (tag) {
          return tag;
        });
        if (!tags.length) {
          return;
        }
        this.areaEdit(function (a) {
          tags.forEach(function (tag) {
            if (a.tags.indexOf(tag) === -1) {
              a.tags.push(tag);
            }
          });
        });
      },

      areaRemoveTag: function (index) {
        this.areaEdit(function (a) {
          a.tags.splice(index, 1);
        });
      },

      areaSetExtra: function (index, part, value) {
        value = part === 'key' ? value.trim() : value;
        if (part === 'key' && value && (SHAPE_KEYS.indexOf(value) !== -1 || !/^[A-Za-z0-9_-]+$/.test(value))) {
          this.areaError = '"' + value + '" can\'t be used as a key here.';
          this.syncAreaState();
          return;
        }
        if (part === 'value' && /^[{\[]/.test(value.trim()) && T.valueProblem(value)) {
          this.areaError = T.valueProblem(value) + '.';
          this.syncAreaState();
          return;
        }
        this.areaError = '';
        this.areaEdit(function (a) {
          a.extras[index][part] = part === 'value' && /^[{\[]/.test(value.trim()) ? T.oneLine(value) : value;
        });
      },

      areaAddExtra: function () {
        this.areaEdit(function (a) {
          a.extras.push({ key: a.extras.some(function (e) {
            return e.key === 'hint';
          }) ? '' : 'hint', value: '' });
        });
      },

      areaRemoveExtra: function (index) {
        this.areaEdit(function (a) {
          a.extras.splice(index, 1);
        });
      },

      areaDeleteSelected: function () {
        var area = ar.selected;
        if (!area) {
          return;
        }
        this.areaCheckpoint();
        var file = ar.files[area.file];
        file.areas = file.areas.filter(function (a) {
          return a !== area;
        });
        file.dirty = true;
        ar.selected = null;
        this.areaRebuild();
      },

      areaDeleteVertex: function (index) {
        var area = ar.selected;
        if (!area) {
          return;
        }
        if (isRect(area)) {
          this.areaError = 'A rectangle keeps its 4 corners — drag an edge\'s midpoint to make it a polygon first.';
          this.syncAreaState();
          return;
        }
        if (area.x.length <= 3) {
          this.areaError = 'A polygon needs at least 3 points.';
          this.syncAreaState();
          return;
        }
        this.areaError = '';
        this.areaEdit(function (a) {
          a.x.splice(index, 1);
          a.y.splice(index, 1);
        });
        ar.vertex = -1;
        this.syncAreaState();
      },

      // --- Drawing a new area ---

      areaStartDraw: function () {
        if (!ar.files.length) {
          return;
        }
        ar.draft = [];
        ar.selected = null;
        this.areaError = '';
        this.syncAreaState();
        this.areaUpdateCursor(null);
        this.scheduleRender();
      },

      areaCancelDraw: function () {
        if (!ar.draft) {
          return false;
        }
        ar.draft = null;
        this.syncAreaState();
        this.areaUpdateCursor(null);
        this.scheduleRender();
        return true;
      },

      // Two points make a rectangle, more a polygon. Drawn on the surface it covers every level
      // (as most areas do); drawn on an upper floor, just that floor.
      areaFinishDraw: function () {
        var draft = ar.draft;
        if (!draft) {
          return;
        }
        if (draft.length < 2) {
          this.areaError = 'Place at least 2 points: 2 for a rectangle, 3 or more for a polygon.';
          this.syncAreaState();
          return;
        }
        var file = ar.files[this.areaTarget] || ar.files[0];
        var name = 'new_area';
        for (var n = 2; this.areaNamed(name); n++) {
          name = 'new_area_' + n;
        }
        var xs = draft.map(function (p) {
          return p.x;
        });
        var ys = draft.map(function (p) {
          return p.y;
        });
        if (draft.length === 2) {
          var b = bounds(xs, ys);
          xs = [b.minX, b.maxX];
          ys = [b.minY, b.maxY];
        }
        this.areaCheckpoint();
        var area = { name: name, x: xs, y: ys, level: this.level === 0 ? null : this.level, tags: [], extras: [], added: true };
        area.orig = stateOf(area);
        file.areas.push(area);
        file.dirty = true;
        ar.draft = null;
        ar.selected = area;
        this.areaError = '';
        this.areaEditPanelOpen = true;
        this.areaRebuild();
        this.areaUpdateCursor(null);
      },

      // --- Interaction (called from worldmap.js's pointer handlers) ---
      //
      //   click inside an area         select it (again: the next one overlapping it, smallest first)
      //   drag a vertex                move it — a rectangle's corner keeps it a rectangle
      //   drag an edge's midpoint      add a vertex there (a rectangle becomes a polygon)
      //   right-click a vertex         delete it
      //   arrows                       nudge the selected vertex, or the whole area (Shift: 8 tiles)
      //   New area (or N), then clicks place its points; Enter or clicking the first one finishes
      //   shift+click                  start a new area with its first point there — the way to
      //                                draw one inside another, where a click would just select
      //   drag the map                 pan, as ever (and any drag at all while Space is held)

      areaActive: function () {
        return this.showAreaPolygons && this.areaLoaded && this.activeEditor() === 'area';
      },

      areaInteractive: function () {
        return this.areaActive() && !ar.panKey;
      },

      // The selected area's vertex or edge-midpoint handle under the cursor, as
      // `{ vertex: i }` / `{ mid: i }` (the midpoint of the edge from vertex i to i + 1), or null.
      areaHandleAt: function (e) {
        var area = ar.selected;
        if (!area || !this.areaOnLevel(area)) {
          return null;
        }
        var p = this.navLocal(e);
        var v = vertices(area);
        var best = null;
        var bestDist = HANDLE_HIT_PX;
        for (var i = 0; i < v.length; i++) {
          var s = this.navScreen(v[i]);
          var d = Math.hypot(s.x - p.x, s.y - p.y);
          if (d <= bestDist) {
            best = { vertex: i };
            bestDist = d;
          }
        }
        if (best) {
          return best;
        }
        for (var m = 0; m < v.length; m++) {
          var mid = this.areaMidpoint(v[m], v[(m + 1) % v.length]);
          if (mid) {
            var dm = Math.hypot(mid.x - p.x, mid.y - p.y);
            if (dm <= bestDist) {
              best = { mid: m };
              bestDist = dm;
            }
          }
        }
        return best;
      },

      // Screen position of the midpoint handle between two vertices, or null if the edge is too
      // short on screen for one.
      areaMidpoint: function (a, b) {
        var sa = this.navScreen(a);
        var sb = this.navScreen(b);
        if (Math.hypot(sb.x - sa.x, sb.y - sa.y) < MID_MIN_PX) {
          return null;
        }
        return { x: (sa.x + sb.x) / 2, y: (sa.y + sb.y) / 2 };
      },

      areaOnLevel: function (area) {
        return area.level === null || area.level === this.level;
      },

      // Every loaded area on this level that `tile` is in, smallest first.
      areasAt: function (tile) {
        var self = this;
        return ar.shapes
          .filter(function (shape) {
            return self.areaOnLevel(shape.area) && contains(shape.x, shape.y, tile.x, tile.y);
          })
          .map(function (shape) {
            var b = bounds(shape.x, shape.y);
            return { area: shape.area, size: (b.maxX - b.minX + 1) * (b.maxY - b.minY + 1) };
          })
          .sort(function (a, b) {
            return a.size - b.size;
          })
          .map(function (entry) {
            return entry.area;
          });
      },

      // Returns true when the press lands on a handle, so the map doesn't also start panning.
      areaPointerDown: function (e) {
        ar.press = null;
        if (!this.areaInteractive()) {
          return false;
        }
        ar.press = { x: e.clientX, y: e.clientY, shift: e.shiftKey };
        var handle = ar.draft ? null : this.areaHandleAt(e);
        if (!handle) {
          return false;
        }
        ar.drag = { handle: handle, moved: false };
        return true;
      },

      areaPointerMove: function (e) {
        if (!this.areaActive()) {
          return false;
        }
        ar.hover = this.navTileAt(e);
        var drag = ar.drag;
        if (drag) {
          if (!drag.moved) {
            if (!ar.press || Math.hypot(e.clientX - ar.press.x, e.clientY - ar.press.y) <= CLICK_SLOP_PX) {
              return true;
            }
            // Past the slop: a move, not a click. One undo step covers the whole drag.
            this.areaCheckpoint();
            drag.moved = true;
            var area = ar.selected;
            if (drag.handle.mid !== undefined) {
              if (isRect(area)) {
                toPolygon(area);
              }
              var at = drag.handle.mid + 1;
              area.x.splice(at, 0, ar.hover.x);
              area.y.splice(at, 0, ar.hover.y);
              drag.vertex = at;
            } else {
              drag.vertex = drag.handle.vertex;
            }
            ar.files[area.file].dirty = true;
          }
          drag.vertex = moveVertex(ar.selected, drag.vertex, ar.hover);
          ar.vertex = drag.vertex;
          this.areaRebuild();
          return true;
        }
        this.areaUpdateCursor(e);
        if (ar.draft) {
          // The preview line follows the cursor.
          this.scheduleRender();
        }
        return false;
      },

      areaPointerUp: function (e) {
        var press = ar.press;
        var drag = ar.drag;
        ar.press = null;
        ar.drag = null;
        if (drag && drag.moved) {
          this.areaUpdateCursor(e);
          return;
        }
        if (!press || !this.areaInteractive() || Math.hypot(e.clientX - press.x, e.clientY - press.y) > CLICK_SLOP_PX) {
          return;
        }
        if (drag && drag.handle.vertex !== undefined) {
          // Clicking a vertex selects it (again to deselect), for the arrow keys and Delete.
          ar.vertex = ar.vertex === drag.handle.vertex ? -1 : drag.handle.vertex;
          this.syncAreaState();
          this.scheduleRender();
          return;
        }
        this.areaClick(e, press.shift);
      },

      areaPointerCancel: function () {
        ar.drag = null;
        ar.press = null;
      },

      areaPointerLeave: function () {
        if (ar.hover) {
          ar.hover = null;
          if (ar.draft) {
            this.scheduleRender();
          }
        }
      },

      areaClick: function (e, shift) {
        var tile = this.navTileAt(e);
        if (!ar.draft && shift) {
          this.areaStartDraw();
        }
        if (ar.draft) {
          var first = ar.draft[0];
          if (first && ar.draft.length >= 2) {
            var s = this.navScreen(first);
            var p = this.navLocal(e);
            if (Math.hypot(s.x - p.x, s.y - p.y) <= HANDLE_HIT_PX) {
              this.areaFinishDraw();
              return;
            }
          }
          var last = ar.draft[ar.draft.length - 1];
          if (!last || last.x !== tile.x || last.y !== tile.y) {
            ar.draft.push({ x: tile.x, y: tile.y });
          }
          this.syncAreaState();
          this.scheduleRender();
          return;
        }
        var under = this.areasAt(tile);
        var at = under.indexOf(ar.selected);
        this.areaSelect(under.length ? under[(at + 1) % under.length] : null);
      },

      // Right-click. Returns true when the editor handled it, so the browser's menu is suppressed.
      areaContextMenu: function (e) {
        if (!this.areaInteractive()) {
          return false;
        }
        if (ar.draft) {
          // Takes back the last point placed.
          ar.draft.pop();
          this.syncAreaState();
          this.scheduleRender();
          return true;
        }
        var handle = this.areaHandleAt(e);
        if (handle && handle.vertex !== undefined) {
          this.areaDeleteVertex(handle.vertex);
        }
        return true;
      },

      areaUpdateCursor: function (e) {
        var vp = this.viewport;
        if (!vp || this.activeEditor() !== 'area') {
          return;
        }
        var active = this.areaInteractive() && !!e;
        var handle = active && !ar.draft && this.areaHandleAt(e);
        vp.classList.toggle('wm-nav-grab', !!handle);
        vp.classList.toggle('wm-nav-add', this.areaInteractive() && !!ar.draft);
        vp.classList.toggle('wm-nav-hot', false);
      },

      areaKeyDown: function (e) {
        if (!this.areaActive()) {
          return;
        }
        var target = e.target;
        if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.tagName === 'SELECT' || target.isContentEditable)) {
          return;
        }
        var mod = e.ctrlKey || e.metaKey;
        if (e.key === ' ') {
          e.preventDefault();
          if (!ar.panKey) {
            ar.panKey = true;
            this.areaUpdateCursor(null);
          }
        } else if (mod && e.key.toLowerCase() === 'z') {
          e.preventDefault();
          this.areaUndo();
        } else if (mod && e.key.toLowerCase() === 's') {
          e.preventDefault();
          this.saveMapFiles();
        } else if (!mod && !ar.draft && e.key.toLowerCase() === 'n') {
          e.preventDefault();
          this.areaStartDraw();
        } else if (e.key === 'Enter' && ar.draft) {
          e.preventDefault();
          this.areaFinishDraw();
        } else if (e.key === 'Escape') {
          if (this.areaCancelDraw()) {
            return;
          }
          if (ar.vertex >= 0) {
            ar.vertex = -1;
          } else {
            ar.selected = null;
          }
          this.syncAreaState();
          this.scheduleRender();
        } else if (e.key === 'Delete' || e.key === 'Backspace') {
          if (!ar.selected) {
            return;
          }
          e.preventDefault();
          if (ar.vertex >= 0) {
            this.areaDeleteVertex(ar.vertex);
          } else {
            this.areaDeleteSelected();
          }
        } else if (!mod && ar.selected && e.key.indexOf('Arrow') === 0) {
          e.preventDefault();
          var step = e.shiftKey ? 8 : 1;
          var dx = e.key === 'ArrowLeft' ? -step : e.key === 'ArrowRight' ? step : 0;
          var dy = e.key === 'ArrowDown' ? -step : e.key === 'ArrowUp' ? step : 0;
          var index = ar.vertex;
          this.areaEdit(function (a) {
            if (index >= 0) {
              var v = vertices(a)[index];
              ar.vertex = moveVertex(a, index, { x: v.x + dx, y: v.y + dy });
            } else {
              a.x = a.x.map(function (x) {
                return x + dx;
              });
              a.y = a.y.map(function (y) {
                return y + dy;
              });
            }
          });
        }
      },

      // --- Rendering ---

      // SVG class for a shape in the polygon layer (see `renderAreaPolygons` in worldmap.js).
      areaShapeClass: function (shape) {
        return shape.area && shape.area === ar.selected ? ' wm-area-polygon-selected' : '';
      },

      // The editor's handles, drawn over the polygon layer: the selected area's vertices (squares,
      // to tell them from the nav graph's round points) and edge midpoints, and the area being drawn.
      renderAreaEditor: function () {
        if (!this.showAreaPolygons || !this.areaLoaded) {
          return '';
        }
        var self = this;
        var active = this.areaActive();
        var html = '';
        function point(t) {
          var s = self.navScreen(t);
          return s.x.toFixed(1) + ',' + s.y.toFixed(1);
        }
        function square(t, cls, half) {
          var s = self.navScreen(t);
          return '<rect class="' + cls + '" x="' + (s.x - half).toFixed(1) + '" y="' + (s.y - half).toFixed(1) + '" width="' + half * 2 + '" height="' + half * 2 + '"></rect>';
        }
        var area = ar.selected;
        if (active && area && this.areaOnLevel(area) && !ar.draft) {
          var v = vertices(area);
          if (active) {
            for (var m = 0; m < v.length; m++) {
              var mid = this.areaMidpoint(v[m], v[(m + 1) % v.length]);
              if (mid) {
                html += '<circle class="wm-area-mid" cx="' + mid.x.toFixed(1) + '" cy="' + mid.y.toFixed(1) + '" r="3.5"></circle>';
              }
            }
          }
          for (var i = 0; i < v.length; i++) {
            html += square(v[i], 'wm-area-vertex' + (i === ar.vertex ? ' wm-area-vertex-selected' : ''), 4.5);
          }
        }
        var draft = ar.draft;
        if (draft && draft.length) {
          var placed = active && ar.hover ? draft.concat([ar.hover]) : draft;
          if (placed.length === 2) {
            // Two points finish as a rectangle, so that's what's previewed.
            var b = bounds([placed[0].x, placed[1].x], [placed[0].y, placed[1].y]);
            html += '<polygon class="wm-area-draft wm-area-draft-rect" points="' +
              [{ x: b.minX, y: b.minY }, { x: b.maxX, y: b.minY }, { x: b.maxX, y: b.maxY }, { x: b.minX, y: b.maxY }].map(point).join(' ') + '"></polygon>';
          } else if (placed.length > 2) {
            html += '<polygon class="wm-area-draft" points="' + placed.map(point).join(' ') + '"></polygon>';
          }
          for (var d = 0; d < draft.length; d++) {
            html += square(draft[d], 'wm-area-vertex' + (d === 0 && draft.length >= 2 ? ' wm-area-vertex-first' : ''), 4.5);
          }
        }
        return html;
      },
    };
  };
})();
