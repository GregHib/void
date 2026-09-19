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

    /** A single map pin. [level] hides it when the viewer isn't on that height level. */
    private fun FlowContent.playerPin(x: Int, y: Int, level: Int, name: String, playerLevel: Int, you: Boolean) {
        div {
            attributes["class"] = "wm-pin"
            attributes["data-gx"] = x.toString()
            attributes["data-gy"] = y.toString()
            attributes["data-level"] = level.toString()
            style = "position:absolute;transform:translate(-50%,-100%);pointer-events:auto;cursor:default"
            div {
                attributes["class"] = "wm-pin-dot"
                style = "position:relative;z-index:1;width:8px;height:8px;border-radius:999px;" +
                    "background:var(--gold-400);margin:0 auto 2px;" +
                    (if (you) "animation:wmPinPulse 2.4s ease-out infinite" else "")
            }
            unsafe {
                raw(
                    """<svg width="16" height="20" viewBox="0 0 24 28" style="display:block;margin:-6px auto 0;position:relative;z-index:2">""" +
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
                    // Example pin only — real player positions will replace this once the server bridge exists.
                    playerPin(3222, 3218, level = 0, name = "Hein", playerLevel = 3, you = true)
                }
            }

            // Map display toggles.
            div {
                style = "position:absolute;top:20px;left:20px;width:190px;z-index:25"
                ui.panel(title = "Map display", padded = false) {
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

            // Bottom-left coordinate readout — the game tile under the cursor.
            div {
                style = "position:absolute;left:20px;bottom:20px;background:var(--surface-panel);" +
                    "border:1px solid var(--border-panel);border-radius:var(--radius-pill);" +
                    "box-shadow:var(--bevel-up),var(--shadow-sm);padding:7px 16px;font:var(--type-code);" +
                    "font-size:var(--text-sm);color:var(--parch-100);z-index:20"
                xText("hoverX + ', ' + hoverY + ', ' + level")
                +"3200, 3200, 0"
            }

            // Bottom-center console: teleport (functional), players/search (placeholders for now).
            div {
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
