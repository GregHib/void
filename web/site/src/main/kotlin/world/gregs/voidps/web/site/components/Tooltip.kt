package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent
import kotlinx.html.span
import kotlinx.html.style

enum class TooltipSide { Top, Right }

/**
 * Wraps [content] in a `x-data` scope and shows [text] on hover, positioned to the [side].
 */
fun Ui.tooltip(text: String, side: TooltipSide = TooltipSide.Top, content: FlowContent.() -> Unit) {
    receiver.span {
        xData("{tooltip:false}")
        attributes["@mouseenter"] = "tooltip = true"
        attributes["@mouseleave"] = "tooltip = false"
        style = "position:relative;display:inline-flex"
        content()
        span {
            attributes["role"] = "tooltip"
            xShow("tooltip")
            style = tooltipStyle(side)
            +text
        }
    }
}

private fun tooltipStyle(side: TooltipSide): String {
    val position = when (side) {
        TooltipSide.Top -> "bottom:calc(100% + 6px);left:50%;transform:translateX(-50%)"
        TooltipSide.Right -> "left:calc(100% + 6px);top:50%;transform:translateY(-50%)"
    }
    return "position:absolute;$position;z-index:40;padding:5px 9px;background:var(--umber-950);" +
        "color:var(--parch-100);border:1px solid var(--border-gold);border-radius:var(--radius-xs);" +
        "box-shadow:var(--shadow-md);font:var(--weight-regular) var(--text-xs)/1.35 var(--font-ui);" +
        "white-space:nowrap;pointer-events:none"
}
