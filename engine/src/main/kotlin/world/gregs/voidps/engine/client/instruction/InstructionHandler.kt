package world.gregs.voidps.engine.client.instruction

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.client.Instruction

/**
 * An abstract handler for processing and validating instructions specific to a player.
 * This class requires implementation for handling validation logic.
 *
 * @param T The type of instruction being handled, constrained to implementations of the [Instruction] interface.
 */
abstract class InstructionHandler<T : Instruction> {

    /**
     * Validates the given instruction for the specified player.
     * Implementations should ensure the provided instruction can be safely and correctly executed
     * based on the player's current state and other conditions.
     *
     * @param player The player for whom the instruction is being validated.
     * @param instruction The instruction to validate.
     */
    abstract fun validate(player: Player, instruction: T)

}