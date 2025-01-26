package content.bot.interact.navigation.graph

import world.gregs.voidps.engine.entity.character.player.Player

interface Condition {
    fun has(player: Player): Boolean
}