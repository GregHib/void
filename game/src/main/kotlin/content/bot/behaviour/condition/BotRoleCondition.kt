package content.bot.behaviour.condition

import content.bot.behaviour.perception.BotRole
import world.gregs.voidps.engine.entity.character.player.Player

data class BotRoleCondition(val equals: Set<String>) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        val role = BotRole.detect(player).name.lowercase()
        return role in equals
    }
}
