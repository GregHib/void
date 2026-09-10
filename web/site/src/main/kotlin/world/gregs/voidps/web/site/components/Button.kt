package world.gregs.voidps.web.site.components

import kotlinx.html.BUTTON
import kotlinx.html.button
import kotlinx.html.style

enum class ButtonVariant(val className: String) {
    Primary("void-btn-primary"),
    Secondary("void-btn-secondary"),
    Ghost("void-btn-ghost"),
    Danger("void-btn-danger"),
    Link("void-btn-link"),
}

enum class ButtonSize(val height: Int, val paddingX: Int, val font: String) {
    Small(28, 12, "var(--weight-semibold) var(--text-xs)/1 var(--font-ui)"),
    Medium(36, 18, "var(--weight-semibold) var(--text-sm)/1 var(--font-ui)"),
    Large(46, 28, "var(--weight-semibold) var(--text-lg)/1 var(--font-ui)"),
}

/**
 * The single primary/secondary/ghost/danger/link action control. Gold ([ButtonVariant.Primary])
 * should appear on at most one button per view. Hover/active/disabled colours come from
 * `components.css` (`.void-btn-*`) since inline styles can't express `:hover`/`:active`.
 * [disabledExpression] binds `:disabled` to an Alpine expression instead of the static [disabled]
 * flag, for buttons (e.g. pagination) whose enabled state depends on client-side state.
 */
fun Ui.button(
    text: String,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    disabled: Boolean = false,
    disabledExpression: String? = null,
    glow: Boolean = false,
    icon: String? = null,
    onClick: String? = null,
    block: BUTTON.() -> Unit = {},
) {
    receiver.button {
        if (disabled) {
            this.disabled = true
        }
        if (disabledExpression != null) {
            attributes["x-bind:disabled"] = disabledExpression
        }
        if (onClick != null) {
            attributes["@click"] = onClick
        }
        val isLink = variant == ButtonVariant.Link
        attributes["class"] = if (isLink) variant.className else "void-btn ${variant.className}"
        val cursor = if (disabled) "not-allowed" else "pointer"
        val animation = if (glow && !disabled) ";animation:voidGlow var(--dur-ambient) var(--ease-glow) infinite" else ""
        style = if (isLink) {
            "display:inline-flex;align-items:center;gap:6px;padding:0;font:${size.font};" +
                "letter-spacing:0.06em;cursor:$cursor"
        } else {
            "display:inline-flex;align-items:center;justify-content:center;gap:8px;height:${size.height}px;" +
                "padding:0 ${size.paddingX}px;border-radius:var(--radius-md);font:${size.font};" +
                "letter-spacing:0.06em;cursor:$cursor;transition:background var(--dur-fast) var(--ease-standard)$animation"
        }
        if (icon != null) {
            icon(icon, size = if (size == ButtonSize.Large) 18 else 16)
        }
        +text
        block()
    }
}

/** A square, icon-only control used for row/toolbar actions (search, settings, view toggles). */
fun Ui.iconButton(
    label: String,
    icon: String,
    active: Boolean = false,
    onClick: String? = null,
    size: Int = 32,
) {
    receiver.button {
        attributes["aria-label"] = label
        attributes["title"] = label
        attributes["class"] = "void-icon-btn"
        if (onClick != null) {
            attributes["@click"] = onClick
        }
        val background = if (active) "var(--surface-active)" else "transparent"
        val color = if (active) "var(--text-accent)" else "var(--text-muted)"
        style = "width:${size}px;height:${size}px;display:inline-flex;align-items:center;justify-content:center;" +
            "border-radius:var(--radius-sm);background:$background;color:$color;border:1px solid transparent;" +
            "cursor:pointer;transition:background var(--dur-fast) var(--ease-standard),color var(--dur-fast) var(--ease-standard)"
        icon(icon, size = 18)
    }
}
