package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player

data class BotMembers(val members: Boolean) : Condition(1) {
    override fun keys() = setOf("members")
    override fun events() = setOf("members")
    override fun check(player: Player) = World.members == members
}
