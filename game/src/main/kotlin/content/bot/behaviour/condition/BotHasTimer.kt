package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotHasTimer(val id: String) : Condition(1) {
    override fun keys() = setOf("timer:$id")
    override fun events() = setOf("timer")
    override fun check(player: Player) = player.timers.contains(id) || player.softTimers.contains(id)
}