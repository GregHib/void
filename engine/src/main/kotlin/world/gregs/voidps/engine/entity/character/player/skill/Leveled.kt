package world.gregs.voidps.engine.entity.character.player.skill

import world.gregs.voidps.engine.event.Event

data class Leveled(val skill: Skill, val from: Int, val to: Int) : Event