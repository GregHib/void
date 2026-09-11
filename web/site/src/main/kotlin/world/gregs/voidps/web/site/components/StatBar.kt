package world.gregs.voidps.web.site.components

import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

/** A level/XP or server-load meter row: label, thin fill bar, and a right-aligned value. */
fun Ui.statBar(label: String, value: Int, max: Int, showValue: Boolean = true) {
    receiver.div {
        style = "display:flex;align-items:center;gap:var(--space-5);padding:var(--space-4) var(--space-5);" +
            "background:var(--surface-panel-raised);border:1px solid var(--border-panel);" +
            "border-radius:var(--radius-sm);box-shadow:var(--bevel-up)"
        div {
            style = "flex:1;min-width:0;display:flex;flex-direction:column;gap:var(--space-2)"
            div {
                style = "display:flex;justify-content:space-between;gap:10px;align-items:baseline"
                span {
                    style = "font:var(--weight-semibold) var(--text-xs)/1 var(--font-ui);" +
                        "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--parch-200)"
                    +label
                }
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                    +"$value / $max"
                }
            }
            val percent = if (max <= 0) 0 else (value * 100 / max).coerceIn(0, 100)
            div {
                style = "height:6px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                    "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden"
                div {
                    style = "width:$percent%;height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500))"
                }
            }
        }
        if (showValue) {
            span {
                style = "font:var(--weight-bold) var(--text-xl)/1 var(--font-display);color:var(--gold-300);" +
                    "min-width:34px;text-align:right"
                +value.toString()
            }
        }
    }
}
