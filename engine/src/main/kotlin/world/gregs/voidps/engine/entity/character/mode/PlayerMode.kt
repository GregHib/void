package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.player.Player

interface PlayerMode : Mode {
    fun tick(player: Player)
}