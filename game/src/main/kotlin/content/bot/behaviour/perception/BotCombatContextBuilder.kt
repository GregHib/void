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
import world.gregs.voidps.engine.map.Spiral

object BotCombatContextBuilder {
    const val DEFAULT_RADIUS = 10

    fun build(bot: Bot, radius: Int = DEFAULT_RADIUS): BotCombatContext {
        val player = bot.player
        val enemies = mutableListOf<Player>()
        val allies = mutableListOf<Player>()
        val byTile = mutableMapOf<Int, MutableList<Player>>()
        for (tile in Spiral.spiral(player.tile, radius)) {
            for (other in Players.at(tile)) {
                if (other === player || other.dead) {
                    continue
                }
                if (Target.attackable(player, other)) {
                    enemies.add(other)
                    byTile.getOrPut(tile.id) { mutableListOf() }.add(other)
                } else {
                    allies.add(other)
                }
            }
        }
        val attacker = (player.attacker as? Player)?.takeIf { player.underAttack }
        return BotCombatContext(
            ownHp = player.levels.get(Skill.Constitution),
            ownMaxHp = player.levels.getMax(Skill.Constitution),
            ownPrayerPoints = player.levels.get(Skill.Prayer),
            nearbyEnemies = enemies,
            nearbyAllies = allies,
            enemiesByTile = byTile,
            incomingAttacker = attacker,
            incomingAttackStyle = attacker?.let { categorize(it) },
            lastHitReceivedTick = -1,
        )
    }

    private fun categorize(attacker: Player): String? = when (Weapon.type(attacker)) {
        "melee" -> "melee"
        "range" -> "ranged"
        "magic" -> "magic"
        else -> null
    }
}
