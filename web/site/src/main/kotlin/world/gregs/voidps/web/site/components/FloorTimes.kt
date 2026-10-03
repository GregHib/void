package world.gregs.voidps.web.site.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.unsafe

/**
 * A player's fastest Daemonheim floor times, one row per floor cleared, shared by the hiscores
 * profile and the adventurer's log. [rows] is an Alpine expression for an array built by
 * `voidFloorRow` in `void.js`; [eyebrow] is the header's live summary.
 */
fun Ui.floorTimesPanel(rows: String, eyebrow: String) {
    panel(title = "Dungeoneering floors", action = eyebrowText(eyebrow), padded = false) {
        tableScroll(
            Column("Floor", "minmax(0,1fr)"),
            Column("Size", "90px"),
            Column("Complexity", "110px", "right"),
            Column("Party", "90px", "right"),
            Column("Time", "100px", "right"),
            Column("Rank", "80px", "right"),
        ) {
            unsafe {
                raw(
                    """
                    <template x-for="row in $rows" :key="row.key">
                      <div :style="{ background: row.bg }" style="display:grid;grid-template-columns:minmax(0,1fr) 90px 110px 90px 100px 80px;align-items:center;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--umber-900)">
                        $FLOOR_CELL
                        <span style="font:var(--type-code);font-size:var(--text-2xs);color:var(--text-body)" x-text="row.size"></span>
                        <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-body)" x-text="row.complexity"></span>
                        <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="row.party"></span>
                        <span style="text-align:right;font:var(--weight-semibold) var(--text-sm)/1 var(--font-mono);color:var(--gold-300)" x-text="row.time"></span>
                        <span style="text-align:right;font:var(--type-code);font-size:var(--text-2xs);color:var(--text-faint)" x-text="row.rank"></span>
                      </div>
                    </template>
                    """.trimIndent(),
                )
            }
        }
        emptyState("!$rows.length", "No dungeon floors cleared yet.")
    }
}

/**
 * Two players' fastest time on every floor either has cleared, for the hiscores comparison.
 * [rows] is an Alpine expression for an array built by `voidFloorCompareRow` in `void.js`.
 */
fun Ui.floorComparisonPanel(rows: String, eyebrow: String) {
    panel(title = "Dungeoneering floors", action = eyebrowText(eyebrow), padded = false) {
        tableScroll(listOf("minmax(0,1fr)", "100px", "190px", "100px")) {
            unsafe {
                raw(
                    """
                    <template x-for="row in $rows" :key="row.key">
                      <div :style="{ background: row.bg }" style="display:grid;grid-template-columns:minmax(0,1fr) 100px 190px 100px;align-items:center;padding:var(--space-4) var(--space-6);border-bottom:1px solid var(--umber-900)">
                        $FLOOR_CELL
                        <span :style="{ color: row.aColor }" style="text-align:right;font:var(--type-code)" x-text="row.aTime"></span>
                        <span style="display:flex;align-items:center;justify-content:center;padding:0 10px">
                          <span :style="{ background: row.deltaBg, borderColor: row.deltaBd, color: row.deltaFg }" style="display:inline-flex;align-items:center;height:22px;padding:0 10px;border-radius:var(--radius-pill);border:1px solid;font:var(--type-code);font-size:var(--text-3xs);white-space:nowrap" x-text="row.deltaText"></span>
                        </span>
                        <span :style="{ color: row.bColor }" style="font:var(--type-code)" x-text="row.bTime"></span>
                      </div>
                    </template>
                    """.trimIndent(),
                )
            }
        }
        emptyState("!$rows.length", "Neither player has cleared a dungeon floor yet.")
    }
}

/** Floor number linking to that floor's leaderboard. */
private const val FLOOR_CELL = """<a :href="row.href" style="font:var(--weight-semibold) var(--text-sm)/1.2 var(--font-ui);color:var(--text-strong);text-decoration:none;white-space:nowrap" x-text="'Floor ' + row.floor"></a>"""

private fun FlowContent.emptyState(condition: String, text: String) {
    div {
        xShow(condition)
        style = "padding:var(--space-8) var(--space-6);text-align:center"
        span {
            style = "font:var(--type-body-sm);color:var(--text-muted)"
            +text
        }
    }
}
