package world.gregs.voidps.web.site.components

import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

enum class ItemSlotSize(val px: Int) { Small(36), Medium(48), Large(64) }

/**
 * The square, inset, bevelled tile used for inventory/equipment/update thumbnails.
 * Pass [onClick] (an Alpine statement) and [selectedWhen] (an Alpine boolean expression,
 * e.g. `"slot === 1"`) together to make a click-to-select slot; leave both null for a static tile.
 */
fun Ui.itemSlot(
    label: String? = null,
    quantity: Int? = null,
    size: ItemSlotSize = ItemSlotSize.Medium,
    borderColor: String = "var(--border-strong)",
    selectedWhen: String? = null,
    onClick: String? = null,
) {
    receiver.div {
        if (onClick != null) {
            attributes["@click"] = onClick
        }
        if (selectedWhen != null) {
            xToggleStyle(
                condition = selectedWhen,
                whenTrue = "border-color:var(--gold-400);box-shadow:var(--bevel-down),0 0 0 2px rgba(224,174,60,.25)",
                whenFalse = "border-color:$borderColor;box-shadow:var(--bevel-down)",
            )
        }
        val cursor = if (onClick != null) "cursor:pointer;" else ""
        style = "position:relative;width:${size.px}px;height:${size.px}px;background:var(--surface-inset);" +
            "border:1px solid $borderColor;border-radius:var(--radius-xs);box-shadow:var(--bevel-down);" +
            "display:flex;align-items:center;justify-content:center;$cursor"
        if (quantity != null) {
            span {
                style = "position:absolute;top:1px;left:3px;font:var(--weight-bold) var(--text-3xs)/1 var(--font-ui);" +
                    "color:var(--gold-200);text-shadow:0 1px 0 rgba(0,0,0,.9)"
                +quantity.toString()
            }
        }
        if (label != null) {
            span {
                style = "font:var(--type-code);font-size:var(--text-3xs);color:var(--text-faint)"
                +label
            }
        }
    }
}
