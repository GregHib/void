package content.bot.behaviour.condition

import content.bot.bot
import content.bot.isBot
import world.gregs.voidps.engine.entity.character.player.Player

data class BotAlliesOnTile(val min: Int? = null, val max: Int? = null) : Condition(1) {
    override fun keys() = emptySet<String>()
    override fun events() = emptySet<String>()
    override fun check(player: Player): Boolean {
        if (!player.isBot) return inRange(0, min, max)
        val context = player.bot.combatContext ?: return inRange(0, min, max)
        val tileId = player.tile.id
        val count = context.nearbyAllies.count { it.tile.id == tileId }
        return inRange(count, min, max)
    }
}
