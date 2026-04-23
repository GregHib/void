package content.bot.behaviour.perception

import world.gregs.voidps.engine.entity.character.player.Player

class BotCombatContext(
    val ownHp: Int,
    val ownMaxHp: Int,
    val ownPrayerPoints: Int,
    val incomingAttacker: Player?,
    val incomingAttackStyle: String?,
    val lastHitReceivedTick: Int,
    nearbyEnemies: List<Player>? = null,
    nearbyAllies: List<Player>? = null,
    enemiesByTile: Map<Int, List<Player>>? = null,
    private val spiralScanner: (() -> SpiralScan)? = null,
) {
    val ownHpPercent: Double = if (ownMaxHp > 0) ownHp.toDouble() / ownMaxHp else 0.0

    /**
     * Holds the expensive spiral-scan outputs. Only built the first time [nearbyEnemies], [nearbyAllies],
     * or [enemiesByTile] is read. Reactive actions that don't touch these fields (e.g. BotAttackerStyle)
     * pay nothing for the scan.
     */
    private var scan: SpiralScan? = if (nearbyEnemies != null || nearbyAllies != null || enemiesByTile != null) {
        SpiralScan(
            nearbyEnemies.orEmpty(),
            nearbyAllies.orEmpty(),
            enemiesByTile.orEmpty(),
        )
    } else {
        null
    }

    val nearbyEnemies: List<Player> get() = ensureScan().enemies
    val nearbyAllies: List<Player> get() = ensureScan().allies
    val enemiesByTile: Map<Int, List<Player>> get() = ensureScan().byTile

    fun copy(
        ownHp: Int = this.ownHp,
        ownMaxHp: Int = this.ownMaxHp,
        ownPrayerPoints: Int = this.ownPrayerPoints,
        incomingAttacker: Player? = this.incomingAttacker,
        incomingAttackStyle: String? = this.incomingAttackStyle,
        lastHitReceivedTick: Int = this.lastHitReceivedTick,
        nearbyEnemies: List<Player> = this.nearbyEnemies,
        nearbyAllies: List<Player> = this.nearbyAllies,
        enemiesByTile: Map<Int, List<Player>> = this.enemiesByTile,
    ): BotCombatContext = BotCombatContext(
        ownHp = ownHp,
        ownMaxHp = ownMaxHp,
        ownPrayerPoints = ownPrayerPoints,
        incomingAttacker = incomingAttacker,
        incomingAttackStyle = incomingAttackStyle,
        lastHitReceivedTick = lastHitReceivedTick,
        nearbyEnemies = nearbyEnemies,
        nearbyAllies = nearbyAllies,
        enemiesByTile = enemiesByTile,
    )

    private fun ensureScan(): SpiralScan {
        val cached = scan
        if (cached != null) return cached
        val fresh = spiralScanner?.invoke() ?: SpiralScan.EMPTY
        scan = fresh
        return fresh
    }

    data class SpiralScan(
        val enemies: List<Player>,
        val allies: List<Player>,
        val byTile: Map<Int, List<Player>>,
    ) {
        companion object {
            val EMPTY = SpiralScan(emptyList(), emptyList(), emptyMap())
        }
    }

    companion object {
        val EMPTY = BotCombatContext(
            ownHp = 0,
            ownMaxHp = 0,
            ownPrayerPoints = 0,
            incomingAttacker = null,
            incomingAttackStyle = null,
            lastHitReceivedTick = -1,
        )
    }
}
