// Shared by the world map's two file-backed editors — the nav graph (navgraph.js, `*.nav-edges.toml`)
// and the area polygons (areas.js, `*.areas.toml`) — and loaded before both:
//
//   VoidToml       the small amount of TOML scanning both need to edit a file's own text in place
//                  (rather than re-serialising it), so comments and layout survive a save.
//   VoidMapFiles   opening files/folders from the viewer's disk and writing them back: the File
//                  System Access API where the browser has it, a file input and a download otherwise.
//   mapEditMethods mixed into `worldMapApp()`: the shared "Load folder…/Load files…" buttons, which
//                  load whichever of the two layers is switched on and still empty, each from its
//                  own files, and `activeEditor`, which decides which editor gets the pointer and
//                  keyboard when both layers are showing.
(function () {
  // Directories under `data/` that can't hold either kind of file and are large enough to make
  // "Load folder" crawl if it had to walk them.
  var SKIPPED_DIRS = { cache: true, 'map-tiles': true, saves: true, dump: true, '.temp': true, '.git': true, node_modules: true };

  // Every file kind an editor loads, by the id `openMapFiles`/`openMapFolder` take.
  var KINDS = {
    nav: { suffix: '.nav-edges.toml', label: 'nav graph' },
    area: { suffix: '.areas.toml', label: 'areas' },
  };

  // --- TOML scanning -------------------------------------------------------------------------

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

  // A value typed into an editor that TOML would read as something other than a string: a number,
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

  // The text of a TOML string literal (`"a"` or `'a'`), or the text itself if it isn't one.
  function stringValue(raw) {
    if (/^"([^"\\]|\\.)*"$/.test(raw)) {
      return JSON.parse(raw);
    }
    if (/^'[^']*'$/.test(raw)) {
      return raw.slice(1, -1);
    }
    return raw;
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

  window.VoidToml = {
    skipString: skipString,
    skipComment: skipComment,
    matchBracket: matchBracket,
    splitArray: splitArray,
    oneLine: oneLine,
    isRawValue: isRawValue,
    displayValue: displayValue,
    tomlValue: tomlValue,
    stringValue: stringValue,
    valueProblem: valueProblem,
  };

  // --- Files ---------------------------------------------------------------------------------

  function endsWith(name, suffix) {
    return name.slice(-suffix.length) === suffix;
  }

  // Which of `kinds` a file belongs to by its name. A file named for neither goes to the only kind
  // asked for, if just one was — "Files…" in one editor's panel opens whatever `.toml` it's given.
  function kindOf(name, kinds) {
    for (var i = 0; i < kinds.length; i++) {
      if (endsWith(name, KINDS[kinds[i]].suffix)) {
        return kinds[i];
      }
    }
    for (var k in KINDS) {
      if (endsWith(name, KINDS[k].suffix)) {
        return null;
      }
    }
    return kinds.length === 1 ? kinds[0] : null;
  }

  // `{ byKind: { nav: [...], area: [...] }, skipped: [names] }` from a flat list of loaded files.
  function group(loaded, kinds) {
    var byKind = {};
    kinds.forEach(function (kind) {
      byKind[kind] = [];
    });
    var skipped = [];
    loaded.forEach(function (entry) {
      var kind = kindOf(entry.name, kinds);
      if (kind) {
        byKind[kind].push(entry);
      } else {
        skipped.push(entry.name);
      }
    });
    Object.keys(byKind).forEach(function (kind) {
      byKind[kind].sort(function (a, b) {
        return a.path.localeCompare(b.path);
      });
    });
    return { byKind: byKind, skipped: skipped };
  }

  function readInputFiles(files, pathOf) {
    return Promise.all(
      files.map(function (file) {
        return file.text().then(function (text) {
          return { name: file.name, path: pathOf(file), text: text, handle: null };
        });
      }),
    );
  }

  // Resolves to null when the viewer cancels the picker.
  function pickFiles(kinds) {
    if (typeof window.showOpenFilePicker === 'function') {
      return window
        .showOpenFilePicker({ multiple: true, types: [{ description: 'Map data', accept: { 'text/plain': ['.toml'] } }] })
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
          return group(loaded, kinds);
        }, cancelled);
    }
    // No File System Access API (Firefox, Safari): read through a file input, save by download.
    return new Promise(function (resolve) {
      var input = document.createElement('input');
      input.type = 'file';
      input.multiple = true;
      input.accept = '.toml';
      input.addEventListener('change', function () {
        readInputFiles(Array.prototype.slice.call(input.files || []), function (file) {
          return file.name;
        }).then(function (loaded) {
          resolve(group(loaded, kinds));
        });
      });
      input.click();
    });
  }

  // Every file of `kinds` anywhere under a picked folder — the repo's `data/` is the one that
  // makes sense, holding everything the server loads.
  function pickFolder(kinds) {
    function wanted(name) {
      return kinds.some(function (kind) {
        return endsWith(name, KINDS[kind].suffix);
      });
    }
    if (typeof window.showDirectoryPicker !== 'function') {
      // Firefox/Safari: a directory <input> hands over every file under the folder (read-only, so
      // saving downloads them, like the plain file picker).
      return new Promise(function (resolve) {
        var input = document.createElement('input');
        input.type = 'file';
        input.setAttribute('webkitdirectory', '');
        input.addEventListener('change', function () {
          var files = Array.prototype.slice.call(input.files || []).filter(function (file) {
            var path = file.webkitRelativePath || file.name;
            return wanted(file.name) && !path.split('/').slice(0, -1).some(function (dir) {
              return SKIPPED_DIRS[dir];
            });
          });
          readInputFiles(files, function (file) {
            return file.webkitRelativePath || file.name;
          }).then(function (loaded) {
            resolve(group(loaded, kinds));
          });
        });
        input.click();
      });
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
          } else if (wanted(entry.name)) {
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
    return window
      .showDirectoryPicker({ mode: 'readwrite' })
      .then(function (dir) {
        return walk(dir, dir.name + '/');
      })
      .then(function () {
        return group(found, kinds);
      }, cancelled);
  }

  function cancelled(e) {
    if (e && e.name === 'AbortError') {
      return null;
    }
    throw e;
  }

  function download(name, text) {
    var blob = new Blob([text], { type: 'text/plain' });
    var a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = name;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(function () {
      URL.revokeObjectURL(a.href);
    }, 1000);
  }

  // Writes `text` through the file's handle and reads it back, so a write that silently didn't
  // land is reported rather than the file just being marked clean. Without a handle (Firefox/Safari,
  // or a browser with the File System Access API turned off) the only way out is a download.
  function write(file, text) {
    if (!file.handle) {
      download(file.name, text);
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
  }

  // A new file of `kind` holding `content`. Where the browser can write files it's created on disk
  // straight away through a save dialog (so it lands wherever the viewer picks, usually next to the
  // others under `data/`); otherwise it's just a name, downloaded on the next Save. Resolves to
  // `{ name, handle, dirty, warning }`, or null when cancelled.
  function create(kind, content) {
    var suffix = KINDS[kind].suffix;
    if (typeof window.showSaveFilePicker === 'function') {
      return window
        .showSaveFilePicker({ suggestedName: 'new' + suffix, types: [{ description: 'Map data', accept: { 'text/plain': ['.toml'] } }] })
        .then(function (handle) {
          return handle.createWritable().then(function (writable) {
            return writable.write(content).then(function () {
              return writable.close();
            });
          }).then(function () {
            return {
              name: handle.name,
              handle: handle,
              dirty: false,
              warning: endsWith(handle.name, suffix) ? '' : 'Files must be named *' + suffix + ' to be loaded by the server.',
            };
          });
        }, cancelled);
    }
    var name = window.prompt('New file name', 'new' + suffix);
    if (!name) {
      return Promise.resolve(null);
    }
    name = name.trim();
    if (!endsWith(name, suffix)) {
      name += suffix;
    }
    return Promise.resolve({ name: name, handle: null, dirty: true, warning: '' });
  }

  window.VoidMapFiles = { KINDS: KINDS, pickFiles: pickFiles, pickFolder: pickFolder, write: write, create: create };

  // --- Alpine methods --------------------------------------------------------------------------

  window.mapEditMethods = function () {
    return {
      // Which editor the pointer and keyboard go to — 'none', 'nav' or 'area', picked with the
      // display panel's "Editing" switch (see `activeEditor`). None to begin with, and loading
      // files doesn't change that, so the map can't be edited by accident while just looking.
      mapEditor: 'none',
      // A problem with the last load (nothing found, a file of neither kind), shown under the
      // load buttons.
      loadError: '',

      mapEditBoot: function () {
        var self = this;
        // Hiding the layer being edited stops editing it, rather than leaving it to come back
        // editable the moment it's switched on again.
        this.$watch('showNavGraph', function (on) {
          if (!on && self.mapEditor === 'nav') {
            self.setMapEditor('none');
          }
        });
        this.$watch('showAreaPolygons', function (on) {
          if (!on && self.mapEditor === 'area') {
            self.setMapEditor('none');
          }
        });
      },

      // Whether each editor has files loaded and its layer showing — what the "Editing" switch
      // offers.
      navEditable: function () {
        return this.showNavGraph && this.navLoaded;
      },

      areaEditable: function () {
        return this.showAreaPolygons && this.areaLoaded;
      },

      // 'nav' or 'area' while that editor is picked and has something to edit, else ''.
      activeEditor: function () {
        if (this.mapEditor === 'nav' && this.navEditable()) {
          return 'nav';
        }
        if (this.mapEditor === 'area' && this.areaEditable()) {
          return 'area';
        }
        return '';
      },

      anyEditable: function () {
        return this.navEditable() || this.areaEditable();
      },

      setMapEditor: function (editor) {
        if (this.mapEditor === editor) {
          return;
        }
        this.mapEditor = editor;
        // Whatever the other editor was previewing under the cursor no longer applies.
        this.navUpdateCursor(null);
        this.areaUpdateCursor(null);
        this.scheduleRender();
      },

      // The layers switched on that have nothing loaded yet — what the display panel's shared load
      // buttons fill.
      kindsToLoad: function () {
        var kinds = [];
        if (this.showNavGraph && !this.navLoaded) {
          kinds.push('nav');
        }
        if (this.showAreaPolygons && !this.areaLoaded) {
          kinds.push('area');
        }
        return kinds;
      },

      loadHint: function () {
        return this.kindsToLoad().map(function (kind) {
          return '*' + KINDS[kind].suffix;
        }).join(' and ');
      },

      // `kinds` left out: whichever layers need loading (see `kindsToLoad`).
      openMapFiles: function (kinds) {
        this.mapLoad(VoidMapFiles.pickFiles, kinds);
      },

      openMapFolder: function (kinds) {
        this.mapLoad(VoidMapFiles.pickFolder, kinds);
      },

      mapLoad: function (pick, kinds) {
        var self = this;
        kinds = kinds || this.kindsToLoad();
        if (!kinds.length) {
          return;
        }
        pick(kinds)
          .then(function (result) {
            if (!result) {
              return;
            }
            var errors = [];
            if (result.skipped.length) {
              errors.push('Not ' + kinds.map(function (kind) {
                return '*' + KINDS[kind].suffix;
              }).join(' or ') + ': ' + result.skipped.join(', '));
            }
            var any = false;
            kinds.forEach(function (kind) {
              var files = result.byKind[kind];
              if (!files.length) {
                if (!result.skipped.length || kinds.length > 1) {
                  errors.push('No *' + KINDS[kind].suffix + ' files found.');
                }
                return;
              }
              any = true;
              if (kind === 'nav') {
                self.navAddFiles(files);
              } else {
                self.areaAddFiles(files);
              }
            });
            self.loadError = errors.join('\n');
            if (!any) {
              self.syncNavState();
              self.syncAreaState();
            }
          })
          .catch(function (e) {
            self.loadError = String((e && e.message) || e);
          });
      },

      // Ctrl+S saves whatever either editor has changed, not just the active one's files.
      saveMapFiles: function () {
        if (this.navDirty) {
          this.saveNavFiles();
        }
        if (this.areaDirty) {
          this.saveAreaFiles();
        }
      },
    };
  };
})();
