package content.bot.behaviour.condition

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

data class BotInArea(val id: String) : Condition(1000) {
    override fun keys() = setOf("area:$id")
    override fun events() = setOf("area")
    override fun check(player: Player) = player.tile in Areas[id]
}