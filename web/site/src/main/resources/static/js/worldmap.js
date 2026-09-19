// World map viewer: a slippy-map-style tile pyramid over `map-tiles/{level}/{zoom}/{x}/{y}.png`
// (see void-map-tiles), panned/zoomed with plain DOM <img> tiles rather than <canvas> so each
// tile fades in independently on load instead of the whole frame popping in at once.
//
// Tile math: at ZOOM 8 one 256px tile image is exactly one 64x64-game-tile region (4px/game-tile
// — see the `pxPerTile` comment). Zoom halves/doubles that scale per level either side, same as a
// standard XYZ tile pyramid, so tile index = floor(worldPx / 256) at every zoom.
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
  var TILE_BASE = 'map-tiles';
  var LOAD_STAGGER_MS = 6;
  var LOAD_STAGGER_MAX = 10;
  var LABEL_CAP_ZOOM = 10;

  function pxPerTile(zoom) {
    return BASE_PX_PER_TILE * Math.pow(2, zoom - BASE_ZOOM);
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
    ptab: 'teleport',
    tpX: '3200',
    tpY: '3200',
    tpZ: '0',
    hoverX: 3200,
    hoverY: 3200,
    levelLabels: ['SURFACE', 'FLOOR 1', 'FLOOR 2', 'FLOOR 3'],

    tileEls: {},
    _raf: null,
    _urlTimer: null,
    _offsetX: 0,
    _offsetY: 0,
    _scale: BASE_PX_PER_TILE,

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
      var zoom = parseInt(params.get('zoom'), 10);
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
      params.set('zoom', this.zoom);
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

    attachInteraction: function () {
      var self = this;
      var vp = this.viewport;
      var dragging = false;
      var lastX = 0;
      var lastY = 0;

      vp.addEventListener('pointerdown', function (e) {
        if (e.button !== 0) {
          return;
        }
        dragging = true;
        lastX = e.clientX;
        lastY = e.clientY;
        try {
          vp.setPointerCapture(e.pointerId);
        } catch (err) {
          // Ignore — dragging still works without capture, just less robust off-element.
        }
        vp.classList.add('wm-dragging');
      });

      vp.addEventListener('pointermove', function (e) {
        var rect = vp.getBoundingClientRect();
        var scale = pxPerTile(self.zoom);
        var localX = e.clientX - rect.left - rect.width / 2;
        var localY = e.clientY - rect.top - rect.height / 2;
        self.hoverX = Math.round(self.gameX + localX / scale);
        self.hoverY = Math.round(self.gameY - localY / scale);

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
        if (!dragging) {
          return;
        }
        dragging = false;
        vp.classList.remove('wm-dragging');
        try {
          vp.releasePointerCapture(e.pointerId);
        } catch (err) {
          // Already released (e.g. pointercancel) — nothing to do.
        }
      }
      vp.addEventListener('pointerup', endDrag);
      vp.addEventListener('pointercancel', endDrag);
      vp.addEventListener('pointerleave', endDrag);

      vp.addEventListener(
        'wheel',
        function (e) {
          e.preventDefault();
          var nextZoom = clamp(self.zoom + (e.deltaY > 0 ? -1 : 1), MIN_ZOOM, MAX_ZOOM);
          if (nextZoom === self.zoom) {
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

      var pad = TILE_SIZE;
      var minTileX = Math.floor((-offsetX - pad) / TILE_SIZE);
      var maxTileX = Math.floor((-offsetX + rect.width + pad) / TILE_SIZE);
      // Y is flipped (see the file header): the row visible at the TOP of the viewport (screen
      // y = -pad) is the highest tile index, and the row at the BOTTOM is the lowest. Converting
      // through gameY (rather than reusing the X-axis's plain pixel-division formula) is what
      // picks up the row-index correction from `tileRowIndex`.
      var gameYAtTop = (offsetY + pad) / scale;
      var gameYAtBottom = (offsetY - rect.height - pad) / scale;
      var maxTileY = tileRowIndex(gameYAtTop, scale);
      var minTileY = tileRowIndex(gameYAtBottom, scale);
      this.renderTiles(minTileX, maxTileX, minTileY, maxTileY, scale);

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
      this.renderAreaLabels();
      this.renderPlayers();
    },

    // Tiles are keyed by `level/zoom/x/y` so switching either immediately drops every tile that
    // no longer matches instead of trying to cross-fade pyramids. Requesting them ordered by
    // distance from the viewport centre (rather than raster/row order) with a small staggered
    // fade-in delay makes the reveal expand outward from the middle of the screen instead of
    // popping in row by row.
    renderTiles: function (minTileX, maxTileX, minTileY, maxTileY, scale) {
      var layer = this.tileLayer;
      var zoom = this.zoom;
      var level = this.level;
      var rowPx = 64 * scale;
      var centerX = (minTileX + maxTileX) / 2;
      var centerY = (minTileY + maxTileY) / 2;
      var wanted = {};
      var pending = [];

      for (var tx = minTileX; tx <= maxTileX; tx++) {
        for (var ty = minTileY; ty <= maxTileY; ty++) {
          var key = level + '/' + zoom + '/' + tx + '/' + ty;
          wanted[key] = true;
          if (this.tileEls[key]) {
            continue;
          }
          pending.push({ key: key, x: tx, y: ty, dist: (tx - centerX) * (tx - centerX) + (ty - centerY) * (ty - centerY) });
        }
      }
      pending.sort(function (a, b) {
        return a.dist - b.dist;
      });

      for (var i = 0; i < pending.length; i++) {
        var tile = pending[i];
        var img = document.createElement('img');
        img.className = 'wm-tile';
        img.alt = '';
        img.draggable = false;
        img.style.position = 'absolute';
        img.style.left = tile.x * TILE_SIZE + 'px';
        img.style.top = -(tile.y * TILE_SIZE + rowPx) + 'px';
        img.style.width = TILE_SIZE + 'px';
        img.style.height = TILE_SIZE + 'px';
        img.style.opacity = '0';
        img.style.transitionDelay = Math.min(i, LOAD_STAGGER_MAX) * LOAD_STAGGER_MS + 'ms';
        img.addEventListener('load', onTileLoad);
        img.addEventListener('error', onTileError);
        img.src = TILE_BASE + '/' + tile.key + '.png';
        layer.appendChild(img);
        this.tileEls[tile.key] = img;
      }

      for (var key in this.tileEls) {
        if (!wanted[key]) {
          this.tileEls[key].remove();
          delete this.tileEls[key];
        }
      }
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
        html += '<polygon class="wm-area-polygon" points="' + points.trim() + '"><title>' + escapeHtml(area.name) + '</title></polygon>';
      }
      this.areaPolygonLayer.innerHTML = html ? '<svg style="position:absolute;overflow:visible">' + html + '</svg>' : '';
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

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, function (c) {
    return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
  });
}
