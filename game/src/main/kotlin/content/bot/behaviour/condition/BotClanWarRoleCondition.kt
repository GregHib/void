package content.bot.behaviour.condition

import content.bot.behaviour.perception.BotClanWarRole
import world.gregs.voidps.engine.entity.character.player.Player

data class BotClanWarRoleCondition(val equals: Set<String>) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        val role = BotClanWarRole.detect(player).name.lowercase()
        return role in equals
    }
}
