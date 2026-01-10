package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character

/**
 * Just a helper for checking basic interactions
 */
abstract class InteractOption(
    character: Character,
    target: Entity,
    approachRange: Int? = null,
    shape: Int? = null,
) : Interact(character, target, approachRange = approachRange, shape = shape) {
    abstract val option: String
}