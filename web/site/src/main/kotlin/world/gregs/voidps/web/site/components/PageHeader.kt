package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.section
import kotlinx.html.span
import kotlinx.html.style

/**
 * The banded eyebrow/title/description block used atop a page's main view — Hiscores, the
 * Adventurer's log overview and the Grand Exchange all share this. [actions] renders trailing
 * controls (a search box, buttons, a live timestamp) at the header's edge. [backgroundImage],
 * when set, renders behind the content with a fade so text stays readable. It's scaled to the
 * section's full width at its natural aspect ratio (never cropped/zoomed like `object-fit:cover`
 * would) — any overflow past the header's height is simply clipped by the section below it.
 */
fun Ui.pageHeader(
    eyebrow: String,
    title: String,
    description: String? = null,
    backgroundImage: String? = null,
    actions: (FlowContent.() -> Unit)? = null,
) {
    receiver.section {
        style = "position:relative;border-bottom:1px solid var(--border-panel);background:var(--surface-inset);overflow:hidden"

        if (backgroundImage != null) {
            img(src = backgroundImage, alt = "") {
                style = "position:absolute;top:50%;left:0;width:100%;height:auto;transform:translateY(-50%);display:block"
            }
            div {
                style = "position:absolute;inset:0;background:linear-gradient(90deg,rgba(23,18,13,.94) 0%," +
                    "rgba(23,18,13,.86) 45%,rgba(23,18,13,.42) 100%)"
            }
        }

        div {
            style = "position:relative;max-width:var(--container-wide);margin:0 auto;" +
                "padding:var(--space-9) var(--space-7) var(--space-7);" +
                "display:flex;align-items:flex-end;justify-content:space-between;gap:var(--space-8);flex-wrap:wrap"

            div {
                style = "display:flex;flex-direction:column;gap:var(--space-4);min-width:0"
                span {
                    style = "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--gold-300)"
                    +eyebrow
                }
                h1 {
                    style = "margin:0;font:var(--type-title);color:var(--text-strong);text-wrap:pretty"
                    +title
                }
                if (description != null) {
                    p {
                        style = "margin:0;max-width:52ch;font:var(--type-body-sm);color:var(--text-muted);text-wrap:pretty"
                        +description
                    }
                }
            }

            if (actions != null) {
                div {
                    style = "display:flex;align-items:flex-end;gap:var(--space-5);flex-wrap:wrap"
                    actions()
                }
            }
        }
    }
}
