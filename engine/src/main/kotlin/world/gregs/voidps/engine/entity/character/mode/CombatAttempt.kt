package world.gregs.voidps.engine.entity.character.mode

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent

class CombatAttempt(
    val target: Character,
    var swingCount: Int
) : CancellableEvent()