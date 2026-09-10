package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The staff-only developer panel: a live world dashboard and a player management workbench.
 * Reachable from [AccountMenu]'s "Developer panel" entry for admin accounts. Both pages are
 * written to their own file under `dev/` by [Site], following the same pattern as [Website]'s
 * marketing pages and [Docs]'s reference pages — plain `<a href>` navigation via [DevChrome.devHeader]
 * rather than an in-page Alpine tab switch.
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

    private data class PlayerEntry(
        val id: String,
        val name: String,
        val world: Int,
        val state: String,
        val tone: BadgeTone,
        val rank: String,
        val account: String,
        val meta: String,
    )

    private val players = listOf(
        PlayerEntry("power-spark", "Power Spark", 9, "Online", BadgeTone.Success, "Member", "1,284,551", "Varrock · West bank · 4h 12m"),
        PlayerEntry("rotce", "rotce", 30, "Online", BadgeTone.Success, "Administrator", "1", "Developer world · Lumbridge · 19h 02m"),
        PlayerEntry("lumbriwick", "Lumbriwick", 12, "Online", BadgeTone.Success, "Member", "1,301,882", "Wilderness · level 24 · 52m"),
        PlayerEntry("graveltoe", "Graveltoe", 9, "Restarting", BadgeTone.Warning, "Free", "1,299,004", "Mining guild · Falador · 2h 41m"),
        PlayerEntry("sablewisp", "Sablewisp", 18, "Offline", BadgeTone.Danger, "Member", "1,240,117", "Last seen · Ardougne market"),
        PlayerEntry("kilnfast", "Kilnfast", 9, "Online", BadgeTone.Success, "Moderator", "884,220", "Karamja · volcano · 6h 05m"),
    )

    private val combatSkills = listOf("Attack" to 74, "Strength" to 77, "Defence" to 70, "Hitpoints" to 76)

    private val skills = listOf(
        "Attack" to 74, "Strength" to 77, "Defence" to 70, "Ranged" to 81, "Prayer" to 62, "Magic" to 85,
        "Runecrafting" to 44, "Construction" to 52, "Hitpoints" to 76, "Agility" to 58, "Herblore" to 61,
        "Thieving" to 55, "Crafting" to 66, "Fletching" to 63, "Slayer" to 62, "Hunter" to 47,
        "Mining" to 59, "Smithing" to 71, "Fishing" to 66, "Cooking" to 89, "Firemaking" to 72,
        "Woodcutting" to 58, "Farming" to 45,
    )

    private val equipment = listOf(
        "Head" to "Void mage helm", "Cape" to "Cooking cape (t)", "Amulet" to "Amulet of fury",
        "Weapon" to "Abyssal whip", "Body" to "Void knight top", "Shield" to "Dragon defender",
        "Legs" to "Void knight robe", "Hands" to "Barrows gloves", "Feet" to "Ranger boots",
        "Ring" to "Ring of wealth", "Ammunition" to "Rune arrow × 480",
    )

    private val inventory = listOf(
        2400 to false, 312 to false, 4200 to false, 0 to true, 980 to false, 4 to false, 8 to false,
        1900000 to false, 12 to false, 67 to false, 0 to true, 0 to true, 24 to false, 2 to false,
        0 to true, 0 to true, 0 to true, 3 to false, 6 to false, 2 to false, 0 to true,
        0 to true, 0 to true, 0 to true, 0 to true, 0 to true, 0 to true, 1 to false,
    )

    private val bank = listOf(
        Triple("Coins", "1,912,447", "—"), Triple("Nature rune", "48,220", "10.3M gp"),
        Triple("Abyssal whip", "2", "5.1M gp"), Triple("Shark", "1,140", "1.3M gp"),
        Triple("Rune bar", "860", "10.9M gp"), Triple("Dragon bones", "2,411", "6.7M gp"),
        Triple("Magic logs", "740", "0.8M gp"),
    )

    private data class VarEntry(val key: String, val label: String, val value: String, val type: String, val scope: String, val updated: String)

    private val variables = listOf(
        VarEntry("varbit.4607", "tutorial stage", "12", "varbit", "account", "2d ago"),
        VarEntry("varbit.8063", "slayer task streak", "41", "varbit", "account", "18m ago"),
        VarEntry("varp.101", "quest points", "218", "varp", "account", "3h ago"),
        VarEntry("attr.slayer_task", "Greater demon", "107 left", "string", "session", "18m ago"),
        VarEntry("attr.run_energy", "run energy", "88", "int", "session", "4s ago"),
        VarEntry("config.xp_rate", "xp multiplier", "5.0", "double", "world", "on boot"),
        VarEntry("flag.muted", "chat mute", "false", "boolean", "account", "—"),
        VarEntry("flag.jailed", "jail flag", "false", "boolean", "account", "—"),
        VarEntry("pref.menu_swap", "menu entry swapper", "on", "string", "account", "7d ago"),
        VarEntry("stat.deaths", "deaths total", "37", "int", "account", "6d ago"),
    )

    private val activity = listOf(
        Triple("14:22:07", "Trade offer created", "4,200 × nature rune @ 214 gp · Grand Exchange slot 1"),
        Triple("14:18:44", "Slayer task advanced", "Greater demon 112 → 107 · streak 41"),
        Triple("14:11:02", "Teleported", "Varrock teleport · 3183, 3436, 0"),
        Triple("13:58:31", "Item withdrawn", "2 × abyssal whip from bank tab 3"),
        Triple("13:44:12", "Level gained", "Cooking 88 → 89 · 4,470,110 xp"),
        Triple("13:19:03", "Login", "Client 0.41.2 · 89.44.12.— · revision 231"),
    )

    private data class AuditEntry(val action: String, val reason: String, val staff: String, val time: String)

    private val audit = listOf(
        AuditEntry("Mute lifted", "Appeal accepted · first offence", "kilnfast", "2 Sept 2026"),
        AuditEntry("Muted 24h", "Offensive language in public chat", "kilnfast", "1 Sept 2026"),
        AuditEntry("Name change approved", "Old name: PowerSpark2", "rotce", "14 Aug 2026"),
        AuditEntry("Password reset", "Requested by account holder", "system", "28 Jul 2026"),
    )

    private val location = listOf(
        "Region" to "Varrock · West bank", "Coordinates" to "3183, 3436, plane 0",
        "World" to "9 · voidmmo-eu-1", "Client" to "0.41.2 · revision 231",
        "IP" to "89.44.12.— masked", "Session" to "4h 12m",
    )

    private fun FlowContent.fact(label: String, value: String, accent: Boolean = false) {
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
                +value
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

    private fun DIV.kpiCard(label: String, value: String, sub: String) {
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
        data = "{ live: true, consoleOpen: true }",
    ) {
        ui.devHeader(pages, active = "dashboard", assetPrefix = "../")

        main {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7);" +
                "display:flex;flex-direction:column;gap:var(--space-8);flex:1;width:100%"

            div {
                style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:var(--space-6)"
                kpiCard("Players online", "11,853", "+212 in the last hour")
                kpiCard("Uptime", "10d 22h", "since last restart")
                kpiCard("Tick duration", "38 ms", "avg 90s · 600 ms budget")
                kpiCard("CPU load", "34%", "8 cores · 2 worlds hosted")
                kpiCard("Heap in use", "6.1 / 12.0 GB", "young gc 8 ms")
            }

            div {
                style = "display:grid;grid-template-columns:minmax(0,1fr) 300px;gap:var(--space-8);align-items:start"

                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-8);min-width:0"

                    ui.panel(title = "System load", meta = "last 90s") {
                        style = "display:flex;flex-direction:column;gap:var(--space-6)"
                        ui.progressBar("CPU", 34, tone = ProgressTone.Gold)
                        ui.progressBar("Heap", 51, tone = ProgressTone.Ember)
                        ui.progressBar("Tick budget used", 27, tone = ProgressTone.Moss)
                    }

                    ui.panel(title = "Tick duration", meta = "600 ms budget") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(96px,1fr));gap:var(--space-5)"
                        fact("Current", "38 ms")
                        fact("Avg 90s", "41 ms")
                        fact("P95", "96 ms")
                        fact("Max", "241 ms", accent = true)
                        fact("Overruns", "3")
                    }

                    ui.panel(title = "Population & logins", meta = "world 9") {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        ui.statBar("Online", 1284, 2000)
                        ui.statBar("Logins / min", 24, 60)
                    }
                }

                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"

                    ui.panel(title = "World list", padded = false) {
                        ui.worldTable("world", worlds)
                    }

                    ui.panel(title = "Recent errors", meta = "24h", padded = false) {
                        for ((index, error) in errors.withIndex()) {
                            div {
                                val border = if (index == errors.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                                style = "display:flex;flex-direction:column;gap:4px;padding:var(--space-5) var(--space-6);$border"
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

                    ui.panel(title = "Runtime", padded = false) {
                        for ((index, entry) in runtime.withIndex()) {
                            keyValueRow(entry.first, entry.second, last = index == runtime.lastIndex)
                        }
                    }
                }
            }
        }

        section {
            style = "flex:0 0 auto;background:var(--surface-inset);border-top:1px solid var(--border-gold);" +
                "box-shadow:var(--shadow-md);margin-top:auto"
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
                ui.button(
                    "Toggle", variant = ButtonVariant.Ghost, size = ButtonSize.Small,
                    onClick = "consoleOpen = !consoleOpen",
                )
            }
            div {
                xShow("consoleOpen")
                style = "border-top:1px solid var(--border-subtle);height:160px;overflow:auto;" +
                    "padding:var(--space-5) var(--space-7);display:flex;flex-direction:column;gap:3px;" +
                    "background:var(--umber-950)"
                val log = listOf(
                    "info" to "void-server 0.41.2 · revision 231 · commit 9f1c3ad",
                    "ok" to "[boot] world 9 listening on 43594 — 2,000 npc spawns, 41 regions",
                    "info" to "[auth] staff session opened for rotce (administrator)",
                    "warn" to "[world 9] tick overrun 241 ms — npc respawn batch exceeded budget",
                )
                for ((level, text) in log) {
                    val color = when (level) {
                        "ok" -> "var(--feedback-success)"
                        "warn" -> "var(--feedback-warning)"
                        else -> "var(--text-muted)"
                    }
                    div {
                        style = "font:var(--type-code);font-size:var(--text-xs);color:$color;white-space:pre-wrap"
                        +text
                    }
                }
            }
        }
    }

    fun playersPage(): String = voidPage(
        title = "Void — player management",
        description = "Look up any account, inspect its live state and take moderation actions.",
        assetPrefix = "../",
        data = "{ ptab: 'skills', varScope: 'all', modReason: 'offensive', modDuration: '24h' }",
    ) {
        ui.devHeader(pages, active = "players", assetPrefix = "../")

        main {
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-12);" +
                "display:flex;flex-wrap:wrap;gap:var(--space-8);align-items:flex-start"

            div {
                style = "flex:0 1 280px;min-width:240px;display:flex;flex-direction:column;gap:var(--space-6)"
                ui.textInput(
                    "dev-player-search", "Look up player", placeholder = "name, account id or IP",
                    mono = true, icon = Icons.SEARCH,
                )
                ui.panel(title = "Results", meta = "${players.size} matches", padded = false) {
                    for ((index, player) in players.withIndex()) {
                        val border = if (index == players.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                        val selected = player.id == "power-spark"
                        div {
                            style = "display:flex;flex-direction:column;gap:4px;padding:var(--space-5) var(--space-6);" +
                                "border-left:2px solid ${if (selected) "var(--gold-400)" else "transparent"};" +
                                "background:${if (selected) "var(--surface-active)" else "var(--surface-panel)"};$border"
                            div {
                                style = "display:flex;align-items:center;justify-content:space-between;gap:8px"
                                span {
                                    style = "font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-50)"
                                    +player.name
                                }
                                ui.badge(player.state, tone = player.tone, dot = true)
                            }
                            span {
                                style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                                +"world ${player.world} · ${player.meta}"
                            }
                        }
                    }
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
                            +"Power Spark"
                        }
                        div {
                            style = "display:flex;align-items:center;gap:var(--space-4);flex-wrap:wrap"
                            ui.badge("Online", tone = BadgeTone.Success, dot = true)
                            ui.badge("Member", tone = BadgeTone.Gold)
                            span {
                                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                                +"Account #1,284,551 · world 9"
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
                        TabItem("activity", "Activity"), TabItem("moderation", "Moderation"),
                    ),
                )

                ui.tabPanel("ptab", "skills") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Combat snapshot", meta = "live") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:var(--space-5)"
                        for ((name, level) in combatSkills) {
                            ui.statBar(name, level, 99)
                        }
                    }
                    ui.panel(title = "Skills", meta = "total level ${skills.sumOf { it.second }}") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:var(--space-4)"
                        for ((name, level) in skills) {
                            ui.statBar(name, level, 99)
                        }
                    }
                }

                ui.tabPanel("ptab", "gear") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Equipment", meta = "${equipment.size} slots") {
                        style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:var(--space-5)"
                        for ((slot, item) in equipment) {
                            div {
                                style = "display:flex;align-items:center;gap:var(--space-5)"
                                ui.itemSlot(size = ItemSlotSize.Small)
                                div {
                                    style = "display:flex;flex-direction:column;gap:3px;min-width:0"
                                    span {
                                        style = "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                                            "letter-spacing:var(--tracking-caps);color:var(--text-faint)"
                                        +slot.uppercase()
                                    }
                                    span { style = "font:var(--type-body-sm);color:var(--parch-100)"; +item }
                                }
                            }
                        }
                    }
                    ui.panel(title = "Inventory", meta = "${inventory.count { !it.second }} / 28") {
                        style = "display:grid;grid-template-columns:repeat(7,minmax(0,1fr));gap:var(--space-3);max-width:420px"
                        for ((quantity, empty) in inventory) {
                            if (empty) ui.itemSlot(size = ItemSlotSize.Small) else ui.itemSlot(quantity = quantity, size = ItemSlotSize.Small)
                        }
                    }
                    ui.panel(title = "Bank", meta = "${bank.size} of ${bank.size} tabs shown", padded = false) {
                        for ((index, row) in bank.withIndex()) {
                            val (item, qty, value) = row
                            val border = if (index == bank.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                            div {
                                style = "display:grid;grid-template-columns:1fr 120px 110px;align-items:center;" +
                                    "gap:var(--space-5);padding:var(--space-4) var(--space-6);$border"
                                span { style = "font:var(--type-body-sm);color:var(--parch-100)"; +item }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-200);text-align:right"
                                    +qty
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-faint);text-align:right"
                                    +value
                                }
                            }
                        }
                    }
                }

                ui.tabPanel("ptab", "variables") {
                    ui.panel(title = "Variables", meta = "${variables.size} shown", padded = false) {
                        div {
                            style = "display:flex;gap:var(--space-5);align-items:flex-end;flex-wrap:wrap;" +
                                "padding:var(--space-6);border-bottom:1px solid var(--border-subtle);background:var(--surface-inset)"
                            ui.textInput("dev-var-filter", "Filter by name", placeholder = "varbit, slayer, config…", mono = true)
                            ui.select(
                                "dev-var-scope", "Scope", model = "varScope",
                                options = listOf("all" to "All scopes", "account" to "Account", "session" to "Session", "world" to "World"),
                            )
                            ui.button("Reset", variant = ButtonVariant.Secondary, size = ButtonSize.Small)
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
                        for ((index, entry) in variables.withIndex()) {
                            val border = if (index == variables.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                            div {
                                style = "display:grid;grid-template-columns:minmax(0,2fr) 110px 96px 130px;" +
                                    "gap:var(--space-5);align-items:center;padding:var(--space-4) var(--space-6);$border"
                                div {
                                    style = "display:flex;flex-direction:column;gap:3px;min-width:0"
                                    span {
                                        style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-50);word-break:break-all"
                                        +entry.key
                                    }
                                    span {
                                        style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)"
                                        +entry.label
                                    }
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300);text-align:right"
                                    +entry.value
                                }
                                span {
                                    style = "font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
                                        "letter-spacing:var(--tracking-caps);color:var(--text-muted)"
                                    +entry.type
                                }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);text-align:right"
                                    +entry.updated
                                }
                            }
                        }
                    }
                }

                ui.tabPanel("ptab", "activity") {
                    style = "display:flex;flex-direction:column;gap:var(--space-8)"
                    ui.panel(title = "Action history", meta = "session", padded = false) {
                        for ((index, row) in activity.withIndex()) {
                            val (time, action, detail) = row
                            val border = if (index == activity.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                            div {
                                style = "display:grid;grid-template-columns:76px 1fr;gap:var(--space-5);" +
                                    "padding:var(--space-5) var(--space-6);$border"
                                span { style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"; +time }
                                div {
                                    style = "display:flex;flex-direction:column;gap:3px"
                                    span {
                                        style = "font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-100)"
                                        +action
                                    }
                                    span {
                                        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)"
                                        +detail
                                    }
                                }
                            }
                        }
                    }
                    ui.panel(title = "Staff audit", meta = "this account", padded = false) {
                        for ((index, entry) in audit.withIndex()) {
                            val border = if (index == audit.lastIndex) "" else "border-bottom:1px solid var(--border-subtle);"
                            div {
                                style = "display:grid;grid-template-columns:1fr 120px 150px;align-items:center;" +
                                    "gap:var(--space-5);padding:var(--space-5) var(--space-6);$border"
                                div {
                                    style = "display:flex;flex-direction:column;gap:3px;min-width:0"
                                    span {
                                        style = "font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--parch-100)"
                                        +entry.action
                                    }
                                    span {
                                        style = "font:var(--type-body-sm);font-size:var(--text-2xs);color:var(--text-faint)"
                                        +entry.reason
                                    }
                                }
                                span { style = "font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300)"; +entry.staff }
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);text-align:right"
                                    +entry.time
                                }
                            }
                        }
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
                                    "offensive" to "Offensive language", "botting" to "Botting / macroing",
                                    "scam" to "Scamming", "other" to "Other",
                                ),
                            )
                            ui.select(
                                "dev-mod-duration", "Duration", model = "modDuration",
                                options = listOf("24h" to "24 hours", "48h" to "48 hours", "7d" to "7 days", "perm" to "Permanent"),
                            )
                            ui.textInput("dev-mod-note", "Staff note", placeholder = "visible to staff only")
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
                    for ((index, entry) in location.withIndex()) {
                        keyValueRow(entry.first, entry.second, last = index == location.lastIndex)
                    }
                }

                ui.panel(title = "Current activity") {
                    style = "display:flex;flex-direction:column;gap:var(--space-5)"
                    span {
                        style = "font:var(--weight-semibold) var(--text-lg)/1.2 var(--font-ui);color:var(--parch-50)"
                        +"Trading · Grand Exchange"
                    }
                    span {
                        style = "font:var(--type-body-sm);color:var(--text-muted)"
                        +"Offer 1 of 3 pending · 4,200 nature runes at 214 gp"
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:6px;margin-top:var(--space-3)"
                        div {
                            style = "display:flex;justify-content:space-between;font:var(--weight-semibold) var(--text-2xs)/1 var(--font-ui);" +
                                "letter-spacing:var(--tracking-caps);color:var(--text-faint)"
                            span { +"SESSION XP RATE" }
                            span {
                                style = "font:var(--type-code);font-size:var(--text-xs);color:var(--gold-300);letter-spacing:0"
                                +"41,200 xp/h"
                            }
                        }
                        ui.progressBar("", 64, tone = ProgressTone.Gold)
                    }
                }

                ui.panel(title = "Teleport") {
                    style = "display:flex;flex-direction:column;gap:var(--space-6)"
                    div {
                        style = "display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:var(--space-4)"
                        ui.textInput("dev-tp-x", "X", mono = true)
                        ui.textInput("dev-tp-y", "Y", mono = true)
                        ui.textInput("dev-tp-z", "Plane", mono = true)
                    }
                    div {
                        style = "display:flex;gap:var(--space-4);flex-wrap:wrap"
                        ui.button("Teleport", size = ButtonSize.Small)
                        ui.button("Send home", variant = ButtonVariant.Ghost, size = ButtonSize.Small)
                    }
                }
            }
        }
    }
}
