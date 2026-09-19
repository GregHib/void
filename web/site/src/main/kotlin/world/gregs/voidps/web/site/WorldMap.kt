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
 * `void-map-tiles`) fetched straight from `map-tiles/` — [world.gregs.voidps.web.site.Site]
 * copies whatever `web.map.tiles` points at alongside the rest of the static assets, the same way
 * it copies `static/`. All of the actual tile math, panning/zoom interaction and URL persistence
 * lives in `js/worldmap.js`'s `worldMapApp()`; this file only renders the static chrome (panels,
 * console, the one example player pin and area labels) that Alpine then positions/reacts to.
 */
object WorldMap {

    /** `data-gx`/`data-gy` anchor a label to a game tile; [WorldMap]'s JS repositions it every render. */
    private fun FlowContent.areaLabel(x: Int, y: Int, text: String) {
        div {
            attributes["class"] = "wm-area-label"
            attributes["data-gx"] = x.toString()
            attributes["data-gy"] = y.toString()
            // `font-size` is a fallback only — [WorldMap]'s JS solves each label's actual size
            // against the map's current zoom every render, the same way it sizes region labels.
            style = "position:absolute;transform:translate(-50%,-50%);white-space:nowrap;" +
                "font-family:var(--font-display);font-weight:var(--weight-semibold);font-size:var(--text-base);line-height:1;" +
                "letter-spacing:var(--tracking-caps);color:var(--text-muted);" +
                "text-shadow:0 1px 4px rgba(10,7,4,.9)"
            +text
        }
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
     * A single map pin. [level] hides it when the viewer isn't on that height level. [rank] labels
     * the pin's owner in its right-click menu header (e.g. "dev") — `null` for a plain player. The
     * right-click menu applies to every pin, including [you] — moving your own view to where you
     * already are is harmless, and an admin may still want to kick themselves off for testing.
     *
     * The outer box is a fixed [PIN_WIDTH]x[PIN_HEIGHT], not sized from its content: every child is
     * positioned by a pixel offset from *its* edges rather than left to stack in flow, so
     * `translate(-50%,-100%)` below anchors a known point — the box's bottom-centre — exactly on the
     * game tile, instead of wherever the content happened to reach. Off that one anchor: [dot] (one
     * game tile's on-screen footprint at the base zoom — see `BASE_PX_PER_TILE` in worldmap.js) is
     * centred exactly on it, and the map-pin icon sits directly above with its tip touching the dot's
     * top edge, so the icon visibly points down at the dot rather than swallowing it.
     */
    private fun FlowContent.playerPin(x: Int, y: Int, level: Int, name: String, playerLevel: Int, you: Boolean, rank: String? = null) {
        val pinWidth = 20
        val pinHeight = 26
        val dotSize = 4
        div {
            attributes["class"] = "wm-pin"
            attributes["data-gx"] = x.toString()
            attributes["data-gy"] = y.toString()
            attributes["data-level"] = level.toString()
            style = "position:absolute;width:${pinWidth}px;height:${pinHeight}px;" +
                "transform:translate(-50%,-100%);pointer-events:auto;cursor:default"
            xData("{ menuOpen: false }")
            onContextMenu("menuOpen = true")
            onClickOutside("menuOpen = false")
            div {
                attributes["class"] = "wm-pin-dot"
                // Centred on the box's bottom edge (the anchor): half of `dotSize` hangs past it.
                style = "position:absolute;left:50%;bottom:-${dotSize / 2}px;transform:translateX(-50%);z-index:1;" +
                    "width:${dotSize}px;height:${dotSize}px;border-radius:999px;background:var(--gold-400);" +
                    (if (you) "animation:wmPinPulse 2.4s ease-out infinite" else "")
            }
            unsafe {
                raw(
                    // `bottom` matches [dot]'s own top edge (`dotSize` above the anchor) so the
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
                    +"$name · Lv $playerLevel"
                }
                div {
                    attributes["class"] = "wm-pin-coords"
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);margin-top:2px"
                    +"$x, $y, $level"
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
                    +if (rank != null) "$name · $rank" else name
                }
                button {
                    menuItemStyle(danger = false, first = true)
                    onClick("moveToPlayer($x, $y, $level); menuOpen = false")
                    icon(Icons.CROSSHAIR, size = 13)
                    +" Move here"
                }
                button {
                    menuItemStyle(danger = true, first = false)
                    onClick("kickPlayer('${escape(name)}'); menuOpen = false")
                    icon(Icons.CIRCLE_X, size = 13)
                    +" Kick"
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
            append("{\"name\":\"${escape(definition.name)}\",\"minLevel\":${levels.first},\"maxLevel\":${levels.last},")
            append("\"x\":[")
            x.joinTo(this, ",")
            append("],\"y\":[")
            y.joinTo(this, ",")
            append("]}")
        }
        append("];")
    }

    private fun escape(text: String): String = text.replace("\\", "\\\\").replace("\"", "\\\"")

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

    fun page(): String = voidPage(
        title = "Void — world map",
        description = "A lazily-tiled, zoomable map of the Void game world.",
        data = "worldMapApp()",
        head = {
            link(rel = "stylesheet", href = "style/world-map.css")
            script { unsafe { raw(areasScript()) } }
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
                    // Example content only — real area boundaries/names will replace these once that data exists.
                    areaLabel(3222, 3218, "Lumbridge")
                    areaLabel(3293, 3182, "Al Kharid")
                    areaLabel(3105, 3250, "Draynor Village")
                }
                div {
                    attributes["id"] = "wm-players"
                    xShow("showPlayerPins")
                    style = "position:absolute;inset:0;pointer-events:none;overflow:hidden"
                    // Example pins only — real player positions will replace these once the server
                    // bridge exists. "Kaelbrand" demonstrates [playerPin]'s `rank` param, shown in
                    // every pin's right-click menu header.
                    playerPin(3222, 3218, level = 0, name = "Hein", playerLevel = 3, you = true)
                    playerPin(3293, 3182, level = 0, name = "Kaelbrand", playerLevel = 45, you = false, rank = "dev")
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

            // Map display toggles — collapsible via [Ui.panel]'s `action` slot so the panel can be
            // shrunk down to just its header on a small screen without losing the toggles.
            div {
                attributes["class"] = "wm-display-wrap"
                style = "position:absolute;top:20px;left:20px;width:190px;z-index:25"
                ui.panel(
                    title = "Map display",
                    padded = false,
                    action = {
                        button {
                            attributes["type"] = "button"
                            attributes["aria-label"] = "Toggle map display options"
                            onClick("displayPanelOpen = !displayPanelOpen")
                            xToggleStyle(condition = "displayPanelOpen", whenTrue = "transform:rotate(0deg)", whenFalse = "transform:rotate(-90deg)")
                            style = "background:transparent;border:none;color:var(--text-muted);cursor:pointer;" +
                                "display:flex;align-items:center;justify-content:center;width:20px;height:20px;padding:0"
                            icon(Icons.CHEVRON_DOWN, size = 14)
                        }
                    },
                ) {
                    xShow("displayPanelOpen")
                    style = "padding:4px var(--space-6) var(--space-4)"
                    displayToggle("Area labels", "showAreaLabels")
                    displayToggle("Area polygons", "showAreaPolygons")
                    displayToggle("Region grid", "showRegionGrid")
                    displayToggle("Region labels", "showRegionLabels")
                    displayToggle("Player pins", "showPlayerPins", last = true)
                }
            }

            // Elevation stepper.
            div {
                attributes["class"] = "wm-elevation-wrap"
                style = "position:absolute;top:20px;right:20px;display:flex;flex-direction:column;" +
                    "align-items:center;gap:7px;z-index:25"
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
                        unsafe {
                            raw(
                                """<template x-for="name in hoverAreaNames" :key="name">""" +
                                    """<div style="background:var(--surface-panel);border:1px solid var(--border-gold);""" +
                                    """border-radius:var(--radius-pill);box-shadow:var(--bevel-up),var(--shadow-sm);""" +
                                    """padding:7px 16px;font:var(--type-body-sm);color:var(--gold-300);max-width:400px;""" +
                                    """width:fit-content;white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="name"></div>""" +
                                    """</template>""",
                            )
                        }
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

            // Bottom-center console: teleport (functional), players/search (placeholders for now).
            div {
                attributes["class"] = "wm-console"
                style = "position:absolute;left:50%;bottom:20px;transform:translateX(-50%);width:640px;" +
                    "background:var(--surface-panel);border:1px solid var(--border-gold);" +
                    "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-lg);box-sizing:border-box;" +
                    "overflow:hidden;z-index:25"
                ui.tabs(
                    model = "ptab",
                    items = listOf(TabItem("teleport", "Teleport"), TabItem("players", "Players"), TabItem("search", "Search")),
                )
                ui.tabPanel("ptab", "teleport") {
                    style = "padding:14px 18px;display:flex;align-items:flex-end;gap:var(--space-4)"
                    div { style = "width:64px;flex:0 0 auto"; ui.textInput("wm-tp-x", "X", model = "tpX", mono = true) }
                    div { style = "width:64px;flex:0 0 auto"; ui.textInput("wm-tp-y", "Y", model = "tpY", mono = true) }
                    div { style = "width:64px;flex:0 0 auto"; ui.textInput("wm-tp-z", "Level", model = "tpZ", mono = true) }
                    ui.button("Teleport", onClick = "teleportTo()")
                }
                ui.tabPanel("ptab", "players") {
                    style = "padding:24px 18px;text-align:center"
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-faint)"
                        +"No connected players yet — this will list online players once the server bridge is wired up."
                    }
                }
                ui.tabPanel("ptab", "search") {
                    style = "padding:14px 18px;display:flex;flex-direction:column;gap:var(--space-4)"
                    ui.textInput("wm-search", "Find a player", placeholder = "name…", icon = Icons.SEARCH)
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-faint)"
                        +"Player search will jump the map to their position once the server bridge is wired up."
                    }
                }
            }
        }
    }
}
