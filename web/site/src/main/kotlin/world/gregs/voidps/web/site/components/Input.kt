package world.gregs.voidps.web.site.components

import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.style

/**
 * A labelled text field. [model] binds `x-model`; pass an [error] message to switch to the
 * danger outline. [hintExpression] makes the hint line reactive (e.g.
 * `"search ? 'Filtering: ' + search : ''"`) — [hint] is shown until Alpine hydrates and used
 * as the static fallback if [hintExpression] is null. [inlineLabel] sets the label beside the
 * field rather than above it, for a narrow column of short fields (an X/Y/Level triplet) where
 * stacking each label on its own line costs more height than the fields themselves; the hint,
 * when there is one, still goes underneath the pair.
 */
fun Ui.textInput(
    id: String,
    label: String,
    model: String? = null,
    placeholder: String? = null,
    hint: String? = null,
    hintExpression: String? = null,
    error: String? = null,
    icon: String? = null,
    mono: Boolean = false,
    type: InputType = InputType.text,
    onEnter: String? = null,
    inlineLabel: Boolean = false,
) {
    receiver.div {
        style = "display:flex;flex-direction:column;gap:var(--space-3)"
        div {
            // The label and the field, as their own group, so an [inlineLabel] can lay the two out
            // in a row without dragging the hint line up alongside them.
            style = if (inlineLabel) {
                "display:flex;align-items:center;gap:var(--space-5)"
            } else {
                "display:flex;flex-direction:column;gap:var(--space-3)"
            }
            label {
                attributes["for"] = id
                style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-muted)" +
                    (if (inlineLabel) ";flex:0 0 auto;min-width:34px" else "")
                +label
            }
            div {
                attributes["class"] = "void-input-frame"
                val border = if (error != null) "var(--feedback-danger)" else "var(--border-strong)"
                style = "display:flex;align-items:center;gap:var(--space-4);height:34px;padding:0 10px;" +
                    "background:var(--surface-inset);border:1px solid $border;border-radius:var(--radius-sm);" +
                    "box-shadow:var(--bevel-down);transition:border-color var(--dur-fast) var(--ease-standard)" +
                    (if (inlineLabel) ";flex:1;min-width:0" else "")
                if (icon != null) {
                    span {
                        style = "color:var(--text-faint);display:flex"
                        icon(icon, size = 14)
                    }
                }
                input(type = type) {
                    attributes["id"] = id
                    attributes["class"] = "void-input"
                    if (model != null) {
                        xModel(model)
                    }
                    if (placeholder != null) {
                        this.placeholder = placeholder
                    }
                    if (onEnter != null) {
                        attributes["@keydown.enter"] = onEnter
                    }
                    val font = if (mono) "var(--type-code)" else "var(--type-body-sm)"
                    style = "flex:1;min-width:0;background:transparent;border:none;font:$font;color:var(--text-strong)"
                }
            }
        }
        val hintColor = if (error != null) "var(--feedback-danger)" else "var(--text-faint)"
        if (error != null || hint != null || hintExpression != null) {
            span {
                style = "font:var(--type-body-sm);font-size:var(--text-xs);color:$hintColor"
                if (hintExpression != null) {
                    xText(hintExpression)
                }
                +(error ?: hint.orEmpty())
            }
        }
    }
}

/** A labelled dropdown. [options] are (value, label) pairs; [model] binds `x-model`. */
fun Ui.select(
    id: String,
    label: String,
    model: String,
    options: List<Pair<String, String>>,
    hint: String? = null,
) {
    receiver.div {
        style = "display:flex;flex-direction:column;gap:var(--space-3)"
        label {
            attributes["for"] = id
            style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-muted)"
            +label
        }
        div {
            style = "position:relative"
            select {
                attributes["id"] = id
                attributes["class"] = "void-select"
                xModel(model)
                style = "width:100%;height:38px;padding:0 34px 0 var(--space-5);appearance:none;" +
                    "background:var(--surface-panel-raised);color:var(--text-strong);" +
                    "border:1px solid var(--border-strong);border-radius:var(--radius-sm);" +
                    "box-shadow:var(--bevel-up);font:var(--type-body);cursor:pointer;color-scheme:dark;" +
                    "accent-color:var(--gold-400)"
                for ((value, text) in options) {
                    option {
                        this.value = value
                        +text
                    }
                }
            }
            span {
                attributes["aria-hidden"] = "true"
                style = "position:absolute;right:12px;top:50%;transform:translateY(-50%);" +
                    "pointer-events:none;color:var(--text-muted);font:var(--text-xs) var(--font-ui)"
                +"▾"
            }
        }
        if (hint != null) {
            span {
                style = "font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)"
                +hint
            }
        }
    }
}

/** A toggle switch. [model] is a boolean Alpine expression; [small] renders the compact 32px track. */
fun Ui.switch(text: String, model: String, small: Boolean = false) {
    receiver.label {
        style = "display:inline-flex;align-items:center;gap:10px;cursor:pointer"
        onClick("$model = !$model")
        val trackWidth = if (small) 32 else 40
        val trackHeight = if (small) 18 else 22
        val knob = if (small) 12 else 16
        span {
            xToggleStyle(
                condition = model,
                whenTrue = "background:var(--gold-500);border-color:var(--gold-600);justify-content:flex-end",
                whenFalse = "background:var(--umber-900);border-color:var(--border-strong);justify-content:flex-start",
            )
            style = "width:${trackWidth}px;height:${trackHeight}px;border-radius:var(--radius-pill);padding:var(--space-1);" +
                "display:flex;justify-content:flex-start;align-items:center;background:var(--umber-900);" +
                "border:1px solid var(--border-strong);box-shadow:var(--bevel-down);" +
                "transition:background var(--dur-fast) var(--ease-standard)"
            span {
                xToggleStyle(condition = model, whenTrue = "background:var(--parch-50)", whenFalse = "background:var(--parch-300)")
                style = "width:${knob}px;height:${knob}px;border-radius:50%;background:var(--parch-300);" +
                    "box-shadow:var(--shadow-xs);transition:background var(--dur-fast) var(--ease-standard)"
            }
        }
        span {
            style = "font:var(--type-body-sm);color:var(--text-body)"
            +text
        }
    }
}
