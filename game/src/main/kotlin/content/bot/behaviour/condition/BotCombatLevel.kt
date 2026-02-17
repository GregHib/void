package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel

data class BotCombatLevel(val min: Int? = null, val max: Int? = null) : Condition(1) {
    override fun keys() = setOf("skill:combat")
    override fun events() = setOf("skill")
    override fun check(player: Player) = inRange(player.combatLevel, min, max)
}