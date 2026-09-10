package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent
import kotlinx.html.span
import kotlinx.html.style

enum class BadgeTone(val background: String, val color: String, val border: String) {
    Neutral("var(--umber-700)", "var(--parch-200)", "var(--border-strong)"),
    Gold("rgba(224,174,60,.14)", "var(--gold-300)", "var(--gold-600)"),
    Success("var(--feedback-success-bg)", "var(--feedback-success)", "var(--moss-600)"),
    Warning("var(--feedback-warning-bg)", "var(--feedback-warning)", "var(--gold-700)"),
    Danger("var(--feedback-danger-bg)", "var(--feedback-danger)", "var(--ember-600)"),
    Info("var(--feedback-info-bg)", "var(--feedback-info)", "var(--steel-600)"),
}

/** A small status/category chip. [dot] adds the leading status dot used for online/offline states. */
fun FlowContent.voidBadge(
    text: String,
    tone: BadgeTone = BadgeTone.Neutral,
    dot: Boolean = false,
    pill: Boolean = true,
) {
    span {
        val radius = if (pill) "var(--radius-pill)" else "var(--radius-xs)"
        style = "display:inline-flex;align-items:center;gap:6px;padding:0 10px;height:20px;" +
            "background:${tone.background};color:${tone.color};border:1px solid ${tone.border};" +
            "border-radius:$radius;font:var(--weight-semibold) var(--text-3xs)/1 var(--font-ui);" +
            "letter-spacing:var(--tracking-caps);text-transform:uppercase;justify-self:start"
        if (dot) {
            span {
                style = "width:5px;height:5px;border-radius:50%;background:currentColor"
            }
        }
        +text
    }
}
