package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The marketing site: Home, Docs, Worlds and Community, all behind the shared [siteHeader]/
 * [siteFooter] chrome. Each page is written to its own file by [Site] so links between them are
 * plain `<a href>`s rather than an in-page Alpine tab switch.
 */
object Website {

    internal val pages = listOf(
        SitePage("home", "Home", "index.html"),
        SitePage("docs", "Docs", "docs/index.html"),
        SitePage("worlds", "Worlds", "worlds.html"),
        SitePage("hiscores", "Hiscores", "hiscores.html"),
        SitePage("log", "Log", "log.html"),
        SitePage("community", "Community", "community.html"),
    )

    private val worlds = listOf(
        WorldEntry(9, "Germany · Falkenstein", members = true, mode = "PvP", players = 812, capacity = 2000, ping = 38, status = WorldStatus.Online),
        WorldEntry(12, "United Kingdom · London", mode = "Normal", players = 1743, capacity = 2000, ping = 64, status = WorldStatus.Online),
        WorldEntry(18, "United States · Ashburn", mode = "Deadman", players = 1980, capacity = 2000, ping = 186, status = WorldStatus.Full),
        WorldEntry(30, "Germany · Falkenstein", mode = "Normal", players = 1622, capacity = 2000, ping = 37, status = WorldStatus.Restarting),
    )

    private fun DIV.stat(value: String, label: String) {
        div {
            style = "display:flex;flex-direction:column;gap:4px"
            span {
                style = "font:var(--weight-bold) var(--text-2xl)/1 var(--font-display);color:var(--gold-300)"
                +value
            }
            span {
                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                    "text-transform:uppercase;color:var(--text-faint)"
                +label
            }
        }
    }

    private fun FlowContent.eyebrow(text: String) {
        span {
            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                "text-transform:uppercase;color:var(--gold-300)"
            +text
        }
    }

    fun homePage(): String = voidPage(
        title = "Void — modern mmo emulation",
        description = "Void is an open-source server emulator and client for the classic era.",
        data = "{ world: 9 }",
    ) {
        ui.siteHeader(pages, active = "home")

        section {
            style = "position:relative;min-height:520px;display:flex;align-items:flex-end;" +
                "border-bottom:1px solid var(--border-panel);overflow:hidden"
            img(src = "void/imagery/repository-bg.png", alt = "") {
                style = "position:absolute;inset:0;width:100%;height:100%;object-fit:cover"
            }
            div { style = "position:absolute;inset:0;background:var(--scrim-bottom)" }
            div {
                style = "position:relative;max-width:var(--container-wide);margin:0 auto;width:100%;" +
                    "padding:var(--space-12) var(--space-8) var(--space-11);display:flex;" +
                    "flex-direction:column;gap:var(--space-7)"
                div {
                    style = "display:flex;gap:var(--space-4)"
                    ui.badge("Revision 231", tone = BadgeTone.Gold)
                    ui.badge("42 worlds online", tone = BadgeTone.Success, dot = true)
                }
                h1 {
                    style = "margin:0;max-width:760px;font:var(--type-hero);color:var(--parch-50)"
                    +"Modern mmo emulation"
                }
                p {
                    style = "margin:0;max-width:620px;font:var(--weight-regular) var(--text-xl)/1.5 var(--font-ui);" +
                        "color:var(--parch-200)"
                    +("Void is an open-source server emulator and client for the classic era. Play on a " +
                        "community world, or clone the repository and run your own.")
                }
                div {
                    style = "display:flex;gap:var(--space-5);align-items:center;flex-wrap:wrap"
                    ui.button("Download the launcher", size = ButtonSize.Large, glow = true, icon = Icons.DOWNLOAD)
                    ui.button(
                        "Run your own world",
                        variant = ButtonVariant.Secondary,
                        size = ButtonSize.Large,
                        icon = Icons.TERMINAL,
                        onClick = "window.location = 'docs.html'",
                    )
                    span {
                        style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-300)"
                        +"Windows · macOS · Linux · JDK 21"
                    }
                }
            }
        }

        section {
            style = "background:var(--surface-inset);border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8);" +
                    "display:grid;grid-template-columns:repeat(4,1fr);gap:var(--space-8)"
                stat("42", "Worlds online")
                stat("11,853", "Players right now")
                stat("231", "Cache revision")
                stat("1,204", "Contributors")
            }
        }

        section {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                "display:flex;flex-direction:column;gap:var(--space-9)"
            header {
                style = "display:flex;flex-direction:column;gap:var(--space-5);max-width:640px"
                eyebrow("What it is")
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"An emulator, a client, and the tooling around them"
                }
            }
            div {
                style = "display:grid;grid-template-columns:repeat(3,1fr);gap:var(--space-6)"
                val features = listOf(
                    Triple(Icons.TERMINAL, "Server emulator", "A Kotlin/JVM world server. Deterministic ticks, scriptable content, a plugin API that survives updates."),
                    Triple(Icons.BOOK, "Protocol reference", "Every opcode for revision 231, documented and versioned alongside the code."),
                    Triple(Icons.USERS, "Community worlds", "Volunteer-hosted worlds in eight regions, with a public status page and restart schedule."),
                )
                for ((iconPath, title, body) in features) {
                    ui.panel {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        span {
                            style = "color:var(--gold-300)"
                            icon(iconPath, size = 22)
                        }
                        h3 {
                            style = "margin:0;font:var(--type-section);font-size:var(--text-lg);color:var(--parch-50)"
                            +title
                        }
                        p {
                            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                            +body
                        }
                        a(href = "#") {
                            style = "font:var(--type-body-sm)"
                            +"Read more →"
                        }
                    }
                }
            }
        }

        section {
            style = "background:var(--surface-inset);border-top:1px solid var(--border-panel);" +
                "border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                    "display:grid;grid-template-columns:1fr 1fr;gap:var(--space-11);align-items:center"
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-6)"
                    eyebrow("Run your own world")
                    h2 {
                        style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                        +"Three commands to a running world"
                    }
                    p {
                        style = "margin:0;font:var(--type-body);color:var(--text-muted)"
                        +("Clone the repository, drop in a cache, and run the Gradle task. The default config " +
                            "boots a single world on port 43594 with 2,000 NPC spawns.")
                    }
                    pre {
                        style = "margin:0;background:var(--umber-950);border:1px solid var(--border-subtle);" +
                            "border-radius:var(--radius-md);box-shadow:var(--bevel-down);padding:var(--space-6);" +
                            "font:var(--type-code);color:var(--parch-100);line-height:1.9;overflow-x:auto"
                        div {
                            span { style = "color:var(--gold-400)"; +"$ " }
                            +"git clone https://github.com/void/emulator.git"
                        }
                        div {
                            span { style = "color:var(--gold-400)"; +"$ " }
                            +"void cache pull --revision 231"
                        }
                        div {
                            span { style = "color:var(--gold-400)"; +"$ " }
                            +"./gradlew run"
                        }
                    }
                    div {
                        style = "display:flex;gap:var(--space-5)"
                        ui.button("Getting started", variant = ButtonVariant.Secondary, onClick = "window.location = 'docs.html'")
                        ui.button("Browse the source", variant = ButtonVariant.Ghost, icon = Icons.EXTERNAL)
                    }
                }
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-6)"
                    ui.panel(title = "World list", meta = "live", padded = false) {
                        ui.worldTable("world", worlds)
                    }
                    ui.panel(title = "Server health") {
                        style = "display:flex;flex-direction:column;gap:var(--space-4)"
                        ui.statBar("Fleet load", 11853, 84000)
                        ui.statBar("Uptime this month", 9971, 10000)
                    }
                }
            }
        }

        section {
            style = "position:relative;overflow:hidden;border-bottom:1px solid var(--border-panel)"
            img(src = "void/imagery/world-layers.jpg", alt = "") {
                style = "position:absolute;inset:0;width:100%;height:100%;object-fit:cover;object-position:50% 30%"
            }
            div {
                style = "position:absolute;inset:0;background:linear-gradient(90deg,rgba(23,18,13,.95) 0%," +
                    "rgba(23,18,13,.72) 55%,rgba(23,18,13,.4) 100%)"
            }
            div {
                style = "position:relative;max-width:var(--container-wide);margin:0 auto;" +
                    "padding:var(--space-12) var(--space-8);display:flex;flex-direction:column;gap:var(--space-6)"
                h2 {
                    style = "margin:0;max-width:560px;font:var(--type-title);color:var(--parch-50)"
                    +"The whole world, from the sky islands down"
                }
                p {
                    style = "margin:0;max-width:520px;font:var(--type-body);color:var(--parch-200)"
                    +("Every region, dungeon and instance from the era is in the cache and playable. " +
                        "Content scripts are open — write a quest, open a pull request.")
                }
                div {
                    ui.button("Join the community", onClick = "window.location = 'community.html'")
                }
            }
        }

        ui.siteFooter()
    }

    fun docsPage(): String = voidPage(
        title = "Void — docs",
        description = "Getting started, protocol reference, cache tooling and content scripting for Void.",
        data = "{ tab: 'guide' }",
    ) {
        ui.siteHeader(pages, active = "docs")

        div {
            style = "display:grid;grid-template-columns:240px minmax(0,1fr) 220px;flex:1;" +
                "max-width:var(--container-wide);margin:0 auto;width:100%"

            aside {
                style = "background:var(--surface-inset);border-right:1px solid var(--border-panel);" +
                    "padding:var(--space-8) var(--space-6);display:flex;flex-direction:column;gap:var(--space-8)"
                ui.textInput("void-doc-search", "Search", placeholder = "Search the docs", icon = Icons.BOOK)
                val groups = listOf(
                    "Getting started" to listOf("Overview", "Install the launcher", "Build from source", "Run a world"),
                    "Protocol" to listOf("Handshake", "Login block", "Opcode table", "Packet sizes"),
                    "Cache" to listOf("Layout", "Pulling a revision", "Repacking", "Sprites"),
                    "Content" to listOf("Plugin API", "Scripting quests", "NPC definitions"),
                )
                for ((group, items) in groups) {
                    nav {
                        style = "display:flex;flex-direction:column;gap:var(--space-3)"
                        span {
                            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:var(--gold-300);margin-bottom:4px"
                            +group
                        }
                        for (item in items) {
                            val on = item == "Run a world"
                            a(href = "#") {
                                val background = if (on) "var(--surface-active)" else "transparent"
                                val border = if (on) "var(--gold-400)" else "transparent"
                                val color = if (on) "var(--parch-50)" else "var(--text-muted)"
                                style = "text-align:left;padding:6px 10px;background:$background;" +
                                    "border-left:2px solid $border;border-radius:var(--radius-xs);" +
                                    "text-decoration:none;font:var(--type-body-sm);color:$color;display:block"
                                +item
                            }
                        }
                    }
                }
            }

            article {
                style = "padding:var(--space-10);min-width:0;max-width:var(--container-body)"
                div {
                    style = "display:flex;align-items:center;gap:8px;font:var(--type-label);" +
                        "letter-spacing:var(--tracking-wide);color:var(--text-faint);margin-bottom:var(--space-6)"
                    +"Docs"
                    icon(Icons.CHEVRON_RIGHT, size = 12)
                    +"Getting started"
                    icon(Icons.CHEVRON_RIGHT, size = 12)
                    span { style = "color:var(--parch-200)"; +"Run a world" }
                }
                h1 {
                    style = "margin:0 0 var(--space-6);font:var(--type-title);color:var(--parch-50)"
                    +"Run a world"
                }
                div {
                    style = "display:flex;gap:var(--space-4);margin-bottom:var(--space-8)"
                    ui.badge("Revision 231", pill = false)
                    ui.badge("Updated 4 Sept 2026", tone = BadgeTone.Gold)
                }
                ui.tabs(
                    model = "tab",
                    items = listOf(TabItem("guide", "Guide"), TabItem("config", "Configuration"), TabItem("api", "API")),
                )
                div {
                    style = "padding:var(--space-8) 0"
                    p {
                        style = "margin:0 0 var(--space-6);font:var(--type-body);color:var(--text-body)"
                        +("You need JDK 21 and a cache for revision 231. The launcher can boot a local checkout, " +
                            "but the server runs fine on its own — the Gradle task below starts a single world " +
                            "with default content.")
                    }
                    pre {
                        style = "margin:0 0 var(--space-8);background:var(--umber-950);" +
                            "border:1px solid var(--border-subtle);border-radius:var(--radius-md);" +
                            "box-shadow:var(--bevel-down);padding:var(--space-6);font:var(--type-code);" +
                            "color:var(--parch-100);line-height:1.9;overflow-x:auto"
                        span { style = "color:var(--gold-400)"; +"$ " }
                        +"""./gradlew run --args="--world 9 --port 43594""""
                    }
                    h2 {
                        style = "margin:0 0 var(--space-5);font:var(--type-section);color:var(--parch-50)"
                        +"Configuration"
                    }
                    p {
                        style = "margin:0 0 var(--space-6);font:var(--type-body);color:var(--text-body)"
                        +"Values below come from "
                        code {
                            style = "font:var(--type-code);color:var(--gold-300)"
                            +"config/world.toml"
                        }
                        +"."
                    }
                    ui.panel(padded = false) {
                        style = "margin-bottom:var(--space-8)"
                        div {
                            style = "display:grid;grid-template-columns:180px 120px minmax(0,1fr);" +
                                "gap:var(--space-5);padding:0 var(--space-6);height:32px;align-items:center;" +
                                "background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
                                "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:var(--gold-300)"
                            span { +"Key" }
                            span { +"Default" }
                            span { +"Description" }
                        }
                        val rows = listOf(
                            Triple("world.id", "9", "World number shown in the launcher list."),
                            Triple("net.port", "43594", "TCP port the world listens on."),
                            Triple("cache.path", "~/.void/cache", "Location of the unpacked cache."),
                            Triple("npc.spawns", "true", "Seed NPC spawns from the cache on boot."),
                        )
                        for ((index, row) in rows.withIndex()) {
                            val (key, default, description) = row
                            val background = if (index % 2 == 1) "var(--umber-850)" else "var(--surface-panel)"
                            div {
                                style = "display:grid;grid-template-columns:180px 120px minmax(0,1fr);" +
                                    "gap:var(--space-5);padding:var(--space-4) var(--space-6);" +
                                    "align-items:baseline;background:$background;" +
                                    "border-bottom:1px solid var(--umber-900)"
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300)"
                                    +key
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-200)"
                                    +default
                                }
                                span {
                                    style = "font:var(--type-body-sm);color:var(--text-muted)"
                                    +description
                                }
                            }
                        }
                    }
                    div {
                        style = "display:flex;gap:var(--space-5);padding-top:var(--space-6);" +
                            "border-top:1px solid var(--border-subtle)"
                        ui.button("← Build from source", variant = ButtonVariant.Secondary)
                        ui.button("Handshake →", variant = ButtonVariant.Secondary)
                    }
                }
            }

            aside {
                style = "padding:var(--space-10) var(--space-6);border-left:1px solid var(--border-panel)"
                div {
                    style = "position:sticky;top:calc(56px + var(--space-6));display:flex;flex-direction:column;gap:var(--space-4)"
                    span {
                        style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                            "text-transform:uppercase;color:var(--gold-300)"
                        +"On this page"
                    }
                    val onPage = listOf("Requirements", "Boot a world", "Configuration", "Plugins", "Troubleshooting")
                    for ((index, item) in onPage.withIndex()) {
                        a(href = "#") {
                            style = "font:var(--type-body-sm);text-decoration:none;" +
                                "color:${if (index == 0) "var(--gold-300)" else "var(--text-muted)"}"
                            +item
                        }
                    }
                }
            }
        }

        ui.siteFooter()
    }

    fun worldsPage(): String = voidPage(
        title = "Void — world list",
        description = "Live status for every Void community world.",
        data = "{ world: 9 }",
    ) {
        ui.siteHeader(pages, active = "worlds")

        main {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-11) var(--space-8);" +
                "display:flex;flex-direction:column;gap:var(--space-8)"
            header {
                style = "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-8)"
                div {
                    h1 {
                        style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                        +"World list"
                    }
                    p {
                        style = "margin:8px 0 0;font:var(--type-body-sm);color:var(--text-muted)"
                        +"Every community-hosted world, its region, mode, population and ping."
                    }
                }
                div {
                    style = "display:flex;gap:var(--space-4)"
                    ui.badge("42 / 44 online", tone = BadgeTone.Success, dot = true)
                    ui.badge("Next restart 04:00 UTC", tone = BadgeTone.Gold)
                }
            }
            ui.panel(title = "Worlds", meta = "click a row to select", padded = false) {
                ui.worldTable("world", worlds)
            }
        }

        ui.siteFooter()
    }

    fun communityPage(): String = voidPage(
        title = "Void — community",
        description = "Forums, wiki and the bug tracker for Void.",
        data = "{ tab: 'all' }",
    ) {
        ui.siteHeader(pages, active = "community")

        div {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-10) var(--space-8);" +
                "display:grid;grid-template-columns:minmax(0,1fr) 320px;gap:var(--space-9)"

            div {
                style = "display:flex;flex-direction:column;gap:var(--space-6);min-width:0"
                header {
                    style = "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-8)"
                    div {
                        h1 {
                            style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                            +"Community"
                        }
                        p {
                            style = "margin:8px 0 0;font:var(--type-body-sm);color:var(--text-muted)"
                            +"Forums, wiki and the bug tracker. Patch notes are posted here first."
                        }
                    }
                    ui.button("New thread", icon = Icons.MAIL)
                }
                div {
                    style = "display:flex;align-items:center;gap:var(--space-6)"
                    ui.tabs(
                        model = "tab",
                        items = listOf(
                            TabItem("all", "All", "1204"),
                            TabItem("server", "Server"),
                            TabItem("protocol", "Protocol"),
                            TabItem("tooling", "Tooling"),
                        ),
                    )
                }
                ui.panel(padded = false) {
                    data class Thread(val title: String, val author: String, val replies: Int, val tag: String, val time: String)
                    val threads = listOf(
                        Thread("Pathfinder rewrite is live on world 30", "rotce", 48, "Server", "2m ago"),
                        Thread("Deadman beta: sign-up thread", "fixer", 212, "Beta", "18m ago"),
                        Thread("Opcode 213 changed between 230 and 231?", "elfinlocks", 7, "Protocol", "1h ago"),
                        Thread("Cache repack tool for Linux arm64", "spark", 31, "Tooling", "3h ago"),
                        Thread("Weekly world status — 8 Sept 2026", "void-bot", 2, "Service", "6h ago"),
                    )
                    for ((index, thread) in threads.withIndex()) {
                        val background = if (index % 2 == 1) "var(--umber-850)" else "var(--surface-panel)"
                        a(href = "#") {
                            style = "display:grid;grid-template-columns:minmax(0,1fr) 110px 90px 80px;" +
                                "gap:var(--space-5);align-items:center;padding:var(--space-5) var(--space-6);" +
                                "text-decoration:none;border-bottom:1px solid var(--umber-900);background:$background"
                            span {
                                style = "display:flex;flex-direction:column;gap:3px;min-width:0"
                                span {
                                    style = "font:var(--weight-semibold) var(--text-sm)/1.3 var(--font-ui);" +
                                        "color:var(--parch-50);white-space:nowrap;overflow:hidden;text-overflow:ellipsis"
                                    +thread.title
                                }
                                span {
                                    style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-faint)"
                                    +"${thread.author} · ${thread.time}"
                                }
                            }
                            ui.badge(thread.tag, tone = if (thread.tag == "Beta") BadgeTone.Info else BadgeTone.Neutral)
                            span {
                                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                                +"${thread.replies} replies"
                            }
                            span {
                                style = "color:var(--text-faint);justify-self:end"
                                icon(Icons.CHEVRON_RIGHT, size = 15)
                            }
                        }
                    }
                }
            }

            div {
                style = "display:flex;flex-direction:column;gap:var(--space-6)"
                ui.panel(title = "Fleet status") {
                    style = "display:flex;flex-direction:column;gap:var(--space-4)"
                    val rows = listOf("Worlds online" to "42 / 44", "Players" to "11,853", "Revision" to "231", "Next restart" to "04:00 UTC")
                    for ((key, value) in rows) {
                        div {
                            style = "display:flex;justify-content:space-between;font:var(--type-body-sm)"
                            span { style = "color:var(--text-faint)"; +key }
                            span { style = "color:var(--parch-100)"; +value }
                        }
                    }
                    ui.badge("All regions healthy", tone = BadgeTone.Success, dot = true)
                }
                ui.panel(title = "Contribute") {
                    style = "display:flex;flex-direction:column;gap:var(--space-5)"
                    p {
                        style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                        +"Good first issues are labelled in the tracker. Patches need one review and a passing build."
                    }
                    ui.button("Open the tracker", variant = ButtonVariant.Secondary, icon = Icons.EXTERNAL)
                }
                div {
                    style = "position:relative;height:150px;border-radius:var(--radius-md);overflow:hidden;" +
                        "border:1px solid var(--border-panel)"
                    img(src = "void/imagery/repository-bg.png", alt = "") {
                        style = "width:100%;height:100%;object-fit:cover"
                    }
                    div { style = "position:absolute;inset:0;background:var(--scrim-bottom)" }
                    div {
                        style = "position:absolute;left:14px;bottom:12px"
                        div {
                            style = "font:var(--weight-semibold) var(--text-base)/1.2 var(--font-ui);color:var(--parch-50)"
                            +"Weekly world night"
                        }
                        div {
                            style = "font:var(--type-label);letter-spacing:var(--tracking-wide);" +
                                "color:var(--parch-200);margin-top:3px"
                            +"Saturdays · 19:00 UTC · World 9"
                        }
                    }
                }
            }
        }

        ui.siteFooter()
    }
}
