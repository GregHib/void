package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The marketing site: Home, Docs and Worlds, all behind the shared [siteHeader]/[siteFooter]
 * chrome. Hiscores, Exchange and Log live elsewhere but are grouped under a "Community" dropdown
 * on the nav bar via [communityPages]. Each page is written to its own file by [Site] so links
 * between them are plain `<a href>`s rather than an in-page Alpine tab switch.
 */
object Website {

    val pages = if (Site.FULL) listOf(
        SitePage("home", "Home", "/index.html"),
        SitePage("docs", "Docs", "/docs/index.html"),
        SitePage("play", "Play", "/play.html"),
    ) else listOf(
        SitePage("home", "Home", "/index.html"),
        SitePage("docs", "Docs", "/docs/index.html"),
    )

    val communityPages = if (Site.FULL) listOf(
        SitePage("hiscores", "Hiscores", "/hiscores.html"),
        SitePage("exchange", "Exchange", "/exchange.html"),
        SitePage("log", "Log", "/log.html"),
        SitePage("worldmap", "World Map", "/world-map.html"),
    ) else listOf(
        SitePage("worldmap", "World Map", "/world-map.html"),
    )

    private fun FlowContent.eyebrow(text: String) {
        span {
            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                    "text-transform:uppercase;color:var(--gold-300)"
            +text
        }
    }

    fun homePage(): String = voidPage(
        title = "Void",
        description = "Void is an open-source emulation server for the 2011 RuneScape era.",
        data = "{ world: 9 }",
    ) {
        ui.siteHeader(pages, active = "home", communityPages = communityPages)

        section {
            style = "position:relative;min-height:520px;display:flex;align-items:flex-end;" +
                    "border-bottom:1px solid var(--border-panel);overflow:hidden"
            img(src = "images/repository-bg.png", alt = "") {
                style = "position:absolute;inset:0;width:100%;height:100%;object-fit:cover"
            }
            div { style = "position:absolute;inset:0;background:var(--scrim-bottom)" }
            div {
                style = "position:relative;max-width:var(--container-wide);margin:0 auto;width:100%;" +
                        "padding:var(--space-12) var(--space-8) var(--space-11);display:flex;" +
                        "flex-direction:column;gap:var(--space-7)"
                h1 {
                    style = "margin:0;max-width:760px;font:var(--type-hero);color:var(--parch-50)"
                    +"RuneScape Revived"
                }
                p {
                    style = "margin:0;max-width:620px;font:var(--weight-regular) var(--text-xl)/1.5 var(--font-ui);" +
                            "color:var(--parch-200)"
                    +("Rediscover 2011 RuneScape with modern server emulation at your fingertips.")
                }
                div {
                    style = "display:flex;gap:var(--space-5);align-items:center;flex-wrap:wrap"
                    ui.button(
                        "Download & play",
                        size = ButtonSize.Large,
                        glow = true,
                        icon = Icons.DOWNLOAD,
                        onClick = "window.location = 'https://github.com/GregHib/void/releases'"
                    )
                    ui.button(
                        "Run your own world",
                        variant = ButtonVariant.Secondary,
                        size = ButtonSize.Large,
                        icon = Icons.TERMINAL,
                        onClick = "window.location = 'https://github.com/GregHib/void#development'",
                    )
                    span {
                        style = "font:var(--type-code);font-size:var(--text-xs);color:var(--parch-300)"
                        +"Windows · macOS · Linux · JDK 21"
                    }
                }
            }
        }

        section {
            style = "border-bottom:1px solid var(--border-panel)"
            div {
                attributes["class"] = "home-what-grid"
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                        "display:grid;grid-template-columns:1fr 1fr;gap:var(--space-11);align-items:center"
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-6)"
                    eyebrow("What is Void?")
                    h2 {
                        style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                        +"A game server for 2011-era RuneScape"
                    }
                    p {
                        style = "margin:0;max-width:540px;font:var(--type-body);font-size:var(--text-lg);color:var(--text-muted)"
                        +("Void lets you play RuneScape exactly as it was in 2011, recreated from scratch to " +
                                "run on your computer or host for others. Free, open-source, and made for " +
                                "everyone.")
                    }
                    p {
                        style = "margin:0;max-width:540px;font:var(--type-body);color:var(--text-faint)"
                        +("Tweak settings, write your own content, or just log in and experience - Void " +
                                "is built to be easy whichever way you want to play.")
                    }
                }
                div {
                    style = "border:1px solid var(--border-gold);border-radius:var(--radius-md);overflow:hidden;" +
                            "box-shadow:var(--bevel-up),var(--shadow-md)"
                    img(src = "images/content/world.png", alt = "") {
                        style = "width:100%;aspect-ratio:4/3;object-fit:cover;display:block"
                    }
                }
            }
        }

        section {
            style = "background:var(--surface-inset);border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                        "display:flex;flex-direction:column;gap:var(--space-9)"
                div {
                    style = "display:flex;flex-direction:column;gap:var(--space-5);max-width:640px"
                    h2 {
                        style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                        +"Content"
                    }
                }
                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(380px,1fr));gap:var(--space-6)"
                    val shots = listOf(
                        Triple("images/content/world-map.png", "Open World", "Nearly every city, dungeon and corner of the map ready to explore."),
                        Triple("images/content/boss.png", "Bosses", "Godwars, KBD, Barrows, Jad and more."),
                        Triple("images/content/skill.png", "Skilling", "Nearly all fully functional skills."),
                        Triple("images/content/quest.png", "Quests", "Over 25 quests recreated and ready to play."),
                        Triple("images/content/minigame.png", "Minigames", "Several minigames including, fight caves, sorceress' garden and vinesweeper."),
                        Triple("images/content/evil-tree.png", "Distractions & Diversions", "Penguin Hide & Seek, Shooting stars, evil trees and more..."),
                    )
                    for ((image, title, body) in shots) {
                        figure {
                            style = "margin:0;min-width:0;display:flex;flex-direction:column;background:var(--surface-panel);" +
                                    "border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
                                    "box-shadow:var(--bevel-up),var(--shadow-sm);overflow:hidden"
                            div {
                                attributes["role"] = "img"
                                style = "aspect-ratio:16/10;background-color:var(--umber-950);" +
                                        "background-image:url($image);background-size:cover;background-position:center"
                            }
                            figcaption {
                                style = "display:flex;flex-direction:column;gap:var(--space-3);" +
                                        "padding:var(--space-6) var(--space-7);border-top:1px solid var(--border-panel)"
                                span {
                                    style = "font:var(--weight-semibold) var(--text-base)/1.3 var(--font-ui);color:var(--parch-50)"
                                    +title
                                }
                                span {
                                    style = "font:var(--type-body-sm);color:var(--text-muted)"
                                    +body
                                }
                            }
                        }
                    }
                }
            }
        }

        section {
            style = "border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                        "display:flex;flex-direction:column;gap:var(--space-11)"
                div {
                    attributes["class"] = "home-run-grid"
                    style = "display:grid;grid-template-columns:1fr 1fr;gap:var(--space-11);align-items:center"
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-6)"
                        h2 {
                            style = "margin:0;font:var(--type-title);font-size:var(--text-4xl);color:var(--parch-50)"
                            +"A server you can run, play and rewrite"
                        }
                        p {
                            style = "margin:0;max-width:520px;font:var(--type-body);font-size:var(--text-lg);color:var(--text-muted)"
                            +("Running your own server is as simple as extracting a zip file, dropping in a cache, and pressing start.")
                        }
                    }
                    div {
                        style = "background:var(--umber-950);border:1px solid var(--border-subtle);" +
                                "border-radius:var(--radius-md);box-shadow:var(--bevel-down),var(--shadow-md);overflow:hidden"
                        div {
                            style = "display:flex;align-items:center;gap:var(--space-4);height:34px;" +
                                    "padding:0 var(--space-6);background:var(--umber-900);border-bottom:1px solid var(--border-panel)"
                            span { style = "width:7px;height:7px;border-radius:50%;background:var(--moss-500)" }
                            span {
                                style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);letter-spacing:var(--tracking-wide)"
                                +"run-server.sh"
                            }
                        }
                        div {
                            style = "padding:var(--space-7);font:var(--type-code);line-height:2;color:var(--parch-100);overflow-x:auto"
                            div { span { style = "color:var(--gold-400)"; +"$ " }; +"./gradlew run" }
                            div { style = "color:var(--text-faint)"; +"[Main] - loading cache 634" }
                            div { style = "color:var(--text-faint)"; +"[Main] - 20,000 npc spawns" }
                            div { span { style = "color:var(--moss-500)"; +"[Main] - Void loaded in 4213ms" } }
                            div { style = "color:var(--text-faint)"; +"[Web] - listening on :8080/play" }
                        }
                    }
                }
                div {
                    attributes["class"] = "home-doors-grid"
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:var(--space-7)"
                    ui.panel(highlight = true) {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        span {
                            style = "color:var(--gold-300)"
                            icon(Icons.PLAY, size = 24)
                        }
                        h3 {
                            style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                            if (Site.FULL) {
                                +"Play online"
                            } else {
                                +"Play offline"
                            }
                        }
                        p {
                            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                            if (Site.FULL) {
                                +"Play in the browser on a live world."
                            } else {
                                +"Play anywhere, no internet required."
                            }
                        }
                        div {
                            style = "margin-top:auto;padding-top:var(--space-4)"
                            if (Site.FULL) {
                                ui.button("Download client", onClick = "window.location = 'play.html'")
                            } else {
                                ui.button("Download server", onClick = "window.location = 'https://github.com/GregHib/void/releases'")
                            }
                        }
                    }
                    ui.panel {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        span {
                            style = "color:var(--gold-300)"
                            icon("""<path d="M4 5h16v6H4zM4 13h16v6H4zM8 8h.01M8 16h.01"></path>""", size = 24)
                        }
                        h3 {
                            style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                            +"Host your own"
                        }
                        p {
                            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                            +"Run a world locally or for friends. Easy setup, no code."
                        }
                        div {
                            style = "margin-top:auto;padding-top:var(--space-4)"
                            ui.button("Quick setup", variant = ButtonVariant.Secondary, onClick = "window.location = 'docs/installation-guide.html'")
                        }
                    }
                    ui.panel {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        span {
                            style = "color:var(--gold-300)"
                            icon(Icons.TERMINAL, size = 24)
                        }
                        h3 {
                            style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                            +"Develop"
                        }
                        p {
                            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                            +"Clone it in IntelliJ and build with gradle."
                        }
                        div {
                            style = "margin-top:auto;padding-top:var(--space-4)"
                            ui.button("Dev guide", variant = ButtonVariant.Secondary, onClick = "window.location = 'docs/ide-setup.html'")
                        }
                    }
                }
            }
        }

        section {
            style = "background:var(--surface-inset);border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                        "display:flex;flex-direction:column;gap:var(--space-10)"
                div {
                    attributes["class"] = "home-content-row"
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));" +
                            "gap:var(--space-10);align-items:center"
                    div {
                        style = "border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
                                "overflow:hidden;box-shadow:var(--bevel-up),var(--shadow-md)"
                        img(src = "images/content/config.png", alt = "") {
                            style = "width:100%;aspect-ratio:16/10;object-fit:cover;display:block"
                        }
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        eyebrow("Configurable")
                        h3 {
                            style = "margin:0;font:var(--type-title);font-size:var(--text-3xl);color:var(--parch-50)"
                            +"Customise without code"
                        }
                        p {
                            style = "margin:0;max-width:480px;font:var(--type-body);font-size:var(--text-lg);color:var(--text-muted)"
                            +("Properties and config files lets you customise your experience without any programming knowledge.")
                        }
                    }
                }
                div {
                    attributes["class"] = "home-content-row"
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));" +
                            "gap:var(--space-10);align-items:center"
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        eyebrow("Bots")
                        h3 {
                            style = "margin:0;font:var(--type-title);font-size:var(--text-3xl);color:var(--parch-50)"
                            +"A world that feels populated on day one"
                        }
                        p {
                            style = "margin:0;max-width:480px;font:var(--type-body);font-size:var(--text-lg);color:var(--text-muted)"
                            +("Intelligent player bots train, fight and move through the world, so a " +
                                    "single-player server still looks and behaves like a live one.")
                        }
                    }
                    div {
                        style = "border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
                                "overflow:hidden;box-shadow:var(--bevel-up),var(--shadow-md)"
                        img(src = "images/content/pvp.png", alt = "") {
                            style = "width:100%;aspect-ratio:16/10;object-fit:cover;display:block"
                        }
                    }
                }
            }
        }

        section {
            style = "border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-12) var(--space-8);" +
                        "display:flex;flex-direction:column;gap:var(--space-10)"
                div {
                    attributes["class"] = "home-bug-grid"
                    style = "display:grid;grid-template-columns:1fr 1fr;gap:var(--space-11);align-items:center"
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-6)"
                        h2 {
                            style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                            +"Found a bug? Open an issue"
                        }
                        p {
                            style = "margin:0;max-width:540px;font:var(--type-body);color:var(--text-muted)"
                            +"If you run into any problems or find any bugs, please open a GitHub Issue describing the problem. Check the contributing guidelines before your first pull request, and run "
                            code {
                                style = "font:var(--type-code);color:var(--gold-300)"
                                +"./gradlew spotlessApply"
                            }
                            +" before committing."
                        }
                        div {
                            style = "display:flex;gap:var(--space-5);flex-wrap:wrap"
                            ui.button("Open an issue", onClick = "window.location = 'https://github.com/GregHib/void/issues'")
                            ui.button(
                                "Contributing guidelines",
                                variant = ButtonVariant.Secondary,
                                onClick = "window.location = 'https://github.com/GregHib/void/blob/master/CONTRIBUTING.md'",
                            )
                        }
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        span {
                            style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
                            +"Thanks to"
                        }
                        div {
                            style = "display:flex;flex-wrap:wrap;gap:var(--space-4)"
                            val thanks = listOf("All contributors", "Kris (osrs-docs)", "Ebp90", "Jarryd", "Tomm (RSMod Pathfinder)", "Graham (OpenRS2)")
                            for (name in thanks) {
                                span {
                                    style = "font:var(--type-body-sm);color:var(--parch-200);background:var(--surface-panel);" +
                                            "border:1px solid var(--border-panel);border-radius:var(--radius-pill);" +
                                            "box-shadow:var(--bevel-up);padding:6px 14px"
                                    +name
                                }
                            }
                        }
                    }
                }
                div {
                    style = "align-self:flex-start"
                    ui.panel(title = "AI policy") {
                        style = "display:flex;flex-direction:column;gap:var(--space-6);padding:var(--space-4) var(--space-2)"
                        p {
                            style = "margin:0;font:var(--type-body);color:var(--text-muted);max-width:760px"
                            +("This project was crafted by hand over 5+ years with care and attention to be open and " +
                                    "accessible. AI-assisted contributions are welcome but held to the same high standard " +
                                    "as any other submission: keep changes small and focused, ensure they are well tested, " +
                                    "with minimal comments, and make sure the code style follows that of the " +
                                    "surrounding codebase. Overly large, low-effort or bulk-generated PRs will not be accepted.")
                        }
                    }
                }
            }
        }

        ui.siteFooter()
    }

    /**
     * The full world list — every community world, not just the navbar [worldMenu] dropdown's
     * quick-switch rows. Deliberately off the nav bar (reached only via "View all worlds" in that
     * dropdown); rows connect the same way the dropdown's do, via `worldMenuData()`'s `select()`,
     * so picking one here persists just as it would from the dropdown.
     */
    fun worldsPage(): String = voidPage(
        title = "Void — world list",
        description = "Live status for every Void community world.",
    ) {
        ui.siteHeader(pages, active = "", communityPages = communityPages)

        main {
            xData("worldMenuData()")
            // width:100% matters here: without it, this flex item (a column-flex child, centered via
            // margin:0 auto instead of stretched) sizes to its own max-content — including the
            // unwrapped width of the flex-wrap:wrap header below — instead of filling the available
            // width, which is exactly what lets that header's content overflow the viewport instead
            // of wrapping (see [Play.page]'s otherwise-identical main, which already sets this).
            style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-11) var(--space-8);" +
                    "display:flex;flex-direction:column;gap:var(--space-8);width:100%"
            header {
                style = "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-8);flex-wrap:wrap"
                div {
                    span {
                        style = "display:block;font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:var(--gold-400);margin-bottom:8px"
                        +"World list"
                    }
                    h1 {
                        style = "margin:0 0 10px;font:var(--type-title);color:var(--parch-50)"
                        +"Choose a world"
                    }
                    p {
                        style = "margin:0;max-width:58ch;font:var(--type-body);color:var(--text-muted)"
                        +("Every world runs the same open-source server build. Pick one by region and latency, or " +
                                "by the ruleset you want to play. Select a row to read its description and hosting details.")
                    }
                }
                div {
                    style = "display:flex;gap:var(--space-8)"
                    worldStat(String.format("%,d", defaultWorlds.sumOf { it.players }), "Players online")
                    val online = defaultWorlds.count { it.status != WorldStatus.Offline }
                    worldStat("$online / ${defaultWorlds.size}", "Worlds up")
                }
            }
            ui.worldList(defaultWorlds, onSelect = { "select(${it.number})" })
        }

        ui.siteFooter()
    }
}
