package content.bot.behaviour.perception

import world.gregs.voidps.engine.entity.character.player.Player

data class BotCombatContext(
    val ownHp: Int,
    val ownMaxHp: Int,
    val ownPrayerPoints: Int,
    val nearbyEnemies: List<Player>,
    val nearbyAllies: List<Player>,
    val enemiesByTile: Map<Int, List<Player>>,
    val incomingAttacker: Player?,
    val incomingAttackStyle: String?,
    val lastHitReceivedTick: Int,
) {
    val ownHpPercent: Double = if (ownMaxHp > 0) ownHp.toDouble() / ownMaxHp else 0.0

    companion object {
        val EMPTY = BotCombatContext(
            ownHp = 0,
            ownMaxHp = 0,
            ownPrayerPoints = 0,
            nearbyEnemies = emptyList(),
            nearbyAllies = emptyList(),
            enemiesByTile = emptyMap(),
            incomingAttacker = null,
            incomingAttackStyle = null,
            lastHitReceivedTick = -1,
        )
    }
}
