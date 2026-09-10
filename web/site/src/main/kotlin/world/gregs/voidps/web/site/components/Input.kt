package world.gregs.voidps.web.site.components

import kotlinx.html.InputType
import kotlinx.html.div
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.option
import kotlinx.html.select
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.unsafe

/**
 * A labelled text field. [model] binds `x-model`; pass an [error] message to switch to the
 * danger outline. [hintExpression] makes the hint line reactive (e.g.
 * `"search ? 'Filtering: ' + search : ''"`) — [hint] is shown until Alpine hydrates and used
 * as the static fallback if [hintExpression] is null.
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
) {
    receiver.div {
        style = "display:flex;flex-direction:column;gap:6px"
        label {
            attributes["for"] = id
            style = "font:var(--type-label);letter-spacing:var(--tracking-wide);color:var(--text-muted)"
            +label
        }
        div {
            attributes["class"] = "void-input-frame"
            val border = if (error != null) "var(--feedback-danger)" else "var(--border-strong)"
            style = "display:flex;align-items:center;gap:8px;height:34px;padding:0 10px;" +
                "background:var(--surface-inset);border:1px solid $border;border-radius:var(--radius-sm);" +
                "box-shadow:var(--bevel-down);transition:border-color var(--dur-fast) var(--ease-standard)"
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
                val font = if (mono) "var(--type-code)" else "var(--type-body-sm)"
                style = "flex:1;min-width:0;background:transparent;border:none;font:$font;color:var(--text-strong)"
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
        style = "display:flex;flex-direction:column;gap:6px"
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
                style = "width:100%;height:38px;padding:0 34px 0 12px;appearance:none;" +
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

/** A checkbox where the box itself carries the bevel treatment; [model] is a boolean Alpine expression. */
fun Ui.checkbox(
    text: String,
    description: String? = null,
    model: String,
    disabled: Boolean = false,
) {
    receiver.label {
        if (disabled) {
            style = "display:flex;gap:10px;align-items:flex-start;cursor:not-allowed;opacity:.55"
        } else {
            style = "display:flex;gap:10px;align-items:flex-start;cursor:pointer"
            onClick("$model = !$model")
        }
        span {
            if (!disabled) {
                xToggleStyle(
                    condition = model,
                    whenTrue = "background:var(--gold-400);border-color:var(--gold-600);box-shadow:var(--bevel-gold)",
                    whenFalse = "background:var(--surface-inset);border-color:var(--border-strong);box-shadow:var(--bevel-down)",
                )
            }
            style = "width:16px;height:16px;margin-top:2px;flex:0 0 auto;display:flex;align-items:center;" +
                "justify-content:center;background:var(--surface-inset);border:1px solid var(--border-strong);" +
                "border-radius:var(--radius-xs);box-shadow:var(--bevel-down);" +
                "transition:background var(--dur-fast) var(--ease-standard)"
            if (!disabled) {
                span {
                    xShow(model)
                    unsafe {
                        raw(
                            """<svg width="10" height="10" viewBox="0 0 12 12" aria-hidden="true">""" +
                                """<path d="M1.5 6.4 4.3 9.2 10.5 3" fill="none" stroke="#17120d" stroke-width="2.2" stroke-linecap="square"></path></svg>""",
                        )
                    }
                }
            }
        }
        span {
            style = "display:flex;flex-direction:column;gap:2px"
            span {
                style = "font:var(--type-body-sm);color:var(--text-body)"
                +text
            }
            if (description != null) {
                span {
                    style = "font:var(--type-body-sm);font-size:var(--text-xs);color:var(--text-faint)"
                    +description
                }
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
            style = "width:${trackWidth}px;height:${trackHeight}px;border-radius:var(--radius-pill);padding:2px;" +
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
