package content.bot.behaviour.condition

import content.bot.bot
import content.bot.isBot
import world.gregs.voidps.engine.entity.character.player.Player

data class BotAttackerStyle(val equals: Set<String>) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        if (!player.isBot) return false
        val style = player.bot.combatContext?.incomingAttackStyle ?: return false
        return style in equals
    }
}
