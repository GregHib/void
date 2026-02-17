package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotHasQueue(val id: String) : Condition(1) {
    override fun keys() = setOf("queue:$id")
    override fun events() = setOf("queue")
    override fun check(player: Player) = player.queue.contains(id)
}