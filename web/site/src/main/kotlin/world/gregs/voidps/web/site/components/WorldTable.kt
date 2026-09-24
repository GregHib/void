package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.gregs.voidps.web.site.Site
import java.io.File

/** The states a world's live `status` (from `worlds.js`'s `worlds` Alpine store) can be in. */
enum class WorldStatus(val label: String, val tone: BadgeTone, val dot: Boolean) {
    Checking("Checking", BadgeTone.Neutral, false),
    Online("Online", BadgeTone.Success, true),
    Full("Full", BadgeTone.Info, false),
    Restarting("Restarting", BadgeTone.Warning, true),
    Offline("Offline", BadgeTone.Danger, true),
    ;

    companion object {
        /** JS expression mapping a status name in [statusExpr] to its tone's text colour. */
        fun colorExpr(statusExpr: String) = "({${entries.joinToString(",") { "${it.name}:'${it.tone.color}'" }}})[$statusExpr]"
    }
}

/**
 * A world's static description, as listed in `worlds.json`. Anything that changes while the world
 * runs (status, players, capacity, xp/drop rates, uptime, ping) isn't here — it's fetched live
 * from the world's own [web] server by `worlds.js` and read back through [live].
 */
@Serializable
data class WorldEntry(
    val number: Int,
    val region: String,
    val mode: String,
    val name: String = "",
    val description: String = "",
    val host: String = "",
    val address: String = "",
    /** Base URL of this world's Void web server, which serves its live `/api/v1/info`. */
    val web: String = "",
    val tags: List<String> = emptyList(),
    /** Label → url links for the detail panel, in order; only the first [MAX_WORLD_LINKS] are shown. */
    val links: Map<String, String> = emptyMap(),
    val note: String = "",
) {
    /** "Name · Region" once a [name] is set, otherwise just the region. */
    val label: String get() = if (name.isEmpty()) region else "$name · $region"

    /** JS expression for this world's live state in the `worlds` Alpine store (see `worlds.js`). */
    val live: String get() = "${'$'}store.worlds.get($number)"
}

private const val MAX_WORLD_LINKS = 3

private const val COLUMNS = "56px 1.1fr 84px 118px 64px 132px"

private val worldsJson = Json { ignoreUnknownKeys = true }

/**
 * Reads the canonical world list out of the `worlds.json` data file (served as-is at the site
 * root) rather than embedding it in Kotlin, so it's the single source of truth for every surface
 * that renders a world list — the navbar's quick-switch menu ([worldMenu]), the play page's world
 * picker, and the full [worldList] page. The browser re-fetches the same file at runtime (see
 * `worlds.js`) to fill in each world's live state from its info endpoint and ping — this
 * server-side read only has to produce the page's static structure.
 */
private fun loadWorlds(path: String = "./web/site/src/main/resources/static/worlds.json"): List<WorldEntry> {
    val file = File(path)
    if (!file.exists()) {
        return emptyList()
    }
    return worldsJson.decodeFromString(file.readText())
}

val defaultWorlds: List<WorldEntry> = loadWorlds()

/** The players/capacity mini progress bar shared by [worldList]'s rows. */
private fun DIV.playersCell(world: WorldEntry) {
    span {
        style = "display:flex;align-items:center;gap:var(--space-4)"
        val live = world.live
        span {
            style = "flex:1;height:4px;background:var(--surface-inset);border-radius:var(--radius-xs);" +
                "box-shadow:var(--bevel-down);overflow:hidden"
            span {
                xEffectStyle("width", "($live.capacity ? Math.min(100, $live.players * 100 / $live.capacity) : 0) + '%'")
                style = "display:block;width:0;height:100%;background:var(--gold-400)"
            }
        }
        span {
            style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);" +
                "min-width:34px;text-align:right"
            xText("voidFormatNumber($live.players)")
            +"—"
        }
    }
}

private fun DIV.pingCell(world: WorldEntry) {
    span {
        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)"
        xText("${world.live}.ping != null ? ${world.live}.ping + 'ms' : '—'")
        +"—"
    }
}

/** One badge per [WorldStatus], with only the one matching the world's live status shown. */
private fun Ui.statusBadge(world: WorldEntry) {
    for (status in WorldStatus.entries) {
        badge(status.label, tone = status.tone, dot = status.dot, showWhen = "${world.live}.status === '${status.name}'")
    }
}

/**
 * One `.void-world-row` grid (the header or a single clickable summary row) wrapped in its own
 * (outer shrink + `overflow-x:auto`, inner `min-width`) scroll box, the same two-box idea
 * [tableScroll] uses — but applied per row instead of around the whole list, so scrolling one row
 * to see a trailing column never drags the sibling detail panel (a separate, normal-width block)
 * sideways with it. [toggle] is the row's `xToggleStyle` triple (condition, whenTrue, whenFalse)
 * for the open/highlighted background; the header passes none.
 */
private fun DIV.worldRowScroll(
    extraStyle: String,
    rowOnClick: String? = null,
    toggle: Triple<String, String, String>? = null,
    content: DIV.() -> Unit,
) {
    div {
        attributes["class"] = "void-world-row-scroll"
        div {
            attributes["class"] = "void-world-row"
            if (rowOnClick != null) {
                onClick(rowOnClick)
            }
            if (toggle != null) {
                val (condition, whenTrue, whenFalse) = toggle
                xToggleStyle(condition, whenTrue, whenFalse)
            }
            style = "display:grid;grid-template-columns:$COLUMNS;align-items:center;gap:var(--space-5);" +
                "padding:0 var(--space-6);min-width:${tableMinWidth(COLUMNS.split(" "))}px;$extraStyle"
            content()
        }
    }
}

/** Escapes a value for safe embedding inside a single-quoted inline Alpine JS expression. */
internal fun jsString(value: String) = value.replace("\\", "\\\\").replace("'", "\\'")

private fun WorldEntry.matchesFilterExpr() = "(filter === 'all' || filter === '${jsString(mode)}')"

/** `query`-matching for a search box's `x-model`, checked against name/region/mode — also used standalone by [worldMenu]. */
internal fun WorldEntry.matchesSearchExpr(): String {
    val haystack = jsString("$name $region $mode".lowercase())
    return "(query.trim() === '' || '$haystack'.includes(query.trim().toLowerCase()))"
}

private fun WorldEntry.visibleExpr() = "${matchesFilterExpr()} && ${matchesSearchExpr()}"

/**
 * The full world-selection surface used by the play page's picker and the "view all worlds" page:
 * mode tabs and a search box above a leaderboard-style list whose rows expand in place to show
 * hosting/ruleset detail and a connect button. Filtering, search and which row is expanded are all
 * local `x-data` state — nested Alpine components still resolve unrecognised names (like [onSelect]'s
 * `select()` call) against whatever ancestor `x-data` this is dropped into.
 */
fun Ui.worldList(worlds: List<WorldEntry>, onSelect: (WorldEntry) -> String) {
    receiver.div {
        xData("{ filter: 'all', query: '', expanded: ${worlds.firstOrNull()?.number ?: "null"} }")
        style = "display:flex;flex-direction:column;gap:var(--space-6)"

        div {
            style = "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-6);flex-wrap:wrap"
            val modes = worlds.map { it.mode }.distinct()
            ui.tabs(
                model = "filter",
                filled = false,
                items = listOf(TabItem("all", "All worlds", worlds.size.toString())) + modes.map { TabItem(it, it) },
            )
            div {
                style = "width:240px"
                ui.textInput("void-world-search", "Search", model = "query", placeholder = "Name, region, or mode", icon = Icons.SEARCH)
            }
        }

        div {
            attributes["class"] = "void-world-list-scroll"
            style = "background:var(--surface-panel);border:1px solid var(--border-panel);" +
                "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-xs);overflow:hidden"

            // Unlike a plain leaderboard table ([tableScroll]), a row here sits directly above its own
            // full-width, wrapping detail panel — so the min-width/overflow-x:auto pairing that makes a
            // too-narrow table scroll instead of clipping has to be scoped to *just* the row/header grids
            // (worldRowScroll), not the whole list. Forcing the list's whole container that wide would
            // drag the (already narrow enough to fit) detail panel sideways along with the row that opened
            // it every time the row itself is scrolled to see a trailing column.
            worldRowScroll(
                extraStyle = "height:34px;background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
                    "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)",
            ) {
                span { +"No." }
                span { +"World" }
                span { +"Mode" }
                span { +"Players" }
                span { +"Ping" }
                span { +"State" }
            }

            for ((index, world) in worlds.withIndex()) {
                val last = index == worlds.lastIndex
                div {
                    xShow(world.visibleExpr())
                    if (!last) {
                        style = "border-bottom:1px solid var(--umber-900)"
                    }

                    worldRowScroll(
                        extraStyle = "height:44px;cursor:pointer;background:var(--surface-panel);" +
                            "border-left:2px solid transparent;transition:background var(--dur-fast) var(--ease-standard)",
                        rowOnClick = "expanded = expanded === ${world.number} ? null : ${world.number}",
                        toggle = Triple(
                            "expanded === ${world.number}",
                            "background:var(--surface-active);border-left-color:var(--gold-400)",
                            "background:var(--surface-panel);border-left-color:transparent",
                        ),
                    ) {
                        span {
                            style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);color:var(--parch-100)"
                            +world.number.toString()
                        }
                        span {
                            style = "font:var(--type-body-sm);color:var(--text-body);white-space:nowrap;" +
                                "overflow:hidden;text-overflow:ellipsis"
                            +world.label
                        }
                        span {
                            style = "font:var(--type-body-sm);color:var(--text-muted)"
                            +world.mode
                        }
                        playersCell(world)
                        pingCell(world)
                        span { ui.statusBadge(world) }
                    }

                    div {
                        // x-show clears any inline `display` it doesn't own (see base.css), so the grid layout
                        // is expressed via the `void-grid` class instead of an inline `display:grid`.
                        attributes["class"] = "void-grid void-world-detail"
                        xShow("expanded === ${world.number}")
                        style = "gap:var(--space-8);padding:var(--space-6) var(--space-6) var(--space-6) " +
                            "calc(var(--space-6) + 56px + var(--space-5));background:var(--umber-900);" +
                            "border-left:2px solid var(--gold-400);box-shadow:var(--bevel-down)"

                        div {
                            style = "min-width:0"
                            h2 {
                                style = "margin:0 0 var(--space-4);font:var(--weight-bold) var(--text-xl)/var(--leading-tight) " +
                                    "var(--font-display);color:var(--parch-50)"
                                +(world.name.ifEmpty { world.region })
                            }
                            if (world.description.isNotEmpty()) {
                                p {
                                    style = "margin:0 0 var(--space-6);font:var(--type-body);color:var(--text-muted);max-width:62ch"
                                    +world.description
                                }
                            }
                            div {
                                style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(120px,1fr));" +
                                    "gap:var(--space-5) var(--space-6)"
                                val live = world.live
                                worldDetailStat("Host", world.host)
                                worldDetailStat("XP rate", expr = "voidFormatRate($live.xpRate)")
                                worldDetailStat("Drop rate", expr = "voidFormatRate($live.dropRate)")
                                worldDetailStat("Uptime", expr = "voidFormatUptime($live.uptimeSeconds)")
                                worldDetailStat(
                                    "Capacity",
                                    expr = "voidFormatNumber($live.players) + ' / ' + voidFormatNumber($live.capacity)",
                                    mono = true,
                                )
                            }
                            if (world.tags.isNotEmpty()) {
                                div {
                                    style = "display:flex;flex-wrap:wrap;gap:8px;margin-top:var(--space-6)"
                                    for (tag in world.tags) {
                                        ui.badge(tag)
                                    }
                                }
                            }
                        }

                        div {
                            style = "display:flex;flex-direction:column;gap:var(--space-5);align-items:stretch"
                            val status = "${world.live}.status"
                            ui.button(
                                "${if (Site.FULL) "Play" else "Select"} world ${world.number}",
                                disabledExpression = "$status === 'Offline'",
                                fullWidth = true,
                                onClick = "if ($status !== 'Offline') { ${onSelect(world)} }",
                                textExpr = "$status === 'Full' ? 'Join queue' : $status === 'Offline' ? 'Unavailable' : '${if (Site.FULL) "Play" else "Select"} world ${world.number}'",
                            )
                            ui.button(
                                "Copy config",
                                variant = ButtonVariant.Secondary,
                                fullWidth = true,
                                onClick = "navigator.clipboard && navigator.clipboard.writeText('${jsString(world.address)}')",
                            )
                            val links = world.links.entries.take(MAX_WORLD_LINKS)
                            if (links.isNotEmpty()) {
                                div { style = "height:1px;background:var(--border-panel)" }
                                div {
                                    style = "display:flex;flex-direction:column;gap:10px"
                                    for ((label, url) in links) {
                                        a(href = url) {
                                            style = "font:var(--type-body-sm)"
                                            +label
                                        }
                                    }
                                }
                            }
                            if (world.note.isNotEmpty()) {
                                span {
                                    style = "font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)"
                                    +world.note
                                }
                            }
                        }
                    }
                }
            }
        }

        div {
            xShow("!(${worlds.joinToString(" || ") { "(${it.visibleExpr()})" }})")
            style = "padding:var(--space-10);text-align:center;font:var(--type-body);color:var(--text-faint)"
            +"No worlds match that filter."
        }
    }
}

private fun DIV.worldDetailLabel(text: String) {
    span {
        style = "display:block;font:var(--type-label);letter-spacing:var(--tracking-caps);" +
            "text-transform:uppercase;color:var(--text-faint);margin-bottom:5px"
        +text
    }
}

/** A big number over a small caption — the players-online/worlds-up pair on [Play.page]/[Website.worldsPage]. */
fun DIV.worldStat(value: String, label: String, expr: String? = null) {
    div {
        span {
            style = "display:block;font:var(--weight-bold) var(--text-2xl)/1 var(--font-display);color:var(--gold-300)"
            if (expr != null) {
                xText(expr)
            }
            +value
        }
        span {
            style = "display:block;margin-top:6px;font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                "text-transform:uppercase;color:var(--text-faint)"
            +label
        }
    }
}

/** A static [value], or a live one bound to the JS [expr] (showing "—" until it resolves). */
private fun DIV.worldDetailStat(label: String, value: String = "—", mono: Boolean = false, expr: String? = null) {
    if (value.isEmpty()) {
        return
    }
    div {
        worldDetailLabel(label)
        span {
            val font = if (mono) "var(--type-code);font-size:var(--text-xs)" else "var(--type-body-sm)"
            style = "font:$font;color:var(--text-body)"
            if (expr != null) {
                xText(expr)
            }
            +value
        }
    }
}
