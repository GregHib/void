package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*
import java.io.File

/**
 * Builds voidmmo.org's static pages out of the reusable [components] and writes them,
 * plus the design-system stylesheet, into `web/site/build/`.
 */
object Site {

    private val nav = listOf(
        MenuItem("overview", "Overview"),
        MenuItem("components", "Components"),
        MenuItem("worlds", "Worlds"),
        MenuItem("docs", "Docs"),
    )

    private val worlds = listOf(
        WorldEntry(9, "Germany", members = true, mode = "PvP", players = 812, capacity = 2000, ping = 38, status = WorldStatus.Online),
        WorldEntry(14, "United Kingdom", mode = "Normal", players = 1881, capacity = 2000, ping = 142, status = WorldStatus.Full),
        WorldEntry(18, "United States", mode = "Normal", players = 241, capacity = 2000, ping = 96, status = WorldStatus.Restarting),
        WorldEntry(34, "Australia", mode = "Skill total", players = 0, capacity = 2000, ping = null, status = WorldStatus.Offline),
    )

    fun buildPage(): String = voidPage(
        title = "Void — component library",
        description = "Buttons, forms, navigation, feedback and game primitives for the Void design system.",
        data = "{ nav: 'components', tab: 'buttons', dialog: false, world: 9, slot: 3, " +
            "toasts: { success: true, info: true, warning: true, danger: true }, " +
            "agree: true, notify: false, beta: false, search: '', port: '43594', region: 'Germany', progress: 64 }",
    ) {
        voidMenuBar(
            model = "nav",
            items = nav,
            logoSrc = "void/void-mark-glyph-gold.svg",
            right = {
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                    +"v0.41.2"
                }
                voidBadge("Account", tone = BadgeTone.Gold, pill = true)
            },
        )

        section {
            style = "position:relative;padding:var(--space-13) var(--space-7) var(--space-12);" +
                "background:var(--surface-app);border-bottom:1px solid var(--border-panel)"
            div {
                style = "max-width:var(--container-body);margin:0 auto;display:flex;flex-direction:column;" +
                    "gap:var(--space-6);align-items:flex-start"
                span {
                    style = "font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                        "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                    +"Component library"
                }
                h1 {
                    style = "margin:0;font:var(--type-hero);font-size:clamp(34px,6vw,62px);color:var(--parch-50)"
                    +"Every standard component, in one page"
                }
                p {
                    style = "margin:0;max-width:56ch;font:var(--type-body);font-size:var(--text-lg);color:var(--parch-200)"
                    +"Buttons, forms, navigation, feedback, and the game primitives, wired with Alpine.js."
                }
                div {
                    style = "display:flex;flex-wrap:wrap;gap:var(--space-5);align-items:center;margin-top:var(--space-2)"
                    voidButton("Play", size = ButtonSize.Large, glow = true, icon = Icons.PLAY)
                    voidButton("Download launcher", variant = ButtonVariant.Secondary, size = ButtonSize.Large, icon = Icons.DOWNLOAD)
                }
            }
        }

        main {
            style = "max-width:var(--container-body);margin:0 auto;padding:var(--space-11) var(--space-7) var(--space-13);" +
                "display:flex;flex-direction:column;gap:var(--space-11)"

            section {
                style = "display:flex;flex-direction:column;gap:var(--space-8)"
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"Core"
                }
                voidPanel(title = "Button", meta = "variant · size · state") {
                    style = "display:flex;flex-direction:column;gap:var(--space-7)"
                    div {
                        style = "display:flex;flex-wrap:wrap;gap:var(--space-5);align-items:center"
                        voidButton("Primary")
                        voidButton("Secondary", variant = ButtonVariant.Secondary)
                        voidButton("Ghost", variant = ButtonVariant.Ghost)
                        voidButton("Delete world", variant = ButtonVariant.Danger)
                        voidButton("Read the changelog →", variant = ButtonVariant.Link)
                        voidButton("Disabled", disabled = true)
                    }
                }

                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(300px,1fr));gap:var(--space-8)"
                    voidPanel(title = "Badge", meta = "6 tones") {
                        style = "display:flex;flex-wrap:wrap;gap:var(--space-4)"
                        voidBadge("Neutral")
                        voidBadge("Members", tone = BadgeTone.Gold)
                        voidBadge("Online", tone = BadgeTone.Success, dot = true)
                        voidBadge("Restarting", tone = BadgeTone.Warning, dot = true)
                        voidBadge("Offline", tone = BadgeTone.Danger, dot = true)
                        voidBadge("Full", tone = BadgeTone.Info)
                    }
                    voidPanel(title = "Panel", subtitle = "inset, with action", inset = true) {
                        p {
                            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
                            +"Panels carry a 1px outline, shallow corners, and the bevel pair that makes flat brown read as stone."
                        }
                    }
                }
            }

            section {
                style = "display:flex;flex-direction:column;gap:var(--space-8)"
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"Forms"
                }
                voidPanel(title = "Input · Select · Checkbox · Switch") {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:var(--space-8)"
                    voidTextInput(
                        "void-in-search",
                        "Search worlds",
                        model = "search",
                        placeholder = "Region or number",
                        icon = Icons.SEARCH,
                        hint = "Matches region, mode, and world number.",
                        hintExpression = "search ? 'Filtering: ' + search : 'Matches region, mode, and world number.'",
                    )
                    voidTextInput("void-in-port", "Listen port", model = "port", mono = true)
                    voidSelect("void-sel-region", "Region", model = "region", options = listOf("United Kingdom" to "United Kingdom", "Germany" to "Germany", "United States" to "United States", "Australia" to "Australia"))
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        voidCheckbox("Run the world headless", description = "Skips the client bootstrap on start.", model = "agree")
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        voidSwitch("Maintenance notices", model = "notify")
                        voidSwitch("Beta revisions (small)", model = "beta", small = true)
                    }
                }
            }

            section {
                style = "display:flex;flex-direction:column;gap:var(--space-8)"
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"Navigation"
                }
                voidPanel(padded = false) {
                    voidTabs(
                        model = "tab",
                        items = listOf(
                            TabItem("buttons", "Changelog", "24"),
                            TabItem("builds", "Builds", "7"),
                            TabItem("issues", "Issues", "112"),
                        ),
                    )
                    div {
                        style = "padding:var(--space-7) var(--space-6);min-height:150px"
                        voidTabPanel("tab", "buttons") {
                            style = "display:flex;flex-direction:column;gap:var(--space-5)"
                            h4 {
                                style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                                +"Menu entry swapper overhaul"
                            }
                            p {
                                style = "margin:0;font:var(--type-body);color:var(--text-muted);max-width:66ch"
                                +"Swapper entries now persist per-account and survive a client restart."
                            }
                        }
                        voidTabPanel("tab", "builds") {
                            style = "display:flex;flex-direction:column;gap:var(--space-5)"
                            h4 {
                                style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                                +"Nightly builds"
                            }
                            p {
                                style = "margin:0;font:var(--type-body);color:var(--text-muted);max-width:66ch"
                                +"Seven builds available. Built from main every day at 03:00 UTC."
                            }
                        }
                        voidTabPanel("tab", "issues") {
                            style = "display:flex;flex-direction:column;gap:var(--space-5)"
                            h4 {
                                style = "margin:0;font:var(--type-section);color:var(--parch-50)"
                                +"Open issues"
                            }
                            p {
                                style = "margin:0;font:var(--type-body);color:var(--text-muted);max-width:66ch"
                                +"Pathfinder and NPC respawn drift are the active areas."
                            }
                        }
                    }
                }
            }

            section {
                style = "display:flex;flex-direction:column;gap:var(--space-8)"
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"Feedback"
                }
                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(320px,1fr));gap:var(--space-8)"
                    voidPanel(
                        title = "Toast",
                        action = {
                            voidButton(
                                "Restore all",
                                variant = ButtonVariant.Link,
                                onClick = "toasts = { success: true, info: true, warning: true, danger: true }",
                            )
                        },
                    ) {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        voidToast("toasts.success", "toasts.success = false", "Cache verified", "All 24,331 files match the manifest.", ToastTone.Success)
                        voidToast("toasts.info", "toasts.info = false", "Update available", "Launcher v0.41.2 is ready to install.", ToastTone.Info)
                        voidToast("toasts.warning", "toasts.warning = false", "Worlds 12–18 restart", "Scheduled for 04:00 UTC.", ToastTone.Warning)
                        voidToast("toasts.danger", "toasts.danger = false", "Connection lost", "World 34 dropped the socket. Retrying.", ToastTone.Danger)
                    }
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-8)"
                        voidPanel(title = "Progress bar") {
                            voidProgressBarLive("Downloading cache", "progress")
                        }
                        voidPanel(title = "Tooltip · Dialog") {
                            style = "display:flex;flex-wrap:wrap;gap:var(--space-8);align-items:center"
                            voidTooltip("Ping measured over 30s") {
                                voidButton("Hover me", variant = ButtonVariant.Secondary)
                            }
                            voidTooltip("Side: right", side = TooltipSide.Right) {
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted);" +
                                        "border-bottom:1px dashed var(--border-strong);cursor:help"
                                    +"a3f91c4"
                                }
                            }
                            voidButton("Open dialog", onClick = "dialog = true")
                        }
                    }
                }
            }

            section {
                style = "display:flex;flex-direction:column;gap:var(--space-8)"
                h2 {
                    style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                    +"Game primitives"
                }
                div {
                    style = "display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:var(--space-8)"
                    div {
                        style = "display:flex;flex-direction:column;gap:var(--space-5)"
                        voidStatBar("Attack", 73, 99)
                        voidStatBar("Server load", 1284, 2000, showValue = false)
                    }
                    voidPanel(title = "Item slot") {
                        style = "display:flex;flex-wrap:wrap;gap:var(--space-4)"
                        voidItemSlot("ORE", quantity = 12, onClick = "slot = 1", selectedWhen = "slot === 1")
                        voidItemSlot("RUN", borderColor = "var(--gold-500)", onClick = "slot = 2", selectedWhen = "slot === 2")
                        voidItemSlot("HRB", borderColor = "var(--moss-600)", onClick = "slot = 3", selectedWhen = "slot === 3")
                        voidItemSlot("RGE", borderColor = "var(--ember-600)", onClick = "slot = 4", selectedWhen = "slot === 4")
                        voidItemSlot()
                        voidItemSlot("SM", size = ItemSlotSize.Small)
                        voidItemSlot("LG", size = ItemSlotSize.Large)
                    }
                }
                voidPanel(title = "World list", padded = false) {
                    voidWorldTable("world", worlds)
                }
            }
        }

        footer {
            style = "display:flex;flex-wrap:wrap;align-items:center;justify-content:space-between;gap:var(--space-6);" +
                "max-width:var(--container-body);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-11)"
            div {
                style = "display:flex;align-items:center;gap:10px"
                img(src = "void/void-mark-tile-gold.svg", alt = "Void") {
                    style = "width:28px;height:28px;display:block"
                }
                span {
                    style = "font:var(--type-body-sm);color:var(--text-muted)"
                    +"Void is an open-source emulation stack. Not affiliated with any rightsholder."
                }
            }
        }

        voidDialog(
            model = "dialog",
            title = "Switch world",
            dialogFooter = {
                voidButton("Cancel", variant = ButtonVariant.Ghost, onClick = "dialog = false")
                voidButton("Switch", onClick = "dialog = false")
            },
        ) {
            p {
                style = "margin:0;color:var(--text-muted)"
                +"You will be disconnected and reconnected to the selected world. Trades in progress are cancelled."
            }
            div {
                style = "display:flex;align-items:center;gap:var(--space-5);padding:var(--space-5);" +
                    "background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                    "border-radius:var(--radius-xs);box-shadow:var(--bevel-down)"
                span {
                    style = "font:var(--type-code);font-size:var(--text-xs);color:var(--text-muted)"
                    +"Target"
                }
                span {
                    style = "font:var(--weight-semibold) var(--text-sm)/1 var(--font-ui);color:var(--parch-50)"
                    xText("'World ' + world + ' · ' + region")
                    +"World 9 · Germany"
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val buildDir = File("./web/site/build")
        buildDir.mkdirs()
        File(buildDir, "index.html").writeText(buildPage())
        copyStaticAssets(buildDir)
    }

    private fun copyStaticAssets(buildDir: File) {
        val source = File("./web/site/src/main/resources/static")
        if (!source.exists()) {
            return
        }
        source.copyRecursively(target = buildDir, overwrite = true)
    }
}
