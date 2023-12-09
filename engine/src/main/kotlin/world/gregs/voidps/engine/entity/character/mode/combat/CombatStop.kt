package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

/**
 * Combat movement has stopped
 */
data class CombatStop(val target: Character) : Event