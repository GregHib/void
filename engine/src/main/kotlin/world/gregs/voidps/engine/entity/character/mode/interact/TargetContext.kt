package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext

/**
 * Context of a queue or interaction action to access any kind of entity [target]
 */
interface TargetContext<C : Character, T : Entity> : CharacterContext<C> {
    val target: T
}