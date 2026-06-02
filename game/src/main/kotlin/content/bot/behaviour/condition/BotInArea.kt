package content.bot.behaviour.condition

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

data class BotInArea(val id: String, val present: Boolean = true) : Condition(1000) {
    override fun keys() = if (present) setOf("area:$id") else setOf()
    override fun events() = setOf("area:$id")
    override fun check(player: Player) = (player.tile in Areas[id]) == present
}
