package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The staff-only developer panel: a live world dashboard and a player management workbench.
 * Reachable from [AccountMenu]'s "Developer panel" entry for admin accounts. Both pages are
 * written to their own file under `dev/` by [Site], following the same pattern as [Website]'s
 * marketing pages and [Docs]'s reference pages — plain `<a href>` navigation via [devHeader]
 * rather than an in-page Alpine tab switch.
 *
 * Both pages are driven by `void/dev.js` the same way [Hiscores] is driven by `void/hiscores.js`:
 * the mock dataset, live simulation and search/filter logic live in JS as an Alpine component
 * (`devDashboardApp`/`devPlayersApp`), and this file renders the static shell plus `x-for`
 * templates (via [rawHtml]) for anything that needs to react to that data — the world telemetry
 * charts, the player search results, and every per-player panel.
 */
object Dev {

    internal val pages = listOf(
        SitePage("dashboard", "Dashboard", "index.html"),
        SitePage("players", "Players", "players.html"),
    )

    private val worlds = listOf(
        WorldEntry(9, "Germany · Falkenstein", members = true, mode = "PvP", players = 812, capacity = 2000, ping = 38, status = WorldStatus.Online),
        WorldEntry(12, "United Kingdom · London", mode = "Normal", players = 1743, capacity = 2000, ping = 64, status = WorldStatus.Online),
        WorldEntry(18, "United States · Ashburn", mode = "Deadman", players = 1980, capacity = 2000, ping = 186, status = WorldStatus.Full),
        WorldEntry(30, "Germany · Falkenstein", mode = "Normal", players = 1622, capacity = 2000, ping = 37, status = WorldStatus.Restarting),
    )

    private data class ErrorEntry(val level: String, val tone: BadgeTone, val time: String, val text: String, val meta: String)

    private val errors = listOf(
        ErrorEntry("ERROR", BadgeTone.Danger, "14:21:58", "NullPointerException: PathFinder.step(Region.java:214)", "world 9 · 3 occurrences · thread game-tick"),
        ErrorEntry("WARN", BadgeTone.Warning, "14:19:12", "Tick overrun 241 ms — npc respawn batch exceeded budget", "world 9 · thread game-tick"),
        ErrorEntry("ERROR", BadgeTone.Danger, "13:58:03", "SQLTransientConnectionException: pool timeout after 5,000 ms", "persistence · retry succeeded"),
        ErrorEntry("WARN", BadgeTone.Warning, "13:41:37", "Login queue depth 84 — throttling to 12 logins/s", "world 9 · login-service"),
        ErrorEntry("ERROR", BadgeTone.Danger, "12:07:44", "IndexOutOfBoundsException: ItemContainer.set(Container.java:88)", "world 18 · 1 occurrence"),
    )

    private val runtime = listOf(
        "Version" to "0.41.2 · revision 231",
        "Commit" to "9f1c3ad",
        "JVM" to "OpenJDK 21.0.3",
        "Worlds hosted" to "2 (9, 30)",
        "Connections" to "1,284 / 4,000",
        "GC pauses (1h)" to "212, avg 9 ms",
    )

    /** Drops the raw [html] straight into the page — see the class doc for why. */
    private fun HTMLTag.rawHtml(html: String) {
        unsafe { raw(html.trimIndent()) }
    }

    /** A single-quoted JS string literal for an [openError]-style inline `@click` call. */
    private fun jsString(text: String): String = "'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'"

    private fun FlowContent.fact(label: String, valueExpr: String, accent: Boolean = false) {
        div {
            style = "display:flex;flex-direction:column;gap:6px"
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                    "text-transform:uppercase;color:var(--text-faint)"
                +label
            }
            span {
                style = "font:var(--type-code);font-size:var(--text-lg);" +
                    "color:${if (accent) "var(--feedback-warning)" else "var(--parch-100)"}"
                xText(valueExpr)
            }
        }
    }

    private fun FlowContent.keyValueRow(key: String, value: String, last: Boolean = false) {
        div {
            style = "display:flex;align-items:baseline;justify-content:space-between;gap:var(--space-5);" +
                "padding:var(--space-4) var(--space-6);" +
                (if (last) "" else "border-bottom:1px solid var(--border-subtle);")
            span {
                style = "font:var(--weight-semibold) var(--text-2xs)/1.2 var(--font-ui);" +
                    "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                +key
            }
            span {
                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-100);text-align:right"
                +value
            }
        }
    }

    /** A narrow world-list row for the dashboard's 300px sidebar — [ui.worldTable]'s columns need
     *  far more width than that, so this drops mode/ping down into a caption line under the region. */
    private fun FlowContent.compactWorldRow(world: WorldEntry, last: Boolean = false) {
        div {
            style = "display:flex;align-items:center;gap:var(--space-4);padding:var(--space-4) var(--space-6);" +
                (if (last) "" else "border-bottom:1px solid var(--border-subtle);")
            span {
                style = "font:var(--weight-bold) var(--text-base)/1 var(--font-display);color:var(--parch-100);min-width:20px"
                +world.number.toString()
            }
            div {
                style = "flex:1;min-width:0;display:flex;flex-direction:column;gap:2px"
                span {
                    style = "font:var(--type-body-sm);color:var(--text-body);white-space:nowrap;" +
                        "overflow:hidden;text-overflow:ellipsis"
                    +world.region
                }
                span {
                    style = "font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)"
                    +"${world.mode} · ${"%,d".format(world.players)}${world.ping?.let { " · ${it}ms" } ?: ""}"
                }
            }
            ui.badge(world.status.label, tone = world.status.tone, dot = world.status.dot)
        }
    }

    private fun DIV.kpiCard(label: String, value: String, sub: String, valueExpr: String? = null) {
        div {
            style = "background:var(--surface-panel);border:1px solid var(--border-panel);" +
                "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-xs);padding:var(--space-6)"
            div {
                style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                    "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                +label.uppercase()
            }
            div {
                style = "font:var(--weight-bold) var(--text-2xl)/1 var(--font-mono);" +
                    "color:var(--parch-50);margin-top:12px"
                if (valueExpr != null) {
                    xText(valueExpr)
                }
                +value
            }
            div {
                style = "font:var(--type-body-sm);color:var(--text-faint);margin-top:6px"
                +sub
            }
        }
    }

    fun dashboardPage(): String = voidPage(
        title = "Void — developer panel",
        description = "Live world telemetry, error console and staff tools for Void administrators.",
        assetPrefix = "../",
        data = "devDashboardApp()",
        head = { script(src = "../void/dev.js") {} },
    ) {
        ui.devHeader(pages, active = "dashboard", assetPrefix = "../")

        main {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7);" +
                "display:flex;flex-direction:column;gap:var(--space-8);flex:1;width:100%"
            attributes["x-bind:style"] = "{ paddingBottom: consoleOpen ? '312px' : '60px' }"

            div {
                style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:var(--space-6)"
                kpiCard("Players online", "11,853", "+212 in the last hour", valueExpr = "popNow")
                kpiCard("Uptime", "10d 22h", "since last restart")
                kpiCard("Tick duration", "38 ms", "avg 90s · 600 ms budget", valueExpr = "tickNow")
                kpiCard("CPU load", "34%", "8 cores · 2 worlds hosted", valueExpr = "cpuNow + '%'")
                kpiCard("Heap in use", "6.1 / 12.0 GB", "young gc 8 ms", valueExpr = "heapNow + ' / 12.0 GB'")
            }

            div {
                style = "display:grid;grid-template-columns:minmax(0,1fr) 300px;gap:var(--space-8);align-items:start"

                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-8);min-width:0"

                    ui.panel(title = "CPU & memory", meta = "last 90s") {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        div {
                            style = "display:flex;gap:var(--space-7);flex-wrap:wrap"
                            div {
                                style = "display:flex;align-items:center;gap:8px"
                                span { style = "width:10px;height:2px;background:var(--gold-400)" }
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                                    +"CPU"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--gold-300)"
                                    xText("cpuNow + '%'")
                                }
                            }
                            div {
                                style = "display:flex;align-items:center;gap:8px"
                                span { style = "width:10px;height:2px;background:var(--steel-500)" }
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                                    +"HEAP"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--steel-500)"
                                    xText("heapNow + ' GB'")
                                }
                            }
                            div {
                                style = "display:flex;align-items:center;gap:8px;margin-left:auto"
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-faint)"
                                    +"RSS"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--text-muted)"
                                    xText("rssNow + ' GB'")
                                }
                            }
                        }
                        div {
                            style = "position:relative;height:260px;background:var(--surface-inset);" +
                                "border:1px solid var(--border-subtle);box-shadow:var(--bevel-down);" +
                                "border-radius:var(--radius-xs);padding:1px"
                            rawHtml(
                                """
                                <svg viewBox="0 0 300 100" preserveAspectRatio="none" style="width:100%;height:100%;display:block">
                                  <line x1="0" y1="25" x2="300" y2="25" stroke="var(--umber-700)" stroke-width="1" vector-effect="non-scaling-stroke"></line>
                                  <line x1="0" y1="50" x2="300" y2="50" stroke="var(--umber-700)" stroke-width="1" vector-effect="non-scaling-stroke"></line>
                                  <line x1="0" y1="75" x2="300" y2="75" stroke="var(--umber-700)" stroke-width="1" vector-effect="non-scaling-stroke"></line>
                                  <path :d="heapArea" fill="rgba(125,157,176,.16)" stroke="none"></path>
                                  <path :d="heapPath" fill="none" stroke="var(--steel-500)" stroke-width="1.5" vector-effect="non-scaling-stroke"></path>
                                  <path :d="cpuPath" fill="none" stroke="var(--gold-400)" stroke-width="1.75" vector-effect="non-scaling-stroke"></path>
                                </svg>
                                """,
                            )
                            span {
                                style = "position:absolute;top:4px;left:8px;font:var(--type-code);" +
                                    "font-size:var(--text-3xs);color:var(--text-faint)"
                                +"100% · 12.0 GB"
                            }
                        }
                    }

                    ui.panel(title = "Tick duration", meta = "600 ms budget") {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        div {
                            style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(84px,1fr));gap:var(--space-5)"
                            fact("Current", "tickNow")
                            fact("Avg 90s", "tickAvg")
                            fact("P95", "tickP95")
                            fact("Max", "tickMax", accent = true)
                            fact("Overruns", "tickOverruns")
                        }
                        div {
                            style = "position:relative;height:190px;background:var(--surface-inset);" +
                                "border:1px solid var(--border-subtle);box-shadow:var(--bevel-down);" +
                                "border-radius:var(--radius-xs);padding:1px"
                            rawHtml(
                                """
                                <svg viewBox="0 0 300 100" preserveAspectRatio="none" style="width:100%;height:100%;display:block">
                                  <line x1="0" y1="33.3" x2="300" y2="33.3" stroke="var(--umber-700)" stroke-width="1" vector-effect="non-scaling-stroke"></line>
                                  <line x1="0" y1="66.7" x2="300" y2="66.7" stroke="var(--amber-900)" stroke-width="1" stroke-dasharray="3 3" vector-effect="non-scaling-stroke"></line>
                                  <path :d="tickArea" fill="rgba(224,174,60,.12)" stroke="none"></path>
                                  <path :d="tickPath" fill="none" stroke="var(--gold-300)" stroke-width="1.6" vector-effect="non-scaling-stroke"></path>
                                </svg>
                                """,
                            )
                            span {
                                style = "position:absolute;top:4px;left:8px;font:var(--type-code);" +
                                    "font-size:var(--text-3xs);color:var(--text-faint)"
                                +"300 ms"
                            }
                            span {
                                style = "position:absolute;top:calc(67% - 12px);left:8px;font:var(--type-code);" +
                                    "font-size:var(--text-3xs);color:var(--feedback-warning)"
                                +"100 ms warn"
                            }
                        }
                    }

                    ui.panel(title = "Population & logins", meta = "world 9") {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        div {
                            style = "display:flex;gap:var(--space-7);flex-wrap:wrap"
                            div {
                                style = "display:flex;align-items:center;gap:8px"
                                span { style = "width:10px;height:2px;background:var(--moss-500)" }
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                                    +"ONLINE"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--moss-500)"
                                    xText("popNow")
                                }
                            }
                            div {
                                style = "display:flex;align-items:center;gap:8px"
                                span { style = "width:10px;height:8px;background:var(--umber-400)" }
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                                    +"LOGINS / MIN"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--parch-100)"
                                    xText("loginsNow")
                                }
                            }
                            div {
                                style = "display:flex;align-items:center;gap:8px;margin-left:auto"
                                span {
                                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-faint)"
                                    +"PEAK TODAY"
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-sm);color:var(--text-muted)"
                                    xText("popPeak")
                                }
                            }
                        }
                        div {
                            style = "position:relative;height:200px;background:var(--surface-inset);" +
                                "border:1px solid var(--border-subtle);box-shadow:var(--bevel-down);" +
                                "border-radius:var(--radius-xs);padding:1px"
                            rawHtml(
                                """
                                <svg viewBox="0 0 300 100" preserveAspectRatio="none" style="width:100%;height:100%;display:block">
                                  <line x1="0" y1="50" x2="300" y2="50" stroke="var(--umber-700)" stroke-width="1" vector-effect="non-scaling-stroke"></line>
                                  <path :d="loginBarsPath" fill="var(--umber-400)"></path>
                                  <path :d="popArea" fill="rgba(127,174,79,.14)" stroke="none"></path>
                                  <path :d="popPath" fill="none" stroke="var(--moss-500)" stroke-width="1.75" vector-effect="non-scaling-stroke"></path>
                                </svg>
                                """,
                            )
                        }
                    }
                }

                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-8);min-width:0"

                    ui.panel(title = "World list", padded = false) {
                        for ((index, world) in worlds.withIndex()) {
                            compactWorldRow(world, last = index == worlds.lastIndex)
                        }
                    }

                    ui.panel(title = "Runtime", padded = false) {
                        for ((index, entry) in runtime.withIndex()) {
                            keyValueRow(entry.first, entry.second, last = index == runtime.lastIndex)
                        }
                    }

                    ui.panel(title = "Recent errors", meta = "24h", padded = false) {
                        for ((index, error) in errors.withIndex()) {
                            div {
                                val border = if (index == errors.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                                style = "display:flex;flex-direction:column;gap:4px;padding:var(--space-5) var(--space-6);" +
                                    "cursor:pointer;$border"
                                onClick(
                                    "openError(${jsString(error.level)}, ${jsString(error.tone.name)}, " +
                                        "${jsString(error.time)}, ${jsString(error.text)}, ${jsString(error.meta)})",
                                )
                                div {
                                    style = "display:flex;align-items:center;gap:8px"
                                    ui.badge(error.level, tone = error.tone)
                                    span {
                                        style = "font:var(--type-code);font-size:var(--text-3xs);" +
                                            "color:var(--text-faint);margin-left:auto"
                                        +error.time
                                    }
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);" +
                                        "color:var(--parch-100);word-break:break-word"
                                    +error.text
                                }
                                span {
                                    style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)"
                                    +error.meta
                                }
                            }
                        }
                    }
                }
            }
        }

        ui.dialog(
            model = "errorOpen",
            title = "Error detail",
            width = 560,
            dialogFooter = {
                ui.button("Close", variant = ButtonVariant.Ghost, onClick = "errorOpen = false")
                ui.button("Copy", variant = ButtonVariant.Secondary, onClick = "copyError()", textExpr = "copied ? 'Copied' : 'Copy'")
            },
        ) {
            div {
                style = "display:flex;align-items:center;gap:10px"
                rawHtml(
                    """
                    <span :style="{ background: DEV_BADGE_TONE[errorSel.tone].bg, color: DEV_BADGE_TONE[errorSel.tone].fg, borderColor: DEV_BADGE_TONE[errorSel.tone].bd }" style="display:inline-flex;align-items:center;padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="errorSel.level"></span>
                    <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="errorSel.time"></span>
                    """,
                )
            }
            div {
                style = "background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                    "box-shadow:var(--bevel-down);border-radius:var(--radius-xs);padding:var(--space-5)"
                rawHtml(
                    """
                    <pre style="margin:0;white-space:pre-wrap;word-break:break-word;font:var(--type-code);font-size:var(--text-xs);color:var(--parch-100)" x-text="errorSel.text"></pre>
                    """,
                )
            }
            span {
                style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)"
                xText("errorSel.meta")
            }
        }

        section {
            style = "position:fixed;left:0;right:0;bottom:0;background:var(--surface-inset);" +
                "border-top:1px solid var(--border-gold);box-shadow:var(--shadow-md);z-index:40"
            div {
                style = "display:flex;align-items:center;gap:var(--space-6);height:36px;padding:0 var(--space-7)"
                span {
                    style = "font:var(--weight-semibold) var(--text-2xs)/1 var(--font-display);" +
                        "letter-spacing:var(--tracking-caps);color:var(--gold-300)"
                    +"CONSOLE"
                }
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                    +"world 9"
                }
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);margin-left:auto"
                    xText("log.length + ' lines'")
                }
                ui.button("Clear", variant = ButtonVariant.Ghost, size = ButtonSize.Small, onClick = "clearLog()")
                ui.button("Toggle", variant = ButtonVariant.Ghost, size = ButtonSize.Small, onClick = "toggleConsole()")
            }
            div {
                xShow("consoleOpen")
                style = "border-top:1px solid var(--border-subtle)"
                div {
                    attributes["x-ref"] = "logEl"
                    style = "height:184px;overflow:auto;padding:var(--space-5) var(--space-7);" +
                        "display:flex;flex-direction:column;gap:3px;background:var(--umber-950)"
                    rawHtml(
                        """
                        <template x-for="(l, i) in log" :key="i">
                          <div style="display:flex;gap:var(--space-5);align-items:baseline">
                            <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--umber-300);flex:0 0 auto" x-text="l.time"></span>
                            <span :style="{ color: l.level === 'ok' ? 'var(--feedback-success)' : (l.level === 'warn' ? 'var(--feedback-warning)' : 'var(--text-muted)') }" style="font:var(--type-code);font-size:var(--text-xs);white-space:pre-wrap;word-break:break-word" x-text="l.text"></span>
                          </div>
                        </template>
                        """,
                    )
                }
                div {
                    style = "display:flex;align-items:center;gap:var(--space-5);padding:var(--space-5) var(--space-7);" +
                        "border-top:1px solid var(--border-subtle);background:var(--surface-inset)"
                    span {
                        style = "font:var(--type-code);font-size:var(--text-sm);color:var(--gold-400)"
                        +"›"
                    }
                    input {
                        xModel("cmd")
                        attributes["@keydown"] = "onCmdKey(${'$'}event)"
                        placeholder = "type a command — help lists them"
                        style = "flex:1;min-width:0;height:32px;padding:0 10px;background:var(--umber-950);" +
                            "color:var(--parch-50);border:1px solid var(--border-strong);border-radius:var(--radius-xs);" +
                            "box-shadow:var(--bevel-down);font:var(--type-code);font-size:var(--text-xs);outline:none"
                    }
                    ui.button("Run", size = ButtonSize.Small, onClick = "runCmd()")
                }
            }
        }
    }

    fun playersPage(): String = voidPage(
        title = "Void — player management",
        description = "Look up any account, inspect its live state and take moderation actions.",
        assetPrefix = "../",
        data = "devPlayersApp()",
        head = { script(src = "../void/dev.js") {} },
    ) {
        ui.devHeader(pages, active = "players", liveModel = null, assetPrefix = "../")

        main {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                "display:flex;flex-wrap:wrap;gap:var(--space-8);align-items:flex-start"

            div {
                style = "flex:0 1 280px;min-width:240px;display:flex;flex-direction:column;gap:var(--space-6)"
                attributes["@keydown.enter"] = "searchEnter()"
                ui.textInput(
                    "dev-player-search", "Look up player", model = "query",
                    hint = "Press enter to jump straight to the top match.",
                    placeholder = "name, account id or IP", mono = true, icon = Icons.SEARCH,
                )
                ui.panel(
                    title = "Results",
                    padded = false,
                    action = { span { style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"; xText("filtered.length + ' matches'") } },
                ) {
                    rawHtml(
                        """
                        <template x-for="p in resultRows" :key="p.id">
                          <div @click="select(p.id)" :style="{ background: p.selected ? 'var(--surface-active)' : 'var(--surface-panel)', borderLeftColor: p.selected ? 'var(--gold-400)' : 'transparent' }" style="display:flex;flex-direction:column;gap:4px;padding:var(--space-5) var(--space-6);border-bottom:1px solid var(--border-subtle);border-left:2px solid transparent;cursor:pointer">
                            <div style="display:flex;align-items:center;justify-content:space-between;gap:8px">
                              <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-50)" x-text="p.name"></span>
                              <span :style="{ background: DEV_BADGE_TONE[p.tone].bg, color: DEV_BADGE_TONE[p.tone].fg, borderColor: DEV_BADGE_TONE[p.tone].bd }" style="display:inline-flex;align-items:center;gap:6px;padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase">
                                <span style="width:5px;height:5px;border-radius:50%;background:currentColor"></span><span x-text="p.state"></span>
                              </span>
                            </div>
                            <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="p.meta"></span>
                          </div>
                        </template>
                        <div x-show="resultRows.length === 0" style="padding:var(--space-7) var(--space-6);font:var(--type-body-sm);color:var(--text-faint)">No account matches that query.</div>
                        """,
                    )
                }
            }

            div {
                style = "flex:1 1 480px;min-width:0;display:flex;flex-direction:column;gap:var(--space-8)"

                div {
                    style = "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-7);flex-wrap:wrap"
                    div {
                        style = "display:flex;flex-direction:column;gap:8px;min-width:0"
                        h1 {
                            style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                            xText("player.name")
                            +"Power Spark"
                        }
                        div {
                            style = "display:flex;align-items:center;gap:var(--space-4);flex-wrap:wrap"
                            rawHtml(
                                """
                                <span :style="{ background: DEV_BADGE_TONE[player.tone].bg, color: DEV_BADGE_TONE[player.tone].fg, borderColor: DEV_BADGE_TONE[player.tone].bd }" style="display:inline-flex;align-items:center;gap:6px;padding:0 10px;height:20px;border:1px solid;border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase">
                                  <span style="width:5px;height:5px;border-radius:50%;background:currentColor"></span><span x-text="player.state"></span>
                                </span>
                                <span style="display:inline-flex;align-items:center;padding:0 10px;height:20px;background:rgba(224,174,60,.14);color:var(--gold-300);border:1px solid var(--gold-600);border-radius:var(--radius-pill);font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="player.rank"></span>
                                """,
                            )
                            span {
                                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                                xText("'Account #' + player.account + ' · world ' + player.world")
                            }
                        }
                    }
                    div {
                        style = "display:flex;gap:var(--space-4);flex-wrap:wrap"
                        ui.button("Copy debug dump", variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                        ui.button("Kick", variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                        ui.button("Follow in-world", size = ButtonSize.Small)
                    }
                }

                ui.tabs(
                    model = "ptab",
                    items = listOf(
                        TabItem("skills", "Skills"), TabItem("gear", "Gear"), TabItem("variables", "Variables"),
                        TabItem("activity", "Activity"), TabItem("chat", "Chat"), TabItem("moderation", "Moderation"),
                    ),
                )

                ui.tabPanel("ptab", "skills") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Combat snapshot", meta = "live") {
                        style = "display:flex;flex-direction:column;gap:var(--space-6)"
                        div {
                            style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:var(--space-5)"
                            rawHtml(statBarTemplate("player.combatSkills"))
                        }
                        div {
                            style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(160px,1fr));gap:var(--space-6);" +
                                "padding-top:var(--space-6);border-top:1px solid var(--border-subtle)"
                            rawHtml(
                                """
                                <template x-for="f in player.combatFacts" :key="f.k">
                                  <div style="display:flex;flex-direction:column;gap:4px">
                                    <span style="font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);color:var(--text-faint)" x-text="f.k"></span>
                                    <span style="font:var(--type-code);font-size:var(--text-sm);color:var(--parch-100)" x-text="f.v"></span>
                                  </div>
                                </template>
                                """,
                            )
                        }
                    }
                    ui.panel(title = "Skills") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:var(--space-4)"
                        rawHtml(statBarTemplate("player.skills", big = false, showRank = true))
                    }
                }

                ui.tabPanel("ptab", "gear") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Equipment") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:var(--space-5)"
                        rawHtml(
                            """
                            <template x-for="e in player.equipment" :key="e.slot">
                              <div style="display:flex;align-items:center;gap:var(--space-5)">
                                <div style="width:36px;height:36px;flex:0 0 auto;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)"></div>
                                <div style="display:flex;flex-direction:column;gap:3px;min-width:0">
                                  <span style="font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);color:var(--text-faint)" x-text="e.slot.toUpperCase()"></span>
                                  <span style="font:var(--type-body-sm);color:var(--parch-100)" x-text="e.item"></span>
                                </div>
                              </div>
                            </template>
                            """,
                        )
                    }
                    ui.panel(title = "Inventory", action = { span { style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"; xText("inventoryUsed + ' / 28'") } }) {
                        style = "display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:var(--space-3);max-width:420px"
                        rawHtml(
                            """
                            <template x-for="(i, idx) in player.inventory" :key="idx">
                              <div style="position:relative;width:36px;height:36px;background:var(--surface-inset);border:1px solid var(--border-strong);border-radius:var(--radius-xs);box-shadow:var(--bevel-down)">
                                <span x-show="!i.empty" style="position:absolute;top:1px;left:3px;font:var(--weight-bold) var(--text-3xs)/1 var(--font-ui);color:var(--gold-200);text-shadow:0 1px 0 rgba(0,0,0,.9)" x-text="i.qty"></span>
                              </div>
                            </template>
                            """,
                        )
                    }
                    ui.panel(
                        title = "Bank",
                        padded = false,
                        action = { span { style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"; xText("filteredBank.length + ' items'") } },
                    ) {
                        div {
                            style = "padding:var(--space-6);border-bottom:1px solid var(--border-subtle);background:var(--surface-inset)"
                            ui.textInput("dev-bank-filter", "Filter items", model = "bankFilter", placeholder = "item name…", mono = true, icon = Icons.SEARCH)
                        }
                        rawHtml(
                            """
                            <template x-for="(b, idx) in filteredBank" :key="idx">
                              <div style="display:grid;grid-template-columns:1fr 120px 110px;align-items:center;gap:var(--space-5);padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--border-subtle)">
                                <span style="font:var(--type-body-sm);color:var(--parch-100)" x-text="b.item"></span>
                                <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--parch-200);text-align:right" x-text="b.qty"></span>
                                <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--text-faint);text-align:right" x-text="b.value"></span>
                              </div>
                            </template>
                            <div x-show="filteredBank.length === 0" style="padding:var(--space-7) var(--space-6);font:var(--type-body-sm);color:var(--text-faint)">No bank items match that filter.</div>
                            """,
                        )
                    }
                }

                ui.tabPanel("ptab", "variables") {
                    ui.panel(
                        title = "Variables",
                        padded = false,
                        action = { span { style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"; xText("filteredVariables.length + ' shown'") } },
                    ) {
                        div {
                            style = "display:flex;gap:var(--space-5);align-items:flex-end;flex-wrap:wrap;" +
                                "padding:var(--space-6);border-bottom:1px solid var(--border-subtle);background:var(--surface-inset)"
                            ui.textInput("dev-var-filter", "Filter by name", model = "varFilter", placeholder = "varbit, slayer, config…", mono = true)
                            ui.select(
                                "dev-var-scope", "Scope", model = "varScope",
                                options = listOf(
                                    "All scopes" to "All scopes", "Account" to "Account",
                                    "Session" to "Session", "World" to "World",
                                ),
                            )
                            ui.button("Reset", variant = ButtonVariant.Secondary, size = ButtonSize.Small, onClick = "resetVarFilter()")
                        }
                        div {
                            style = "display:grid;grid-template-columns:minmax(0,2fr) 110px 96px 130px;gap:var(--space-5);" +
                                "padding:var(--space-4) var(--space-6);background:var(--surface-header);" +
                                "border-bottom:1px solid var(--border-gold)"
                            for (label in listOf("KEY", "VALUE", "TYPE", "UPDATED")) {
                                span {
                                    style = "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--gold-300)"
                                    +label
                                }
                            }
                        }
                        rawHtml(
                            """
                            <template x-for="v in filteredVariables" :key="v.key">
                              <div style="display:grid;grid-template-columns:minmax(0,2fr) 110px 96px 130px;gap:var(--space-5);align-items:center;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--border-subtle)">
                                <div style="display:flex;flex-direction:column;gap:3px;min-width:0">
                                  <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--parch-50);word-break:break-all" x-text="v.key"></span>
                                  <span style="font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)" x-text="v.label"></span>
                                </div>
                                <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300);text-align:right" x-text="v.value"></span>
                                <span style="font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);color:var(--text-muted)" x-text="v.type"></span>
                                <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);text-align:right" x-text="v.updated"></span>
                              </div>
                            </template>
                            <div x-show="filteredVariables.length === 0" style="padding:var(--space-7) var(--space-6);font:var(--type-body-sm);color:var(--text-faint)">No variable matches that filter.</div>
                            """,
                        )
                    }
                }

                ui.tabPanel("ptab", "activity") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Action history", meta = "session", padded = false) {
                        rawHtml(
                            """
                            <template x-for="(a, idx) in player.activityLog" :key="idx">
                              <div x-data="{ open: false }" style="border-bottom:1px solid var(--border-subtle)">
                                <div @click="a.expand && (open = !open)" :style="{ cursor: a.expand ? 'pointer' : 'default' }" style="display:grid;grid-template-columns:76px 1fr 18px;gap:var(--space-5);align-items:start;padding:var(--space-5) var(--space-6)">
                                  <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="a.time"></span>
                                  <div style="display:flex;flex-direction:column;gap:3px">
                                    <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-100)" x-text="a.action"></span>
                                    <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)" x-text="a.detail"></span>
                                  </div>
                                  <span x-show="a.expand" :style="{ transform: open ? 'rotate(90deg)' : 'rotate(0deg)' }" style="font:var(--text-xs) var(--font-ui);color:var(--text-faint);line-height:1.4;transition:transform var(--dur-fast) var(--ease-standard)">›</span>
                                </div>
                                <div x-show="open && a.expand" style="padding:0 var(--space-6) var(--space-5) 92px;background:var(--surface-inset)">
                                  <div style="display:flex;flex-direction:column;gap:4px">
                                    <template x-for="(line, li) in (a.expand || [])" :key="li">
                                      <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="line"></span>
                                    </template>
                                  </div>
                                </div>
                              </div>
                            </template>
                            """,
                        )
                    }
                    ui.panel(title = "Staff audit", meta = "this account", padded = false) {
                        rawHtml(
                            """
                            <template x-for="(a, idx) in player.audit" :key="idx">
                              <div style="display:grid;grid-template-columns:1fr 120px 150px;align-items:center;gap:var(--space-5);padding:var(--space-5) var(--space-6);border-bottom:1px solid var(--border-subtle)">
                                <div style="display:flex;flex-direction:column;gap:3px;min-width:0">
                                  <span style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-100)" x-text="a.action"></span>
                                  <span style="font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)" x-text="a.reason"></span>
                                </div>
                                <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300)" x-text="a.staff"></span>
                                <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);text-align:right" x-text="a.time"></span>
                              </div>
                            </template>
                            """,
                        )
                    }
                }

                ui.tabPanel("ptab", "chat") {
                    ui.panel(title = "Chat log", meta = "last 30 min", padded = false) {
                        rawHtml(
                            """
                            <template x-for="(c, idx) in player.chat" :key="idx">
                              <div style="display:grid;grid-template-columns:70px 84px 1fr;gap:var(--space-5);align-items:baseline;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--border-subtle)">
                                <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="c.time"></span>
                                <span :style="{ color: c.tint }" style="font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase" x-text="c.channel"></span>
                                <span style="font:var(--type-body-sm);color:var(--parch-100)" x-text="c.text"></span>
                              </div>
                            </template>
                            <div x-show="player.chat.length === 0" style="padding:var(--space-7) var(--space-6);font:var(--type-body-sm);color:var(--text-faint)">No recent chat for this account.</div>
                            """,
                        )
                    }
                }

                ui.tabPanel("ptab", "moderation") {
                    ui.panel(title = "Moderation", meta = "no active flags") {
                        style = "display:flex;flex-direction:column;gap:var(--space-6)"
                        div {
                            style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:var(--space-6)"
                            ui.select(
                                "dev-mod-reason", "Reason", model = "modReason",
                                options = listOf(
                                    "Offensive language" to "Offensive language", "Botting / macroing" to "Botting / macroing",
                                    "Scamming" to "Scamming", "Other" to "Other",
                                ),
                            )
                            ui.select(
                                "dev-mod-duration", "Duration", model = "modDuration",
                                options = listOf("24 hours" to "24 hours", "48 hours" to "48 hours", "7 days" to "7 days", "Permanent" to "Permanent"),
                            )
                            ui.textInput("dev-mod-note", "Staff note", model = "modNote", placeholder = "visible to staff only")
                        }
                        div {
                            style = "display:flex;gap:var(--space-4);flex-wrap:wrap;padding-top:var(--space-6);" +
                                "border-top:1px solid var(--border-subtle)"
                            ui.button("Mute", variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                            ui.button("Jail", variant = ButtonVariant.Secondary, size = ButtonSize.Small)
                            ui.button("Ban", variant = ButtonVariant.Danger, size = ButtonSize.Small)
                        }
                    }
                }
            }

            div {
                style = "flex:0 1 300px;min-width:260px;display:flex;flex-direction:column;gap:var(--space-8)"

                ui.panel(title = "Location", meta = "live", padded = false) {
                    rawHtml(
                        """
                        <template x-for="(r, idx) in locationRows" :key="r.k">
                          <div style="display:flex;align-items:baseline;justify-content:space-between;gap:var(--space-5);padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--border-subtle)">
                            <span style="font:var(--weight-semibold) var(--text-2xs)/1.2 var(--font-ui);letter-spacing:var(--tracking-caps);color:var(--text-muted)" x-text="r.k"></span>
                            <span style="font:var(--type-code);font-size:var(--text-xs);color:var(--parch-100);text-align:right" x-text="r.v"></span>
                          </div>
                        </template>
                        """,
                    )
                }

                ui.panel(title = "Current activity") {
                    style = "display:flex;flex-direction:column;gap:var(--space-5)"
                    span {
                        style = "font:var(--weight-semibold) var(--text-lg)/1.2 var(--font-ui);color:var(--parch-50)"
                        xText("player.activity")
                    }
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-muted)"
                        xText("player.detail")
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:6px;margin-top:var(--space-3)"
                        div {
                            style = "display:flex;justify-content:space-between;font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                "letter-spacing:var(--tracking-caps);color:var(--text-faint)"
                            span { +"SESSION XP RATE" }
                            span {
                                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300);letter-spacing:0"
                                xText("player.xpRate")
                            }
                        }
                        div {
                            style = "height:8px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                                "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden"
                            div {
                                attributes["x-bind:style"] = "{ width: player.xpPct + '%' }"
                                style = "height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"
                            }
                        }
                    }
                }

                ui.panel(title = "Teleport") {
                    style = "display:flex;flex-direction:column;gap:var(--space-6)"
                    div {
                        style = "display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-4)"
                        ui.textInput("dev-tp-x", "X", model = "tpX", mono = true)
                        ui.textInput("dev-tp-y", "Y", model = "tpY", mono = true)
                        ui.textInput("dev-tp-z", "Plane", model = "tpZ", mono = true)
                    }
                    div {
                        style = "display:flex;gap:var(--space-4);flex-wrap:wrap"
                        ui.button("Teleport", size = ButtonSize.Small)
                        ui.button("Send home", variant = ButtonVariant.Ghost, size = ButtonSize.Small, onClick = "syncTeleport()")
                    }
                }
            }
        }
    }

    /** A [listExpr] of `{name, level, rank}` objects rendered as the same meter row `ui.statBar`
     *  draws, plus the skill's icon from `void/images/skills/` (matched by lowercased name — see
     *  [Hiscores]'s `skillIcon`, which the same sprite set backs). [big] shows the oversized level
     *  number used for a small highlight set (e.g. combat snapshot); the full skill list turns it
     *  off in favour of [showRank], which appends the account's hiscore rank for that skill —
     *  showing both at full size crowds a 24-row grid, so the two are mutually exclusive in practice. */
    private fun statBarTemplate(listExpr: String, big: Boolean = true, showRank: Boolean = false): String {
        val bigNumber = """<span style="font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--gold-300);min-width:34px;text-align:right" x-text="s.level"></span>""".takeIf { big }.orEmpty()
        val rankLine = """
            <span style="font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)">Rank <span x-text="s.rank.toLocaleString()"></span></span>
        """.takeIf { showRank }.orEmpty()
        return """
            <template x-for="s in $listExpr" :key="s.name">
              <div style="display:flex;align-items:center;gap:var(--space-5);padding:var(--space-4) var(--space-5);background:var(--surface-panel-raised);border:1px solid var(--border-panel);border-radius:var(--radius-sm);box-shadow:var(--bevel-up)">
                <span style="width:20px;height:20px;flex:none;display:flex;align-items:center;justify-content:center">
                  <img :src="'../void/images/skills/' + s.name.toLowerCase() + '.png'" alt="" style="max-width:100%;max-height:100%;width:auto;height:auto;display:block">
                </span>
                <div style="flex:1;min-width:0;display:flex;flex-direction:column;gap:5px">
                  <div style="display:flex;justify-content:space-between;gap:10px;align-items:baseline">
                    <span style="font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-200);white-space:nowrap;overflow:hidden;text-overflow:ellipsis" x-text="s.name"></span>
                    <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);white-space:nowrap"><span x-text="s.level"></span> / 99</span>
                  </div>
                  <div style="height:6px;background:var(--surface-inset);border:1px solid var(--border-subtle);border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden">
                    <div :style="{ width: (s.level * 100 / 99) + '%' }" style="height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"></div>
                  </div>
                  $rankLine
                </div>
                $bigNumber
              </div>
            </template>
        """
    }
}
