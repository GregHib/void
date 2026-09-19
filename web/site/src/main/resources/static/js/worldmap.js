// World map viewer: a slippy-map-style tile pyramid over `map-tiles/{level}/{zoom}/{x}/{y}.png`
// (see void-map-tiles), panned/zoomed with plain DOM <img> tiles rather than <canvas> so each
// tile fades in independently on load instead of the whole frame popping in at once.
//
// Tile math: at ZOOM 8 one 256px tile image is exactly one 64x64-game-tile region (4px/game-tile
// — see the `pxPerTile` comment). Zoom halves/doubles that scale per level either side, same as a
// standard XYZ tile pyramid, so tile index = floor(worldPx / 256) at every zoom.
//
// `zoom` itself is continuous (see ZOOM_STEP), but the tile pyramid only has images at integer
// levels — there's no `8.4/x/y.png`. So tiles are always fetched at `nativeZoom`, the nearest
// integer to the current zoom, and then scaled up/down via `zoomRatio` to match the in-between
// continuous scale — the same "snap the tile set, scale it smoothly in between" trick standard
// slippy maps use for fractional zoom. Everything else (panning, offsets, grid, labels) uses the
// continuous scale directly and doesn't need to know about the native/fractional distinction.
//
// Y is inverted between game space and screen space: game Y increases north, which should read
// as "up" on screen, but CSS/canvas pixel Y increases downward. The tile row index in the
// filename (`ty`) increases together with game Y, so fetching a tile still just floors game Y
// into a row number — but *placing* that row on screen flips, and every other screen-space use
// of a raw game Y (centering, hover, labels, pins) negates it the same way. Get this wrong and
// tiles/labels/pins still look individually fine but end up stacked in the wrong vertical order.
//
// The row number itself also isn't a plain `floor(gameY * scale / 256)`: the generated tile set
// (void-map-tiles) has a constant one-tile offset baked in that only cancels out at zoom 8 — see
// `tileRowIndex` below. Confirmed by diffing tiles pixel-for-pixel across zooms 7/8/9/11 against
// the known Lumbridge tile (region 50,50): a naive row index is off by exactly one zoom-8 tile
// (64 game tiles) at every zoom except 8 itself, which is why "normal zoom" looked right while
// one step either side put the pin a whole region away from the terrain under it.
window.worldMapApp = function () {
  var TILE_SIZE = 256;
  var BASE_ZOOM = 8;
  var BASE_PX_PER_TILE = 4;
  var MIN_ZOOM = 4;
  var MAX_ZOOM = 11;
  var ZOOM_STEP = 0.1;
  var TILE_BASE = 'map-tiles';
  var LOAD_STAGGER_MS = 6;
  var LOAD_STAGGER_MAX = 10;
  var LABEL_CAP_ZOOM = 10;
  // Must match `.wm-tile`'s `transition: opacity var(--dur-fast) ...` in world-map.css (currently
  // 130ms). A tile's `load` event fires the instant its bytes are decoded — well before its
  // opacity has actually finished animating from 0 to 1 — so `flushStaleTiles` can't treat "loaded"
  // as "safe to drop what's behind it" without racing the fade. `transitionend` looked like the
  // fix, but is unreliable here: a tile that resolves fast enough (cached/local) can have its
  // opacity flip 0->1 before the browser ever paints the 0 frame, which skips the transition (and
  // the event) entirely rather than shortening it. A fixed wait matching the CSS duration covers
  // both cases uniformly instead of depending on paint timing.
  var TILE_FADE_MS = 130;

  function pxPerTile(zoom) {
    return BASE_PX_PER_TILE * Math.pow(2, zoom - BASE_ZOOM);
  }

  function nativeZoomFor(zoom) {
    return clamp(Math.round(zoom), MIN_ZOOM, MAX_ZOOM);
  }

  // See the file header: the deployed tile pyramid's row index runs one zoom-8 tile (64 game
  // tiles) ahead of a naive `floor(gameY * scale / 256)`, at every zoom except 8 itself (where
  // the shift cancels out exactly, since 64 * scale(8) == TILE_SIZE). Column indices (x) don't
  // have this issue — only rows do.
  function tileRowIndex(gameY, scale) {
    return Math.floor((gameY - 64) * scale / TILE_SIZE) + 1;
  }

  function clamp(v, min, max) {
    return Math.max(min, Math.min(max, v));
  }

  return {
    gameX: 3200,
    gameY: 3200,
    level: 0,
    zoom: BASE_ZOOM,
    showAreaLabels: true,
    showAreaPolygons: false,
    showRegionGrid: true,
    showRegionLabels: false,
    showPlayerPins: true,
    displayPanelOpen: true,
    tilesMissing: false,
    ptab: 'teleport',
    tpX: '3200',
    tpY: '3200',
    tpZ: '0',
    hoverX: 3200,
    hoverY: 3200,
    hoverAreaNames: [],
    levelLabels: ['SURFACE', 'FLOOR 1', 'FLOOR 2', 'FLOOR 3'],

    tileEls: {},
    _raf: null,
    _urlTimer: null,
    _offsetX: 0,
    _offsetY: 0,
    _scale: BASE_PX_PER_TILE,
    // Tiles no longer in the wanted set (e.g. the previous native zoom's, after a zoom crossing)
    // aren't removed until their replacements finish loading — otherwise the already-loaded old
    // tile disappears immediately while the new one is still fading in from opacity 0, flashing
    // empty background over content that was already there. `_pendingTileLoads` is a running
    // count across every in-flight `<img>`, so a stale tile only gets flushed once *everything*
    // currently loading has settled, not just whatever a single render() pass happened to request.
    // Entries are `{key, el}` (rather than bare elements) so `reviveStaleTile` can find one by key
    // and reclaim it if the zoom crosses back before it's flushed — see renderTiles.
    _staleTileEls: [],
    _pendingTileLoads: 0,
    // Drives `tilesMissing` (the "run MapZoomImageGenerator" empty state): every attempted tile
    // request counts here, and the moment one ever succeeds `tilesMissing` is latched false for
    // good — see `onTileSettled`. Deliberately never resets on pan/zoom, so panning past the one
    // generated corner of an otherwise-empty tile set doesn't make the message flicker back.
    _tileSuccessCount: 0,

    // Named `boot`, not `init` — Alpine auto-calls a data object's own `init()` method with no
    // arguments as a component lifecycle hook, which would collide with (and crash before) this
    // one, which needs the root element passed in from `x-init="boot($el)"`.
    boot: function (root) {
      this.viewport = root.querySelector('#wm-viewport');
      this.tileLayer = root.querySelector('#wm-tile-layer');
      this.gridLayer = root.querySelector('#wm-grid-layer');
      this.regionLabelLayer = root.querySelector('#wm-region-labels');
      this.areaPolygonLayer = root.querySelector('#wm-area-polygons');
      this.areaLabelLayer = root.querySelector('#wm-area-labels');
      this.playerLayer = root.querySelector('#wm-players');

      this.readStateFromUrl();
      this.tpX = String(Math.round(this.gameX));
      this.tpY = String(Math.round(this.gameY));
      this.tpZ = String(this.level);
      this.hoverX = Math.round(this.gameX);
      this.hoverY = Math.round(this.gameY);

      this.attachInteraction();
      this.scheduleRender();

      var self = this;
      window.addEventListener('resize', function () {
        self.scheduleRender();
      });

      // Layers whose content is only built inside render() (rather than always kept in sync and
      // merely hidden via `x-show`, like the pre-rendered area label/player pin layers) need an
      // explicit re-render when their toggle flips — otherwise switching one on before the next
      // pan/zoom would just reveal an empty layer. Region grid/labels also route through here even
      // though they default on, in case a future toggle-off-then-on leaves them stale.
      ['showRegionGrid', 'showRegionLabels', 'showAreaPolygons'].forEach(function (key) {
        self.$watch(key, function () {
          self.scheduleRender();
        });
      });
    },

    readStateFromUrl: function () {
      var params = new URLSearchParams(window.location.search);
      var x = parseInt(params.get('x'), 10);
      var y = parseInt(params.get('y'), 10);
      var z = parseInt(params.get('z'), 10);
      var zoom = parseFloat(params.get('zoom'));
      if (!isNaN(x)) {
        this.gameX = x;
      }
      if (!isNaN(y)) {
        this.gameY = y;
      }
      if (!isNaN(z) && z >= 0 && z <= 3) {
        this.level = z;
      }
      if (!isNaN(zoom)) {
        this.zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);
      }
    },

    writeStateToUrl: function () {
      var params = new URLSearchParams(window.location.search);
      params.set('x', Math.round(this.gameX));
      params.set('y', Math.round(this.gameY));
      params.set('z', this.level);
      params.set('zoom', this.zoom.toFixed(2));
      var url = window.location.pathname + '?' + params.toString();
      window.history.replaceState(null, '', url);
    },

    scheduleUrlWrite: function () {
      var self = this;
      clearTimeout(this._urlTimer);
      this._urlTimer = setTimeout(function () {
        self.writeStateToUrl();
      }, 350);
    },

    // Single-finger drag pans (pointer events unify mouse/touch/pen, and `.wm-viewport`'s
    // `touch-action:none` stops the browser's own scroll/zoom from competing with that drag).
    // Two fingers pinch-zoom instead: `pointers` tracks every currently-down touch by id, and the
    // moment a second one lands, panning is handed off to `updatePinch`, which re-derives zoom and
    // pan together from the two fingers' current midpoint/spread every move — see its comment.
    attachInteraction: function () {
      var self = this;
      var vp = this.viewport;
      var dragging = false;
      var lastX = 0;
      var lastY = 0;
      var pointers = {};
      var pinching = false;
      var pinchStartDist = 0;
      var pinchStartZoom = 0;
      var pinchAnchorGameX = 0;
      var pinchAnchorGameY = 0;

      function pointerCount() {
        return Object.keys(pointers).length;
      }

      function twoPointers() {
        var ids = Object.keys(pointers);
        return [pointers[ids[0]], pointers[ids[1]]];
      }

      function distance(a, b) {
        return Math.hypot(b.x - a.x, b.y - a.y);
      }

      // Re-anchors the pinch on whichever two fingers are down right now — called both when a
      // pinch starts and whenever the finger count changes but stays at 2 (e.g. a third finger
      // touches down and lifts again) — so a stale anchor from a different finger pair never bends
      // the map. `pinchAnchorGameX/Y` is the game coordinate under the fingers' midpoint *at this
      // instant*; `updatePinch` then keeps that same game point under the moving midpoint.
      function startPinch() {
        var pts = twoPointers();
        pinchStartDist = distance(pts[0], pts[1]);
        pinchStartZoom = self.zoom;
        var rect = vp.getBoundingClientRect();
        var scale = pxPerTile(self.zoom);
        var midX = (pts[0].x + pts[1].x) / 2;
        var midY = (pts[0].y + pts[1].y) / 2;
        var localX = midX - rect.left - rect.width / 2;
        var localY = midY - rect.top - rect.height / 2;
        pinchAnchorGameX = self.gameX + localX / scale;
        pinchAnchorGameY = self.gameY - localY / scale;
      }

      // Mirrors the wheel handler's "keep the point under the cursor stationary" trick, but with
      // the pinch midpoint standing in for the cursor and re-solved from the live finger positions
      // every move (rather than the wheel's per-event cursor position), so panning falls naturally
      // out of the two fingers translating together instead of needing separate drag handling.
      function updatePinch() {
        var pts = twoPointers();
        var dist = distance(pts[0], pts[1]);
        if (pinchStartDist < 1) {
          return;
        }
        var nextZoom = clamp(pinchStartZoom + Math.log2(dist / pinchStartDist), MIN_ZOOM, MAX_ZOOM);
        var rect = vp.getBoundingClientRect();
        var midX = (pts[0].x + pts[1].x) / 2;
        var midY = (pts[0].y + pts[1].y) / 2;
        var localX = midX - rect.left - rect.width / 2;
        var localY = midY - rect.top - rect.height / 2;
        self.zoom = nextZoom;
        var scaleAfter = pxPerTile(self.zoom);
        self.gameX = pinchAnchorGameX - localX / scaleAfter;
        self.gameY = pinchAnchorGameY + localY / scaleAfter;
        self.scheduleRender();
        self.scheduleUrlWrite();
      }

      vp.addEventListener('pointerdown', function (e) {
        if (e.button !== 0) {
          return;
        }
        pointers[e.pointerId] = { x: e.clientX, y: e.clientY };
        try {
          vp.setPointerCapture(e.pointerId);
        } catch (err) {
          // Ignore — dragging still works without capture, just less robust off-element.
        }

        if (pointerCount() >= 2) {
          dragging = false;
          vp.classList.remove('wm-dragging');
          pinching = true;
          startPinch();
          return;
        }
        dragging = true;
        lastX = e.clientX;
        lastY = e.clientY;
        vp.classList.add('wm-dragging');
      });

      vp.addEventListener('pointermove', function (e) {
        if (pointers[e.pointerId]) {
          pointers[e.pointerId].x = e.clientX;
          pointers[e.pointerId].y = e.clientY;
        }

        if (pinching) {
          if (pointerCount() >= 2) {
            updatePinch();
          }
          return;
        }

        var rect = vp.getBoundingClientRect();
        var scale = pxPerTile(self.zoom);
        var localX = e.clientX - rect.left - rect.width / 2;
        var localY = e.clientY - rect.top - rect.height / 2;
        self.hoverX = Math.round(self.gameX + localX / scale);
        self.hoverY = Math.round(self.gameY - localY / scale);
        self.updateHoverAreas();

        if (!dragging) {
          return;
        }
        var dx = e.clientX - lastX;
        var dy = e.clientY - lastY;
        lastX = e.clientX;
        lastY = e.clientY;
        self.gameX -= dx / scale;
        self.gameY += dy / scale;
        self.scheduleRender();
        self.scheduleUrlWrite();
      });

      function endDrag(e) {
        delete pointers[e.pointerId];
        try {
          vp.releasePointerCapture(e.pointerId);
        } catch (err) {
          // Already released (e.g. pointercancel) — nothing to do.
        }

        if (pinching) {
          if (pointerCount() >= 2) {
            // Still 2+ fingers down (one of 3+ lifted) — re-anchor on whichever pair remains
            // instead of ending the pinch, so the map doesn't jump.
            startPinch();
            return;
          }
          pinching = false;
          if (pointerCount() === 1) {
            // One finger remains — resume a plain drag from here rather than requiring a fresh
            // pointerdown, so lifting the second finger doesn't stop the pan dead.
            var ids = Object.keys(pointers);
            var p = pointers[ids[0]];
            dragging = true;
            lastX = p.x;
            lastY = p.y;
            vp.classList.add('wm-dragging');
          }
          return;
        }

        if (!dragging) {
          return;
        }
        dragging = false;
        vp.classList.remove('wm-dragging');
      }
      vp.addEventListener('pointerup', endDrag);
      vp.addEventListener('pointercancel', endDrag);
      vp.addEventListener('pointerleave', endDrag);

      vp.addEventListener(
        'wheel',
        function (e) {
          e.preventDefault();
          var nextZoom = clamp(self.zoom + (e.deltaY > 0 ? -ZOOM_STEP : ZOOM_STEP), MIN_ZOOM, MAX_ZOOM);
          if (Math.abs(nextZoom - self.zoom) < 1e-9) {
            return;
          }
          var rect = vp.getBoundingClientRect();
          var scaleBefore = pxPerTile(self.zoom);
          var localX = e.clientX - rect.left - rect.width / 2;
          var localY = e.clientY - rect.top - rect.height / 2;
          var gxUnderCursor = self.gameX + localX / scaleBefore;
          var gyUnderCursor = self.gameY - localY / scaleBefore;

          self.zoom = nextZoom;
          var scaleAfter = pxPerTile(self.zoom);
          self.gameX = gxUnderCursor - localX / scaleAfter;
          self.gameY = gyUnderCursor + localY / scaleAfter;
          self.scheduleRender();
          self.scheduleUrlWrite();
        },
        { passive: false },
      );
    },

    setLevel: function (level) {
      if (level < 0 || level > 3 || level === this.level) {
        return;
      }
      this.level = level;
      this.tpZ = String(level);
      this.scheduleRender();
      this.scheduleUrlWrite();
    },

    teleportTo: function () {
      var x = parseInt(this.tpX, 10);
      var y = parseInt(this.tpY, 10);
      var z = parseInt(this.tpZ, 10);
      if (!isNaN(x)) {
        this.gameX = x;
      }
      if (!isNaN(y)) {
        this.gameY = y;
      }
      if (!isNaN(z) && z >= 0 && z <= 3) {
        this.level = z;
      }
      this.scheduleRender();
      this.scheduleUrlWrite();
    },

    // "Move here" from a [WorldMap.playerPin]'s right-click menu — jumps the map view to that
    // player's position, same as typing their coordinates into the teleport console.
    moveToPlayer: function (x, y, level) {
      this.tpX = String(x);
      this.tpY = String(y);
      this.tpZ = String(level);
      this.teleportTo();
    },

    // "Kick" from a [WorldMap.playerPin]'s right-click menu — placeholder until the server bridge
    // is wired up, same as the Players/Search console tabs.
    kickPlayer: function (name) {
      console.log('Kick requested for ' + name + ' (server bridge not wired up yet)');
    },

    scheduleRender: function () {
      var self = this;
      if (this._raf) {
        return;
      }
      this._raf = requestAnimationFrame(function () {
        self._raf = null;
        self.render();
      });
    },

    render: function () {
      var vp = this.viewport;
      var rect = vp.getBoundingClientRect();
      if (rect.width === 0 || rect.height === 0) {
        return;
      }
      var scale = pxPerTile(this.zoom);
      var worldCenterX = this.gameX * scale;
      var worldCenterY = -this.gameY * scale;
      var offsetX = rect.width / 2 - worldCenterX;
      var offsetY = rect.height / 2 - worldCenterY;
      this._offsetX = offsetX;
      this._offsetY = offsetY;
      this._scale = scale;

      this.tileLayer.style.transform = 'translate(' + offsetX.toFixed(1) + 'px,' + offsetY.toFixed(1) + 'px)';

      // Tiles are always fetched at the nearest native pyramid level and scaled by `zoomRatio` to
      // match the continuous `scale` — see the file header. Tile-index math below runs in native
      // worldPx (dividing continuous worldPx by `zoomRatio` first); gameYAtTop/Bottom don't need
      // that conversion since they solve for an actual game coordinate, which is native-agnostic.
      var nativeZoom = nativeZoomFor(this.zoom);
      var nativeScale = pxPerTile(nativeZoom);
      var zoomRatio = scale / nativeScale;

      var pad = TILE_SIZE;
      var minTileX = Math.floor((-offsetX - pad) / zoomRatio / TILE_SIZE);
      var maxTileX = Math.floor((-offsetX + rect.width + pad) / zoomRatio / TILE_SIZE);
      // Y is flipped (see the file header): the row visible at the TOP of the viewport (screen
      // y = -pad) is the highest tile index, and the row at the BOTTOM is the lowest. Converting
      // through gameY (rather than reusing the X-axis's plain pixel-division formula) is what
      // picks up the row-index correction from `tileRowIndex`.
      var gameYAtTop = (offsetY + pad) / scale;
      var gameYAtBottom = (offsetY - rect.height - pad) / scale;
      var maxTileY = tileRowIndex(gameYAtTop, nativeScale);
      var minTileY = tileRowIndex(gameYAtBottom, nativeScale);
      this.renderTiles(minTileX, maxTileX, minTileY, maxTileY, nativeZoom, nativeScale, zoomRatio);

      if (this.showRegionGrid) {
        this.renderGrid(offsetX, offsetY, scale, rect);
      } else if (this.gridLayer) {
        this.gridLayer.innerHTML = '';
      }
      if (this.showRegionLabels) {
        this.renderRegionLabels(offsetX, offsetY, scale, rect);
      } else if (this.regionLabelLayer) {
        this.regionLabelLayer.innerHTML = '';
      }
      if (this.showAreaPolygons) {
        this.renderAreaPolygons(offsetX, offsetY, scale);
      } else if (this.areaPolygonLayer) {
        this.areaPolygonLayer.innerHTML = '';
      }
      this.renderAreaLabels();
      this.renderPlayers();
      // Re-checked on every render (not just pointermove) so panning/zooming/changing level under
      // a stationary cursor, or toggling the polygon layer on, keeps the hover state honest.
      this.updateHoverAreas();
    },

    // Tiles are keyed by `level/zoom/x/y` so switching either immediately drops every tile that
    // no longer matches instead of trying to cross-fade pyramids. Requesting them ordered by
    // distance from the viewport centre (rather than raster/row order) with a small staggered
    // fade-in delay makes the reveal expand outward from the middle of the screen instead of
    // popping in row by row.
    renderTiles: function (minTileX, maxTileX, minTileY, maxTileY, zoom, nativeScale, zoomRatio) {
      var layer = this.tileLayer;
      var level = this.level;
      var rowPx = 64 * nativeScale;
      var centerX = (minTileX + maxTileX) / 2;
      var centerY = (minTileY + maxTileY) / 2;
      var wanted = {};
      var pending = [];

      for (var tx = minTileX; tx <= maxTileX; tx++) {
        for (var ty = minTileY; ty <= maxTileY; ty++) {
          var key = level + '/' + zoom + '/' + tx + '/' + ty;
          wanted[key] = true;
          var existing = this.tileEls[key];
          if (existing) {
            this.positionTile(existing, tx, ty, rowPx, zoomRatio);
            continue;
          }
          // A tile that was wanted a render or two ago (e.g. hovering right on a .5 zoom boundary
          // flips `nativeZoom` back and forth) may still be sitting in `_staleTileEls`, already
          // loaded and fully opaque, waiting on `flushStaleTiles` to confirm nothing needs it
          // anymore. Reclaiming it here — instead of falling through to `pending` and creating a
          // brand-new `<img>` for the same URL — keeps it at opacity 1 the whole time, rather than
          // restarting it from opacity 0 and having it fade in on top of itself (or, worse, having
          // `flushStaleTiles` remove the old one before the new duplicate finishes loading, which
          // is what caused the flash).
          var revived = this.reviveStaleTile(key);
          if (revived) {
            this.positionTile(revived, tx, ty, rowPx, zoomRatio);
            this.tileEls[key] = revived;
            continue;
          }
          pending.push({ key: key, x: tx, y: ty, dist: (tx - centerX) * (tx - centerX) + (ty - centerY) * (ty - centerY) });
        }
      }
      pending.sort(function (a, b) {
        return a.dist - b.dist;
      });

      var self = this;
      for (var i = 0; i < pending.length; i++) {
        var tile = pending[i];
        var img = document.createElement('img');
        img.className = 'wm-tile';
        img.alt = '';
        img.draggable = false;
        img.style.position = 'absolute';
        this.positionTile(img, tile.x, tile.y, rowPx, zoomRatio);
        img.style.opacity = '0';
        var delayMs = Math.min(i, LOAD_STAGGER_MAX) * LOAD_STAGGER_MS;
        img.style.transitionDelay = delayMs + 'ms';
        this._pendingTileLoads++;
        img.addEventListener('load', function () {
          onTileLoad.call(this);
          self._tileSuccessCount++;
          self.tilesMissing = false;
          // Bytes decoded doesn't mean the fade has visually finished — see `TILE_FADE_MS`. Only
          // once this timer's up is it actually safe to let `flushStaleTiles` drop whatever this
          // tile is covering, so the old tile stays put for the whole crossfade instead of
          // vanishing out from under a still-transparent replacement.
          setTimeout(function () {
            self._pendingTileLoads--;
            self.flushStaleTiles();
          }, delayMs + TILE_FADE_MS);
        });
        img.addEventListener('error', function () {
          onTileError.call(this);
          self._pendingTileLoads--;
          self.checkTilesMissing();
          self.flushStaleTiles();
        });
        img.src = TILE_BASE + '/' + tile.key + '.png';
        layer.appendChild(img);
        this.tileEls[tile.key] = img;
      }

      // No-longer-wanted tiles (previous zoom/level's) are handed to `_staleTileEls` instead of
      // being removed here — see the field comment. They stay visible, underneath whatever just
      // got appended above, until `flushStaleTiles` decides it's safe to drop them, or
      // `reviveStaleTile` reclaims one that turns out to be wanted again after all. `nativeZoom`
      // is parsed back out of the key (its own pyramid level, not necessarily this render's) so
      // `repositionStaleTiles` can keep scaling it correctly for as long as it lingers.
      for (var key in this.tileEls) {
        if (!wanted[key]) {
          var parts = key.split('/');
          this._staleTileEls.push({
            key: key,
            el: this.tileEls[key],
            nativeZoom: parseInt(parts[1], 10),
            tx: parseInt(parts[2], 10),
            ty: parseInt(parts[3], 10),
          });
          delete this.tileEls[key];
        }
      }
      // A tile doesn't stop needing repositioning just because it's stale — see
      // `repositionStaleTiles` for why leaving it at its last on-screen size/position is itself
      // the flash the .5-zoom bug reports were describing.
      this.repositionStaleTiles();
      this.flushStaleTiles();
    },

    // The tile layer's own `translate(offsetX, offsetY)` is recomputed every render from the live
    // continuous `scale` (see `render`), but a stale tile's `left`/`top`/`width`/`height` were only
    // ever set once, back when it was still wanted, using whatever `scale` was current *then*.
    // Continuous zoom keeps moving for as long as the gesture continues, so by the very next frame
    // that frozen size/position no longer matches the live translate it's sitting inside — a real,
    // immediate geometric misalignment, not a loading race. That mismatch *is* the flash: it fires
    // on every crossing of a `.5` zoom boundary, at any zoom speed, because nativeZoom flipping is
    // what pushes a tile into `_staleTileEls` in the first place. Recomputing every stale tile's
    // position each render — same formula as a live tile, just using its own remembered native
    // zoom instead of this render's — keeps it correctly aligned for however long it lingers.
    repositionStaleTiles: function () {
      var scale = this._scale;
      for (var i = 0; i < this._staleTileEls.length; i++) {
        var entry = this._staleTileEls[i];
        var nativeScale = pxPerTile(entry.nativeZoom);
        var rowPx = 64 * nativeScale;
        this.positionTile(entry.el, entry.tx, entry.ty, rowPx, scale / nativeScale);
      }
    },

    // Pulls a still-loaded tile back out of `_staleTileEls` for `key`, if one's there — see the
    // call site in renderTiles. Returns null when there isn't one, so the caller falls back to
    // requesting it fresh.
    reviveStaleTile: function (key) {
      for (var i = 0; i < this._staleTileEls.length; i++) {
        if (this._staleTileEls[i].key === key) {
          var el = this._staleTileEls[i].el;
          this._staleTileEls.splice(i, 1);
          return el;
        }
      }
      return null;
    },

    // Only removes stale tiles once nothing is still loading — if new tiles are still fading in,
    // dropping the old ones now would flash empty background over content that was already there.
    flushStaleTiles: function () {
      if (this._pendingTileLoads > 0 || this._staleTileEls.length === 0) {
        return;
      }
      for (var i = 0; i < this._staleTileEls.length; i++) {
        this._staleTileEls[i].el.remove();
      }
      this._staleTileEls = [];
    },

    // Shows the "run MapZoomImageGenerator" empty state once every tile requested so far has come
    // back a 404 and none are still in flight — i.e. `map-tiles/` looks entirely ungenerated rather
    // than just missing the one tile under an ocean. Only ever flips `tilesMissing` on; a load
    // flips it back off directly (see the `load` listener above) the moment any tile succeeds.
    checkTilesMissing: function () {
      if (this._tileSuccessCount > 0 || this._pendingTileLoads > 0) {
        return;
      }
      this.tilesMissing = true;
    },

    // Tiles are always fetched at native resolution and re-scaled by `zoomRatio` (current
    // continuous scale / native tile scale) to match the continuous zoom in between native levels
    // — see the file header.
    positionTile: function (img, tx, ty, rowPx, zoomRatio) {
      var renderSize = TILE_SIZE * zoomRatio;
      img.style.left = tx * renderSize + 'px';
      img.style.top = -(ty * renderSize + rowPx * zoomRatio) + 'px';
      img.style.width = renderSize + 'px';
      img.style.height = renderSize + 'px';
    },

    // Drawn as plain positioned lines (using the exact same offsetX/offsetY/regionPx formula as
    // renderTiles/renderRegionLabels) rather than an animated CSS `background-position` pattern.
    // The tile layer pans via a GPU-composited `transform`, which never drops a frame; a
    // repeating-gradient's `background-position` is paint-bound instead, so during a fast drag it
    // can lag a frame or more behind the transform-driven tiles and visibly "shift" relative to
    // them. Explicit line elements recomputed every render() stay perfectly in sync since they
    // share the same numbers the tiles and labels use.
    renderGrid: function (offsetX, offsetY, scale, rect) {
      var regionPx = 64 * scale;
      var minCol = Math.floor(-offsetX / regionPx);
      var maxCol = Math.floor((-offsetX + rect.width) / regionPx);
      var minRow = Math.floor((offsetY - rect.height) / regionPx);
      var maxRow = Math.floor(offsetY / regionPx);

      var html = '';
      for (var col = minCol; col <= maxCol + 1; col++) {
        var x = offsetX + col * regionPx;
        html += '<div class="wm-grid-line wm-grid-line-v" style="left:' + x + 'px"></div>';
      }
      // Y flipped, same reasoning as renderTiles/renderRegionLabels: row boundaries move up
      // (smaller screen y) as the row number increases.
      for (var row = minRow; row <= maxRow + 1; row++) {
        var y = offsetY - row * regionPx;
        html += '<div class="wm-grid-line wm-grid-line-h" style="top:' + y + 'px"></div>';
      }
      this.gridLayer.innerHTML = html;
    },

    // Region ids follow Void's own region numbering (see the game engine's Region class):
    // `(regionY & 0xff) + ((regionX & 0xff) << 8)`.
    renderRegionLabels: function (offsetX, offsetY, scale, rect) {
      var regionPx = 64 * scale;
      if (regionPx < 28) {
        this.regionLabelLayer.innerHTML = '';
        return;
      }
      var targetWidth = this.labelTargetWidth(regionPx);
      var minCol = Math.floor(-offsetX / regionPx);
      var maxCol = Math.floor((-offsetX + rect.width) / regionPx);
      // Y flipped, same reasoning as renderTiles: the row at screen y = 0 is the highest index.
      var minRow = Math.floor((offsetY - rect.height) / regionPx);
      var maxRow = Math.floor(offsetY / regionPx);

      var html = '';
      for (var col = minCol; col <= maxCol; col++) {
        if (col < 0 || col > 255) {
          continue;
        }
        var left = offsetX + col * regionPx;
        for (var row = minRow; row <= maxRow; row++) {
          if (row < 0 || row > 255) {
            continue;
          }
          var id = (row & 0xff) + ((col & 0xff) << 8);
          var text = String(id);
          var top = offsetY - (row + 1) * regionPx;
          var x = left + regionPx / 2;
          var y = top + regionPx / 2;
          var fontPx = this.labelFontSize(text, targetWidth, 700);
          html += '<span class="wm-region-label" style="left:' + x + 'px;top:' + y + 'px;font-size:' + fontPx + 'px">' + text + '</span>';
        }
      }
      this.regionLabelLayer.innerHTML = html;
    },

    // Half the region's on-screen width, capped at its size at LABEL_CAP_ZOOM so labels stop
    // growing past that zoom instead of ballooning further at the max zoom. Shared by region and
    // area labels so both scale with zoom the same way.
    labelTargetWidth: function (regionPx) {
      var cappedRegionPx = Math.min(regionPx, 64 * pxPerTile(LABEL_CAP_ZOOM));
      return cappedRegionPx / 2;
    },

    // Solves for the font-size that renders `text` at `targetWidth`, measuring via a scratch
    // canvas rather than a fixed px-per-character guess, since text length varies (region ids run
    // 0-65535, area names are arbitrary) and glyph width isn't a fixed fraction of font-size
    // across fonts/weights. `weight` must match the caller's own CSS (700 for `.wm-region-label`,
    // 600 for `.wm-area-label`'s `--weight-semibold`) or the solved size will be off.
    labelFontSize: function (text, targetWidth, weight) {
      if (!this._measureCtx) {
        this._measureCtx = document.createElement('canvas').getContext('2d');
      }
      var REF_SIZE = 100;
      this._measureCtx.font = weight + ' ' + REF_SIZE + 'px "Cinzel",Georgia,"Times New Roman",serif';
      var refWidth = this._measureCtx.measureText(text).width;
      if (refWidth <= 0) {
        return REF_SIZE;
      }
      return (targetWidth / refWidth) * REF_SIZE;
    },

    // `window.VOID_AREAS` is injected server-side by [WorldMap.areasScript] — each entry's `x`/`y`
    // are game-space polygon vertices (box areas pre-expanded to four corners), drawn here as one
    // SVG <polygon> apiece rather than DOM divs since a polygon isn't expressible as a CSS box.
    renderAreaPolygons: function (offsetX, offsetY, scale) {
      var areas = window.VOID_AREAS || [];
      var level = this.level;
      var html = '';
      for (var i = 0; i < areas.length; i++) {
        var area = areas[i];
        if (level < area.minLevel || level > area.maxLevel) {
          continue;
        }
        var points = '';
        for (var p = 0; p < area.x.length; p++) {
          var x = offsetX + area.x[p] * scale;
          var y = offsetY - area.y[p] * scale;
          points += x.toFixed(1) + ',' + y.toFixed(1) + ' ';
        }
        html += '<polygon class="wm-area-polygon" data-i="' + i + '" points="' + points.trim() + '"><title>' + escapeHtml(area.name) + '</title></polygon>';
      }
      this.areaPolygonLayer.innerHTML = html ? '<svg style="position:absolute;overflow:visible">' + html + '</svg>' : '';
    },

    // Which `window.VOID_AREAS` entries (see [renderAreaPolygons]) the cursor's current game tile
    // (`hoverX`/`hoverY`) falls inside, on the current level. Drives both `hoverAreaNames` (read by
    // the bottom-left panel via Alpine reactivity) and the `wm-area-polygon-hover` highlight class —
    // the latter toggled directly on the existing `<polygon>` elements rather than going through a
    // full `renderAreaPolygons` rebuild, since this runs on every `pointermove` and a full innerHTML
    // rebuild per mouse pixel would be wasteful (and would restart the fill/stroke transition).
    updateHoverAreas: function () {
      var areas = window.VOID_AREAS || [];
      var level = this.level;
      var x = this.hoverX;
      var y = this.hoverY;
      var names = [];
      var hovered = {};
      for (var i = 0; i < areas.length; i++) {
        var area = areas[i];
        if (level < area.minLevel || level > area.maxLevel) {
          continue;
        }
        if (pointInPolygon(x, y, area.x, area.y)) {
          names.push(area.name);
          hovered[i] = true;
        }
      }
      this.hoverAreaNames = names;
      if (this.areaPolygonLayer) {
        var nodes = this.areaPolygonLayer.querySelectorAll('.wm-area-polygon');
        for (var n = 0; n < nodes.length; n++) {
          var node = nodes[n];
          node.classList.toggle('wm-area-polygon-hover', !!hovered[node.getAttribute('data-i')]);
        }
      }
    },

    renderAreaLabels: function () {
      var targetWidth = this.labelTargetWidth(64 * this._scale);
      var nodes = this.areaLabelLayer.children;
      for (var i = 0; i < nodes.length; i++) {
        var el = nodes[i];
        el.style.fontSize = this.labelFontSize(el.textContent, targetWidth, 600) + 'px';
      }
      this.positionLayer(this.areaLabelLayer);
    },

    renderPlayers: function () {
      var nodes = this.playerLayer.children;
      for (var i = 0; i < nodes.length; i++) {
        var el = nodes[i];
        var onLevel = parseInt(el.getAttribute('data-level'), 10) === this.level;
        el.style.display = onLevel ? '' : 'none';
      }
      this.positionLayer(this.playerLayer);
    },

    positionLayer: function (layer) {
      var offsetX = this._offsetX;
      var offsetY = this._offsetY;
      var scale = this._scale;
      var nodes = layer.children;
      for (var i = 0; i < nodes.length; i++) {
        var el = nodes[i];
        var gx = parseFloat(el.getAttribute('data-gx'));
        var gy = parseFloat(el.getAttribute('data-gy'));
        el.style.left = offsetX + gx * scale + 'px';
        el.style.top = offsetY - gy * scale + 'px';
      }
    },
  };
};

function onTileLoad() {
  this.style.opacity = '1';
}

function onTileError() {
  // Missing tile (ocean, ungenerated zoom, or a level with nothing on it) — leave transparent
  // rather than showing a broken-image icon.
  this.style.display = 'none';
}

// Standard even-odd ray-casting point-in-polygon test (ties broken however they fall on an edge —
// unlike the server's [world.gregs.voidps.type.area.Polygon.pointInPolygon], exact edge inclusion
// doesn't need to match pixel-for-pixel here, this is just for the hover highlight).
function pointInPolygon(x, y, xs, ys) {
  var inside = false;
  for (var i = 0, j = xs.length - 1; i < xs.length; j = i++) {
    var xi = xs[i];
    var yi = ys[i];
    var xj = xs[j];
    var yj = ys[j];
    if (yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi) {
      inside = !inside;
    }
  }
  return inside;
}

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}
