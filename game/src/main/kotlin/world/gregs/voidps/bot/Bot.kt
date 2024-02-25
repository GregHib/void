package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Instruction

data class Bot(val player: Player) : Character by player {
    var step: Instruction? = null
}