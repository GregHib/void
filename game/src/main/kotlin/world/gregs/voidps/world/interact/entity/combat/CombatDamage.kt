package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

data class CombatDamage(val target: Character, val type: String, val damage: Int) : Event
