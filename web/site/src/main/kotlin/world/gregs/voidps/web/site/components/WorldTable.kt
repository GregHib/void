package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style

enum class WorldStatus(val label: String, val tone: BadgeTone, val dot: Boolean) {
    Online("Online", BadgeTone.Success, true),
    Full("Full", BadgeTone.Info, false),
    Restarting("Restarting", BadgeTone.Warning, true),
    Offline("Offline", BadgeTone.Danger, true),
}

data class WorldEntry(
    val number: Int,
    val region: String,
    val members: Boolean = false,
    val mode: String,
    val players: Int,
    val capacity: Int,
    val ping: Int?,
    val status: WorldStatus,
    // The fields below back [worldList]'s expanded detail panel — the compact [worldTable] and
    // navbar [worldMenu] rows only ever read the fields above, so mock entries that don't care
    // about hosting/ruleset detail (Site.kt's component-library page) can leave these at default.
    val name: String = "",
    val description: String = "",
    val host: String = "",
    val revision: String = "",
    val xpRate: String = "",
    val uptime: String = "",
    val address: String = "",
    val tags: List<String> = emptyList(),
    val site: String = "",
    val siteLabel: String = "",
    val note: String = "",
) {
    /** "Name · Region" once a [name] is set, otherwise just the region — [worldTable]'s rows only ever had a region. */
    val label: String get() = if (name.isEmpty()) region else "$name · $region"
}

private const val COLUMNS = "56px 1.1fr 84px 118px 64px 132px"

/** Shared mock world list for surfaces that need one but don't render their own — the navbar's
 * quick-switch menu ([worldMenu]), the play page's world picker, and the full [worldList] page. */
val defaultWorlds = listOf(
    WorldEntry(
        number = 4, name = "Aldergate", region = "United Kingdom", mode = "Normal",
        players = 1284, capacity = 2000, ping = 24, status = WorldStatus.Online,
        description = "The default entry world. Vanilla ruleset, no rate changes, and the largest population " +
            "on the network. New accounts land here unless they pick otherwise.",
        host = "London, UK", revision = "Rev 231", xpRate = "1×", uptime = "19d 04h",
        address = "ald.voidmmo.org:43594", site = "#", siteLabel = "voidmmo.org/aldergate",
        tags = listOf("Vanilla", "Grand exchange", "Skill events"),
        note = "Recommended for first-time accounts.",
    ),
    WorldEntry(
        number = 7, name = "Frostwood", region = "Germany", members = true, mode = "PvP",
        players = 812, capacity = 1200, ping = 38, status = WorldStatus.Online,
        description = "Open world PvP outside the safe zones, with a 15-second combat logout timer. " +
            "Item loss is on and the wilderness has no level cap.",
        host = "Frankfurt, DE", revision = "Rev 231", xpRate = "1.5×", uptime = "6d 21h",
        address = "frost.voidmmo.org:43594", site = "#", siteLabel = "frostwood.gg",
        tags = listOf("Full loot", "No cap wilderness", "Clan wars"),
        note = "Item loss is enabled everywhere outside banks.",
    ),
    WorldEntry(
        number = 9, name = "Tidemoor", region = "United States (East)", mode = "Normal",
        players = 604, capacity = 2000, ping = 96, status = WorldStatus.Online,
        description = "North American mirror of the default ruleset, hosted in Ashburn. Shares the item " +
            "database with Aldergate but keeps a separate economy.",
        host = "Ashburn, US", revision = "Rev 231", xpRate = "1×", uptime = "31d 12h",
        address = "tide.voidmmo.org:43594", site = "#", siteLabel = "voidmmo.org/tidemoor",
        tags = listOf("Vanilla", "Separate economy"),
    ),
    WorldEntry(
        number = 12, name = "Emberfall", region = "Germany", members = true, mode = "Hardcore",
        players = 317, capacity = 600, ping = 41, status = WorldStatus.Online,
        description = "One life. Death deletes the character and posts it to the memorial board. Drop rates " +
            "are unchanged; XP is doubled to make the run viable.",
        host = "Frankfurt, DE", revision = "Rev 231", xpRate = "2×", uptime = "11d 02h",
        address = "ember.voidmmo.org:43594", site = "#", siteLabel = "emberfall.world",
        tags = listOf("Permadeath", "Memorial board", "2× XP"),
        note = "Character deletion on death is permanent and cannot be appealed.",
    ),
    WorldEntry(
        number = 15, name = "Greyhollow", region = "United Kingdom", mode = "Ironman",
        players = 498, capacity = 1200, ping = 27, status = WorldStatus.Restarting,
        description = "Solo-only account rules enforced server-side: no trading, no shared drops, no grand " +
            "exchange. Group ironman is available through the account portal.",
        host = "London, UK", revision = "Rev 231", xpRate = "1×", uptime = "0d 00h",
        address = "grey.voidmmo.org:43594", site = "#", siteLabel = "voidmmo.org/greyhollow",
        tags = listOf("Solo only", "Group ironman", "No trade"),
        note = "Restarting for a scheduled cache update. Back at 04:00 UTC.",
    ),
    WorldEntry(
        number = 18, name = "Saltmarch", region = "Australia", mode = "Normal",
        players = 186, capacity = 800, ping = 174, status = WorldStatus.Online,
        description = "Oceania world running on community-donated hardware in Sydney. Latency to Europe is " +
            "high by design — this world exists for AU and NZ players.",
        host = "Sydney, AU", revision = "Rev 231", xpRate = "1×", uptime = "8d 17h",
        address = "salt.voidmmo.org:43594", site = "#", siteLabel = "saltmarch.au",
        tags = listOf("Community hosted", "Oceania"),
        note = "Hosted by the AU community, not by the Void team.",
    ),
    WorldEntry(
        number = 21, name = "Ashenvale", region = "United States (West)", members = true, mode = "PvE",
        players = 742, capacity = 800, ping = 118, status = WorldStatus.Full,
        description = "Raid-focused world with a persistent group finder and weekly boss rotations. Queue " +
            "opens automatically when a slot frees up.",
        host = "Portland, US", revision = "Rev 231", xpRate = "1.25×", uptime = "24d 09h",
        address = "ashen.voidmmo.org:43594", site = "#", siteLabel = "ashenvale.gg",
        tags = listOf("Raids", "Group finder", "Weekly rotation"),
        note = "World is at capacity. You will be queued on connect.",
    ),
    WorldEntry(
        number = 30, name = "Nullreach", region = "Netherlands", mode = "Beta",
        players = 0, capacity = 400, ping = 33, status = WorldStatus.Offline,
        description = "Staging world for the next protocol revision. Runs unstable builds from the main " +
            "branch, wipes weekly, and is open to anyone testing patches.",
        host = "Amsterdam, NL", revision = "Rev 232 (beta)", xpRate = "5×", uptime = "0d 00h",
        address = "null.voidmmo.org:43594", site = "#", siteLabel = "github.com/void/server",
        tags = listOf("Weekly wipe", "Unstable build", "5× XP"),
        note = "Offline between test cycles. Progress is wiped every Monday.",
    ),
)

/**
 * The world/server list: a header row of labels plus a click-to-select body row per [WorldEntry].
 * A row's click statement defaults to setting [model] to the clicked world's number; pass
 * [onSelect] to run something else instead — the play page's picker calls its `select()` method
 * so choosing a world redirects rather than just updating local state.
 */
fun Ui.worldTable(model: String, worlds: List<WorldEntry>, onSelect: (WorldEntry) -> String = { "$model = ${it.number}" }) {
    receiver.tableScroll(COLUMNS.split(" ")) {
        div {
            style = "display:grid;grid-template-columns:$COLUMNS;gap:var(--space-5);align-items:center;" +
                "padding:0 var(--space-6);height:30px;background:var(--surface-inset);" +
                "border-bottom:1px solid var(--umber-900);font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                "color:var(--text-faint)"
            span { +"#" }
            span { +"REGION" }
            span { +"MODE" }
            span { +"PLAYERS" }
            span { +"PING" }
            span { +"STATE" }
        }
        for ((index, world) in worlds.withIndex()) {
            val last = index == worlds.lastIndex
            div {
                onClick(onSelect(world))
                xToggleStyle(
                    condition = "$model === ${world.number}",
                    whenTrue = "background:var(--surface-active);border-left-color:var(--gold-400)",
                    whenFalse = "background:var(--surface-panel);border-left-color:transparent",
                )
                val borderBottom = if (last) "" else "border-bottom:1px solid var(--umber-900);"
                style = "display:grid;grid-template-columns:$COLUMNS;align-items:center;gap:var(--space-5);" +
                    "padding:0 var(--space-6);height:44px;cursor:pointer;background:var(--surface-panel);" +
                    "border-left:2px solid transparent;$borderBottom" +
                    "transition:background var(--dur-fast) var(--ease-standard)"
                span {
                    style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);color:var(--parch-100)"
                    +world.number.toString()
                }
                span {
                    style = "font:var(--type-body-sm);color:var(--text-body);white-space:nowrap;" +
                        "overflow:hidden;text-overflow:ellipsis"
                    +world.label
                    if (world.members) {
                        span {
                            style = "color:var(--gold-400);margin-left:var(--space-4);font:var(--type-label);" +
                                "letter-spacing:var(--tracking-caps)"
                            +"MEMBERS"
                        }
                    }
                }
                span {
                    style = "font:var(--type-body-sm);color:var(--text-muted)"
                    +world.mode
                }
                playersCell(world)
                pingCell(world)
                ui.badge(world.status.label, tone = world.status.tone, dot = world.status.dot)
            }
        }
    }
}

/** The players/capacity mini progress bar shared by [worldTable] and [worldList]'s rows. */
private fun DIV.playersCell(world: WorldEntry) {
    span {
        style = "display:flex;align-items:center;gap:var(--space-4)"
        val percent = if (world.capacity <= 0) 0 else (world.players * 100 / world.capacity).coerceIn(0, 100)
        span {
            style = "flex:1;height:4px;background:var(--surface-inset);border-radius:var(--radius-xs);" +
                "box-shadow:var(--bevel-down);overflow:hidden"
            span {
                style = "display:block;width:$percent%;height:100%;background:var(--gold-400)"
            }
        }
        span {
            style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);" +
                "min-width:34px;text-align:right"
            +String.format("%,d", world.players)
        }
    }
}

private fun DIV.pingCell(world: WorldEntry) {
    span {
        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)"
        +(world.ping?.let { "${it}ms" } ?: "—")
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
 * mode tabs and a search box above a [worldTable]-style list whose rows expand in place to show
 * hosting/ruleset detail and a connect button. Filtering, search and which row is expanded are all
 * local `x-data` state — nested Alpine components still resolve unrecognised names (like [onSelect]'s
 * `select()` call) against whatever ancestor `x-data` this is dropped into, same as [worldTable].
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
                            if (world.members) {
                                span {
                                    style = "color:var(--gold-400);margin-left:var(--space-4);font:var(--type-label);" +
                                        "letter-spacing:var(--tracking-caps)"
                                    +"MEMBERS"
                                }
                            }
                        }
                        span {
                            style = "font:var(--type-body-sm);color:var(--text-muted)"
                            +world.mode
                        }
                        playersCell(world)
                        pingCell(world)
                        ui.badge(world.status.label, tone = world.status.tone, dot = world.status.dot)
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
                                worldDetailStat("Host", world.host)
                                worldDetailStat("Revision", world.revision, mono = true)
                                worldDetailStat("XP rate", world.xpRate)
                                worldDetailStat("Uptime", world.uptime)
                                worldDetailStat("Capacity", "${String.format("%,d", world.players)} / ${String.format("%,d", world.capacity)}", mono = true)
                                if (world.address.isNotEmpty()) {
                                    div {
                                        style = "min-width:0;grid-column:1/-1"
                                        worldDetailLabel("Address")
                                        span {
                                            style = "display:block;font:var(--type-code);font-size:var(--text-xs);" +
                                                "color:var(--text-body);overflow-wrap:anywhere"
                                            +world.address
                                        }
                                    }
                                }
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
                            val locked = world.status == WorldStatus.Offline
                            val playLabel = when (world.status) {
                                WorldStatus.Full -> "Join queue"
                                WorldStatus.Offline -> "Unavailable"
                                else -> "Play world ${world.number}"
                            }
                            ui.button(playLabel, disabled = locked, fullWidth = true, onClick = if (locked) null else onSelect(world))
                            ui.button(
                                "Copy config",
                                variant = ButtonVariant.Secondary,
                                fullWidth = true,
                                onClick = "navigator.clipboard && navigator.clipboard.writeText('${jsString(world.address)}')",
                            )
                            div { style = "height:1px;background:var(--border-panel)" }
                            div {
                                style = "display:flex;flex-direction:column;gap:10px"
                                if (world.site.isNotEmpty()) {
                                    a(href = world.site) {
                                        style = "font:var(--type-body-sm)"
                                        +"${world.siteLabel} →"
                                    }
                                }
                                a(href = "#") { style = "font:var(--type-body-sm)"; +"Status & uptime history →" }
                                a(href = "#") { style = "font:var(--type-body-sm)"; +"World rules →" }
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
fun DIV.worldStat(value: String, label: String) {
    div {
        span {
            style = "display:block;font:var(--weight-bold) var(--text-2xl)/1 var(--font-display);color:var(--gold-300)"
            +value
        }
        span {
            style = "display:block;margin-top:6px;font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                "text-transform:uppercase;color:var(--text-faint)"
            +label
        }
    }
}

private fun DIV.worldDetailStat(label: String, value: String, mono: Boolean = false) {
    if (value.isEmpty()) {
        return
    }
    div {
        worldDetailLabel(label)
        span {
            val font = if (mono) "var(--type-code);font-size:var(--text-xs)" else "var(--type-body-sm)"
            style = "font:$font;color:var(--text-body)"
            +value
        }
    }
}
