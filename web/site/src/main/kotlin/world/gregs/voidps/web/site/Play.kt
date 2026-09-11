package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The play entry point. `playApp()` (in `void/void.js`) resolves the connected world on load —
 * from `?world=` if present, otherwise from the `localStorage` value the navbar's [worldMenu] or
 * a previous visit left behind — and redirects to `play.html?world=N` once one is known so the
 * URL always reflects it. With no world resolved, this renders the same world picker the old
 * worlds page had; picking a row calls `select()`, which redirects the same way. With a world
 * resolved, it's just the nav bar over a centered "loading" line — establishing the actual game
 * connection is out of scope for the static site.
 */
object Play {

    fun page(): String = voidPage(
        title = "Void — play",
        description = "Connect to a Void world and start playing.",
    ) {
        ui.siteHeader(Website.pages, active = "play")

        div {
            xData("playApp()")
            attributes["x-init"] = "init()"
            style = "flex:1;display:flex;flex-direction:column"

            main {
                xShow("!world")
                style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-11) var(--space-8);" +
                    "display:flex;flex-direction:column;gap:var(--space-8);width:100%"
                header {
                    h1 {
                        style = "margin:0;font:var(--type-title);color:var(--parch-50)"
                        +"Choose a world"
                    }
                    p {
                        style = "margin:8px 0 0;font:var(--type-body-sm);color:var(--text-muted)"
                        +"Pick a world to connect to. You can switch worlds any time from the nav bar."
                    }
                }
                ui.panel(title = "Worlds", meta = "click a row to connect", padded = false) {
                    ui.worldTable("world", defaultWorlds, onSelect = { "select(${it.number})" })
                }
            }

            main {
                xShow("world")
                style = "flex:1;display:flex;align-items:center;justify-content:center;padding:var(--space-11)"
                span {
                    style = "font:var(--type-title);color:var(--parch-200)"
                    xText("'Loading world ' + world + ' ...'")
                    +"Loading world …"
                }
            }
        }

        ui.siteFooter()
    }
}
