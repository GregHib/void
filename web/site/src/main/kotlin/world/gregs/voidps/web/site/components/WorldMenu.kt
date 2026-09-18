package world.gregs.voidps.web.site.components

import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.input
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
            style = "display:inline-flex;align-items:center;gap:var(--space-4);height:28px;padding:0 10px 0 var(--space-5);" +
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
            xData("{ query: '' }")
            transition()
            onClickStop("null")
            style = "position:absolute;top:calc(100% + 10px);right:0;width:300px;" +
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
                attributes["class"] = "void-input-frame"
                style = "display:flex;align-items:center;gap:var(--space-4);height:32px;margin:var(--space-3) var(--space-3) 0;" +
                    "padding:0 10px;background:var(--surface-inset);border:1px solid var(--border-strong);" +
                    "border-radius:var(--radius-sm);box-shadow:var(--bevel-down)"
                span {
                    style = "color:var(--text-faint);display:flex"
                    icon(Icons.SEARCH, size = 13)
                }
                input {
                    attributes["class"] = "void-input"
                    xModel("query")
                    placeholder = "Search worlds"
                    style = "flex:1;min-width:0;background:transparent;border:none;font:var(--type-body-sm);color:var(--text-strong)"
                }
            }

            div {
                style = "display:flex;flex-direction:column;padding:var(--space-3);max-height:320px;overflow-y:auto"
                for (world in worlds) {
                    button {
                        // x-show clears any inline `display` it doesn't own (see base.css), so the flex layout
                        // is expressed via the `void-flex` class instead of an inline `display:flex`.
                        attributes["class"] = "void-flex"
                        xShow(world.matchesSearchExpr())
                        onClick("select(${world.number})")
                        xToggleStyle(
                            condition = "world === ${world.number}",
                            whenTrue = "background:var(--surface-active)",
                            whenFalse = "background:transparent",
                        )
                        style = "width:100%;align-items:center;justify-content:space-between;gap:10px;" +
                            "padding:var(--space-4) 10px;border:none;border-radius:var(--radius-xs);cursor:pointer;" +
                            "text-align:left;background:transparent;color:var(--text-body);" +
                            "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                            "transition:background var(--dur-fast) var(--ease-standard)"
                        span {
                            style = "display:flex;flex-direction:column;gap:var(--space-1);min-width:0"
                            span {
                                style = "color:var(--parch-50)"
                                +"World ${world.number}"
                                if (world.members) {
                                    span {
                                        style = "color:var(--gold-400);margin-left:var(--space-3);font:var(--type-label);" +
                                            "letter-spacing:var(--tracking-caps)"
                                        +"MEMBERS"
                                    }
                                }
                            }
                            span {
                                style = "font:var(--type-label);letter-spacing:var(--tracking-wide);" +
                                    "color:var(--text-faint);white-space:nowrap;overflow:hidden;text-overflow:ellipsis"
                                +"${world.label} · ${world.mode}"
                            }
                        }
                        span {
                            style = "flex:0 0 auto;display:flex;flex-direction:column;align-items:flex-end;gap:var(--space-1)"
                            span {
                                style = "font:var(--type-label);letter-spacing:var(--tracking-caps);" +
                                    "text-transform:uppercase;color:${world.status.tone.color}"
                                +world.status.label
                            }
                            if (world.ping != null) {
                                span {
                                    style = "font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)"
                                    +"${world.ping}ms"
                                }
                            }
                        }
                    }
                }
            }

            div {
                xShow("world")
                style = "padding:var(--space-3);border-top:1px solid var(--border-subtle)"
                button {
                    onClick("disconnect()")
                    style = "width:100%;display:flex;align-items:center;gap:10px;padding:var(--space-4) 10px;" +
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
                        "padding:var(--space-4) 10px;border-radius:var(--radius-xs);text-decoration:none;" +
                        "color:var(--gold-300);font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);" +
                        "transition:background var(--dur-fast) var(--ease-standard)"
                    +"View all worlds"
                    icon(Icons.CHEVRON_RIGHT, size = 14)
                }
            }
        }
    }
}
