package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event

data class CombatDamage(val target: Character, val skill: Skill, val damage: Int) : Event
