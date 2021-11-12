package world.gregs.voidps.engine.map.nav

import world.gregs.voidps.engine.entity.character.player.Player

interface Condition {
    fun has(player: Player): Boolean
}