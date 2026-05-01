package content.bot.behaviour.perception

import world.gregs.voidps.engine.entity.character.player.Player

class BotCombatContext(
    val incomingAttackStyle: String?,
    enemiesByTile: Map<Int, List<Player>>? = null,
    private val spiralScanner: (() -> SpiralScan)? = null,
) {
    /**
     * Holds the spiral-scan output. Only built the first time [enemiesByTile] is read. Reactive
     * actions that don't touch this field (e.g. BotAttackerStyle) pay nothing for the scan.
     */
    private var scan: SpiralScan? = if (enemiesByTile != null) {
        SpiralScan(enemiesByTile)
    } else {
        null
    }

    val enemiesByTile: Map<Int, List<Player>> get() = ensureScan().byTile

    fun copy(
        incomingAttackStyle: String? = this.incomingAttackStyle,
        enemiesByTile: Map<Int, List<Player>> = this.enemiesByTile,
    ): BotCombatContext = BotCombatContext(
        incomingAttackStyle = incomingAttackStyle,
        enemiesByTile = enemiesByTile,
    )

    private fun ensureScan(): SpiralScan {
        val cached = scan
        if (cached != null) return cached
        val fresh = spiralScanner?.invoke() ?: SpiralScan.EMPTY
        scan = fresh
        return fresh
    }

    data class SpiralScan(val byTile: Map<Int, List<Player>>) {
        companion object {
            val EMPTY = SpiralScan(emptyMap())
        }
    }

    companion object {
        val EMPTY = BotCombatContext(incomingAttackStyle = null)
    }
}
