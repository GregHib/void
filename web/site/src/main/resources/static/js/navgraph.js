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
// download otherwise), which is also why the display toggle stays disabled until one is opened.
//
// Editing is done on the file's own text rather than a re-serialised copy of it: `parseNavFile`
// splits the array into one chunk per edge, remembering where in each element the `from`/`to`
// tables sit, and `serializeNavFile` stitches the chunks back together with only the tables that
// actually changed rewritten. Comments, hand-wrapped multi-line `actions`, key order and spacing all
// survive a save untouched, so the diff is just the edges that were edited.
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
      keys[key[0]] = { start: start, end: i };
    }
    return keys;
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
          // Edges with actions are one-way (and walk-only ones both ways) — see loadGraph.
          directed: !!keys.actions,
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

  function newEdge(file, from, to) {
    var element = '{ from = ' + formatTile(from) + ', to = ' + formatTile(to) + ' }';
    return {
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
      directed: false,
      added: true,
    };
  }

  function elementText(edge) {
    if (edge.added) {
      return '{ from = ' + formatTile(edge.from) + ', to = ' + formatTile(edge.to) + ' }';
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
      undo: [],
      drag: null,
      press: null,
    };

    return {
      showNavGraph: false,
      navPanelOpen: true,
      navLoaded: false,
      navFiles: [],
      navTool: 'select',
      navTarget: 0,
      navDirty: false,
      navCanUndo: false,
      navStats: '',
      navSelection: '',
      navError: '',
      navFolderAccess: typeof window.showDirectoryPicker === 'function',

      navBoot: function (root) {
        var self = this;
        this.navLayer = root.querySelector('#wm-nav-graph');
        this.$watch('showNavGraph', function () {
          self.navUpdateCursor(null);
          self.scheduleRender();
        });
        this.$watch('navTool', function () {
          self.navUpdateCursor(null);
          self.scheduleRender();
        });
        window.addEventListener('keydown', function (e) {
          self.navKeyDown(e);
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
        this.navTarget = 0;
        this.navRebuild();
      },

      saveNavFiles: function () {
        var self = this;
        var dirty = nav.files.filter(function (file) {
          return file.dirty;
        });
        var saves = dirty.map(function (file) {
          var text = serializeNavFile(file);
          return self.navWrite(file, text).then(function () {
            // Re-parse what was written so the next save diffs against it, not the original.
            var parsed = parseNavFile(text);
            file.head = parsed.head;
            file.items = parsed.items;
            file.tail = parsed.tail;
            file.dirty = false;
          });
        });
        Promise.all(saves)
          .then(function () {
            self.navError = nav.loose.length ? nav.loose.length + ' unconnected point(s) not saved — connect them with an edge first.' : '';
            nav.undo = [];
            self.navRebuild();
          })
          .catch(function (e) {
            self.navError = 'Save failed: ' + (e.message || e);
            self.navRebuild();
          });
      },

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
        var permission = typeof handle.requestPermission === 'function' ? handle.requestPermission({ mode: 'readwrite' }) : Promise.resolve('granted');
        return permission.then(function (state) {
          if (state !== 'granted') {
            throw new Error('write permission denied for ' + file.name);
          }
          return handle.createWritable().then(function (writable) {
            return writable.write(text).then(function () {
              return writable.close();
            });
          });
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
          return { index: i, name: file.name, path: file.path, dirty: file.dirty };
        });
        this.navDirty = nav.files.some(function (file) {
          return file.dirty;
        });
        this.navCanUndo = nav.undo.length > 0;
        if (this.navTarget >= nav.files.length) {
          this.navTarget = 0;
        }
        var sel = nav.selected;
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

      // --- Interaction (called from worldmap.js's pointer handlers) ---

      navEditing: function () {
        return this.showNavGraph && this.navLoaded;
      },

      // Viewport-local screen position of a game tile, matching how every other layer places one.
      navScreen: function (t) {
        return { x: this._offsetX + t.x * this._scale, y: this._offsetY - t.y * this._scale };
      },

      navLocal: function (e) {
        var rect = this.viewport.getBoundingClientRect();
        return { x: e.clientX - rect.left, y: e.clientY - rect.top };
      },

      navTileAt: function (e) {
        var p = this.navLocal(e);
        return {
          x: Math.round((p.x - this._offsetX) / this._scale),
          y: Math.round((this._offsetY - p.y) / this._scale),
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

      // Returns true when the press belongs to the editor (a node grabbed by the Move tool), so the
      // map doesn't also start panning. Every other tool acts on release instead — see navPointerUp
      // — which leaves drag-to-pan working whichever tool is picked.
      navPointerDown: function (e) {
        nav.press = null;
        if (!this.navEditing()) {
          return false;
        }
        nav.press = { x: e.clientX, y: e.clientY };
        if (this.navTool !== 'move') {
          return false;
        }
        var node = this.navNodeAt(e);
        if (!node) {
          return false;
        }
        this.navCheckpoint();
        nav.drag = { key: node.key, moved: false };
        this.navSelectNode(node.key);
        return true;
      },

      // Returns true while a node is being dragged, so the map doesn't pan underneath it.
      navPointerMove: function (e) {
        if (!this.navEditing()) {
          return false;
        }
        nav.hover = this.navTileAt(e);
        if (nav.drag) {
          var tile = this.navTileAt(e);
          tile.level = nav.nodes[nav.drag.key] ? nav.nodes[nav.drag.key].level : this.level;
          if (tileKey(tile) !== nav.drag.key && this.navMoveNode(nav.drag.key, tile)) {
            nav.drag.key = tileKey(tile);
            nav.drag.moved = true;
          }
          return true;
        }
        this.navUpdateCursor(e);
        if (this.navTool === 'add' && nav.selected && nav.selected.node) {
          this.scheduleRender();
        }
        return false;
      },

      navPointerUp: function (e) {
        if (nav.drag) {
          if (!nav.drag.moved) {
            // A press that never moved isn't an edit — drop the checkpoint it took.
            nav.undo.pop();
            this.syncNavState();
          }
          nav.drag = null;
          return;
        }
        var press = nav.press;
        nav.press = null;
        if (!press || !this.navEditing() || Math.hypot(e.clientX - press.x, e.clientY - press.y) > CLICK_SLOP_PX) {
          return;
        }
        this.navClick(e);
      },

      // Pointer lost mid-press (cancelled, or a second finger turned it into a pinch): whatever a
      // drag already moved stays moved, but the press no longer counts as a click.
      navPointerCancel: function () {
        if (nav.drag && !nav.drag.moved) {
          nav.undo.pop();
          this.syncNavState();
        }
        nav.drag = null;
        nav.press = null;
      },

      navClick: function (e) {
        var node = this.navNodeAt(e);
        var tool = this.navTool;
        if (tool === 'delete') {
          if (node) {
            this.navDeleteNode(node.key);
          } else {
            var doomed = this.navEdgeAt(e);
            if (doomed) {
              this.navDeleteEdge(doomed);
            }
          }
          return;
        }
        if (tool === 'add') {
          var from = nav.selected && nav.selected.node ? nav.nodes[nav.selected.node] : null;
          var to = node ? copyTile(node) : this.navTileAt(e);
          if (!from) {
            // Nothing to connect from yet: an existing point just becomes the start, and empty
            // ground becomes a new (for now unconnected) point to draw out from.
            if (!node) {
              this.navCheckpoint();
              nav.loose.push(to);
              this.navRebuild();
            }
            this.navSelectNode(tileKey(to));
            return;
          }
          this.navCheckpoint();
          if (!this.navConnect(from, to)) {
            nav.undo.pop();
            if (!node) {
              return;
            }
          }
          // Keep drawing from the point just reached, so a path is a run of clicks; clicking an
          // existing point without a new edge (it's already joined, or it's the same point) just
          // carries on from there instead.
          this.navRebuild();
          this.navSelectNode(tileKey(to));
          return;
        }
        // Select / Move: pick a point, else an edge, else clear.
        if (node) {
          this.navSelectNode(node.key);
          return;
        }
        var edge = this.navEdgeAt(e);
        nav.selected = edge ? { edge: edge } : null;
        if (edge) {
          this.navTarget = edge.file;
        }
        this.syncNavState();
        this.scheduleRender();
      },

      navUpdateCursor: function (e) {
        var vp = this.viewport;
        if (!vp) {
          return;
        }
        var editing = this.navEditing();
        vp.classList.toggle('wm-nav-add', editing && this.navTool === 'add');
        var hot = editing && e && this.navTool !== 'add' && (this.navNodeAt(e) || (this.navTool !== 'move' && this.navEdgeAt(e)));
        vp.classList.toggle('wm-nav-hot', !!hot);
        vp.classList.toggle('wm-nav-grab', !!hot && this.navTool === 'move');
        vp.classList.toggle('wm-nav-delete', !!hot && this.navTool === 'delete');
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
        if (mod && e.key.toLowerCase() === 'z') {
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
          nav.selected = null;
          this.syncNavState();
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
        } else if (!mod && !e.altKey) {
          var tools = { v: 'select', m: 'move', a: 'add', d: 'delete' };
          var tool = tools[e.key.toLowerCase()];
          if (tool) {
            this.navTool = tool;
          }
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
            var line = '<line class="' + cls + '" x1="' + a.x.toFixed(1) + '" y1="' + a.y.toFixed(1) + '" x2="' + b.x.toFixed(1) + '" y2="' + b.y.toFixed(1) + '"' +
              (edge.directed ? ' marker-end="url(#wm-nav-arrow)"' : '') + '></line>';
            if (selected) {
              marked += line;
            } else {
              edges += line;
            }
          }
        }
        // Add tool: a dashed line from the point being drawn from to the tile under the cursor.
        if (this.navTool === 'add' && sel && sel.node && nav.hover && nav.nodes[sel.node] && nav.nodes[sel.node].level === level) {
          var from = self.navScreen(nav.nodes[sel.node]);
          var to = self.navScreen(nav.hover);
          marked += '<line class="wm-nav-edge wm-nav-edge-preview" x1="' + from.x.toFixed(1) + '" y1="' + from.y.toFixed(1) + '" x2="' + to.x.toFixed(1) + '" y2="' + to.y.toFixed(1) + '"></line>';
        }
        var radius = Math.max(3, Math.min(7, scale * 0.6));
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
        var defs = '<defs><marker id="wm-nav-arrow" viewBox="0 0 10 10" refX="16" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">' +
          '<path d="M0,0 L10,5 L0,10 z" class="wm-nav-arrow"></path></marker></defs>';
        layer.innerHTML = '<svg style="position:absolute;inset:0;width:100%;height:100%;overflow:visible">' + defs + edges + marked + nodes + '</svg>';
      },
    };
  };
})();
