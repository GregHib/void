package world.gregs.voidps.web.site.components

import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.strong
import kotlinx.html.style

enum class ToastTone(val accent: String) {
    Success("var(--moss-600)"),
    Info("var(--steel-600)"),
    Warning("var(--gold-700)"),
    Danger("var(--ember-600)"),
}

/**
 * A dismissible status card. [visible] is a boolean Alpine expression (e.g. `"toasts.success"`);
 * [dismiss] is the statement run when the close button is clicked (e.g. `"toasts.success = false"`).
 */
fun Ui.toast(
    visible: String,
    dismiss: String,
    title: String,
    message: String,
    tone: ToastTone,
) {
    receiver.div {
        attributes["role"] = "status"
        attributes["class"] = "void-flex"
        xShow(visible)
        style = "align-items:flex-start;gap:var(--space-5);padding:var(--space-5) var(--space-6);" +
            "background:var(--surface-panel-raised);" +
            "border-top:1px solid var(--border-panel);border-right:1px solid var(--border-panel);" +
            "border-bottom:1px solid var(--border-panel);border-left:2px solid ${tone.accent};" +
            "border-radius:var(--radius-md);box-shadow:var(--bevel-up),var(--shadow-md)"
        div {
            style = "flex:1;min-width:0;display:flex;flex-direction:column;gap:2px"
            strong {
                style = "font:var(--weight-semibold) var(--text-sm)/1.3 var(--font-ui);color:var(--text-strong)"
                +title
            }
            span {
                style = "font:var(--type-body-sm);color:var(--text-muted)"
                +message
            }
        }
        button {
            attributes["aria-label"] = "Dismiss"
            onClick(dismiss)
            style = "background:transparent;border:none;color:var(--text-faint);cursor:pointer;" +
                "font:var(--text-base) var(--font-ui);line-height:1"
            +"×"
        }
    }
}
