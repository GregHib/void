package content.bot.behaviour.perception

import content.bot.Bot
import content.bot.BotMetrics
import content.entity.combat.Target
import content.entity.combat.attacker
import content.entity.combat.dead
import content.entity.combat.underAttack
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players

class BotCombatContext(
    val incomingAttackStyle: String?,
    private var scan: Map<Int, List<Player>>? = null,
    private val spiralScanner: () -> Map<Int, List<Player>>,
) {
    /**
     * Holds the spiral-scan output. Only built the first time [enemiesByTile] is read. Reactive
     * actions that don't touch this field (e.g. BotAttackerStyle) pay nothing for the scan.
     */
    val enemiesByTile: Map<Int, List<Player>>
        get() {
            val cached = scan
            if (cached != null) {
                return cached
            }
            val fresh = spiralScanner.invoke()
            scan = fresh
            return fresh
        }

    companion object {
        private const val DEFAULT_RADIUS = 15

        /**
         * Builds the cheap part of the context immediately and defers the spiral scan to first access
         * of [BotCombatContext.enemiesByTile]. Reactive actions that only need
         * [BotCombatContext.incomingAttackStyle] pay nothing for the scan.
         */
        operator fun invoke(bot: Bot, radius: Int = DEFAULT_RADIUS): BotCombatContext {
            val player = bot.player
            val attacker = (player.attacker as? Player)?.takeIf { player.underAttack }
            return BotCombatContext(
                incomingAttackStyle = attacker?.let { categorize(it) },
                spiralScanner = { scan(player, radius) },
            )
        }

        private fun scan(player: Player, radius: Int): Map<Int, List<Player>> {
            BotMetrics.incScans()
            val byTile = mutableMapOf<Int, MutableList<Player>>()
            Players.forEachInRadius(player.tile, radius) { other ->
                if (other === player || other.dead) {
                    return@forEachInRadius
                }
                if (Target.attackable(player, other, message = false)) {
                    byTile.getOrPut(other.tile.id) { mutableListOf() }.add(other)
                }
            }
            return byTile
        }

        private fun categorize(attacker: Player): String? = when (Weapon.type(attacker)) {
            "melee" -> "melee"
            "range" -> "ranged"
            "magic" -> "magic"
            else -> null
        }
    }
}
