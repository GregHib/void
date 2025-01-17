package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character

/**
 * Context of an event, queue or action with a [target] entity
 */
interface TargetContext<C : Character, T : Entity> : CharacterContext<C> {
    val target: T
}