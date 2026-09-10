package world.gregs.voidps.web.site.components

import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

enum class WorldStatus(val label: String, val tone: BadgeTone, val dot: Boolean) {
    Online("Online", BadgeTone.Success, true),
    Full("Full", BadgeTone.Info, false),
    Restarting("Restarting", BadgeTone.Warning, true),
    Offline("Offline", BadgeTone.Danger, true),
}

data class WorldEntry(
    val number: Int,
    val region: String,
    val members: Boolean = false,
    val mode: String,
    val players: Int,
    val capacity: Int,
    val ping: Int?,
    val status: WorldStatus,
)

private const val COLUMNS = "56px 1.1fr 84px 118px 64px 132px"

/** The world/server list: a header row of labels plus a click-to-select body row per [WorldEntry]. */
fun Ui.worldTable(model: String, worlds: List<WorldEntry>) {
    receiver.div {
        style = "display:grid;grid-template-columns:$COLUMNS;gap:var(--space-5);align-items:center;" +
            "padding:0 var(--space-6);height:30px;background:var(--surface-inset);" +
            "border-bottom:1px solid var(--umber-900);font:var(--type-label);letter-spacing:var(--tracking-caps);" +
            "color:var(--text-faint)"
        span { +"#" }
        span { +"REGION" }
        span { +"MODE" }
        span { +"PLAYERS" }
        span { +"PING" }
        span { +"STATE" }
    }
    for ((index, world) in worlds.withIndex()) {
        val last = index == worlds.lastIndex
        receiver.div {
            onClick("$model = ${world.number}")
            xToggleStyle(
                condition = "$model === ${world.number}",
                whenTrue = "background:var(--surface-active);border-left-color:var(--gold-400)",
                whenFalse = "background:var(--surface-panel);border-left-color:transparent",
            )
            val borderBottom = if (last) "" else "border-bottom:1px solid var(--umber-900);"
            style = "display:grid;grid-template-columns:$COLUMNS;align-items:center;gap:var(--space-5);" +
                "padding:0 var(--space-6);height:44px;cursor:pointer;background:var(--surface-panel);" +
                "border-left:2px solid transparent;$borderBottom" +
                "transition:background var(--dur-fast) var(--ease-standard)"
            span {
                style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);color:var(--parch-100)"
                +world.number.toString()
            }
            span {
                style = "font:var(--type-body-sm);color:var(--text-body);white-space:nowrap;" +
                    "overflow:hidden;text-overflow:ellipsis"
                +world.region
                if (world.members) {
                    span {
                        style = "color:var(--gold-400);margin-left:8px;font:var(--type-label);" +
                            "letter-spacing:var(--tracking-caps)"
                        +"MEMBERS"
                    }
                }
            }
            span {
                style = "font:var(--type-body-sm);color:var(--text-muted)"
                +world.mode
            }
            span {
                style = "display:flex;align-items:center;gap:8px"
                val percent = if (world.capacity <= 0) 0 else (world.players * 100 / world.capacity).coerceIn(0, 100)
                span {
                    style = "flex:1;height:4px;background:var(--surface-inset);border-radius:var(--radius-xs);" +
                        "box-shadow:var(--bevel-down);overflow:hidden"
                    span {
                        style = "display:block;width:$percent%;height:100%;background:var(--gold-400)"
                    }
                }
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint);" +
                        "min-width:34px;text-align:right"
                    +String.format("%,d", world.players)
                }
            }
            span {
                style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-muted)"
                +(world.ping?.let { "${it}ms" } ?: "—")
            }
            ui.badge(world.status.label, tone = world.status.tone, dot = world.status.dot)
        }
    }
}
