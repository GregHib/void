package content.bot.behaviour.perception

import content.bot.Bot
import content.entity.combat.Target
import content.entity.combat.attacker
import content.entity.combat.dead
import content.entity.combat.underAttack
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill

object BotCombatContextBuilder {
    const val DEFAULT_RADIUS = 15

    /**
     * Builds the cheap part of the context immediately and defers the spiral scan to first access
     * of [BotCombatContext.nearbyEnemies] / nearbyAllies / enemiesByTile. Reactive actions that only
     * need [BotCombatContext.incomingAttackStyle] or ownHp% pay nothing for the scan.
     */
    fun build(bot: Bot, radius: Int = DEFAULT_RADIUS): BotCombatContext {
        val player = bot.player
        val attacker = (player.attacker as? Player)?.takeIf { player.underAttack }
        return BotCombatContext(
            ownHp = player.levels.get(Skill.Constitution),
            ownMaxHp = player.levels.getMax(Skill.Constitution),
            ownPrayerPoints = player.levels.get(Skill.Prayer),
            incomingAttacker = attacker,
            incomingAttackStyle = attacker?.let { categorize(it) },
            lastHitReceivedTick = -1,
            spiralScanner = { scan(player, radius) },
        )
    }

    private fun scan(player: Player, radius: Int): BotCombatContext.SpiralScan {
        content.bot.BotMetrics.incScans()
        val enemies = mutableListOf<Player>()
        val allies = mutableListOf<Player>()
        val byTile = mutableMapOf<Int, MutableList<Player>>()
        Players.forEachInRadius(player.tile, radius) { other ->
            if (other === player || other.dead) {
                return@forEachInRadius
            }
            if (Target.attackable(player, other)) {
                enemies.add(other)
                byTile.getOrPut(other.tile.id) { mutableListOf() }.add(other)
            } else {
                allies.add(other)
            }
        }
        return BotCombatContext.SpiralScan(enemies, allies, byTile)
    }

    private fun categorize(attacker: Player): String? = when (Weapon.type(attacker)) {
        "melee" -> "melee"
        "range" -> "ranged"
        "magic" -> "magic"
        else -> null
    }
}
