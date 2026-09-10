package world.gregs.voidps.web.site.components

import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.span
import kotlinx.html.style

enum class ProgressTone(val from: String, val to: String) {
    Gold("var(--gold-300)", "var(--gold-500)"),
    Moss("var(--moss-500)", "var(--moss-600)"),
    Ember("var(--ember-500)", "var(--ember-600)"),
}

enum class ProgressSize(val height: Int) { Small(4), Medium(8), Large(12) }

/** A static meter bar filled to [percent] (0–100). */
fun Ui.progressBar(
    label: String,
    percent: Int,
    tone: ProgressTone = ProgressTone.Gold,
    size: ProgressSize = ProgressSize.Medium,
) {
    receiver.div {
        style = "display:flex;flex-direction:column;gap:6px"
        span {
            style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-muted)"
            +label
        }
        div {
            attributes["role"] = "progressbar"
            attributes["aria-valuenow"] = percent.toString()
            attributes["aria-valuemin"] = "0"
            attributes["aria-valuemax"] = "100"
            style = "height:${size.height}px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden"
            div {
                style = "width:$percent%;height:100%;background:linear-gradient(180deg,${tone.from},${tone.to})"
            }
        }
    }
}

/** A live meter bound to a numeric Alpine [model] (0–100), with an editable range input. */
fun Ui.progressBarLive(label: String, model: String) {
    receiver.div {
        style = "display:flex;flex-direction:column;gap:var(--space-7)"
        div {
            style = "display:flex;flex-direction:column;gap:6px"
            div {
                style = "display:flex;justify-content:space-between;gap:12px;font:var(--type-label);" +
                    "letter-spacing:var(--tracking-wide);color:var(--text-muted)"
                span { +label }
                span {
                    style = "font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)"
                    xText("$model + ' / 100'")
                }
            }
            div {
                attributes["role"] = "progressbar"
                style = "height:8px;background:var(--surface-inset);border:1px solid var(--border-subtle);" +
                    "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);overflow:hidden"
                div {
                    xEffectStyle("width", "$model + '%'")
                    style = "height:100%;background:linear-gradient(180deg,var(--gold-300),var(--gold-500));" +
                        "transition:width var(--dur-slow) var(--ease-out)"
                }
            }
        }
        input(type = InputType.range) {
            attributes["class"] = "void-range"
            xModel(model)
            min = "0"
            max = "100"
            attributes["aria-label"] = label
        }
    }
}
