// Bot navigation graph overlay and editor for the world map (see WorldMap.kt's "Nav graph" panel).
//
// The graph lives in `*.nav-edges.toml` files under `data/` (read by the game's
// NavigationGraph.loadGraph): one `edges = [ ... ]` array per file, each element an inline table
// with a `from` and `to` tile and, optionally, a `cost`, `actions` and `requires`. Nodes aren't
// declared anywhere — a node is simply a tile that some edge starts or ends on — so "adding a
// point" here always means adding an edge to it, and deleting one deletes every edge touching it.
//
// Nothing is baked into the page: the files are opened from the viewer's own disk (the File System
// Access API where the browser has it, so saving writes straight back; a plain file input and a
// download otherwise), which is also why the toggle offers to load them before there's anything to show.
//
// Editing is done on the file's own text rather than a re-serialised copy of it: `parseNavFile`
// splits the array into one chunk per edge, remembering where in each element the `from`/`to`
// tables sit, and `serializeNavFile` stitches the chunks back together with only the tables that
// actually changed rewritten. Comments, hand-wrapped multi-line `actions`, key order and spacing all
// survive a save untouched, so the diff is just the edges that were edited. An edge whose cost,
// actions or requirements were changed in the Edge panel is the exception: it's written out afresh,
// on one line, from what `readMeta` parsed out of it.
(function () {
  // Screen-space hit radii, in CSS pixels, for picking a node or an edge under the cursor.
  var NODE_HIT_PX = 9;
  var EDGE_HIT_PX = 5;
  // A press that travels further than this before release is a pan, not a click.
  var CLICK_SLOP_PX = 4;
  var UNDO_LIMIT = 100;
  // Directories under `data/` that can't hold nav files and are large enough to make "Open folder"
  // crawl if it had to walk them.
  var SKIPPED_DIRS = { cache: true, 'map-tiles': true, saves: true, dump: true, '.temp': true, '.git': true, node_modules: true };
  var NAV_SUFFIX = '.nav-edges.toml';

  // Keys each bot action type takes, from content.bot.behaviour.action.ActionParser — just the
  // types that make sense on a nav edge. Drives the Edge panel's key suggestions, the keys added
  // when a type is picked, and the "missing" warning. Any other type can still be typed in.
  var ACTION_KEYS = {
    object: { required: ['option', 'id', 'success'], optional: ['x', 'y', 'radius', 'delay', 'if'] },
    tile: { required: ['x', 'y'], optional: ['radius'] },
    npc: { required: ['option', 'id'], optional: ['success', 'radius', 'delay'] },
    floor_item: { required: ['option', 'id', 'success'], optional: ['x', 'y', 'radius', 'delay'] },
    item_on_object: { required: ['id', 'object'], optional: ['success', 'delay'] },
    item_on_item: { required: ['id', 'on'], optional: ['success'] },
    interface: { required: ['option', 'id'], optional: ['success', 'if'] },
    interface_close: { required: ['id'], optional: [] },
    continue: { required: ['id'], optional: ['option', 'success'] },
    enter: { required: [], optional: ['int', 'string'] },
    wait: { required: ['ticks'], optional: [] },
    go_to: { required: [], optional: ['area', 'nearest'] },
    jewellery_teleport: { required: ['item', 'area'], optional: ['if', 'success'] },
    restart: { required: ['success'], optional: ['wait_if'] },
  };

  function field(key, value) {
    return { key: key, value: String(value) };
  }

  // The Edge panel's "+ Door", "+ Stairs"... buttons: the shapes the existing files use, filled in
  // from the edge's own ends where that's a good guess.
  var ACTION_TEMPLATES = {
    door: function (edge) {
      return [
        { type: 'object', fields: [field('option', 'Open'), field('id', ''), field('x', edge.to.x), field('y', edge.to.y),
          field('success', '{ object = { id = "", x = ' + edge.to.x + ', y = ' + edge.to.y + ' } }')] },
        { type: 'tile', fields: [field('x', edge.to.x), field('y', edge.to.y), field('radius', 1)] },
      ];
    },
    stairs: function (edge) {
      var up = (edge.to.level || 0) > (edge.from.level || 0);
      return [
        { type: 'object', fields: [field('option', up ? 'Climb-up' : 'Climb-down'), field('id', ''), field('x', edge.from.x), field('y', edge.from.y),
          field('success', '{ tile = { level = ' + (edge.to.level || 0) + ' } }')] },
      ];
    },
    object: function (edge) {
      return [{ type: 'object', fields: [field('option', ''), field('id', ''), field('x', edge.to.x), field('y', edge.to.y), field('success', '{ tile = { x = ' + edge.to.x + ', y = ' + edge.to.y + ' } }')] }];
    },
    walk: function (edge) {
      return [{ type: 'tile', fields: [field('x', edge.to.x), field('y', edge.to.y)] }];
    },
    other: function () {
      return [{ type: '', fields: [] }];
    },
  };

  function tileKey(t) {
    return t.x + ',' + t.y + ',' + (t.level || 0);
  }

  function sameTile(a, b) {
    return a.x === b.x && a.y === b.y && (a.level || 0) === (b.level || 0);
  }

  function copyTile(t) {
    return { x: t.x, y: t.y, level: t.level || 0 };
  }

  // Matches the style the existing files are written in: `level` left out on the ground floor.
  function formatTile(t) {
    return '{ x = ' + t.x + ', y = ' + t.y + ((t.level || 0) !== 0 ? ', level = ' + t.level : '') + ' }';
  }

  // --- Parsing -------------------------------------------------------------------------------

  // Index just past the string starting at `i` (which is on its opening quote). Handles basic
  // ("...", with escapes) and literal ('...') strings; multi-line strings don't appear in these
  // files.
  function skipString(text, i) {
    var quote = text[i];
    i++;
    while (i < text.length && text[i] !== quote) {
      if (quote === '"' && text[i] === '\\') {
        i++;
      }
      i++;
    }
    return i + 1;
  }

  function skipComment(text, i) {
    while (i < text.length && text[i] !== '\n') {
      i++;
    }
    return i;
  }

  // Index just past the bracket matching the one at `i`, skipping strings and comments.
  function matchBracket(text, i) {
    var depth = 0;
    while (i < text.length) {
      var c = text[i];
      if (c === '"' || c === "'") {
        i = skipString(text, i);
        continue;
      }
      if (c === '#') {
        i = skipComment(text, i);
        continue;
      }
      if (c === '{' || c === '[') {
        depth++;
      } else if (c === '}' || c === ']') {
        depth--;
        if (depth === 0) {
          return i + 1;
        }
      }
      i++;
    }
    throw new Error('Unclosed bracket');
  }

  function readTile(table) {
    var tile = { x: 0, y: 0, level: 0 };
    var re = /(\w+)\s*=\s*(-?\d+)/g;
    var m;
    while ((m = re.exec(table))) {
      if (m[1] === 'x' || m[1] === 'y' || m[1] === 'level') {
        tile[m[1]] = parseInt(m[2], 10);
      }
    }
    return tile;
  }

  // Top-level keys of one edge element (`{ ... }`), each with the span of its value relative to
  // the element's own text.
  function readEdgeKeys(element) {
    var keys = {};
    readEntries(element).forEach(function (entry) {
      keys[entry.key] = { start: entry.start, end: entry.end };
    });
    return keys;
  }

  // The `key = value` entries of an inline table (`{ ... }`), in order, each with the span of its
  // value relative to the table's own text.
  function readEntries(element) {
    var entries = [];
    var i = 1;
    var end = element.length - 1;
    while (i < end) {
      var c = element[i];
      if (c === '#') {
        i = skipComment(element, i);
        continue;
      }
      var key = /^[A-Za-z_][\w-]*/.exec(element.slice(i, i + 64));
      if (!key) {
        i++;
        continue;
      }
      i += key[0].length;
      while (i < end && /\s/.test(element[i])) {
        i++;
      }
      if (element[i] !== '=') {
        continue;
      }
      i++;
      while (i < end && /\s/.test(element[i])) {
        i++;
      }
      var start = i;
      if (element[i] === '{' || element[i] === '[') {
        i = matchBracket(element, i);
      } else if (element[i] === '"' || element[i] === "'") {
        i = skipString(element, i);
      } else {
        while (i < end && element[i] !== ',' && element[i] !== '}' && !/\s/.test(element[i])) {
          i++;
        }
      }
      entries.push({ key: key[0], start: start, end: i });
    }
    return entries;
  }

  // The elements of an inline array (`[ ... ]`), as their own text.
  function splitArray(text) {
    var out = [];
    var i = 1;
    var end = text.length - 1;
    while (i < end) {
      var c = text[i];
      if (c === '#') {
        i = skipComment(text, i);
      } else if (c === ',' || /\s/.test(c)) {
        i++;
      } else {
        var start = i;
        if (c === '{' || c === '[') {
          i = matchBracket(text, i);
        } else if (c === '"' || c === "'") {
          i = skipString(text, i);
        } else {
          while (i < end && text[i] !== ',' && !/\s/.test(text[i])) {
            i++;
          }
        }
        out.push(text.slice(start, i));
      }
    }
    return out;
  }

  // A value's text on one line: comments dropped and every run of whitespace outside a string
  // collapsed to a single space, so a hand-wrapped `success = { ... }` fits in a text field.
  function oneLine(text) {
    var out = '';
    var i = 0;
    while (i < text.length) {
      var c = text[i];
      if (c === '"' || c === "'") {
        var j = skipString(text, i);
        out += text.slice(i, j);
        i = j;
      } else if (c === '#') {
        i = skipComment(text, i);
      } else if (/\s/.test(c)) {
        while (i < text.length && /\s/.test(text[i])) {
          i++;
        }
        out += ' ';
      } else {
        out += c;
        i++;
      }
    }
    return out.trim().replace(/\[ /g, '[').replace(/ \]/g, ']').replace(/,\s*([\]}])/g, ' $1').replace(/\[ \]/g, '[]');
  }

  // A value typed into the editor that TOML would read as something other than a string: a number,
  // a boolean, a quoted string, or an inline table/array.
  function isRawValue(text) {
    return /^-?\d+$/.test(text) || text === 'true' || text === 'false' || /^["'{\[]/.test(text);
  }

  // Strings are shown and edited without their quotes (`Open`, not `"Open"`), as long as the bare
  // text would still read back as a string.
  function displayValue(raw) {
    if (/^"([^"\\]|\\.)*"$/.test(raw)) {
      var text = JSON.parse(raw);
      if (!isRawValue(text) && text.trim() === text && text !== '') {
        return text;
      }
    }
    return raw;
  }

  function tomlValue(text) {
    text = text.trim();
    return isRawValue(text) ? text : JSON.stringify(text);
  }

  // Why `text` isn't a usable value, or '' if it is: brackets must balance and close at the end.
  function valueProblem(text) {
    text = text.trim();
    if (text === '') {
      return 'empty';
    }
    if (/^[{\[]/.test(text)) {
      try {
        if (matchBracket(text, 0) !== text.length) {
          return 'unexpected text after the closing bracket';
        }
      } catch (e) {
        return 'unclosed bracket';
      }
    }
    if (/^["']/.test(text) && skipString(text, 0) !== text.length) {
      return 'unclosed or trailing text after string';
    }
    return '';
  }

  // `actions = [ { object = { option = "Open", ... } }, { tile = { ... } } ]` as
  // `[{ type: 'object', fields: [{ key: 'option', value: 'Open' }, ...] }, ...]`.
  function parseActions(text) {
    return splitArray(text).map(function (element) {
      var entry = readEntries(element)[0];
      if (!entry) {
        return { type: '', fields: [] };
      }
      var body = element.slice(entry.start, entry.end);
      return {
        type: entry.key,
        fields: body[0] === '{'
          ? readEntries(body).map(function (field) {
            return { key: field.key, value: displayValue(oneLine(body.slice(field.start, field.end))) };
          })
          : [],
      };
    });
  }

  function formatAction(action) {
    var fields = action.fields.filter(function (field) {
      return field.key;
    }).map(function (field) {
      return field.key + ' = ' + tomlValue(field.value);
    });
    return '{ ' + (action.type || 'object') + ' = ' + (fields.length ? '{ ' + fields.join(', ') + ' }' : '{}') + ' }';
  }

  // The parts of an edge the Edge panel edits, besides its ends. `cost` and `requires` are kept as
  // TOML text ('' when absent).
  function readMeta(element, keys) {
    function raw(key) {
      return keys[key] ? oneLine(element.slice(keys[key].start, keys[key].end)) : '';
    }
    return {
      cost: raw('cost'),
      actions: keys.actions ? parseActions(element.slice(keys.actions.start, keys.actions.end)) : [],
      requires: raw('requires'),
    };
  }

  function copyMeta(meta) {
    return JSON.parse(JSON.stringify(meta));
  }

  // Splits a nav-edges file into `head` (everything up to and including `edges = [`), `items` (in
  // order: `{gap}` for blank lines/standalone comments between edges, `{edge}` for an edge and its
  // own line — indentation, trailing comma and same-line comment included) and `tail` (from the
  // whitespace before the closing `]` to the end of the file).
  function parseNavFile(text) {
    var open = /^[ \t]*edges[ \t]*=[ \t]*\[/m.exec(text);
    if (!open) {
      throw new Error('No `edges = [` array found');
    }
    var head = text.slice(0, open.index + open[0].length);
    var items = [];
    var i = head.length;
    var gapStart = i;
    while (i < text.length) {
      var c = text[i];
      if (c === '#') {
        i = skipComment(text, i);
        continue;
      }
      if (c === ']') {
        break;
      }
      if (c !== '{') {
        i++;
        continue;
      }
      // An edge's chunk starts at its line's indentation when nothing else precedes it on that
      // line, so deleting it takes the whole line rather than leaving the indent behind.
      var lineStart = text.lastIndexOf('\n', i - 1) + 1;
      var chunkStart = lineStart >= gapStart && /^[ \t]*$/.test(text.slice(lineStart, i)) ? lineStart : i;
      if (chunkStart > gapStart) {
        items.push({ gap: text.slice(gapStart, chunkStart) });
      }
      var elementEnd = matchBracket(text, i);
      var j = elementEnd;
      while (j < text.length && (text[j] === ' ' || text[j] === '\t')) {
        j++;
      }
      var hasComma = text[j] === ',';
      if (hasComma) {
        j++;
      }
      // Anything else up to the end of the line — a trailing `# name` comment, mostly — stays
      // with this edge, so it goes wherever the edge does.
      var lineEnd = text.indexOf('\n', j);
      var rest = lineEnd === -1 ? text.slice(j) : text.slice(j, lineEnd + 1);
      var restEnd = j + rest.length;
      if (!/^[ \t]*(#[^\n]*)?\r?\n?$/.test(rest)) {
        // Another element follows on the same line; stop this chunk at the comma.
        rest = '';
        restEnd = j;
      }
      var element = text.slice(i, elementEnd);
      var keys = readEdgeKeys(element);
      if (!keys.from || !keys.to) {
        throw new Error('Edge without from/to near: ' + element.slice(0, 80));
      }
      var from = readTile(element.slice(keys.from.start, keys.from.end));
      var to = readTile(element.slice(keys.to.start, keys.to.end));
      var meta = readMeta(element, keys);
      items.push({
        edge: {
          pre: text.slice(chunkStart, i),
          element: element,
          between: text.slice(elementEnd, j - (hasComma ? 1 : 0)),
          hasComma: hasComma,
          post: rest,
          fromSpan: keys.from,
          toSpan: keys.to,
          origFrom: from,
          origTo: to,
          from: copyTile(from),
          to: copyTile(to),
          meta: meta,
          // What `meta` was when read, to tell whether the element needs rewriting.
          origMeta: JSON.stringify(meta),
          // Edges with actions are one-way (and walk-only ones both ways) — see loadGraph.
          directed: meta.actions.length > 0,
        },
      });
      i = restEnd;
      gapStart = i;
    }
    if (i >= text.length) {
      throw new Error('Unclosed `edges` array');
    }
    // Whitespace/comments between the last edge and `]` belong to the tail, so appended edges
    // land after the last one rather than after a trailing comment block.
    return { head: head, items: items, tail: text.slice(gapStart), eol: text.indexOf('\r\n') !== -1 ? '\r\n' : '\n' };
  }

  function indentOf(file) {
    for (var i = 0; i < file.items.length; i++) {
      var edge = file.items[i].edge;
      if (edge && /^[ \t]+$/.test(edge.pre)) {
        return edge.pre;
      }
    }
    return '    ';
  }

  function newEdge(file, from, to, meta) {
    meta = meta || { cost: '', actions: [], requires: '' };
    var element = '{ from = ' + formatTile(from) + ', to = ' + formatTile(to) + ' }';
    return {
      meta: meta,
      origMeta: '',
      pre: indentOf(file),
      element: element,
      between: '',
      hasComma: true,
      post: file.eol,
      fromSpan: null,
      toSpan: null,
      origFrom: copyTile(from),
      origTo: copyTile(to),
      from: copyTile(from),
      to: copyTile(to),
      directed: meta.actions.length > 0,
      added: true,
    };
  }

  // The whole element written out afresh, on one line, in the order the existing files use.
  function formatElement(edge) {
    var meta = edge.meta;
    var text = '{ from = ' + formatTile(edge.from) + ', to = ' + formatTile(edge.to);
    if (meta.cost !== '') {
      text += ', cost = ' + meta.cost;
    }
    if (meta.actions.length) {
      text += ', actions = [' + meta.actions.map(formatAction).join(', ') + ']';
    }
    if (meta.requires !== '') {
      text += ', requires = ' + meta.requires;
    }
    return text + ' }';
  }

  // An edge whose cost, actions or requirements were edited is rewritten whole (so a hand-wrapped
  // one ends up on a single line); one that only had its ends moved keeps the rest of its text.
  function elementText(edge) {
    if (edge.added || JSON.stringify(edge.meta) !== edge.origMeta) {
      return formatElement(edge);
    }
    var text = edge.element;
    // Later span first, so the earlier one's offsets still hold.
    var spans = [
      { span: edge.fromSpan, now: edge.from, orig: edge.origFrom },
      { span: edge.toSpan, now: edge.to, orig: edge.origTo },
    ].sort(function (a, b) {
      return b.span.start - a.span.start;
    });
    for (var i = 0; i < spans.length; i++) {
      var s = spans[i];
      if (!sameTile(s.now, s.orig)) {
        text = text.slice(0, s.span.start) + formatTile(s.now) + text.slice(s.span.end);
      }
    }
    return text;
  }

  function serializeNavFile(file) {
    var out = file.head;
    var edges = file.items.filter(function (item) {
      return item.edge;
    });
    var lastEdge = edges.length ? edges[edges.length - 1].edge : null;
    for (var i = 0; i < file.items.length; i++) {
      var item = file.items[i];
      if (item.gap !== undefined) {
        out += item.gap;
        continue;
      }
      var edge = item.edge;
      if (edge.added && !/\n$/.test(out)) {
        out += file.eol;
      }
      // An edge that used to be last may have had no comma; one is needed once anything follows.
      if (edge.hasComma) {
        out += edge.pre + elementText(edge) + edge.between + ',' + edge.post;
      } else {
        out += edge.pre + elementText(edge) + (edge !== lastEdge ? ',' : '') + edge.between + edge.post;
      }
    }
    var tail = file.tail;
    if (lastEdge && lastEdge.added && !/^\s*\n/.test(tail) && !/\n$/.test(out)) {
      out += file.eol;
    }
    return out + tail;
  }

  // --- Geometry ------------------------------------------------------------------------------

  function distToSegment(px, py, ax, ay, bx, by) {
    var dx = bx - ax;
    var dy = by - ay;
    var len = dx * dx + dy * dy;
    var t = len === 0 ? 0 : Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / len));
    return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
  }

  // Every distinct tile an open file's edges touch, in level/x/y order, for the panel's list.
  function navPointsOf(file) {
    var seen = {};
    var tiles = [];
    for (var i = 0; i < file.items.length; i++) {
      var edge = file.items[i].edge;
      if (!edge) {
        continue;
      }
      [edge.from, edge.to].forEach(function (t) {
        var key = tileKey(t);
        if (!seen[key]) {
          seen[key] = true;
          tiles.push({ key: key, x: t.x, y: t.y, level: t.level || 0 });
        }
      });
    }
    tiles.sort(function (a, b) {
      return a.level - b.level || a.x - b.x || a.y - b.y;
    });
    return tiles.map(function (t) {
      return { key: t.key, label: t.x + ', ' + t.y + (t.level ? ', ' + t.level : ''), x: t.x, y: t.y, level: t.level };
    });
  }

  window.VoidNavGraph = { parse: parseNavFile, serialize: serializeNavFile };

  // --- Alpine methods --------------------------------------------------------------------------

  // Mixed into `worldMapApp()`'s data object. The files, their handles and the undo history are
  // kept out here in the closure rather than on the (Alpine-proxied) object: a
  // FileSystemFileHandle called through a Proxy throws "Illegal invocation", and none of it needs
  // to be reactive — the panel only reads the summary fields `syncNavState` copies across.
  window.navGraphMethods = function () {
    var nav = {
      files: [],
      // New nodes that nothing connects to yet. Kept only until an edge reaches them — a lone node
      // can't be written to a file, which only holds edges.
      loose: [],
      nodes: {},
      selected: null,
      selSig: '',
      undo: [],
      drag: null,
      press: null,
      hover: null,
      hoverNode: null,
      hoverShift: false,
      // Space held: the editor ignores the pointer so every drag pans.
      panKey: false,
      // Index of the selected edge's action whose x/y the next map click sets, or null.
      pick: null,
      // The selected point's edges, in the order the panel lists them (see `navPickEdge`).
      nodeEdges: [],
    };

    return {
      showNavGraph: false,
      navPanelOpen: true,
      navEdgePanelOpen: true,
      navLoaded: false,
      navFiles: [],
      navTarget: 0,
      // The file whose points are listed in the panel (-1: none). Follows the selection, and is
      // where new edges go.
      navOpen: -1,
      navPoints: [],
      navSelectedKey: '',
      navDirty: false,
      navCanUndo: false,
      navStats: '',
      navSelection: '',
      navError: '',
      // What the last save did — which files were written in place and which could only be downloaded.
      navNotice: '',
      // The Edge panel's copy of the selected edge (null when a point or nothing is selected), and
      // the selected point's edge list.
      navEdge: null,
      navNodeEdges: [],
      navPicking: -1,
      navActionTypes: Object.keys(ACTION_KEYS),
      navFolderAccess: typeof window.showDirectoryPicker === 'function',

      navBoot: function (root) {
        var self = this;
        this.navLayer = root.querySelector('#wm-nav-graph');
        this.$watch('showNavGraph', function () {
          self.navUpdateCursor(null);
          self.scheduleRender();
        });
        window.addEventListener('keydown', function (e) {
          self.navKeyDown(e);
        });
        window.addEventListener('keyup', function (e) {
          self.navKeyUp(e);
        });
        // Released while the page didn't have focus: the keyup never arrives.
        window.addEventListener('blur', function () {
          nav.hoverShift = false;
          self.navSetPanKey(false);
        });
        window.addEventListener('beforeunload', function (e) {
          if (self.navDirty) {
            e.preventDefault();
            e.returnValue = '';
          }
        });
      },

      // --- Files ---

      openNavFiles: function () {
        var self = this;
        if (typeof window.showOpenFilePicker === 'function') {
          window
            .showOpenFilePicker({
              multiple: true,
              types: [{ description: 'Nav graph edges', accept: { 'text/plain': ['.toml'] } }],
            })
            .then(function (handles) {
              return Promise.all(
                handles.map(function (handle) {
                  return handle.getFile().then(function (file) {
                    return file.text().then(function (text) {
                      return { name: handle.name, path: handle.name, text: text, handle: handle };
                    });
                  });
                }),
              );
            })
            .then(function (loaded) {
              self.navAddFiles(loaded);
            })
            .catch(function (e) {
              if (e && e.name !== 'AbortError') {
                self.navError = String(e.message || e);
              }
            });
          return;
        }
        // No File System Access API (Firefox, Safari): read through a file input, save by download.
        var input = document.createElement('input');
        input.type = 'file';
        input.multiple = true;
        input.accept = '.toml';
        input.addEventListener('change', function () {
          var files = Array.prototype.slice.call(input.files || []);
          Promise.all(
            files.map(function (file) {
              return file.text().then(function (text) {
                return { name: file.name, path: file.name, text: text, handle: null };
              });
            }),
          ).then(function (loaded) {
            self.navAddFiles(loaded);
          });
        });
        input.click();
      },

      // Picks a folder — the repo's `data/` is the one that makes sense — and opens every
      // `*.nav-edges.toml` anywhere under it, which is the whole graph the server loads.
      openNavFolder: function () {
        var self = this;
        if (typeof window.showDirectoryPicker !== 'function') {
          // Firefox/Safari: a directory <input> hands over every file under the folder (read-only, so
          // saving downloads them, like the plain file picker).
          var input = document.createElement('input');
          input.type = 'file';
          input.setAttribute('webkitdirectory', '');
          input.addEventListener('change', function () {
            var files = Array.prototype.slice.call(input.files || []).filter(function (file) {
              var path = file.webkitRelativePath || file.name;
              return (
                file.name.slice(-NAV_SUFFIX.length) === NAV_SUFFIX &&
                !path.split('/').slice(0, -1).some(function (dir) {
                  return SKIPPED_DIRS[dir];
                })
              );
            });
            if (!files.length) {
              self.navError = 'No *' + NAV_SUFFIX + ' files in that folder.';
              self.syncNavState();
              return;
            }
            Promise.all(
              files.map(function (file) {
                return file.text().then(function (text) {
                  return { name: file.name, path: file.webkitRelativePath || file.name, text: text, handle: null };
                });
              }),
            ).then(function (loaded) {
              loaded.sort(function (x, y) {
                return x.path.localeCompare(y.path);
              });
              self.navAddFiles(loaded);
            });
          });
          input.click();
          return;
        }
        var found = [];
        function walk(dir, path) {
          var pending = [];
          var iterator = dir.values();
          function next() {
            return iterator.next().then(function (step) {
              if (step.done) {
                return Promise.all(pending);
              }
              var entry = step.value;
              if (entry.kind === 'directory') {
                if (!SKIPPED_DIRS[entry.name]) {
                  pending.push(walk(entry, path + entry.name + '/'));
                }
              } else if (entry.name.slice(-NAV_SUFFIX.length) === NAV_SUFFIX) {
                pending.push(
                  entry
                    .getFile()
                    .then(function (file) {
                      return file.text();
                    })
                    .then(function (text) {
                      found.push({ name: entry.name, path: path + entry.name, text: text, handle: entry });
                    }),
                );
              }
              return next();
            });
          }
          return next();
        }
        window
          .showDirectoryPicker({ mode: 'readwrite' })
          .then(function (dir) {
            self.navStats = 'Scanning ' + dir.name + '/…';
            return walk(dir, dir.name + '/');
          })
          .then(function () {
            if (!found.length) {
              self.navError = 'No *' + NAV_SUFFIX + ' files in that folder.';
              self.syncNavState();
              return;
            }
            found.sort(function (a, b) {
              return a.path.localeCompare(b.path);
            });
            self.navAddFiles(found);
          })
          .catch(function (e) {
            if (e && e.name !== 'AbortError') {
              self.navError = String(e.message || e);
            }
            self.syncNavState();
          });
      },

      // Opening a file that's already open (by path) replaces it rather than doubling its edges.
      navAddFiles: function (loaded) {
        var errors = [];
        for (var i = 0; i < loaded.length; i++) {
          var entry = loaded[i];
          var parsed;
          try {
            parsed = parseNavFile(entry.text);
          } catch (e) {
            errors.push(entry.name + ': ' + e.message);
            continue;
          }
          parsed.name = entry.name;
          parsed.path = entry.path;
          parsed.handle = entry.handle;
          parsed.dirty = false;
          var existing = -1;
          for (var f = 0; f < nav.files.length; f++) {
            if (nav.files[f].path === entry.path) {
              existing = f;
            }
          }
          if (existing === -1) {
            nav.files.push(parsed);
          } else {
            nav.files[existing] = parsed;
          }
        }
        this.navError = errors.join('\n');
        this.navNotice = '';
        if (nav.files.length) {
          this.showNavGraph = true;
        }
        nav.undo = [];
        this.navRebuild();
      },

      closeNavFiles: function () {
        if (this.navDirty && !window.confirm('Discard unsaved nav graph changes?')) {
          return;
        }
        nav.files = [];
        nav.loose = [];
        nav.selected = null;
        nav.undo = [];
        this.showNavGraph = false;
        this.navError = '';
        this.navNotice = '';
        this.navTarget = 0;
        this.navOpen = -1;
        this.navRebuild();
      },

      // Expands `index`'s point list (collapsing the last) and makes it the file new edges go to;
      // clicking the open one collapses it.
      navToggleFile: function (index) {
        if (this.navOpen === index) {
          this.navOpen = -1;
        } else {
          this.navOpen = index;
          this.navTarget = index;
        }
        this.navPoints = this.navOpen >= 0 ? navPointsOf(nav.files[this.navOpen]) : [];
      },

      // A row of a file's point list: selects the point and moves the map onto it.
      navPickPoint: function (key) {
        var node = nav.nodes[key];
        if (!node) {
          return;
        }
        this.navSelectNode(key);
        this.focusOn(node.x, node.y, node.level);
      },

      // Adds an empty `*.nav-edges.toml`. Where the browser can write files it's created on disk
      // straight away through a save dialog (so it lands wherever the viewer picks, usually next to
      // the others under `data/`); otherwise it's a name to be downloaded on the next Save.
      navNewFile: function () {
        var self = this;
        var content = 'edges = [\n]\n';
        function add(name, handle, dirty) {
          for (var i = 0; i < nav.files.length; i++) {
            if (nav.files[i].path === name) {
              self.navError = name + ' is already open.';
              return;
            }
          }
          var parsed = parseNavFile(content);
          parsed.name = name;
          parsed.path = name;
          parsed.handle = handle;
          parsed.dirty = dirty;
          nav.files.push(parsed);
          nav.undo = [];
          self.navError = '';
          self.showNavGraph = true;
          self.navOpen = nav.files.length - 1;
          self.navTarget = self.navOpen;
          self.navRebuild();
        }
        if (typeof window.showSaveFilePicker === 'function') {
          window
            .showSaveFilePicker({
              suggestedName: 'new' + NAV_SUFFIX,
              types: [{ description: 'Nav graph edges', accept: { 'text/plain': ['.toml'] } }],
            })
            .then(function (handle) {
              if (handle.name.slice(-NAV_SUFFIX.length) !== NAV_SUFFIX) {
                self.navError = 'Nav files must be named *' + NAV_SUFFIX + ' to be loaded by the server.';
              }
              return handle.createWritable().then(function (writable) {
                return writable.write(content).then(function () {
                  return writable.close();
                });
              }).then(function () {
                add(handle.name, handle, false);
              });
            })
            .catch(function (e) {
              if (e && e.name !== 'AbortError') {
                self.navError = String(e.message || e);
              }
            });
          return;
        }
        var name = window.prompt('New file name', 'new' + NAV_SUFFIX);
        if (!name) {
          return;
        }
        name = name.trim();
        if (name.slice(-NAV_SUFFIX.length) !== NAV_SUFFIX) {
          name += NAV_SUFFIX;
        }
        add(name, null, true);
      },

      // Files are written one after another rather than all at once: a file opened through "Files…"
      // only has read access until its first save asks for write access, and a second permission
      // prompt fired while the first is still showing is rejected outright.
      saveNavFiles: function () {
        var self = this;
        var dirty = nav.files.filter(function (file) {
          return file.dirty;
        });
        var written = [];
        var downloaded = [];
        var chain = Promise.resolve();
        dirty.forEach(function (file) {
          chain = chain.then(function () {
            var text = serializeNavFile(file);
            return self.navWrite(file, text).then(function () {
              (file.handle ? written : downloaded).push(file.name);
              // Re-parse what was written so the next save diffs against it, not the original.
              var parsed = parseNavFile(text);
              file.head = parsed.head;
              file.items = parsed.items;
              file.tail = parsed.tail;
              file.dirty = false;
            });
          });
        });
        this.navNotice = '';
        chain
          .then(function () {
            self.navError = nav.loose.length ? nav.loose.length + ' unconnected point(s) not saved — connect them with an edge first.' : '';
            nav.undo = [];
          })
          .catch(function (e) {
            self.navError = 'Save failed: ' + (e.message || e);
          })
          .then(function () {
            var notice = [];
            if (written.length) {
              notice.push('Saved ' + written.join(', ') + '.');
            }
            if (downloaded.length) {
              // No file handle to write through (Firefox/Safari, or a browser with the File System
              // Access API turned off): the only way out is a download.
              notice.push('Downloaded ' + downloaded.join(', ') + ' — this browser can\'t write to the opened files, so copy the download over the original.');
            }
            self.navNotice = notice.join(' ');
            self.navRebuild();
          });
      },

      // Writes `text` through the file's handle and reads it back, so a write that silently didn't
      // land is reported rather than the file just being marked clean.
      navWrite: function (file, text) {
        if (!file.handle) {
          var blob = new Blob([text], { type: 'text/plain' });
          var a = document.createElement('a');
          a.href = URL.createObjectURL(blob);
          a.download = file.name;
          document.body.appendChild(a);
          a.click();
          a.remove();
          setTimeout(function () {
            URL.revokeObjectURL(a.href);
          }, 1000);
          return Promise.resolve();
        }
        var handle = file.handle;
        var query = typeof handle.queryPermission === 'function' ? handle.queryPermission({ mode: 'readwrite' }) : Promise.resolve('granted');
        return query
          .then(function (state) {
            // Only prompt when access isn't already there — a folder opened read-write covers every
            // file under it, and asking again would needlessly spend the click's user activation.
            return state === 'granted' || typeof handle.requestPermission !== 'function' ? state : handle.requestPermission({ mode: 'readwrite' });
          })
          .then(function (state) {
            if (state !== 'granted') {
              throw new Error('write permission denied for ' + file.name);
            }
            return handle.createWritable();
          })
          .then(function (writable) {
            return writable.write(text).then(function () {
              return writable.close();
            });
          })
          .then(function () {
            return handle.getFile();
          })
          .then(function (written) {
            return written.text();
          })
          .then(function (content) {
            if (content !== text) {
              throw new Error(file.name + ' was written but reads back differently');
            }
          });
      },

      // --- Model ---

      // Rebuilds the node index from every open file's edges (plus loose nodes) and refreshes the
      // panel's summary fields. Cheap enough (a few hundred edges) to run after every edit.
      navRebuild: function () {
        var nodes = {};
        function node(tile) {
          var key = tileKey(tile);
          if (!nodes[key]) {
            nodes[key] = { key: key, x: tile.x, y: tile.y, level: tile.level || 0, edges: [], loose: false };
          }
          return nodes[key];
        }
        var edgeCount = 0;
        for (var f = 0; f < nav.files.length; f++) {
          var items = nav.files[f].items;
          for (var i = 0; i < items.length; i++) {
            var edge = items[i].edge;
            if (!edge) {
              continue;
            }
            edge.file = f;
            edgeCount++;
            node(edge.from).edges.push(edge);
            node(edge.to).edges.push(edge);
          }
        }
        var loose = [];
        for (var l = 0; l < nav.loose.length; l++) {
          var key = tileKey(nav.loose[l]);
          if (!nodes[key]) {
            node(nav.loose[l]).loose = true;
            loose.push(nav.loose[l]);
          }
        }
        nav.loose = loose;
        nav.nodes = nodes;
        var sel = nav.selected;
        if (sel && ((sel.node && !nodes[sel.node]) || (sel.edge && !this.navEdgeExists(sel.edge)))) {
          nav.selected = null;
        }
        this.navStats = nav.files.length ? edgeCount + ' edges · ' + Object.keys(nodes).length + ' points' : '';
        this.syncNavState();
        this.scheduleRender();
      },

      navEdgeExists: function (edge) {
        var file = nav.files[edge.file];
        return !!file && file.items.some(function (item) {
          return item.edge === edge;
        });
      },

      syncNavState: function () {
        this.navLoaded = nav.files.length > 0;
        this.navFiles = nav.files.map(function (file, i) {
          var edges = 0;
          for (var k = 0; k < file.items.length; k++) {
            if (file.items[k].edge) {
              edges++;
            }
          }
          return { index: i, name: file.name, path: file.path, dirty: file.dirty, edges: edges, writable: !!file.handle };
        });
        this.navDirty = nav.files.some(function (file) {
          return file.dirty;
        });
        this.navCanUndo = nav.undo.length > 0;
        if (this.navTarget >= nav.files.length) {
          this.navTarget = 0;
        }
        if (this.navOpen >= nav.files.length) {
          this.navOpen = -1;
        }
        var sel = nav.selected;
        // A newly picked point or edge opens the file it lives in (and collapses the rest); an
        // unchanged selection leaves whichever file was opened by hand alone.
        var sig = !sel ? '' : sel.node ? sel.node : tileKey(sel.edge.from) + '>' + tileKey(sel.edge.to) + '@' + sel.edge.file;
        if (sig !== nav.selSig) {
          nav.selSig = sig;
          var owner = -1;
          if (sel && sel.node && nav.nodes[sel.node] && nav.nodes[sel.node].edges.length) {
            var touching = nav.nodes[sel.node].edges;
            var target = this.navTarget;
            owner = touching.some(function (edge) {
              return edge.file === target;
            })
              ? target
              : touching[0].file;
          } else if (sel && sel.edge) {
            owner = sel.edge.file;
          }
          if (owner >= 0) {
            this.navOpen = owner;
            this.navTarget = owner;
          }
          if (sig) {
            this.$nextTick(function () {
              // Scrolls only the file list, not the page: `scrollIntoView` would also scroll every
              // ancestor, jolting the whole map on each click that selects something.
              var row = document.querySelector('[data-nav-selected="true"]');
              var list = row && row.closest('.wm-nav-files');
              if (list) {
                var top = row.getBoundingClientRect().top - list.getBoundingClientRect().top + list.scrollTop;
                if (top < list.scrollTop) {
                  list.scrollTop = top;
                } else if (top + row.offsetHeight > list.scrollTop + list.clientHeight) {
                  list.scrollTop = top + row.offsetHeight - list.clientHeight;
                }
              }
            });
          }
        }
        this.navSelectedKey = sel && sel.node ? sel.node : '';
        this.navPoints = this.navOpen >= 0 ? navPointsOf(nav.files[this.navOpen]) : [];
        if (!sel) {
          this.navSelection = '';
        } else if (sel.node) {
          var n = nav.nodes[sel.node];
          var oneWay = n.edges.filter(function (edge) {
            return edge.directed;
          }).length;
          this.navSelection = n.x + ', ' + n.y + ', ' + n.level + ' — ' + (n.loose ? 'unconnected' : n.edges.length + ' edge' + (n.edges.length === 1 ? '' : 's')) +
            (oneWay ? ' (' + oneWay + ' with actions)' : '');
        } else {
          var e = sel.edge;
          this.navSelection = tileKey(e.from).replace(/,/g, ', ') + ' → ' + tileKey(e.to).replace(/,/g, ', ') +
            (e.directed ? ' (one-way, has actions)' : '') + ' — ' + nav.files[e.file].name;
        }
        this.syncEdgePanel();
      },

      // Plain copies for the Edge panel — the panel edits through the navSet*/navAdd* methods,
      // never these objects, so they're rebuilt from scratch on every change.
      syncEdgePanel: function () {
        var sel = nav.selected;
        var edge = sel && sel.edge;
        if (!edge) {
          nav.pick = null;
        }
        this.navPicking = nav.pick === null ? -1 : nav.pick;
        function label(t) {
          return t.x + ', ' + t.y + ((t.level || 0) ? ', ' + t.level : '');
        }
        function summary(e) {
          return e.meta.actions.map(function (a) {
            return a.type || '?';
          }).join(' → ');
        }
        var node = sel && sel.node ? nav.nodes[sel.node] : null;
        nav.nodeEdges = node ? node.edges.slice() : [];
        this.navNodeEdges = nav.nodeEdges.map(function (e, i) {
          var out = tileKey(e.from) === node.key;
          return {
            i: i,
            label: (e.directed ? (out ? '→ ' : '← ') : '↔ ') + label(out ? e.to : e.from),
            actions: summary(e),
          };
        });
        if (!edge) {
          this.navEdge = null;
          return;
        }
        var reverse = this.navReverseOf(edge);
        this.navEdge = {
          title: label(edge.from) + (edge.directed ? ' → ' : ' ↔ ') + label(edge.to),
          file: nav.files[edge.file] ? nav.files[edge.file].name : '',
          directed: edge.directed,
          cost: edge.meta.cost,
          walkCost: Math.abs(edge.to.x - edge.from.x) + Math.abs(edge.to.y - edge.from.y),
          requires: edge.meta.requires,
          hasReverse: !!reverse,
          reverseSummary: reverse ? summary(reverse) || 'walk' : '',
          actions: edge.meta.actions.map(function (action) {
            var spec = ACTION_KEYS[action.type];
            var keys = action.fields.map(function (f) {
              return f.key;
            });
            return {
              type: action.type,
              known: !!spec,
              fields: action.fields.map(function (f) {
                return { key: f.key, value: f.value, empty: f.value.trim() === '' };
              }),
              missing: spec ? spec.required.filter(function (key) {
                return keys.indexOf(key) === -1;
              }) : [],
              suggestions: spec ? spec.required.concat(spec.optional).filter(function (key) {
                return keys.indexOf(key) === -1;
              }) : [],
              pickable: action.type === 'object' || action.type === 'tile' || action.type === 'floor_item' || keys.indexOf('x') !== -1,
            };
          }),
        };
      },

      // Snapshot of every file's edge list (edges copied, since moves mutate them in place) for
      // undo. Taken before each edit.
      navCheckpoint: function () {
        nav.undo.push({
          files: nav.files.map(function (file) {
            return {
              dirty: file.dirty,
              items: file.items.map(function (item) {
                if (!item.edge) {
                  return item;
                }
                var copy = Object.assign({}, item.edge);
                copy.from = copyTile(item.edge.from);
                copy.to = copyTile(item.edge.to);
                copy.meta = copyMeta(item.edge.meta);
                return { edge: copy };
              }),
            };
          }),
          loose: nav.loose.map(copyTile),
          target: this.navTarget,
        });
        if (nav.undo.length > UNDO_LIMIT) {
          nav.undo.shift();
        }
      },

      navUndo: function () {
        var state = nav.undo.pop();
        if (!state) {
          return;
        }
        for (var f = 0; f < state.files.length && f < nav.files.length; f++) {
          nav.files[f].items = state.files[f].items;
          nav.files[f].dirty = state.files[f].dirty;
        }
        nav.loose = state.loose;
        nav.selected = null;
        this.navTarget = state.target;
        this.navRebuild();
      },

      navMarkDirty: function (fileIndex) {
        if (nav.files[fileIndex]) {
          nav.files[fileIndex].dirty = true;
        }
      },

      // Every open file holding an edge that touches `key` gets that edge's endpoint moved. Refused
      // (returns false) onto a tile that's already a point — merging two points by accident while
      // dragging past one would be easy and hard to notice.
      navMoveNode: function (key, tile) {
        var node = nav.nodes[key];
        if (!node || nav.nodes[tileKey(tile)]) {
          return false;
        }
        if (node.loose) {
          for (var l = 0; l < nav.loose.length; l++) {
            if (tileKey(nav.loose[l]) === key) {
              nav.loose[l] = copyTile(tile);
            }
          }
        }
        for (var i = 0; i < node.edges.length; i++) {
          var edge = node.edges[i];
          if (tileKey(edge.from) === key) {
            edge.from = copyTile(tile);
          }
          if (tileKey(edge.to) === key) {
            edge.to = copyTile(tile);
          }
          this.navMarkDirty(edge.file);
        }
        nav.selected = { node: tileKey(tile) };
        this.navRebuild();
        return true;
      },

      navDeleteNode: function (key) {
        var node = nav.nodes[key];
        if (!node) {
          return;
        }
        this.navCheckpoint();
        nav.loose = nav.loose.filter(function (t) {
          return tileKey(t) !== key;
        });
        for (var f = 0; f < nav.files.length; f++) {
          var before = nav.files[f].items.length;
          nav.files[f].items = nav.files[f].items.filter(function (item) {
            return !item.edge || (tileKey(item.edge.from) !== key && tileKey(item.edge.to) !== key);
          });
          if (nav.files[f].items.length !== before) {
            nav.files[f].dirty = true;
          }
        }
        nav.selected = null;
        this.navRebuild();
      },

      navDeleteEdge: function (edge) {
        var file = nav.files[edge.file];
        if (!file) {
          return;
        }
        this.navCheckpoint();
        file.items = file.items.filter(function (item) {
          return item.edge !== edge;
        });
        file.dirty = true;
        nav.selected = null;
        this.navRebuild();
      },

      // Walk edge between two tiles, into `navTarget`'s file. Skipped if the two are already joined.
      navConnect: function (from, to) {
        if (sameTile(from, to)) {
          return false;
        }
        var existing = nav.nodes[tileKey(from)];
        if (existing) {
          for (var i = 0; i < existing.edges.length; i++) {
            var e = existing.edges[i];
            if ((sameTile(e.from, from) && sameTile(e.to, to)) || (!e.directed && sameTile(e.from, to) && sameTile(e.to, from))) {
              return false;
            }
          }
        }
        var file = nav.files[this.navTarget];
        if (!file) {
          return false;
        }
        file.items.push({ edge: newEdge(file, from, to) });
        file.dirty = true;
        return true;
      },

      navDeleteSelected: function () {
        var sel = nav.selected;
        if (!sel) {
          return;
        }
        if (sel.node) {
          this.navDeleteNode(sel.node);
        } else {
          this.navDeleteEdge(sel.edge);
        }
      },

      navSelectNode: function (key) {
        nav.selected = key ? { node: key } : null;
        // New edges from here go into the file this point already belongs to, so extending an
        // area's graph doesn't scatter its edges into whichever file happened to be picked.
        var node = key && nav.nodes[key];
        if (node && node.edges.length) {
          this.navTarget = node.edges[0].file;
        }
        this.syncNavState();
        this.scheduleRender();
      },

      // --- Edge editor (the "Edge" panel) ---
      //
      // Every change goes through `navEditEdge`, which takes an undo checkpoint, applies it to the
      // selected edge's `meta` and refreshes the panel's copy (`navEdge`). Fields commit on `change`
      // (Enter or leaving the field), not per keystroke, so one edit is one undo step.

      navSelectedEdge: function () {
        return nav.selected && nav.selected.edge ? nav.selected.edge : null;
      },

      navEditEdge: function (change) {
        var edge = this.navSelectedEdge();
        if (!edge) {
          return;
        }
        var before = JSON.stringify(edge.meta);
        this.navCheckpoint();
        change.call(this, edge.meta, edge);
        if (JSON.stringify(edge.meta) === before) {
          nav.undo.pop();
          this.syncNavState();
          return;
        }
        edge.directed = edge.meta.actions.length > 0;
        this.navMarkDirty(edge.file);
        this.navRebuild();
      },

      navSetCost: function (value) {
        value = value.trim();
        if (value !== '' && !/^\d+$/.test(value)) {
          this.navError = 'Cost must be a whole number.';
          this.syncNavState();
          return;
        }
        this.navError = '';
        this.navEditEdge(function (meta) {
          meta.cost = value;
        });
      },

      navSetRequires: function (value) {
        value = value.trim();
        var problem = value === '' ? '' : value[0] !== '[' ? 'must be an array: [{ ... }]' : valueProblem(value);
        if (problem) {
          this.navError = 'Requires: ' + problem + '.';
          this.syncNavState();
          return;
        }
        this.navError = '';
        this.navEditEdge(function (meta) {
          meta.requires = value === '' ? '' : oneLine(value);
        });
      },

      // Changing an action's type fills in the keys the new type can't do without.
      navSetActionType: function (index, type) {
        type = type.trim();
        this.navEditEdge(function (meta) {
          var action = meta.actions[index];
          action.type = type;
          var spec = ACTION_KEYS[type];
          (spec ? spec.required : []).forEach(function (key) {
            if (!action.fields.some(function (field) {
              return field.key === key;
            })) {
              action.fields.push({ key: key, value: '' });
            }
          });
        });
      },

      navSetField: function (index, fieldIndex, part, value) {
        value = part === 'key' ? value.trim() : value;
        if (part === 'value' && /^[{\[]/.test(value.trim()) && valueProblem(value)) {
          this.navError = 'Action ' + (index + 1) + ': ' + valueProblem(value) + '.';
          this.syncNavState();
          return;
        }
        this.navError = '';
        this.navEditEdge(function (meta) {
          var field = meta.actions[index].fields[fieldIndex];
          field[part] = part === 'value' && /^[{\[]/.test(value.trim()) ? oneLine(value) : value;
        });
      },

      navAddField: function (index, key) {
        this.navEditEdge(function (meta) {
          meta.actions[index].fields.push({ key: key || '', value: '' });
        });
      },

      navRemoveField: function (index, fieldIndex) {
        this.navEditEdge(function (meta) {
          meta.actions[index].fields.splice(fieldIndex, 1);
        });
      },

      navRemoveAction: function (index) {
        this.navEditEdge(function (meta) {
          meta.actions.splice(index, 1);
        });
      },

      navMoveAction: function (index, by) {
        this.navEditEdge(function (meta) {
          var to = index + by;
          if (to < 0 || to >= meta.actions.length) {
            return;
          }
          var moved = meta.actions.splice(index, 1)[0];
          meta.actions.splice(to, 0, moved);
        });
      },

      // Appends one of the ready-made actions (see ACTION_TEMPLATES). The first action on an edge
      // also turns it one-way, and gives it a cost if it had none — edges with actions aren't
      // costed by distance like walk edges are, so without one it'd be free.
      navAddAction: function (template) {
        this.navEditEdge(function (meta, edge) {
          if (!meta.actions.length && meta.cost === '') {
            meta.cost = String(Math.max(1, Math.abs(edge.to.x - edge.from.x) + Math.abs(edge.to.y - edge.from.y)));
          }
          ACTION_TEMPLATES[template](edge).forEach(function (action) {
            meta.actions.push(action);
          });
        });
      },

      // The next click on the map sets this action's `x`/`y` instead of editing the graph.
      navPickTile: function (index) {
        nav.pick = nav.pick === index ? null : index;
        this.syncNavState();
        this.navUpdateCursor(null);
      },

      navPickAt: function (tile) {
        var index = nav.pick;
        nav.pick = null;
        this.navEditEdge(function (meta) {
          var action = meta.actions[index];
          if (!action) {
            return;
          }
          ['x', 'y'].forEach(function (key) {
            var field = action.fields.filter(function (f) {
              return f.key === key;
            })[0];
            if (!field) {
              field = { key: key, value: '' };
              action.fields.push(field);
            }
            field.value = String(tile[key]);
          });
        });
      },

      // The edge going the other way between the same two tiles, if there is one.
      navReverseOf: function (edge) {
        var node = nav.nodes[tileKey(edge.to)];
        if (!node) {
          return null;
        }
        for (var i = 0; i < node.edges.length; i++) {
          var other = node.edges[i];
          if (other !== edge && sameTile(other.from, edge.to) && sameTile(other.to, edge.from)) {
            return other;
          }
        }
        return null;
      },

      navSelectEdge: function (edge) {
        nav.selected = { edge: edge };
        nav.pick = null;
        this.navTarget = edge.file;
        this.syncNavState();
        this.scheduleRender();
      },

      navGoReverse: function () {
        var edge = this.navSelectedEdge();
        var reverse = edge && this.navReverseOf(edge);
        if (reverse) {
          this.navSelectEdge(reverse);
        }
      },

      // One-way edges mostly come in pairs (through a door and back). Copies this edge's actions
      // onto a new edge the other way, with any action that walked to this edge's end retargeted
      // to its start — the rest (which door, which object) is the same both ways.
      navCreateReverse: function () {
        var edge = this.navSelectedEdge();
        var file = edge && nav.files[edge.file];
        if (!file || this.navReverseOf(edge)) {
          return;
        }
        this.navCheckpoint();
        var meta = copyMeta(edge.meta);
        meta.actions.forEach(function (action) {
          if (action.type !== 'tile') {
            return;
          }
          var x = action.fields.filter(function (f) {
            return f.key === 'x';
          })[0];
          var y = action.fields.filter(function (f) {
            return f.key === 'y';
          })[0];
          if (x && y && x.value === String(edge.to.x) && y.value === String(edge.to.y)) {
            x.value = String(edge.from.x);
            y.value = String(edge.from.y);
          }
        });
        var reverse = newEdge(file, edge.to, edge.from, meta);
        // Straight after the original, where the existing files keep each pair.
        var at = file.items.findIndex(function (item) {
          return item.edge === edge;
        });
        file.items.splice(at + 1, 0, { edge: reverse });
        file.dirty = true;
        this.navRebuild();
        this.navSelectEdge(reverse);
      },

      // For the point panel: select one of a point's edges (the only way to reach one of a
      // two-way pair, which draw over each other).
      navPickEdge: function (index) {
        var edge = nav.nodeEdges[index];
        if (edge) {
          this.navSelectEdge(edge);
        }
      },

      navCancelPick: function () {
        if (nav.pick === null) {
          return false;
        }
        nav.pick = null;
        this.syncNavState();
        this.navUpdateCursor(null);
        return true;
      },

      // --- Interaction (called from worldmap.js's pointer handlers) ---

      navEditing: function () {
        return this.showNavGraph && this.navLoaded;
      },

      // Viewport-local screen position of the centre of a game tile. Tile (x, y) covers the map from
      // x to x + 1 and y to y + 1 (the same square `updateHoverTile` highlights), so a point sits in
      // the middle of the tile it's on rather than on its south-west corner.
      navScreen: function (t) {
        return { x: this._offsetX + (t.x + 0.5) * this._scale, y: this._offsetY - (t.y + 0.5) * this._scale };
      },

      navLocal: function (e) {
        var rect = this.viewport.getBoundingClientRect();
        return { x: e.clientX - rect.left, y: e.clientY - rect.top };
      },

      navTileAt: function (e) {
        var p = this.navLocal(e);
        return {
          x: Math.floor((p.x - this._offsetX) / this._scale),
          y: Math.floor((this._offsetY - p.y) / this._scale),
          level: this.level,
        };
      },

      navNodeAt: function (e) {
        var p = this.navLocal(e);
        var best = null;
        var bestDist = NODE_HIT_PX;
        for (var key in nav.nodes) {
          var n = nav.nodes[key];
          if (n.level !== this.level) {
            continue;
          }
          var s = this.navScreen(n);
          var d = Math.hypot(s.x - p.x, s.y - p.y);
          if (d <= bestDist) {
            best = n;
            bestDist = d;
          }
        }
        return best;
      },

      navEdgeAt: function (e) {
        var p = this.navLocal(e);
        var best = null;
        var bestDist = EDGE_HIT_PX;
        for (var f = 0; f < nav.files.length; f++) {
          var items = nav.files[f].items;
          for (var i = 0; i < items.length; i++) {
            var edge = items[i].edge;
            if (!edge || edge.from.level !== this.level || edge.to.level !== this.level) {
              continue;
            }
            var a = this.navScreen(edge.from);
            var b = this.navScreen(edge.to);
            var d = distToSegment(p.x, p.y, a.x, a.y, b.x, b.y);
            if (d <= bestDist) {
              best = edge;
              bestDist = d;
            }
          }
        }
        return best;
      },

      // The editor has no modes; what a press does depends on what's under it:
      //
      //   press on a point, drag   move the point
      //   click a point            select it (clicking the selected one again deselects it)
      //   shift+click a point      join the selected point to it
      //   click the map            with a point selected: add a point joined to it and carry on
      //                            drawing from there; otherwise select the edge under the cursor,
      //                            or start a new point if there isn't one
      //   right-click              delete the point or edge under the cursor, else stop drawing
      //   drag the map             pan, as ever
      //
      // Holding Space switches all of that off, so any drag (even one starting on a point) pans.
      navInteractive: function () {
        return this.navEditing() && !nav.panKey;
      },

      // Returns true when the press lands on a point, so the map doesn't also start panning — it
      // may become a drag (move) or stay a click (select), which navPointerMove/Up decide.
      navPointerDown: function (e) {
        nav.press = null;
        if (!this.navInteractive()) {
          return false;
        }
        nav.press = { x: e.clientX, y: e.clientY, shift: e.shiftKey };
        // Picking a tile for an action: a press on a point picks that tile rather than dragging it.
        var node = nav.pick === null && this.navNodeAt(e);
        if (!node) {
          return false;
        }
        nav.drag = { key: node.key, moved: false };
        return true;
      },

      // Returns true while a point is pressed, so the map doesn't pan underneath it.
      navPointerMove: function (e) {
        if (!this.navEditing()) {
          return false;
        }
        nav.hover = this.navTileAt(e);
        // The point a click here would land on — the same hit test the click uses, so the preview
        // never promises something the click won't do.
        var hovered = nav.drag ? null : this.navNodeAt(e);
        nav.hoverNode = hovered ? hovered.key : null;
        nav.hoverShift = e.shiftKey;
        if (nav.drag) {
          var press = nav.press;
          if (!nav.drag.moved) {
            if (!press || Math.hypot(e.clientX - press.x, e.clientY - press.y) <= CLICK_SLOP_PX) {
              return true;
            }
            // Past the slop: it's a move, not a click. One undo step covers the whole drag.
            this.navCheckpoint();
            nav.drag.moved = true;
            this.navSelectNode(nav.drag.key);
          }
          var tile = this.navTileAt(e);
          tile.level = nav.nodes[nav.drag.key] ? nav.nodes[nav.drag.key].level : this.level;
          if (tileKey(tile) !== nav.drag.key && this.navMoveNode(nav.drag.key, tile)) {
            nav.drag.key = tileKey(tile);
          }
          return true;
        }
        this.navUpdateCursor(e);
        if (nav.selected && nav.selected.node) {
          // The preview line follows the cursor.
          this.scheduleRender();
        }
        return false;
      },

      navPointerUp: function (e) {
        var press = nav.press;
        var drag = nav.drag;
        nav.press = null;
        nav.drag = null;
        if (drag && drag.moved) {
          this.syncNavState();
          this.navUpdateCursor(e);
          return;
        }
        if (!press || !this.navInteractive() || Math.hypot(e.clientX - press.x, e.clientY - press.y) > CLICK_SLOP_PX) {
          return;
        }
        this.navClick(e, press.shift);
      },

      // Pointer lost mid-press (cancelled, or a second finger turned it into a pinch): whatever a
      // drag already moved stays moved, but the press no longer counts as a click.
      navPointerCancel: function () {
        nav.drag = null;
        nav.press = null;
      },

      navClick: function (e, shift) {
        if (nav.pick !== null) {
          this.navPickAt(this.navTileAt(e));
          return;
        }
        var node = this.navNodeAt(e);
        var sel = nav.selected;
        var from = sel && sel.node ? nav.nodes[sel.node] : null;
        if (node) {
          if (shift && from && from.key !== node.key) {
            this.navCheckpoint();
            if (this.navConnect(from, node)) {
              this.navRebuild();
            } else {
              nav.undo.pop();
            }
            this.navSelectNode(node.key);
          } else if (from && from.key === node.key) {
            this.navSelectNode(null);
          } else {
            this.navSelectNode(node.key);
          }
          return;
        }
        var tile = this.navTileAt(e);
        if (from) {
          // Keep drawing: a new point joined to the selected one, which then becomes the selection,
          // so a path is just a run of clicks.
          this.navCheckpoint();
          if (this.navConnect(from, tile)) {
            this.navRebuild();
            this.navSelectNode(tileKey(tile));
          } else {
            nav.undo.pop();
          }
          return;
        }
        var edge = this.navEdgeAt(e);
        if (edge || sel) {
          // An edge picks it; empty map with an edge selected just clears the selection.
          nav.selected = edge ? { edge: edge } : null;
          if (edge) {
            this.navTarget = edge.file;
          }
          this.syncNavState();
          this.scheduleRender();
          return;
        }
        // Nothing selected, nothing hit: start a new (for now unconnected) point to draw out from.
        this.navCheckpoint();
        nav.loose.push(tile);
        this.navRebuild();
        this.navSelectNode(tileKey(tile));
      },

      // Off the map: no preview line hanging off the edge towards wherever the cursor left.
      navPointerLeave: function () {
        if (nav.hover) {
          nav.hover = null;
          nav.hoverNode = null;
          this.scheduleRender();
        }
      },

      // Right-click. Returns true when the editor handled it, so the browser's menu is suppressed.
      navContextMenu: function (e) {
        if (!this.navInteractive()) {
          return false;
        }
        var node = this.navNodeAt(e);
        var edge = node ? null : this.navEdgeAt(e);
        if (node) {
          this.navDeleteNode(node.key);
        } else if (edge) {
          this.navDeleteEdge(edge);
        } else if (nav.selected) {
          nav.selected = null;
          this.syncNavState();
          this.scheduleRender();
        }
        this.navUpdateCursor(e);
        return true;
      },

      navUpdateCursor: function (e) {
        var vp = this.viewport;
        if (!vp) {
          return;
        }
        var active = this.navInteractive() && !!e;
        vp.classList.toggle('wm-nav-picking', this.navInteractive() && nav.pick !== null);
        var node = active && nav.pick === null && this.navNodeAt(e);
        var drawing = active && !node && nav.selected && nav.selected.node;
        var edge = active && !node && !drawing && this.navEdgeAt(e);
        vp.classList.toggle('wm-nav-grab', !!node);
        vp.classList.toggle('wm-nav-add', !!drawing);
        vp.classList.toggle('wm-nav-hot', !!edge);
      },

      navSetPanKey: function (down) {
        if (nav.panKey === down) {
          return;
        }
        nav.panKey = down;
        this.navUpdateCursor(null);
        this.scheduleRender();
      },

      navKeyDown: function (e) {
        if (!this.navEditing()) {
          return;
        }
        var target = e.target;
        if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.tagName === 'SELECT' || target.isContentEditable)) {
          return;
        }
        var mod = e.ctrlKey || e.metaKey;
        if (e.key === ' ') {
          // Also stops the page scrolling, or a focused panel button being pressed.
          e.preventDefault();
          this.navSetPanKey(true);
        } else if (mod && e.key.toLowerCase() === 'z') {
          e.preventDefault();
          this.navUndo();
        } else if (mod && e.key.toLowerCase() === 's') {
          e.preventDefault();
          if (this.navDirty) {
            this.saveNavFiles();
          }
        } else if (e.key === 'Delete' || e.key === 'Backspace') {
          if (nav.selected) {
            e.preventDefault();
            this.navDeleteSelected();
          }
        } else if (e.key === 'Escape') {
          if (this.navCancelPick()) {
            return;
          }
          nav.selected = null;
          this.syncNavState();
          this.scheduleRender();
        } else if (e.key === 'Shift') {
          // Redraw the preview: with Shift held it snaps to the point under the cursor.
          nav.hoverShift = true;
          this.scheduleRender();
        } else if (!mod && nav.selected && nav.selected.node && e.key.indexOf('Arrow') === 0) {
          // Nudge the selected point a tile at a time — finer than dragging at low zoom.
          e.preventDefault();
          var n = nav.nodes[nav.selected.node];
          var dx = e.key === 'ArrowLeft' ? -1 : e.key === 'ArrowRight' ? 1 : 0;
          var dy = e.key === 'ArrowDown' ? -1 : e.key === 'ArrowUp' ? 1 : 0;
          this.navCheckpoint();
          if (!this.navMoveNode(n.key, { x: n.x + dx, y: n.y + dy, level: n.level })) {
            nav.undo.pop();
          }
        }
      },

      navKeyUp: function (e) {
        if (e.key === ' ') {
          if (nav.panKey) {
            e.preventDefault();
          }
          this.navSetPanKey(false);
        } else if (e.key === 'Shift') {
          nav.hoverShift = false;
          this.scheduleRender();
        }
      },

      // --- Rendering ---

      renderNavGraph: function (rect) {
        var layer = this.navLayer;
        if (!layer) {
          return;
        }
        if (!this.navEditing()) {
          layer.innerHTML = '';
          return;
        }
        var level = this.level;
        var scale = this._scale;
        var pad = 40;
        var self = this;
        function visible(a, b) {
          return !(
            (a.x < -pad && b.x < -pad) ||
            (a.x > rect.width + pad && b.x > rect.width + pad) ||
            (a.y < -pad && b.y < -pad) ||
            (a.y > rect.height + pad && b.y > rect.height + pad)
          );
        }
        var sel = nav.selected;
        var radius = Math.max(3, Math.min(7, scale * 0.6));
        // Drawn size of the point at `tile` (diamonds are bigger), so an arrowhead can end on its rim.
        function rimOf(tile) {
          var node = nav.nodes[tileKey(tile)];
          var transfer = node && node.edges.some(function (e) {
            return e.from.level !== e.to.level;
          });
          return (transfer ? radius * 1.3 : radius) + 1.5;
        }
        var edges = '';
        var marked = '';
        for (var f = 0; f < nav.files.length; f++) {
          var items = nav.files[f].items;
          for (var i = 0; i < items.length; i++) {
            var edge = items[i].edge;
            if (!edge || edge.from.level !== level || edge.to.level !== level) {
              continue;
            }
            var a = self.navScreen(edge.from);
            var b = self.navScreen(edge.to);
            if (!visible(a, b)) {
              continue;
            }
            var selected = sel && (sel.edge === edge || (sel.node && (tileKey(edge.from) === sel.node || tileKey(edge.to) === sel.node)));
            var cls = 'wm-nav-edge' + (edge.directed ? ' wm-nav-edge-directed' : '') + (selected ? ' wm-nav-edge-selected' : '') +
              (sel && sel.edge === edge ? ' wm-nav-edge-picked' : '');
            var end = b;
            var head = '';
            var dx = b.x - a.x;
            var dy = b.y - a.y;
            var len = Math.hypot(dx, dy);
            var headLen = 9;
            // One-way edge: the triangle's tip sits on the destination point's rim and the line stops
            // at its base, instead of a marker that stops short and overlaps the line.
            if (edge.directed && len > rimOf(edge.to) + headLen) {
              var ux = dx / len;
              var uy = dy / len;
              var rim = rimOf(edge.to);
              var tipX = b.x - ux * rim;
              var tipY = b.y - uy * rim;
              var baseX = tipX - ux * headLen;
              var baseY = tipY - uy * headLen;
              end = { x: baseX + ux * 1, y: baseY + uy * 1 };
              head = '<polygon class="wm-nav-arrow' + (selected ? ' wm-nav-arrow-selected' : '') + '" points="' + tipX.toFixed(1) + ',' + tipY.toFixed(1) + ' ' +
                (baseX - uy * 4.5).toFixed(1) + ',' + (baseY + ux * 4.5).toFixed(1) + ' ' + (baseX + uy * 4.5).toFixed(1) + ',' + (baseY - ux * 4.5).toFixed(1) + '"></polygon>';
            }
            var line = '<line class="' + cls + '" x1="' + a.x.toFixed(1) + '" y1="' + a.y.toFixed(1) + '" x2="' + end.x.toFixed(1) + '" y2="' + end.y.toFixed(1) + '"></line>' + head;
            if (selected) {
              marked += line;
            } else {
              edges += line;
            }
          }
        }
        // Euclidean distance in tiles from `anchor` to `tile` ("7.6"), drawn halfway along the line between them.
        // Skipped where the line is too short on screen for the label to sit clear of its ends.
        var diffs = '';
        function diff(anchor, tile) {
          var a = self.navScreen(anchor);
          var b = self.navScreen(tile);
          if (Math.hypot(b.x - a.x, b.y - a.y) < 28) {
            return;
          }
          var dist = Math.hypot(tile.x - anchor.x, tile.y - anchor.y);
          diffs += '<text class="wm-nav-diff" x="' + ((a.x + b.x) / 2).toFixed(1) + '" y="' + ((a.y + b.y) / 2).toFixed(1) + '">' +
            (Number.isInteger(dist) ? dist : dist.toFixed(1)) + '</text>';
        }
        var selNode = sel && sel.node ? nav.nodes[sel.node] : null;
        if (selNode && selNode.level === level && !nav.panKey && !nav.drag) {
          // A dashed line from the selected point to where a click would join it: the tile under the
          // cursor, or with Shift held, the point under it. Nothing over a point without Shift,
          // since clicking there just selects it.
          var target = nav.hoverNode ? (nav.hoverShift ? nav.nodes[nav.hoverNode] : null) : nav.hover;
          if (target && tileKey(target) !== selNode.key) {
            var from = self.navScreen(selNode);
            var to = self.navScreen(target);
            marked += '<line class="wm-nav-edge wm-nav-edge-preview" x1="' + from.x.toFixed(1) + '" y1="' + from.y.toFixed(1) + '" x2="' + to.x.toFixed(1) + '" y2="' + to.y.toFixed(1) + '"></line>';
            diff(selNode, target);
          }
        }
        // The selected edge's actions that name a tile (the door to open, the tile to walk to) are
        // outlined where they point, numbered in the order the bot does them.
        if (sel && sel.edge && (sel.edge.from.level || 0) === level) {
          sel.edge.meta.actions.forEach(function (action, i) {
            var at = {};
            action.fields.forEach(function (f) {
              if ((f.key === 'x' || f.key === 'y') && /^-?\d+$/.test(f.value.trim())) {
                at[f.key] = parseInt(f.value, 10);
              }
            });
            if (at.x === undefined || at.y === undefined) {
              return;
            }
            var p = self.navScreen(at);
            var half = Math.max(scale, 8) / 2;
            diffs += '<rect class="wm-nav-target' + (nav.pick === i ? ' wm-nav-target-picking' : '') + '" x="' + (p.x - half).toFixed(1) + '" y="' + (p.y - half).toFixed(1) +
              '" width="' + (half * 2).toFixed(1) + '" height="' + (half * 2).toFixed(1) + '"></rect>' +
              '<text class="wm-nav-diff" x="' + (p.x + half + 6).toFixed(1) + '" y="' + (p.y - half).toFixed(1) + '">' + (i + 1) + '</text>';
          });
        }
        // The selected point's distance from each neighbour, updating as it's dragged or nudged.
        if (selNode && selNode.level === level) {
          for (var k = 0; k < selNode.edges.length; k++) {
            var touching = selNode.edges[k];
            var other = tileKey(touching.from) === selNode.key ? touching.to : touching.from;
            if ((other.level || 0) === level) {
              diff(other, selNode);
            }
          }
        }
        var nodes = '';
        for (var key in nav.nodes) {
          var n = nav.nodes[key];
          if (n.level !== level) {
            continue;
          }
          var s = self.navScreen(n);
          if (s.x < -pad || s.x > rect.width + pad || s.y < -pad || s.y > rect.height + pad) {
            continue;
          }
          // A point with an edge to another floor (stairs, ladders) is drawn as a diamond.
          var transfer = n.edges.some(function (edge) {
            return edge.from.level !== edge.to.level;
          });
          var ncls = 'wm-nav-node' + (n.loose ? ' wm-nav-node-loose' : '') + (transfer ? ' wm-nav-node-transfer' : '') +
            (sel && sel.node === key ? ' wm-nav-node-selected' : '');
          var title = '<title>' + n.x + ', ' + n.y + ', ' + n.level + '</title>';
          if (transfer) {
            var r = radius * 1.3;
            nodes += '<polygon class="' + ncls + '" points="' + s.x.toFixed(1) + ',' + (s.y - r).toFixed(1) + ' ' + (s.x + r).toFixed(1) + ',' + s.y.toFixed(1) + ' ' +
              s.x.toFixed(1) + ',' + (s.y + r).toFixed(1) + ' ' + (s.x - r).toFixed(1) + ',' + s.y.toFixed(1) + '">' + title + '</polygon>';
          } else {
            nodes += '<circle class="' + ncls + '" cx="' + s.x.toFixed(1) + '" cy="' + s.y.toFixed(1) + '" r="' + radius.toFixed(1) + '">' + title + '</circle>';
          }
        }
        layer.innerHTML = '<svg style="position:absolute;inset:0;width:100%;height:100%;overflow:visible">' + edges + marked + nodes + diffs + '</svg>';
      },
    };
  };
})();
