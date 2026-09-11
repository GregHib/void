package world.gregs.voidps.web.site.components

import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

/**
 * The navbar's world slot: a compact "World N" trigger that opens a dropdown listing every
 * community world for a quick switch, plus a link to the full world list page (`worlds.html`,
 * built by [world.gregs.voidps.web.site.Website.worldsPage] — not itself on the nav bar).
 * A "Disconnect" row (shown only once a world is connected) clears it back to none. Selection
 * state (`worldMenuData()` in `void.js`) is backed by `localStorage`, so the connected world
 * persists across page navigation; both picking a world and disconnecting redirect to a bare
 * `play.html`/`play.html?world=N` when already on the play page, so the loading screen or world
 * picker there picks up the change immediately. Self-contained, like [accountMenu] — safe to drop
 * into [siteHeader]'s right-hand slot without any page-level wiring.
 */
fun Ui.worldMenu(worlds: List<WorldEntry>, worldsHref: String = "worlds.html") {
    receiver.div {
        xData("worldMenuData()")
        onClickOutside("open = false")
        style = "position:relative;flex:0 0 auto"

        button {
            onClick("open = !open")
            attributes["aria-label"] = "Switch world"
            xToggleStyle(
                condition = "open",
                whenTrue = "background:var(--surface-active);border-color:var(--gold-500)",
                whenFalse = "background:var(--surface-inset);border-color:var(--border-strong)",
            )
            style = "display:inline-flex;align-items:center;gap:8px;height:28px;padding:0 10px 0 12px;" +
                "border:1px solid var(--border-strong);border-radius:var(--radius-md);cursor:pointer;" +
                "color:var(--parch-100);font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                "transition:background var(--dur-fast) var(--ease-standard)"
            span {
                xText("world ? ('World ' + world) : 'Select world'")
                +"Select world"
            }
            icon(Icons.CHEVRON_DOWN, size = 12)
        }

        div {
            xShow("open")
            transition()
            onClickStop("null")
            style = "position:absolute;top:calc(100% + 10px);right:0;width:240px;" +
                "background:var(--surface-panel);border:1px solid var(--border-gold);" +
                "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-lg);" +
                "overflow:hidden;z-index:40"

            div {
                style = "padding:var(--space-5) var(--space-6);background:var(--surface-header);" +
                    "border-bottom:1px solid var(--border-gold);font:var(--type-label);" +
                    "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                +"Switch world"
            }

            div {
                style = "display:flex;flex-direction:column;padding:var(--space-3);max-height:280px;overflow-y:auto"
                for (world in worlds) {
                    button {
                        onClick("select(${world.number})")
                        xToggleStyle(
                            condition = "world === ${world.number}",
                            whenTrue = "background:var(--surface-active)",
                            whenFalse = "background:transparent",
                        )
                        style = "width:100%;display:flex;align-items:center;justify-content:space-between;gap:10px;" +
                            "padding:8px 10px;border:none;border-radius:var(--radius-xs);cursor:pointer;" +
                            "text-align:left;background:transparent;color:var(--text-body);" +
                            "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                            "transition:background var(--dur-fast) var(--ease-standard)"
                        span {
                            style = "display:flex;flex-direction:column;gap:2px;min-width:0"
                            span {
                                style = "color:var(--parch-50)"
                                +"World ${world.number}"
                            }
                            span {
                                style = "font:var(--type-label);letter-spacing:var(--tracking-wide);" +
                                    "color:var(--text-faint);white-space:nowrap;overflow:hidden;text-overflow:ellipsis"
                                +world.region
                            }
                        }
                        span {
                            style = "flex:0 0 auto;font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                "text-transform:uppercase;color:${world.status.tone.color}"
                            +world.status.label
                        }
                    }
                }
            }

            div {
                xShow("world")
                style = "padding:var(--space-3);border-top:1px solid var(--border-subtle)"
                button {
                    onClick("disconnect()")
                    style = "width:100%;display:flex;align-items:center;gap:10px;padding:8px 10px;" +
                        "background:transparent;border:none;border-radius:var(--radius-xs);" +
                        "color:var(--feedback-danger);cursor:pointer;text-align:left;" +
                        "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                        "transition:background var(--dur-fast) var(--ease-standard)"
                    icon(Icons.LOGOUT, size = 15)
                    +"Disconnect"
                }
            }

            div {
                style = "padding:var(--space-3);border-top:1px solid var(--border-subtle)"
                a(href = worldsHref) {
                    style = "display:flex;align-items:center;justify-content:space-between;gap:10px;" +
                        "padding:8px 10px;border-radius:var(--radius-xs);text-decoration:none;" +
                        "color:var(--gold-300);font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                        "transition:background var(--dur-fast) var(--ease-standard)"
                    +"View all worlds"
                    icon(Icons.CHEVRON_RIGHT, size = 14)
                }
            }
        }
    }
}
