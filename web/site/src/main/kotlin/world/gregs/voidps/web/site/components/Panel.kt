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
 */
fun Ui.panel(
    title: String? = null,
    subtitle: String? = null,
    meta: String? = null,
    inset: Boolean = false,
    padded: Boolean = true,
    action: (FlowContent.() -> Unit)? = null,
    content: DIV.() -> Unit,
) {
    receiver.div {
        val surface = if (inset) "var(--surface-inset)" else "var(--surface-panel)"
        style = "background:$surface;border:1px solid var(--border-panel);border-radius:var(--radius-md);" +
            "box-shadow:var(--bevel-up),var(--shadow-xs)"
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
