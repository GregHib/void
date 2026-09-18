package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h3
import kotlinx.html.header
import kotlinx.html.span
import kotlinx.html.style

/**
 * The stone-panel card used everywhere: an outlined, bevelled surface with an optional
 * ALL-CAPS header band. [meta] renders small mono text on the right of the header
 * (e.g. "variant · size · state"), [action] renders an interactive element there instead.
 * [padded] wraps [content] in the standard `--space-6` padding; pass `false` when content
 * (e.g. a tab strip) needs to sit flush against the panel edges.
 *
 * The panel does *not* clip overflow, so popovers/tooltips inside [content] can escape its
 * bounds — the header's own top corners are rounded separately to match the panel outline.
 * [highlight] swaps the border for gold and lifts the shadow, for the one card in a row that
 * should read as the featured/primary option (e.g. the recommended path in a set of choices).
 */
/**
 * Small mono "eyebrow" text used as a [Ui.panel]'s `action` — a live count, timestamp or other
 * short status rendered from an Alpine [expression] (e.g. `"items.length + ' shown'"`).
 */
fun eyebrowText(expression: String): FlowContent.() -> Unit = {
    span {
        style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
        xText(expression)
    }
}

fun Ui.panel(
    title: String? = null,
    subtitle: String? = null,
    meta: String? = null,
    inset: Boolean = false,
    padded: Boolean = true,
    highlight: Boolean = false,
    action: (FlowContent.() -> Unit)? = null,
    content: DIV.() -> Unit,
) {
    receiver.div {
        val surface = if (inset) "var(--surface-inset)" else "var(--surface-panel)"
        val border = if (highlight) "var(--border-gold)" else "var(--border-panel)"
        val shadow = if (highlight) "var(--shadow-md)" else "var(--shadow-xs)"
        style = "background:$surface;border:1px solid $border;border-radius:var(--radius-md);" +
            "box-shadow:var(--bevel-up),$shadow"
        if (title != null) {
            header {
                attributes["class"] = "void-panel-header"
                style = "display:flex;align-items:center;justify-content:space-between;gap:var(--space-5);" +
                    "flex-wrap:wrap;row-gap:var(--space-2);padding:var(--space-3) var(--space-6);min-height:38px;" +
                    "background:var(--surface-header);" +
                    "border-top-left-radius:var(--radius-md);border-top-right-radius:var(--radius-md);" +
                    "border-bottom:1px solid var(--border-gold)"
                div {
                    style = "display:flex;align-items:baseline;gap:10px;min-width:0;flex:1"
                    h3 {
                        style = "margin:0;font:var(--type-panel-head);letter-spacing:var(--tracking-caps);" +
                            "text-transform:uppercase;color:var(--gold-300);white-space:nowrap;overflow:hidden;" +
                            "text-overflow:ellipsis"
                        +title
                    }
                    if (subtitle != null) {
                        span {
                            style = "font:var(--type-body-sm);color:var(--text-faint);white-space:nowrap;" +
                                "overflow:hidden;text-overflow:ellipsis"
                            +subtitle
                        }
                    }
                }
                if (action != null) {
                    div {
                        style = "flex:0 0 auto;display:flex;align-items:center;min-width:0"
                        action()
                    }
                } else if (meta != null) {
                    span {
                        style = "flex:0 0 auto;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                        +meta
                    }
                }
            }
        }
        div {
            if (padded) {
                style = "padding:var(--space-6)"
            }
            div {
                content()
            }
        }
    }
}
