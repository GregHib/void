package content.bot.behaviour.condition

import content.bot.bot
import content.bot.isBot
import content.entity.combat.attacker
import world.gregs.voidps.engine.entity.character.player.Player

data class BotOutmatched(val attackersMin: Int? = null, val ownHpPercentMax: Double? = null) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        if (!player.isBot) return false
        val context = player.bot.combatContext ?: return false
        if (attackersMin != null) {
            val count = context.nearbyEnemies.count { it.attacker === player }
            if (count < attackersMin) return false
        }
        if (ownHpPercentMax != null) {
            if (context.ownHpPercent > ownHpPercentMax) return false
        }
        return true
    }
}
