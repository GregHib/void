package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

data class CombatHit(val source: Character, val skill: Skill, val damage: Int) : Event
