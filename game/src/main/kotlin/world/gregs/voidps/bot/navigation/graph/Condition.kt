package world.gregs.voidps.bot.navigation.graph

import world.gregs.voidps.engine.entity.character.player.Player

interface Condition {
    fun has(player: Player): Boolean
}