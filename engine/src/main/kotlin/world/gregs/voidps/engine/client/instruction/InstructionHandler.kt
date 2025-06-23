package world.gregs.voidps.engine.client.instruction

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Instruction

abstract class InstructionHandler<T : Instruction> {

    /**
     * Validates the [instruction] information is correct and emits a [Player] event with the relevant data
     */
    abstract fun validate(player: Player, instruction: T)
}
