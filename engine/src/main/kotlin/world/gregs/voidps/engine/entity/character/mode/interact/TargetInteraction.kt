package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.TargetContext

abstract class TargetInteraction<C : Character, T : Entity> :
    Interaction<C>(),
    TargetContext<C, T>
