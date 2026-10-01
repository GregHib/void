package world.gregs.voidps.web.site.components

import kotlinx.html.*

/**
 * The full world-selection view shared by `worlds.html` ([world.gregs.voidps.web.site.Website.worldsPage])
 * and the play page's picker ([world.gregs.voidps.web.site.Play.page]): page header, password
 * warning, [worldList] and the "your own servers" panel. Rows call `select(n)`, resolved against
 * whichever ancestor `x-data` this is dropped into — `worldMenuData()` just selects the world,
 * `playApp()` opens the client on it.
 */
fun Ui.worldSelection(worlds: List<WorldEntry> = defaultWorlds) {
    pageHeader(
        eyebrow = "World list",
        title = "Choose a world",
        description = "Community run servers. Pick one by region, latency, or the ruleset you want to play.",
        backgroundImage = "images/bg/worlds.jpg",
        actions = {
            div {
                style = "display:flex;gap:var(--space-8)"
                worldStat("—", "Players online", expr = "voidFormatNumber(${'$'}store.worlds.totalPlayers())")
            }
        },
    )

    receiver.div {
        // width:100% matters here: without it, this flex item (a column-flex child, centered via
        // margin:0 auto instead of stretched) sizes to its own max-content instead of filling the
        // available width.
        style = "max-width:var(--container-wide);margin:0 auto;padding:var(--space-8) var(--space-7) var(--space-11);" +
            "display:flex;flex-direction:column;gap:var(--space-8);width:100%;box-sizing:border-box"
        passwordNotice()
        ui.worldList(worlds, onSelect = { "select(${it.number})" })
        p {
            style = "margin:calc(-1 * var(--space-4)) 0 0;font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)"
            xText("${'$'}store.worlds.updatedAt ? 'Last updated ' + ${'$'}store.worlds.updatedAt.toLocaleTimeString() : 'Checking worlds…'")
            +"Checking worlds…"
        }

        ui.panel(title = "Your own servers") {
            style = "display:flex;flex-direction:column;gap:var(--space-6)"
            xData("{ customName: '', customAddress: '' }")
            p {
                style = "margin:0;font:var(--type-body-sm);color:var(--text-muted);max-width:60ch"
                +"Not on the list? Save a server in your browser to keep track of it."
            }
            div {
                style = "display:grid;grid-template-columns:1fr 1fr auto;gap:var(--space-6);align-items:end"
                ui.textInput("custom-world-name", "Name", model = "customName", placeholder = "e.g. My server")
                ui.textInput(
                    "custom-world-address",
                    "Web address",
                    model = "customAddress",
                    placeholder = "e.g. play.example.com:8080",
                    onEnter = "if (voidAddCustomWorld(customName, customAddress)) { customName = ''; customAddress = '' }",
                )
                ui.button(
                    "Add server",
                    onClick = "if (voidAddCustomWorld(customName, customAddress)) { customName = ''; customAddress = '' }",
                )
            }
            div {
                attributes["id"] = "void-custom-worlds"
                style = "display:flex;flex-direction:column;gap:var(--space-4)"
                +"No custom servers saved yet."
            }
        }
    }
}

/** Warns that a world's host can see the passwords used on it. */
private fun FlowContent.passwordNotice() {
    div {
        attributes["role"] = "note"
        style = "padding:var(--space-5) var(--space-6);border-left:3px solid var(--amber-500);" +
            "background:var(--surface-inset);border-radius:0 var(--radius-xs) var(--radius-xs) 0"
        p {
            style = "display:flex;align-items:center;gap:8px;margin:0 0 var(--space-3);" +
                "font:var(--weight-semibold) var(--text-sm)/1 var(--font-ui);" +
                "letter-spacing:var(--tracking-wide);color:var(--feedback-warning)"
            icon(Icons.ALERT_TRIANGLE, size = 16)
            +"Protect your password"
        }
        p {
            style = "margin:0;font:var(--type-body-sm);color:var(--text-muted)"
            +(
                "Worlds are run by independent hosts, whoever runs a server could see passwords " +
                    "used to log in to it. Use a unique password for each server, and never reuse one from " +
                    "another game, application or login."
                )
        }
    }
}
