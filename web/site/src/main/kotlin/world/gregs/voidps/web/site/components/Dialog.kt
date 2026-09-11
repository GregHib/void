package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h2
import kotlinx.html.header
import kotlinx.html.style

/**
 * A centred modal. [model] is a boolean Alpine expression controlling visibility; clicking the
 * backdrop closes it, clicking the panel itself does not. Supply [footer] for action buttons.
 */
fun Ui.dialog(
    model: String,
    title: String,
    width: Int = 440,
    dialogFooter: (FlowContent.() -> Unit)? = null,
    content: DIV.() -> Unit,
) {
    receiver.div {
        attributes["role"] = "dialog"
        attributes["aria-modal"] = "true"
        attributes["class"] = "void-flex"
        xShow(model)
        onClick("$model = false")
        style = "position:fixed;inset:0;align-items:center;justify-content:center;" +
            "background:var(--surface-overlay);backdrop-filter:blur(3px);z-index:50;padding:var(--space-6)"
        div {
            onClickStop("null")
            style = "width:${width}px;max-width:92%;background:var(--surface-panel);" +
                "border:1px solid var(--border-gold);border-radius:var(--radius-md);" +
                "box-shadow:var(--bevel-up),var(--shadow-lg);overflow:hidden"
            header {
                style = "display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);" +
                    "padding:0 var(--space-6);height:42px;background:var(--surface-header);" +
                    "border-bottom:1px solid var(--border-gold)"
                h2 {
                    style = "margin:0;font:var(--type-panel-head);font-size:var(--text-base);" +
                        "letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                    +title
                }
                button {
                    attributes["aria-label"] = "Close"
                    onClick("$model = false")
                    style = "background:transparent;border:none;color:var(--text-muted);cursor:pointer;" +
                        "font:var(--text-lg) var(--font-ui);line-height:1"
                    +"×"
                }
            }
            div {
                style = "padding:var(--space-7) var(--space-6);font:var(--type-body);color:var(--text-body);" +
                    "display:flex;flex-direction:column;gap:var(--space-6)"
                content()
            }
            if (dialogFooter != null) {
                footer {
                    style = "display:flex;justify-content:flex-end;gap:var(--space-4);" +
                        "padding:var(--space-5) var(--space-6);background:var(--umber-850);" +
                        "border-top:1px solid var(--border-subtle)"
                    dialogFooter()
                }
            }
        }
    }
}
