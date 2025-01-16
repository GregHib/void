package world.gregs.voidps.engine.event

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character

interface TargetContext<C : Character, T : Entity> : SourceContext<C> {
    val target: T
}