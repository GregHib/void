package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.web.site.components.*

/**
 * A near-infinite, lazily-tiled world map: pan by drag, zoom by wheel, and step between the four
 * height levels. Tiles are plain `{level}/{zoom}/{x}/{y}.png` images (256px, see
 * `void-map-tiles`), fetched from whichever base [Site.tileBase] resolves to — the built site's
 * own `map-tiles/`, which [world.gregs.voidps.web.site.Site] copies in alongside the rest of the
 * static assets, or the remote tile repository. All of the actual tile math, panning/zoom interaction and URL persistence
 * lives in `js/worldmap.js`'s `worldMapApp()`; this file only renders the static chrome (panels,
 * console, the player pin template) that Alpine then positions/reacts to, plus the JSON every
 * layer and the search index are built from — [areasScript]'s area polygons and [MapLabels]' cache
 * place names. Online players aren't baked in: `worldmap.js` polls the connected world's
 * `/api/v1/players` (see WorldsRoutes.kt) while a world is connected in the navbar's world
 * switcher, and Alpine stamps out a [playerPin] and a console row per entry.
 *
 * Chrome layout: the map display toggles top-left, the elevation stepper and teleport panel
 * stacked top-right, the hovered-tile readout bottom-left and the console (players, search)
 * bottom-centre. Only "Kick" is left behind [Site.FULL] — it's staff-only and not wired up yet.
 *
 * The bots' navigation graph ([navGraphPanel]) isn't baked in either: it's read from the viewer's
 * own `*.nav-edges.toml` files, and can be edited and saved back to them — see `js/navgraph.js`.
 * The area polygons are baked in for viewing, but can likewise be loaded from `*.areas.toml` files
 * to edit ([areaPanel], [areaEditPanel], `js/areas.js`); both load through [mapFileLoader], and
 * `js/mapedit.js` holds what the two editors share.
 */
object WorldMap {

    /** Drops the raw [html] straight into the page — Alpine `<template>` loops kotlinx.html can't express. */
    private fun HTMLTag.rawHtml(html: String) {
        unsafe { raw(html.trimIndent()) }
    }

    /** Shared style for a row in [playerPin]'s right-click menu — see its [danger] variant for "Kick". */
    private fun BUTTON.menuItemStyle(danger: Boolean, first: Boolean) {
        attributes["type"] = "button"
        style = "width:100%;box-sizing:border-box;text-align:left;display:flex;align-items:center;gap:8px;" +
            "padding:9px 12px;background:transparent;border:none;font:var(--type-body-sm);cursor:pointer;" +
            "color:" + (if (danger) "var(--feedback-danger)" else "var(--text-body)") +
            (if (first) "" else ";border-top:1px solid var(--border-subtle)")
    }

    /**
     * The map pin for `p`, one entry of `worldmap.js`'s `players` — this is the body of an Alpine
     * `x-for` template, so every value is bound rather than written. `data-level` hides it when the
     * viewer isn't on that height level, and `data-name` is how `renderPlayers` finds the pin
     * belonging to the console's selected row — see `.wm-pin-selected` in world-map.css.
     *
     * The outer box is a fixed [pinWidth]x[pinHeight], not sized from its content: every child is
     * positioned by a pixel offset from *its* edges rather than left to stack in flow, so
     * `translate(-50%,-100%)` below anchors a known point — the box's bottom-centre — exactly on the
     * game tile, instead of wherever the content happened to reach. Off that one anchor: the dot
     * (one game tile's on-screen footprint at the base zoom — see `BASE_PX_PER_TILE` in worldmap.js)
     * is centred exactly on it, and the map-pin icon sits directly above with its tip touching the
     * dot's top edge, so the icon visibly points down at the dot rather than swallowing it.
     */
    private fun FlowContent.playerPin() {
        val pinWidth = 20
        val pinHeight = 26
        val dotSize = 4
        div {
            attributes["class"] = "wm-pin"
            attributes[":data-gx"] = "p.x"
            attributes[":data-gy"] = "p.y"
            attributes[":data-level"] = "p.level"
            attributes[":data-name"] = "p.name"
            style = "position:absolute;width:${pinWidth}px;height:${pinHeight}px;" +
                "transform:translate(-50%,-100%);pointer-events:auto;cursor:default"
            xData("{ menuOpen: false }")
            onContextMenu("menuOpen = true")
            onClickOutside("menuOpen = false")
            div {
                attributes["class"] = "wm-pin-dot"
                // Centred on the box's bottom edge (the anchor): half of `dotSize` hangs past it.
                style = "position:absolute;left:50%;bottom:-${dotSize / 2}px;transform:translateX(-50%);z-index:1;" +
                    "width:${dotSize}px;height:${dotSize}px;border-radius:999px;background:var(--gold-400)"
            }
            unsafe {
                raw(
                    // `bottom` matches the dot's own top edge (`dotSize` above the anchor) so the
                    // teardrop's point sits flush against the dot instead of overlapping it.
                    """<svg width="16" height="20" viewBox="0 0 24 28" style="position:absolute;left:50%;""" +
                        """bottom:${dotSize}px;transform:translateX(-50%);z-index:2">""" +
                        """<path d="M12 1C6.48 1 2 5.48 2 11c0 7 10 16 10 16s10-9 10-16c0-5.52-4.48-10-10-10z" """ +
                        """fill="var(--gold-400)" stroke="var(--umber-950)" stroke-width="1.5"></path>""" +
                        """<circle cx="12" cy="11" r="3.2" fill="var(--umber-950)"></circle></svg>""",
                )
            }
            div {
                attributes["class"] = "wm-pin-tooltip"
                style = "position:absolute;bottom:100%;left:50%;transform:translateX(-50%);white-space:nowrap;" +
                    "background:var(--surface-panel);border:1px solid var(--border-gold);border-radius:var(--radius-xs);" +
                    "padding:6px 10px;font:var(--type-body-sm);color:var(--text-strong);" +
                    "box-shadow:var(--bevel-up),var(--shadow-md);margin-bottom:6px"
                div {
                    style = "font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui)"
                    xText("p.name")
                }
                div {
                    attributes["class"] = "wm-pin-coords"
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);margin-top:2px"
                    xText("p.x + ', ' + p.y + ', ' + p.level")
                }
            }
            div {
                attributes["class"] = "wm-pin-menu"
                xShow("menuOpen")
                transition()
                style = "position:absolute;top:-6px;left:24px;width:172px;z-index:10;" +
                    "background:var(--surface-panel);border:1px solid var(--border-panel);" +
                    "border-radius:var(--radius-md);box-shadow:var(--shadow-md);overflow:hidden"
                div {
                    style = "padding:9px 12px;font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);" +
                        "color:var(--text-strong);background:var(--surface-header);border-bottom:1px solid var(--border-subtle)"
                    xText("p.name")
                }
                if (Site.FULL) {
                    button {
                        menuItemStyle(danger = false, first = true)
                        // Routed through `selectPlayer` rather than a bare coordinate jump so the
                        // console's players list highlights whoever the map just moved to.
                        onClick("selectPlayer(p.name); menuOpen = false")
                        icon(Icons.CROSSHAIR, size = 13)
                        +" Move here"
                    }
                    button {
                        menuItemStyle(danger = true, first = false)
                        onClick("kickPlayer(p.name); menuOpen = false")
                        icon(Icons.CIRCLE_X, size = 13)
                        +" Kick"
                    }
                }
            }
        }
    }

    /**
     * Corner/vertex points of [area] in game coordinates, or `null` for an [Area] shape not drawable
     * as a polygon. [Rectangle]/[Cuboid] are box-shaped so their four corners are used directly.
     */
    private fun points(area: Area): Pair<IntArray, IntArray>? = when (area) {
        is Polygon -> area.xPoints to area.yPoints
        is Rectangle -> intArrayOf(area.minX, area.maxX, area.maxX, area.minX) to intArrayOf(area.minY, area.minY, area.maxY, area.maxY)
        is Cuboid -> intArrayOf(area.minX, area.maxX, area.maxX, area.minX) to intArrayOf(area.minY, area.minY, area.maxY, area.maxY)
        else -> null
    }

    /** The height levels [area] should be drawn on, matching [Areas.load]'s "no level means every level" rule. */
    private fun levels(area: Area): IntRange = when (area) {
        is Polygon -> area.bounds.minLevel..area.bounds.maxLevel
        is Cuboid -> area.minLevel..area.maxLevel
        else -> 0..3
    }

    /** Inline `<script>` body defining `window.VOID_TILE_BASE` — see [Site.tileBase] for the two it picks between. */
    private fun tileBaseScript(): String = "window.VOID_TILE_BASE=\"${jsonString(Site.tileBase())}\";"

    /** Inline `<script>` body defining `window.VOID_AREAS` as JSON for `worldmap.js` to read (see [WorldMap.page]). */
    private fun areasScript(): String = buildString {
        append("window.VOID_AREAS=[")
        var first = true
        for (definition: AreaDefinition in Areas.getAll()) {
            val (x, y) = points(definition.area) ?: continue
            if (!first) {
                append(",")
            }
            first = false
            val levels = levels(definition.area)
            append("{\"name\":\"${jsonString(definition.name)}\",\"minLevel\":${levels.first},\"maxLevel\":${levels.last},")
            append("\"x\":[")
            x.joinTo(this, ",")
            append("],\"y\":[")
            y.joinTo(this, ",")
            append("]}")
        }
        append("];")
    }

    private fun DIV.displayToggle(label: String, model: String, last: Boolean = false) {
        div {
            style = "display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);padding:10px 0;" +
                (if (last) "" else "border-bottom:1px solid var(--border-subtle);")
            span {
                style = "font:var(--type-body-sm);color:var(--text-body)"
                +label
            }
            ui.switch("", model = model, small = true)
        }
    }

    /**
     * The "Load folder…"/"Load files…" buttons under the display panel's toggles, shared by the two
     * layers that can be edited from files: the nav graph (`*.nav-edges.toml`, nothing to show
     * until loaded) and the area polygons (`*.areas.toml`, shown from the areas baked into the page
     * until then). Shown while either is switched on with nothing loaded, and loads every layer in
     * that state at once, each from its own files — see `kindsToLoad` in mapedit.js.
     */
    private fun DIV.mapFileLoader() {
        div {
            xShow("kindsToLoad().length")
            div {
                style = "display:flex;flex-direction:column;gap:6px;padding-bottom:10px"
                div {
                    style = HINT_STYLE
                    xText("'Load ' + loadHint() + ' from disk to edit.'")
                }
                div {
                    attributes["title"] = "Pick a folder (the repo's data/) — every matching file under it, however deep, is loaded"
                    ui.button(
                        "Load folder…", variant = ButtonVariant.Secondary, size = ButtonSize.Small, fullWidth = true,
                        onClick = "openMapFolder()",
                    )
                }
                ui.button(
                    "Load files…", variant = ButtonVariant.Secondary, size = ButtonSize.Small, fullWidth = true,
                    onClick = "openMapFiles()",
                )
                errorText("[loadError, navLoaded ? '' : navError, areaLoaded ? '' : areaError].filter(Boolean).join('\\n')")
            }
        }
    }

    /**
     * Which editor gets the pointer and keyboard, shown while either layer has files loaded and is
     * showing — see `activeEditor` in mapedit.js. "None" by default, so loaded files are only
     * looked at until an editor is picked; each editor is offered while its layer is showing. Only
     * the picked editor's panels are shown, so each button carries a dot while its files are unsaved.
     */
    private fun DIV.editorSwitch() {
        div {
            xShow("anyEditable()")
            div {
                style = "display:flex;flex-direction:column;gap:6px;padding-bottom:10px"
                span {
                    style = HINT_STYLE
                    +"Editing"
                }
                rawHtml(
                    """
                    <div class="wm-editor-switch">
                      <button type="button" :class="{ 'wm-editor-switch-on': !activeEditor() }" @click="setMapEditor('none')">None</button>
                      <button type="button" x-show="navEditable()" :class="{ 'wm-editor-switch-on': activeEditor() === 'nav' }" @click="setMapEditor('nav')" x-text="'Nav graph' + (navDirty ? ' •' : '')">Nav graph</button>
                      <button type="button" x-show="areaEditable()" :class="{ 'wm-editor-switch-on': activeEditor() === 'area' }" @click="setMapEditor('area')" x-text="'Areas' + (areaDirty ? ' •' : '')">Areas</button>
                    </div>
                    """,
                )
            }
        }
    }

    private const val HINT_STYLE = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint);line-height:var(--leading-normal)"

    /** A red, pre-wrapped message, shown while [expression] is non-empty. */
    private fun FlowContent.errorText(expression: String) {
        div {
            xShow(expression)
            div {
                style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--feedback-danger);white-space:pre-wrap"
                xText(expression)
            }
        }
    }

    /**
     * The nav graph editor's controls, as a legend. There are no tools to switch between: what a
     * click or drag does depends on what's under it — see `navInteractive` in navgraph.js.
     */
    private val navControls = listOf(
        "Shift+click point" to "Join the selection to it",
        "Right-click" to "Delete point or edge",
        "Esc" to "Stop drawing",
    )

    /**
     * The nav graph editor, under the display panel while it's the active editor (see
     * [editorSwitch]). Drag-to-pan works
     * everywhere except on a point, which drags the point instead (or anywhere at all with Space
     * held). Edits stay in memory until Save writes the changed files back (or downloads them, in a
     * browser that can't write files).
     */
    private fun FlowContent.navGraphPanel() {
        val hint = HINT_STYLE
        div {
            xShow("activeEditor() === 'nav'")
            ui.panel(
                title = "Nav graph",
                padded = false,
                action = { collapseToggle("navPanelOpen", "Toggle nav graph editor") },
                headerClick = "navPanelOpen = !navPanelOpen",
            ) {
                xShow("navPanelOpen")
                div {
                    // Layout on a nested div, not the `x-show`n one — see the bottom-left readout's comment.
                    style = "padding:var(--space-5) var(--space-6) var(--space-6);display:flex;flex-direction:column;gap:var(--space-4)"
                    div {
                        attributes["class"] = "wm-nav-controls"
                        for ((input, action) in navControls) {
                            span { +input }
                            span { +action }
                        }
                    }
                    div {
                        xShow("navSelection")
                        div {
                            style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--gold-300);" +
                                "padding:6px 8px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                                "border-radius:var(--radius-sm);overflow-wrap:anywhere"
                            xText("navSelection")
                        }
                    }
                    div {
                        style = hint
                        +"Files — new edges go into the highlighted one."
                    }
                    rawHtml(
                        """
                        <div style="display:flex;flex-direction:column;gap:4px;max-height:260px;overflow-y:auto">
                          <template x-for="f in navFiles" :key="f.path">
                            <button type="button" class="wm-nav-file" :class="{ 'wm-nav-file-target': f.index === navTarget }" :title="f.path + (f.writable ? '' : ' (read-only in this browser: Save downloads a copy)')" @click="navPickFile(f.index)">
                              <span class="wm-nav-file-name" x-text="f.name + (f.dirty ? ' •' : '')"></span>
                              <span class="wm-nav-file-count" x-text="f.edges"></span>
                            </button>
                          </template>
                        </div>
                        """,
                    )
                    div {
                        style = hint
                        xText(
                            "navStats + ' in ' + navFiles.length + (navFiles.length === 1 ? ' file' : ' files') + " +
                                "(navDirty ? ' — ' + navFiles.filter(f => f.dirty).length + ' unsaved' : '')",
                        )
                    }
                    div {
                        xShow("navError")
                        div {
                            style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--feedback-danger);white-space:pre-wrap"
                            xText("navError")
                        }
                    }
                    div {
                        xShow("navNotice")
                        div {
                            style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-muted);white-space:pre-wrap"
                            xText("navNotice")
                        }
                    }
                    div {
                        style = "display:grid;grid-template-columns:1fr 1fr;gap:6px"
                        div {
                            attributes["title"] = "Undo (Ctrl+Z)"
                            ui.button(
                                "Undo", variant = ButtonVariant.Secondary, size = ButtonSize.Small, fullWidth = true,
                                disabledExpression = "!navCanUndo", onClick = "navUndo()",
                            )
                        }
                        div {
                            attributes["title"] = "Write the selected file"
                            ui.button("Save", size = ButtonSize.Small, fullWidth = true, disabledExpression = "!(navFiles[navTarget] && navFiles[navTarget].dirty)", onClick = "saveNavFile()")
                        }
                    }
                    div {
                        attributes["title"] = "Write every changed file (Ctrl+S)"
                        ui.button("Save all", size = ButtonSize.Small, fullWidth = true, disabledExpression = "!navDirty", onClick = "saveNavFiles()")
                    }
                    div {
                        style = "display:flex;flex-wrap:wrap;justify-content:space-between;gap:2px var(--space-4);min-width:0"
                        ui.button("New", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "navNewFile()")
                        ui.button("Files…", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "openMapFiles(['nav'])")
                        ui.button("Folder…", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "openMapFolder(['nav'])")
                        ui.button("Close", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "closeNavFile()")
                        ui.button("Close all", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "closeNavFiles()")
                    }
                }
            }
        }
    }

    /** The area editor's controls, as a legend — see `areaPointerDown` and friends in areas.js. */
    private val areaControls = listOf(
        "Click area" to "Select it (again: the one beneath)",
        "Shift+click" to "Start a new area there (N: New area)",
        "Drag corner" to "Move it",
        "Drag midpoint" to "Add a corner there",
        "Right-click" to "Delete corner",
        "Arrows" to "Nudge corner or area (Shift: 8)",
    )

    /**
     * The area editor, under the display panel while it's the active editor (see [editorSwitch]):
     * the files, with the one new areas go into highlighted, and Undo/Save. Only the loaded files'
     * areas are drawn while it's up, so an area in a file that wasn't loaded disappears rather than
     * looking editable. The selected area itself is edited in [areaEditPanel].
     */
    private fun FlowContent.areaPanel() {
        div {
            xShow("activeEditor() === 'area'")
            ui.panel(
                title = "Areas",
                padded = false,
                action = { collapseToggle("areaPanelOpen", "Toggle area editor") },
                headerClick = "areaPanelOpen = !areaPanelOpen",
            ) {
                xShow("areaPanelOpen")
                div {
                    // Layout on a nested div, not the `x-show`n one — see the bottom-left readout's comment.
                    style = "padding:var(--space-5) var(--space-6) var(--space-6);display:flex;flex-direction:column;gap:var(--space-4)"
                    div {
                        attributes["class"] = "wm-nav-controls"
                        for ((input, action) in areaControls) {
                            span { +input }
                            span { +action }
                        }
                    }
                    ui.button(
                        "New area", variant = ButtonVariant.Secondary, size = ButtonSize.Small, fullWidth = true,
                        disabledExpression = "areaDrawing >= 0 || !areaActive()", onClick = "areaStartDraw()",
                    )
                    div {
                        style = HINT_STYLE
                        +"Files — new areas go into the highlighted one."
                    }
                    rawHtml(
                        """
                        <div style="display:flex;flex-direction:column;gap:4px;max-height:200px;overflow-y:auto">
                          <template x-for="f in areaFiles" :key="f.path">
                            <button type="button" class="wm-nav-file" :class="{ 'wm-nav-file-target': f.index === areaTarget }" :title="f.path + (f.writable ? '' : ' (read-only in this browser: Save downloads a copy)')" @click="areaPickFile(f.index)">
                              <span class="wm-nav-file-name" x-text="f.name + (f.dirty ? ' •' : '')"></span>
                              <span class="wm-nav-file-count" x-text="f.count"></span>
                            </button>
                          </template>
                        </div>
                        """,
                    )
                    div {
                        style = HINT_STYLE
                        xText(
                            "areaStats + ' in ' + areaFiles.length + (areaFiles.length === 1 ? ' file' : ' files') + " +
                                "(areaDirty ? ' — ' + areaFiles.filter(f => f.dirty).length + ' unsaved' : '')",
                        )
                    }
                    // Shown in the Area panel instead while that's open, next to the field it's about.
                    errorText("!(areaActive() && (areaSel || areaDrawing >= 0)) && areaError")
                    div {
                        xShow("areaNotice")
                        div {
                            style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-muted);white-space:pre-wrap"
                            xText("areaNotice")
                        }
                    }
                    div {
                        style = "display:grid;grid-template-columns:1fr 1fr;gap:6px"
                        div {
                            attributes["title"] = "Undo (Ctrl+Z)"
                            ui.button(
                                "Undo", variant = ButtonVariant.Secondary, size = ButtonSize.Small, fullWidth = true,
                                disabledExpression = "!areaCanUndo", onClick = "areaUndo()",
                            )
                        }
                        div {
                            attributes["title"] = "Write the selected file"
                            ui.button("Save", size = ButtonSize.Small, fullWidth = true, disabledExpression = "!(areaFiles[areaTarget] && areaFiles[areaTarget].dirty)", onClick = "saveAreaFile()")
                        }
                    }
                    div {
                        attributes["title"] = "Write every changed file (Ctrl+S)"
                        ui.button("Save all", size = ButtonSize.Small, fullWidth = true, disabledExpression = "!areaDirty", onClick = "saveAreaFiles()")
                    }
                    div {
                        style = "display:flex;flex-wrap:wrap;justify-content:space-between;gap:2px var(--space-4);min-width:0"
                        ui.button("New file", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "areaNewFile()")
                        ui.button("Files…", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "openMapFiles(['area'])")
                        ui.button("Folder…", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "openMapFolder(['area'])")
                        ui.button("Close", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "closeAreaFile()")
                        ui.button("Close all", variant = ButtonVariant.Link, size = ButtonSize.Small, onClick = "closeAreaFiles()")
                    }
                }
            }
        }
    }

    /**
     * The selected area's editor, in the top-right column where the nav graph's Edges panel goes
     * while that's the active editor: its name, level, tags and any other keys (a `hint`, params),
     * each committing on Enter or when it loses focus. While a new area is being drawn it shows how
     * to finish it instead.
     */
    private fun FlowContent.areaEditPanel() {
        div {
            attributes["class"] = "wm-nav-edge-wrap"
            xShow("areaActive() && (areaSel || areaDrawing >= 0)")
            ui.panel(
                title = "Area",
                padded = false,
                action = { collapseToggle("areaEditPanelOpen", "Toggle area editor") },
                headerClick = "areaEditPanelOpen = !areaEditPanelOpen",
            ) {
                xShow("areaEditPanelOpen")
                rawHtml(
                    """
                    <div class="wm-nav-edge-body">
                      <template x-if="areaDrawing >= 0">
                        <div class="wm-nav-edge-section">
                          <div class="wm-nav-edge-title">New area</div>
                          <div class="wm-nav-edge-hint wm-nav-edge-picking" x-text="areaDrawing === 0 ? 'Click the map to place the first corner.' : areaDrawing + (areaDrawing === 1 ? ' corner' : ' corners') + ' placed.'"></div>
                          <div class="wm-nav-edge-hint">Two corners make a rectangle, three or more a polygon. Enter (or clicking the first corner) finishes it, right-click takes the last corner back, Esc cancels.</div>
                          <div class="wm-nav-edge-hint" x-text="'Goes into ' + (areaFiles[areaTarget] ? areaFiles[areaTarget].name : '') + ', on ' + (level === 0 ? 'every level' : 'level ' + level + ' only') + '.'"></div>
                          <div class="wm-nav-edge-warn" x-show="areaError" x-text="areaError"></div>
                          <div class="wm-nav-templates">
                            <button type="button" class="wm-nav-chip" :disabled="areaDrawing < 2" @click="areaFinishDraw()">Finish</button>
                            <button type="button" class="wm-nav-chip" @click="areaCancelDraw()">Cancel</button>
                          </div>
                        </div>
                      </template>
                      <template x-if="areaDrawing < 0 && areaSel">
                        <div class="wm-nav-edge-section">
                          <label class="wm-nav-edge-field">
                            <span>Name</span>
                            <input class="wm-nav-input wm-area-name" spellcheck="false" :value="areaSel.name"
                              @change="areaRename(${'$'}event.target.value); ${'$'}event.target.value = areaSel.name" @keydown.enter="${'$'}event.target.blur()">
                          </label>
                          <div class="wm-nav-edge-warn" x-show="areaSel.clash">Another loaded area has this name.</div>
                          <div class="wm-nav-edge-warn" x-show="areaError" x-text="areaError"></div>
                          <div class="wm-nav-edge-hint" x-text="areaSel.file + ' · ' + areaSel.shape + ' · ' + areaSel.bounds"></div>
                          <label class="wm-nav-edge-field">
                            <span>Level</span>
                            <select class="wm-nav-input" :value="areaSel.level" @change="areaSetLevel(${'$'}event.target.value)">
                              <option value="">Every level</option>
                              <option value="0">0 only</option>
                              <option value="1">1 only</option>
                              <option value="2">2 only</option>
                              <option value="3">3 only</option>
                            </select>
                          </label>

                          <div class="wm-nav-edge-label">Tags</div>
                          <div class="wm-area-tags">
                            <template x-for="(t, ti) in areaSel.tags" :key="t">
                              <span class="wm-area-tag"><span x-text="t"></span><button type="button" title="Remove tag" @click="areaRemoveTag(ti)">✕</button></span>
                            </template>
                            <input class="wm-nav-input wm-area-tag-input" list="wm-area-tag-list" placeholder="+ tag" spellcheck="false"
                              @keydown.enter.prevent="areaAddTag(${'$'}event.target.value); ${'$'}event.target.value = ''"
                              @change="areaAddTag(${'$'}event.target.value); ${'$'}event.target.value = ''">
                          </div>
                          <datalist id="wm-area-tag-list">
                            <template x-for="t in areaTags" :key="t"><option :value="t"></option></template>
                          </datalist>

                          <div class="wm-nav-edge-label">Other keys</div>
                          <div class="wm-nav-edge-hint" x-show="!areaSel.extras.length">None — a hint, or any param the content reads.</div>
                          <template x-for="(f, fi) in areaSel.extras" :key="fi + ':' + f.key">
                            <div class="wm-nav-action-field">
                              <input class="wm-nav-input wm-nav-key" placeholder="key" spellcheck="false" :value="f.key"
                                @change="areaSetExtra(fi, 'key', ${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                              <input class="wm-nav-input" :class="{ 'wm-nav-input-empty': !f.value.trim() }" placeholder="value" :value="f.value" :title="f.value"
                                @change="areaSetExtra(fi, 'value', ${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                              <button type="button" class="wm-nav-icon" title="Remove" @click="areaRemoveExtra(fi)">✕</button>
                            </div>
                          </template>
                          <button type="button" class="wm-nav-link" @click="areaAddExtra()">+ key</button>

                          <div class="wm-nav-edge-hint wm-nav-edge-picking" x-show="areaSel.vertex" x-text="'Corner ' + areaSel.vertex + ' selected — arrows nudge it' + (areaSel.canDeleteVertex ? ', Delete removes it.' : '.')"></div>
                          <div class="wm-nav-templates">
                            <button type="button" class="wm-nav-chip" @click="areaFrameSelected()" title="Fit the whole area in view">Zoom to</button>
                            <button type="button" class="wm-nav-chip wm-nav-chip-danger" @click="areaDeleteSelected()" title="Delete this area (Delete)">Delete area</button>
                          </div>
                        </div>
                      </template>
                    </div>
                    """,
                )
            }
        }
    }

    /**
     * The chevron in a corner panel's header that collapses it down to that header, bound to
     * [model]. The whole header band toggles as well (see [Ui.panel]'s `headerClick`) — this stays
     * a real button so the control is reachable by keyboard and named for a screen reader, and
     * stops its own click so the band underneath doesn't toggle straight back.
     */
    private fun FlowContent.collapseToggle(model: String, label: String) {
        button {
            attributes["type"] = "button"
            attributes["aria-label"] = label
            onClickStop("$model = !$model")
            xToggleStyle(condition = model, whenTrue = "transform:rotate(0deg)", whenFalse = "transform:rotate(-90deg)")
            style = "background:transparent;border:none;color:var(--text-muted);cursor:pointer;" +
                "display:flex;align-items:center;justify-content:center;width:20px;height:20px;padding:0"
            icon(Icons.CHEVRON_DOWN, size = 14)
        }
    }

    /**
     * The teleport panel, stacked under the elevation stepper in the top-right corner. Laid out as
     * a single column — three label-beside-field rows over a full-width button — rather than the
     * one-line row it was as part of the bottom console. Every field commits on Enter as well as
     * through the button, since typing a coordinate and then reaching for the mouse is the slower
     * half of it.
     *
     * The width is set by the header, not the fields: a game coordinate is five digits at most, so
     * the widest row needs 34px of label, 12px of gap and ~60px of field, while "TELEPORT" plus
     * the collapse chevron and the header's own padding needs ~147px — with a few px of slack on
     * top, since the title ellipsises rather than wraps if the display font renders any wider
     * than it measures here. Anything narrower costs the title, not real space.
     */
    private fun FlowContent.teleportPanel() {
        div {
            attributes["class"] = "wm-teleport-wrap"
            style = "width:162px"
            ui.panel(
                title = "Teleport",
                padded = false,
                action = { collapseToggle("teleportPanelOpen", "Toggle teleport panel") },
                headerClick = "teleportPanelOpen = !teleportPanelOpen",
            ) {
                xShow("teleportPanelOpen")
                div {
                    // The column layout lives on this nested div rather than on the `x-show`n one
                    // above it: showing an element strips `display` from its inline style outright
                    // instead of restoring what was there (see the bottom-left readout's comment),
                    // so a `display:flex` up there is dropped on Alpine's very first pass — taking
                    // `gap` with it and collapsing the rows and button against each other.
                    style = "padding:var(--space-5) var(--space-6) var(--space-6);display:flex;" +
                        "flex-direction:column;gap:var(--space-5)"
                    ui.textInput("wm-tp-x", "X", model = "tpX", mono = true, onEnter = "teleportTo()", inlineLabel = true)
                    ui.textInput("wm-tp-y", "Y", model = "tpY", mono = true, onEnter = "teleportTo()", inlineLabel = true)
                    ui.textInput("wm-tp-z", "Level", model = "tpZ", mono = true, onEnter = "teleportTo()", inlineLabel = true)
                    div {
                        // Set apart from the fields above it by more than the row gap — it commits
                        // them rather than being another one of them.
                        style = "padding-top:var(--space-3)"
                        ui.button("Teleport", size = ButtonSize.Small, fullWidth = true, onClick = "teleportTo()")
                    }
                }
            }
        }
    }

    /**
     * The nav graph's edge inspector, under the teleport panel. With a point selected it lists the
     * point's edges (the only way to reach one of a two-way pair, which draw over each other); with
     * an edge selected it edits that edge's cost, actions and requirements — see the "Edge editor"
     * section of navgraph.js. Every field commits on Enter or when it loses focus.
     */
    private fun FlowContent.navEdgePanel() {
        div {
            attributes["class"] = "wm-nav-edge-wrap"
            xShow("navActive() && (navEdge || navNodeEdges.length)")
            ui.panel(
                title = "Edges",
                padded = false,
                action = { collapseToggle("navEdgePanelOpen", "Toggle edge editor") },
                headerClick = "navEdgePanelOpen = !navEdgePanelOpen",
            ) {
                xShow("navEdgePanelOpen")
                rawHtml(
                    """
                    <div class="wm-nav-edge-body">
                      <template x-if="!navEdge">
                        <div class="wm-nav-edge-section">
                          <div class="wm-nav-edge-hint">This point's edges — pick one to edit its actions.</div>
                          <template x-for="e in navNodeEdges" :key="e.i">
                            <button type="button" class="wm-nav-edge-row" @click="navPickEdge(e.i)">
                              <span x-text="e.label"></span>
                              <span class="wm-nav-edge-row-actions" x-text="e.actions || 'walk'"></span>
                            </button>
                          </template>
                        </div>
                      </template>
                      <template x-if="navEdge">
                        <div class="wm-nav-edge-section">
                          <div>
                            <div class="wm-nav-edge-title" x-text="navEdge.title"></div>
                            <div class="wm-nav-edge-hint" x-text="navEdge.file + ' · ' + (navEdge.directed ? 'one-way' : 'walk edge, both ways')"></div>
                          </div>
                          <button type="button" class="wm-nav-link" x-show="navEdge.hasReverse" @click="navGoReverse()" x-text="'Other way: ' + navEdge.reverseSummary"></button>
                          <button type="button" class="wm-nav-link" x-show="navEdge.directed && !navEdge.hasReverse" @click="navCreateReverse()">+ Add the same edge the other way</button>

                          <label class="wm-nav-edge-field">
                            <span>Cost</span>
                            <input class="wm-nav-input" inputmode="numeric" :value="navEdge.cost" :disabled="!navEdge.directed"
                              :placeholder="navEdge.directed ? '0' : navEdge.walkCost + ' (distance)'"
                              @change="navSetCost(${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                          </label>

                          <div class="wm-nav-edge-label">Actions</div>
                          <div class="wm-nav-edge-hint" x-show="!navEdge.actions.length">None: bots just walk it, either way. Adding one makes the edge one-way.</div>
                          <div class="wm-nav-edge-hint wm-nav-edge-picking" x-show="navPicking >= 0" x-text="'Click the map to set action ' + (navPicking + 1) + '\'s x, y — Esc cancels.'"></div>
                          <template x-for="(a, ai) in navEdge.actions" :key="ai + ':' + a.type + ':' + a.fields.length">
                            <div class="wm-nav-action">
                              <div class="wm-nav-action-head">
                                <span class="wm-nav-action-num" x-text="ai + 1"></span>
                                <input class="wm-nav-input wm-nav-action-type" list="wm-nav-action-types" placeholder="type" :value="a.type"
                                  @change="navSetActionType(ai, ${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                                <button type="button" class="wm-nav-icon" title="Set x, y by clicking the map" x-show="a.pickable"
                                  :class="{ 'wm-nav-icon-active': navPicking === ai }" @click="navPickTile(ai)">⌖</button>
                                <button type="button" class="wm-nav-icon" title="Move up" :disabled="ai === 0" @click="navMoveAction(ai, -1)">↑</button>
                                <button type="button" class="wm-nav-icon" title="Move down" :disabled="ai === navEdge.actions.length - 1" @click="navMoveAction(ai, 1)">↓</button>
                                <button type="button" class="wm-nav-icon" title="Remove action" @click="navRemoveAction(ai)">✕</button>
                              </div>
                              <datalist :id="'wm-nav-keys-' + ai">
                                <template x-for="k in a.suggestions" :key="k"><option :value="k"></option></template>
                              </datalist>
                              <template x-for="(f, fi) in a.fields" :key="fi + ':' + f.key">
                                <div class="wm-nav-action-field">
                                  <input class="wm-nav-input wm-nav-key" :list="'wm-nav-keys-' + ai" placeholder="key" :value="f.key"
                                    @change="navSetField(ai, fi, 'key', ${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                                  <input class="wm-nav-input" :class="{ 'wm-nav-input-empty': f.empty }" placeholder="value" :value="f.value" :title="f.value"
                                    @change="navSetField(ai, fi, 'value', ${'$'}event.target.value)" @keydown.enter="${'$'}event.target.blur()">
                                  <button type="button" class="wm-nav-icon" title="Remove" @click="navRemoveField(ai, fi)">✕</button>
                                </div>
                              </template>
                              <div class="wm-nav-action-foot">
                                <button type="button" class="wm-nav-link" @click="navAddField(ai, a.suggestions[0] || '')"
                                  x-text="'+ ' + (a.suggestions[0] || 'field')"></button>
                                <span class="wm-nav-edge-warn" x-show="a.missing.length" x-text="'Missing: ' + a.missing.join(', ')"></span>
                                <span class="wm-nav-edge-warn" x-show="a.type && !a.known">Not a nav action type</span>
                              </div>
                            </div>
                          </template>
                          <div class="wm-nav-templates">
                            <button type="button" class="wm-nav-chip" @click="navAddAction('door')" title="Open a door, then walk through it">+ Door</button>
                            <button type="button" class="wm-nav-chip" @click="navAddAction('stairs')" title="Climb stairs or a ladder to the other end's level">+ Stairs</button>
                            <button type="button" class="wm-nav-chip" @click="navAddAction('object')" title="Interact with an object">+ Object</button>
                            <button type="button" class="wm-nav-chip" @click="navAddAction('walk')" title="Walk to a tile">+ Walk</button>
                            <button type="button" class="wm-nav-chip" @click="navAddAction('other')" title="Any other action type">+ Other</button>
                          </div>

                          <label class="wm-nav-edge-field wm-nav-edge-field-stacked">
                            <span>Requires</span>
                            <textarea class="wm-nav-input" rows="2" spellcheck="false" :value="navEdge.requires"
                              placeholder='[{ inventory = [{ id = "coins", amount = 10 }] }]'
                              @change="navSetRequires(${'$'}event.target.value)"></textarea>
                          </label>
                        </div>
                      </template>
                      <datalist id="wm-nav-action-types">
                        <template x-for="t in navActionTypes" :key="t"><option :value="t"></option></template>
                      </datalist>
                    </div>
                    """,
                )
            }
        }
    }

    /** Shared style for one row of the console's players/search lists. */
    private const val ROW_STYLE = "display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);" +
        "padding:8px 14px;border-left:2px solid transparent;border-bottom:1px solid var(--border-subtle);cursor:pointer"

    /** Shared style for the small uppercase pill on the right of a row (a rank, a result's kind). */
    private const val PILL_STYLE = "flex:0 0 auto;display:inline-flex;align-items:center;height:17px;padding:0 7px;" +
        "border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
        "letter-spacing:var(--tracking-caps);text-transform:uppercase"

    /**
     * The players pane: every online player, filterable by name, with the selected row and that
     * player's pin highlighted together — `selectedPlayer` drives both (see `renderPlayers`).
     * Picking a row moves the map to that player, switching height level when they're on a
     * different one; "Kick" ([Site.FULL] only) stops the click bubbling so it doesn't also move
     * the view.
     */
    private fun DIV.playersPane() {
        val kickButton = if (Site.FULL) {
            """<button type="button" class="wm-row-action" @click.stop="kickPlayer(p.name)" style="flex:0 0 auto;height:22px;padding:0 10px;background:transparent;border:1px solid var(--border-strong);border-radius:var(--radius-pill);color:var(--text-muted);font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);cursor:pointer">Kick</button>"""
        } else {
            ""
        }
        style = "padding:var(--space-5) 0 0;display:flex;flex-direction:column"
        div {
            // Padded below as well as at the sides: the field's hint line is the last thing in the
            // pane whenever the list under it is empty, and would otherwise sit on the panel edge.
            style = "padding:0 14px var(--space-5)"
            ui.textInput(
                "wm-player-filter", "Online players", model = "playerQuery",
                placeholder = "filter by name…", icon = Icons.SEARCH, onEnter = "selectFirstPlayer()",
                hintExpression = "filteredPlayers.length + ' of ' + players.length + ' shown'",
            )
        }
        div {
            style = "max-height:212px;overflow-y:auto;border-top:1px solid var(--border-subtle)"
            rawHtml(
                """
                <template x-for="p in filteredPlayers" :key="p.name">
                  <div @click="selectPlayer(p.name)" :style="{ background: p.name === selectedPlayer ? 'var(--surface-hover)' : 'transparent', borderLeftColor: p.name === selectedPlayer ? 'var(--gold-400)' : 'transparent' }" style="$ROW_STYLE">
                    <span style="min-width:0;display:flex;flex-direction:column;gap:3px">
                      <span style="display:flex;align-items:center;gap:6px;min-width:0">
                        <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-50);overflow:hidden;text-overflow:ellipsis;white-space:nowrap" x-text="p.name"></span>
                      </span>
                      <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="p.x + ', ' + p.y + ', ' + p.level"></span>
                    </span>
                    $kickButton
                  </div>
                </template>
                <div x-show="players.length && !filteredPlayers.length" style="padding:var(--space-7) 14px;font:var(--type-body-sm);color:var(--text-faint)">No online player matches that name.</div>
                <div x-show="!players.length" style="padding:var(--space-7) 14px;font:var(--type-body-sm);color:var(--text-faint)" x-text="!${'$'}store.world.current ? 'Select a world to see its players on the map.' : playersError ? 'Player locations are unavailable — this world is not responding.' : 'Nobody is showing on the map right now.'"></div>
                """,
            )
        }
    }

    /**
     * The search pane: one field over three sources — online players, the area polygons from
     * [areasScript] and the cache place names from [MapLabels] — each result carrying the position
     * the map jumps to when it's picked. Arrow keys move the highlight and Enter picks it, so the
     * whole thing works without leaving the keyboard; those handlers sit on this wrapper rather
     * than the field because [Ui.textInput] owns the `<input>`'s own attributes, and keyboard
     * events bubble out to here anyway.
     */
    private fun DIV.searchPane() {
        style = "padding:var(--space-5) 0 0;display:flex;flex-direction:column"
        attributes["@keydown.arrow-down.prevent"] = "moveSearchCursor(1)"
        attributes["@keydown.arrow-up.prevent"] = "moveSearchCursor(-1)"
        attributes["@keydown.enter.prevent"] = "openSearchResult()"
        div {
            // Padded below as well as at the sides: with no query typed the field's hint line is
            // the last thing in the pane, and would otherwise sit on the panel's bottom edge.
            style = "padding:0 14px var(--space-5)"
            ui.textInput(
                "wm-search", "Search the world", model = "searchQuery",
                placeholder = "player, area or place name…", icon = Icons.SEARCH,
                hintExpression = "!searchQuery.trim() ? 'Jumps the map to any player, area or place name.' : " +
                    "searchResults.length + (searchResults.length === 1 ? ' match' : ' matches') + " +
                    "' — ↑↓ to browse, enter to go'",
            )
        }
        div {
            // Wrapper carries no `display` of its own — see the bottom-left readout's comment for
            // why an `x-show`ed element can't be the one holding the layout.
            xShow("searchQuery.trim().length > 0")
            div {
                // `moveSearchCursor` scrolls the highlighted row back into view through this id,
                // and picks the row out by `data-row` — the `<template>` itself is a child of this
                // element too, so a plain index into `children` would be off by one.
                attributes["id"] = "wm-search-results"
                style = "max-height:212px;overflow-y:auto;border-top:1px solid var(--border-subtle)"
                rawHtml(
                    """
                    <template x-for="(r, i) in searchResults" :key="r.key">
                      <div data-row @click="goToResult(r)" @mouseenter="searchCursor = i" :style="{ background: searchCursor === i ? 'var(--surface-hover)' : 'transparent', borderLeftColor: searchCursor === i ? 'var(--gold-400)' : 'transparent' }" style="$ROW_STYLE">
                        <span style="min-width:0;display:flex;flex-direction:column;gap:3px">
                          <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-50);overflow:hidden;text-overflow:ellipsis;white-space:nowrap" x-text="r.name"></span>
                          <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="r.x + ', ' + r.y + ', ' + r.level"></span>
                        </span>
                        <span :style="{ color: r.colour, borderColor: r.colour }" style="$PILL_STYLE" x-text="r.kind"></span>
                      </div>
                    </template>
                    <div x-show="!searchResults.length" style="padding:var(--space-7) 14px;font:var(--type-body-sm);color:var(--text-faint)">Nothing on the map matches that name.</div>
                    """,
                )
            }
        }
    }

    fun page(mapLabels: MapLabels): String = voidPage(
        title = "Void - World Map",
        description = "An interactive map of the Void game world.",
        data = "worldMapApp()",
        head = {
            link(rel = "stylesheet", href = "style/world-map.css")
            script { unsafe { raw(tileBaseScript() + areasScript() + mapLabels.script()) } }
            script(src = "js/mapedit.js") {}
            script(src = "js/navgraph.js") {}
            script(src = "js/areas.js") {}
            script(src = "js/worldmap.js") {}
        },
    ) {
        ui.siteHeader(Website.pages, active = "worldmap", communityPages = Website.communityPages)

        main {
            attributes["id"] = "wm-root"
            attributes["x-init"] = "boot(${'$'}el)"
            style = "flex:1;min-height:0;position:relative;overflow:hidden;background:var(--umber-950)"

            div {
                attributes["id"] = "wm-viewport"
                attributes["class"] = "wm-viewport"
                style = "position:absolute;inset:0;overflow:hidden"

                div { attributes["id"] = "wm-tile-layer"; style = "position:absolute;left:0;top:0;will-change:transform" }
                div {
                    attributes["id"] = "wm-grid-layer"
                    xShow("showRegionGrid")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                }
                div {
                    attributes["id"] = "wm-region-labels"
                    xShow("showRegionLabels")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                }
                div {
                    attributes["id"] = "wm-area-polygons"
                    xShow("showAreaPolygons")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                }
                div {
                    attributes["id"] = "wm-area-labels"
                    xShow("showAreaLabels")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                    // Filled from `window.VOID_MAP_LABELS` (see [MapLabels]) on every render.
                }
                div {
                    // Outline of the tile under the cursor, placed by `updateHoverTile`.
                    attributes["id"] = "wm-hover-tile"
                    attributes["class"] = "wm-hover-tile"
                }
                div {
                    // Rebuilt by `renderNavGraph` (navgraph.js) on every render while it's on, and
                    // emptied while it's off. Nothing in it takes pointer events: the editor hit-tests
                    // the graph itself from the viewport's own handlers.
                    attributes["id"] = "wm-nav-graph"
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                }
                div {
                    attributes["id"] = "wm-players"
                    xShow("showPlayerPins")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                    // One pin per entry of the live `players` list `worldmap.js` polls for: Alpine
                    // stamps them out beside the template and `renderPlayers` positions them.
                    unsafe { raw("""<template x-for="p in players" :key="p.name">""") }
                    playerPin()
                    unsafe { raw("</template>") }
                }
            }

            // Empty state — shown once every attempted tile request has failed, meaning nobody has
            // generated `map-tiles/` yet. Hidden the moment a single tile loads (see `tilesMissing`
            // in worldmap.js), so it never lingers behind a map that's actually there.
            div {
                // `xToggleStyle` (not `xShow`) because Alpine's `x-show` toggles `display` between
                // `none` and simply *unset* rather than restoring `flex` — which would collapse this
                // div's centering the moment it's shown after being hidden once.
                xToggleStyle(condition = "tilesMissing", whenTrue = "display:flex", whenFalse = "display:none")
                style = "position:absolute;inset:0;display:none;align-items:center;justify-content:center;" +
                    "pointer-events:none;z-index:15"
                div {
                    style = "pointer-events:auto;max-width:380px;text-align:center;background:var(--surface-panel);" +
                        "border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
                        "box-shadow:var(--bevel-up),var(--shadow-md);padding:var(--space-8) var(--space-7)"
                    h3 {
                        style = "margin:0 0 var(--space-3);font:var(--type-panel-head);letter-spacing:var(--tracking-caps);" +
                            "text-transform:uppercase;color:var(--gold-300)"
                        +"No map tiles found"
                    }
                    p {
                        style = "margin:0;font:var(--type-body-sm);color:var(--text-muted);line-height:var(--leading-normal)"
                        // Two different failures wear the same empty state: a local build hasn't
                        // generated its tiles, whereas a [Site.REMOTE_MAP_TILES] one can only be
                        // failing to reach the host — telling that reader to run a generator would
                        // send them after a file the page never asks for.
                        if (Site.REMOTE_MAP_TILES) {
                            +"Couldn't load any tiles from "
                            code { style = "font:var(--type-code);color:var(--text-accent)"; +Site.MAP_TILES_URL }
                            +". Check your connection and reload."
                        } else {
                            +"Run "
                            code { style = "font:var(--type-code);color:var(--text-accent)"; +"MapZoomImageGenerator" }
                            +" (in the "
                            code { style = "font:var(--type-code);color:var(--text-accent)"; +"tools" }
                            +" module) to render "
                            code { style = "font:var(--type-code);color:var(--text-accent)"; +"map-tiles/" }
                            +", then point "
                            code { style = "font:var(--type-code);color:var(--text-accent)"; +"web.map.tiles" }
                            +" at its output and reload."
                        }
                    }
                }
            }

            // Map display toggles — collapsible via [Ui.panel]'s `action` slot so the panel can be
            // shrunk down to just its header on a small screen without losing the toggles.
            div {
                attributes["class"] = "wm-display-wrap"
                style = "position:absolute;top:20px;left:20px;width:190px;z-index:25;display:flex;" +
                    "flex-direction:column;gap:var(--space-5)"
                ui.panel(
                    title = "Map display",
                    padded = false,
                    action = { collapseToggle("displayPanelOpen", "Toggle map display options") },
                    headerClick = "displayPanelOpen = !displayPanelOpen",
                ) {
                    xShow("displayPanelOpen")
                    style = "padding:4px var(--space-6) var(--space-4)"
                    displayToggle("Area labels", "showAreaLabels")
                    displayToggle("Area polygons", "showAreaPolygons")
                    displayToggle("Region grid", "showRegionGrid")
                    displayToggle("Region labels", "showRegionLabels")
                    displayToggle("Player pins", "showPlayerPins")
                    displayToggle("Nav graph", "showNavGraph", last = true)
                    mapFileLoader()
                    editorSwitch()
                }
                navGraphPanel()
                areaPanel()
            }

            // Top-right column: the elevation stepper, with the teleport panel stacked under it.
            // The column is sized by its contents rather than to a width of its own — the stepper
            // pill has a minimum width, and a narrower column would simply centre it over the
            // column's edges and push it off the side of a phone screen. Stacked a level above the
            // bottom console, which the nav graph's edge panel can grow down beside.
            div {
                attributes["class"] = "wm-top-right"
                style = "position:absolute;top:20px;right:20px;display:flex;flex-direction:column;" +
                    "align-items:flex-end;gap:var(--space-5);z-index:26"
                div {
                    style = "display:flex;flex-direction:column;align-items:center;gap:7px"
                    div {
                        style = "display:flex;align-items:stretch;background:var(--surface-panel);" +
                            "border:1px solid var(--border-panel);border-radius:var(--radius-pill);" +
                            "box-shadow:var(--bevel-up),var(--shadow-md);overflow:hidden"
                        button {
                            attributes["class"] = "wm-elevation-btn"
                            attributes["aria-label"] = "Descend a level"
                            attributes["x-bind:disabled"] = "level <= 0"
                            onClick("setLevel(level - 1)")
                            style = "width:32px;height:36px;background:transparent;border:none;" +
                                "border-right:1px solid var(--border-subtle);color:var(--text-muted);" +
                                "display:flex;align-items:center;justify-content:center;cursor:pointer"
                            icon("""<line x1="5" y1="12" x2="19" y2="12"></line>""", size = 13)
                        }
                        div {
                            style = "padding:0 14px;display:flex;align-items:center"
                            span {
                                style = "font:var(--type-panel-head);letter-spacing:var(--tracking-caps);" +
                                    "color:var(--gold-300);white-space:nowrap"
                                xText("levelLabels[level]")
                                +"SURFACE"
                            }
                        }
                        button {
                            attributes["class"] = "wm-elevation-btn"
                            attributes["aria-label"] = "Ascend a level"
                            attributes["x-bind:disabled"] = "level >= 3"
                            onClick("setLevel(level + 1)")
                            style = "width:32px;height:36px;background:transparent;border:none;" +
                                "border-left:1px solid var(--border-subtle);color:var(--text-muted);" +
                                "display:flex;align-items:center;justify-content:center;cursor:pointer"
                            icon("""<line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line>""", size = 13)
                        }
                    }
                    div {
                        style = "display:flex;gap:5px"
                        for (i in 0..3) {
                            span {
                                xToggleStyle(
                                    condition = "level === $i",
                                    whenTrue = "background:var(--gold-400);border-color:var(--gold-400)",
                                    whenFalse = "background:transparent;border-color:var(--border-strong)",
                                )
                                style = "width:5px;height:5px;border-radius:999px;border:1px solid var(--border-strong)"
                            }
                        }
                    }
                }
                teleportPanel()
                navEdgePanel()
                areaEditPanel()
            }

            // Bottom-left coordinate readout — the game tile under the cursor, plus the name(s) of
            // whichever area polygon(s) (see [areasScript]) that tile falls inside, if any, each on
            // its own line above the readout — but only while the polygon layer is actually on, so
            // the list doesn't call out areas the map isn't currently outlining. `align-items:flex-
            // start` (on both this column and the nested name list) keeps every pill sized to its own
            // text — without it a flex column stretches every child to the widest one, so a long area
            // name would otherwise widen its neighbours, including the (fixed-content) coordinate
            // pill beneath it, to match.
            div {
                style = "position:absolute;left:20px;bottom:20px;display:flex;flex-direction:column;" +
                    "align-items:flex-start;gap:8px;z-index:20"
                div {
                    // `x-show` (no `x-transition`) unconditionally strips the `display` property
                    // from an element's inline style when re-showing it — it doesn't restore
                    // whatever value was there before, it just removes the property outright (see
                    // Alpine's `show` directive source). So this outer div, the one `xShow` toggles,
                    // deliberately carries no `display:` of its own — the actual `display:flex`/`gap`
                    // that lays out the name pills lives on the nested, never-toggled div below,
                    // which x-show can't touch. Getting this backwards silently collapses the list to
                    // default block layout (and `gap` with it) the first time it's hidden then shown.
                    xShow("showAreaPolygons && hoverAreaNames.length")
                    div {
                        style = "display:flex;flex-direction:column;align-items:flex-start;gap:12px"
                        rawHtml(
                            """
                            <template x-for="name in hoverAreaNames" :key="name">
                              <div style="background:var(--surface-panel);border:1px solid var(--border-gold);border-radius:var(--radius-pill);box-shadow:var(--bevel-up),var(--shadow-sm);padding:7px 16px;font:var(--type-body-sm);color:var(--gold-300);max-width:400px;width:fit-content;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="name"></div>
                            </template>
                            """,
                        )
                    }
                }
                div {
                    style = "background:var(--surface-panel);border:1px solid var(--border-panel);" +
                        "border-radius:var(--radius-pill);box-shadow:var(--bevel-up),var(--shadow-sm);" +
                        "padding:7px 16px;font:var(--type-code);font-size:var(--text-sm);color:var(--parch-100)"
                    xText("hoverX + ', ' + hoverY + ', ' + level")
                    +"3200, 3200, 0"
                }
            }

            // Bottom-centre console: the online players list and search, one tab each.
            div {
                attributes["class"] = "wm-console"
                // A fixed width rather than a shrink-wrapped one: both panes are lists, and their
                // rows would otherwise resize the whole panel as results change under the cursor.
                // `translateX(-50%)` keeps it centred; the `.wm-console` media rule in
                // world-map.css overrides the width outright on a viewport too narrow for it.
                style = "position:absolute;left:50%;bottom:20px;transform:translateX(-50%);width:460px;" +
                    "background:var(--surface-panel);border:1px solid var(--border-gold);" +
                    "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-lg);box-sizing:border-box;" +
                    "overflow:hidden;z-index:25"
                ui.tabs(
                    model = "ptab",
                    items = listOf(TabItem("search", "Search"), TabItem("players", "Players")),
                )
                ui.tabPanel("ptab", "players") { playersPane() }
                ui.tabPanel("ptab", "search") { searchPane() }
            }
        }
    }
}
