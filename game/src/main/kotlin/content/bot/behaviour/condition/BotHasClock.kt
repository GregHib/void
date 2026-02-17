package content.bot.behaviour.condition

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.Player

data class BotHasClock(val id: String) : Condition(1) {
    override fun keys() = setOf("clock:$id")
    override fun events() = setOf("clock")
    override fun check(player: Player) = player.hasClock(id)
}
