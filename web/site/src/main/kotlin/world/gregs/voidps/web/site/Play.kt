package world.gregs.voidps.web.site

import kotlinx.html.*
import world.gregs.voidps.web.site.components.*

/**
 * The play entry point. `playApp()` (in `void.js`) resolves the connected world on load —
 * from `?world=` if present, otherwise from the `localStorage` value the navbar's [worldMenu] or
 * a previous visit left behind — and redirects to `play.html?world=N` once one is known so the
 * URL always reflects it. With no world resolved, this renders the same [worldSelection] view as
 * `worlds.html`; picking a row calls `select()`, which redirects the same way. With a world
 * resolved, the nav bar sits over the `#client` container, and `playApp()` loads the web client
 * (`/play/void-client.js`) from that world's web server, pointing it at the world's `/proxy`
 * websocket.
 */
object Play {

    fun page(): String = voidPage(
        title = "Void - Play",
        description = "Connect to a world and start playing.",
    ) {
        ui.siteHeader(
            Website.pages,
            active = "play",
            communityPages = Website.communityPages,
            hiddenWhen = "${'$'}store.playNav.hidden && ${'$'}store.world.current",
        )

        div {
            // No x-init: Alpine already calls the data's own `init()`, and a second call would
            // launch a second copy of the client.
            xData("playApp()")
            style = "flex:1;display:flex;flex-direction:column"

            main {
                // Not x-show: showing again strips the inline `display`, losing the flex column.
                xEffectStyle("display", "world ? 'none' : 'flex'")
                // flex:1 fills the space a short world list leaves, keeping the footer at the bottom.
                style = "flex:1;display:flex;flex-direction:column"
                ui.worldSelection()
            }

            // Exactly the viewport below the 56px header (or all of it with the header hidden), so the
            // client never pushes the page into scrolling; `playApp()` sizes the game to `#client` (see `voidClientLayout`).
            main {
                xShow("world")
                id = "client-frame"
                style = "position:relative;height:calc(100dvh - 56px);background:#000"
                xEffectStyle("height", "${'$'}store.playNav.hidden ? '100dvh' : 'calc(100dvh - 56px)'")
                div {
                    id = "client"
                    style = "position:absolute;inset:0;overflow:hidden;z-index:1"
                }
                // Minimal tab to bring the nav bar back, faded over the game until hovered.
                button {
                    attributes["class"] = "void-nav-reveal"
                    attributes["aria-label"] = "Show nav bar"
                    attributes["title"] = "Show nav bar"
                    xEffectStyle("display", "${'$'}store.playNav.hidden ? 'inline-flex' : 'none'")
                    onClick("${'$'}store.playNav.setHidden(false)")
                    style = "position:absolute;top:0;left:50%;transform:translateX(-50%);z-index:3;" +
                        "display:none;align-items:center;justify-content:center;width:40px;height:16px;padding:0;" +
                        "background:var(--surface-header);border:1px solid var(--border-gold);border-top:none;" +
                        "border-radius:0 0 var(--radius-md) var(--radius-md);color:var(--gold-300);cursor:pointer"
                    icon(Icons.CHEVRON_DOWN, size = 12)
                }
                span {
                    attributes["class"] = "void-flex"
                    xShow("status")
                    style = "position:absolute;inset:0;z-index:2;align-items:center;justify-content:center;" +
                        "padding:var(--space-11);font:var(--type-title);color:var(--parch-200)"
                    xText("status")
                    +"Loading world …"
                }
            }

            div {
                xShow("!world")
                ui.siteFooter()
            }
        }
    }
}
