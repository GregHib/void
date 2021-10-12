package world.gregs.voidps.engine.client.instruction

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Instruction

abstract class InstructionHandler<T : Instruction> {

    abstract fun validate(player: Player, instruction: T)

}