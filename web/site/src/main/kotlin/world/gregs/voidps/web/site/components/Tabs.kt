package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

data class TabItem(val id: String, val label: String, val count: String? = null)

/**
 * The tab strip (2px gold rule on the active tab). Pair with [Ui.tabPanel] for each tab's content.
 * [filled] toggles whether the active tab gets a raised panel background, or just the gold underline.
 * [onSelect] overrides the default `model = 'id'` click expression per tab id — pass this when the
 * page needs to route tab switches through a history-aware navigation method instead.
 */
fun Ui.tabs(model: String, items: List<TabItem>, filled: Boolean = true, onSelect: ((String) -> String)? = null) {
    receiver.div {
        attributes["role"] = "tablist"
        val background = if (filled) "background:var(--surface-header);" else ""
        style = "display:flex;align-items:stretch;gap:2px;border-bottom:1px solid var(--border-panel);" +
            "${background}border-top-left-radius:var(--radius-md);" +
            "border-top-right-radius:var(--radius-md)"
        for (item in items) {
            button {
                attributes["role"] = "tab"
                onClick(onSelect?.invoke(item.id) ?: "$model = '${item.id}'")
                xToggleStyle(
                    condition = "$model === '${item.id}'",
                    whenTrue = "${if (filled) "background:var(--surface-panel);" else ""}border-bottom-color:var(--gold-400);color:var(--text-strong)",
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
