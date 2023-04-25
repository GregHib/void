package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent

/**
 * Emitted when within attack range of combat target.
 */
class CombatReached(val target: Character) : CancellableEvent()