package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.header
import kotlinx.html.img
import kotlinx.html.nav
import kotlinx.html.span
import kotlinx.html.style

data class MenuItem(val id: String, val label: String)

/**
 * The fixed 56px top bar: mark + wordmark, the nav tab row, and a right-hand slot for
 * account/version chrome.
 */
fun FlowContent.voidMenuBar(
    model: String,
    items: List<MenuItem>,
    brand: String = "VOID",
    logoSrc: String? = null,
    right: (FlowContent.() -> Unit)? = null,
) {
    header {
        style = "height:56px;display:flex;align-items:stretch;gap:var(--space-8);padding:0 var(--space-7);" +
            "background:var(--surface-header);border-bottom:1px solid var(--border-gold);" +
            "box-shadow:var(--shadow-sm);position:sticky;top:0;z-index:30"
        div {
            style = "display:flex;align-items:center;gap:10px;flex:0 0 auto"
            if (logoSrc != null) {
                img(src = logoSrc, alt = brand) {
                    style = "width:26px;height:26px;display:block"
                }
            }
            span {
                style = "font:var(--weight-bold) var(--text-lg)/1 var(--font-display);" +
                    "letter-spacing:var(--tracking-caps);color:var(--parch-50)"
                +brand
            }
        }
        nav {
            style = "display:flex;align-items:stretch;gap:var(--space-2);flex:1;min-width:0;overflow:hidden"
            for (item in items) {
                button {
                    onClick("$model = '${item.id}'")
                    xToggleStyle(
                        condition = "$model === '${item.id}'",
                        whenTrue = "border-bottom-color:var(--gold-400);color:var(--gold-300)",
                        whenFalse = "border-bottom-color:transparent;color:var(--text-muted)",
                    )
                    style = "display:inline-flex;align-items:center;padding:0 14px;background:transparent;" +
                        "border:none;border-bottom:2px solid transparent;color:var(--text-muted);" +
                        "font:var(--weight-medium) var(--text-sm)/1 var(--font-ui);letter-spacing:var(--tracking-wide);" +
                        "cursor:pointer;margin-bottom:-1px;transition:color var(--dur-fast) var(--ease-standard)"
                    +item.label
                }
            }
        }
        if (right != null) {
            div {
                style = "display:flex;align-items:center;gap:var(--space-6);flex:0 0 auto"
                right()
            }
        }
    }
}
