package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

data class TabItem(val id: String, val label: String, val count: String? = null)

/** The tab strip (2px gold rule on the active tab). Pair with [Ui.tabPanel] for each tab's content. */
fun Ui.tabs(model: String, items: List<TabItem>) {
    receiver.div {
        attributes["role"] = "tablist"
        style = "display:flex;align-items:stretch;gap:2px;border-bottom:1px solid var(--border-panel);" +
            "background:var(--surface-header);border-top-left-radius:var(--radius-md);" +
            "border-top-right-radius:var(--radius-md)"
        for (item in items) {
            button {
                attributes["role"] = "tab"
                onClick("$model = '${item.id}'")
                xToggleStyle(
                    condition = "$model === '${item.id}'",
                    whenTrue = "background:var(--surface-panel);border-bottom-color:var(--gold-400);color:var(--text-strong)",
                    whenFalse = "background:transparent;border-bottom-color:transparent;color:var(--text-muted)",
                )
                style = "height:42px;padding:0 18px;display:inline-flex;align-items:center;gap:8px;" +
                    "background:transparent;border:none;border-bottom:2px solid transparent;color:var(--text-muted);" +
                    "font:var(--weight-semibold) var(--text-sm)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                    "cursor:pointer;margin-bottom:-1px;white-space:nowrap;" +
                    "transition:color var(--dur-fast) var(--ease-standard)"
                +item.label
                if (item.count != null) {
                    span {
                        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                        +item.count
                    }
                }
            }
        }
    }
}

/**
 * One tab's content, shown when `model === id`. [content] renders into a plain nested `div` so
 * it can freely set `style` (including `display`) without fighting the x-show wrapper, which
 * Alpine manages by adding/removing an inline `display` of its own.
 */
fun Ui.tabPanel(model: String, id: String, content: DIV.() -> Unit) {
    receiver.div {
        xShow("$model === '$id'")
        div {
            content()
        }
    }
}
