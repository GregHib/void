package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BotWorld
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile

object BotArenaCenter {
    private const val RECENTER_THRESHOLD = 12

    private val centers = mapOf(
        "clan_wars_ffa_safe_arena" to Tile(2815, 5515, 0),
        "clan_wars_ffa_dangerous_arena" to Tile(3007, 5514, 0),
    )

    fun maybeRecenter(bot: Bot, world: BotWorld, area: String?): Boolean {
        val center = centers[area] ?: return false
        if (bot.tile.level != center.level) return false
        if (bot.tile.distanceTo(center) <= RECENTER_THRESHOLD) return false
        val dx = (center.x - bot.tile.x).coerceIn(-1, 1)
        val dy = (center.y - bot.tile.y).coerceIn(-1, 1)
        if (dx == 0 && dy == 0) return false
        val nx = bot.tile.x + dx * 2
        val ny = bot.tile.y + dy * 2
        val dest = Tile(nx, ny, bot.tile.level)
        if (area != null && dest !in Areas[area]) return false
        world.execute(bot.player, Walk(nx, ny))
        bot.player.clear("bot_kite_anchor")
        bot.player.clear("bot_kite_anchor_target")
        return true
    }
}
