package world.gregs.voidps.network

import world.gregs.voidps.engine.entity.character.player.Player

abstract class Handler<T : Instruction> {

    abstract fun validate(player: Player, instruction: T)

}