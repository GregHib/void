package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player

data class BotAtTile(val x: Int? = null, val y: Int? = null, val level: Int? = null) : Condition(1000) {
    override fun keys() = setOf("tile")
    override fun events() = setOf("tile")
    override fun check(player: Player): Boolean {
        if (x != null && player.tile.x != x) {
            return false
        }
        if (y != null && player.tile.y != y) {
            return false
        }
        if (level != null && player.tile.level != level) {
            return false
        }
        return true
    }
}
