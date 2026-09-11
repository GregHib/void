package world.gregs.voidps.web.site.components

import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style

/**
 * One `grid-template-columns` track for a ranked/leaderboard-style table (see [tableScroll]):
 * [width] is a literal token exactly as it appears in `grid-template-columns` (`"76px"`,
 * `"minmax(0,1fr)"`); [align] is `"left"`, `"right"` or `"center"`.
 */
data class Column(val label: String, val width: String, val align: String = "left")

/** A label row for [columns] — used standalone, or via [tableScroll] which keeps it lined up with the body rows next to it. */
fun FlowContent.tableHeader(vararg columns: Column) {
    div {
        style = "display:grid;grid-template-columns:${columns.joinToString(" ") { it.width }};" +
            "padding:var(--space-4) var(--space-6);background:var(--umber-900);border-bottom:1px solid var(--border-panel);" +
            "font:var(--type-label);letter-spacing:var(--tracking-caps);text-transform:uppercase;color:var(--text-faint)"
        for (column in columns) {
            span {
                if (column.align != "left") {
                    style = "text-align:${column.align}"
                }
                +column.label
            }
        }
    }
}

/**
 * Wraps a ranked/leaderboard-style table — a [tableHeader] for [columns] plus body rows that are
 * each their *own* `display:grid` div repeating the same `grid-template-columns` (typically an
 * Alpine `<template x-for>` emitted as raw HTML) — so the whole thing scrolls horizontally as one
 * piece on a screen narrower than the table's natural width, instead of forcing its panel wider.
 *
 * This takes two nested boxes, not one: the outer `.void-table-scroll` stays free to shrink with
 * its container (`overflow-x:auto` is what will end up scrolling), while the inner box is pinned
 * to an explicit `min-width` computed from [columns]. That inner width has to be a real, definite
 * number rather than each row just being left to size itself with `min-width:max-content` —
 * every row is its own separate grid container, so if each one resolves its flexible (`fr`/
 * `minmax`) track against its *own* intrinsic content independently, rows with different content
 * (a longer name, say) end up genuinely different widths from each other and from the header,
 * drifting out of column alignment instead of lining up while they scroll. Giving the inner box
 * one fixed width up front means every row (and the header) sizes against that *same* number —
 * still by plain default block sizing, not per-row guesswork — so their tracks always agree.
 */
fun FlowContent.tableScroll(vararg columns: Column, content: DIV.() -> Unit) {
    tableScroll(columns.map { it.width }) {
        tableHeader(*columns)
        content()
    }
}

/** Same idea as the [Column] overload, for a table with no header row — just the literal `grid-template-columns` tokens the body rows use. */
fun FlowContent.tableScroll(columnWidths: List<String>, content: DIV.() -> Unit) {
    div {
        attributes["class"] = "void-table-scroll"
        div {
            style = "min-width:${tableMinWidth(columnWidths)}px"
            content()
        }
    }
}

/** A fixed `Npx` track contributes its own width; a flexible one (`fr`/`minmax`/`auto`) gets a flat estimate — exact precision doesn't matter, only that every row and the header agree on the same total. */
private fun columnMinPx(token: String): Int =
    Regex("^(\\d+)px$").matchEntire(token.trim())?.groupValues?.get(1)?.toInt() ?: 180

/** The same width computation [tableScroll] uses, exposed for tables that build their own bespoke header/scroll wrapper instead of using [tableScroll] directly (see [world.gregs.voidps.web.site.components.worldTable]/`worldList`). */
fun tableMinWidth(columnWidths: List<String>): Int = columnWidths.sumOf(::columnMinPx)
