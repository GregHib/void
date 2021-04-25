package world.gregs.voidps.engine.entity.character.player.skill

import world.gregs.voidps.engine.event.Event

data class BlockedExperience(
    val skill: Skill,
    val experience: Double
) : Event
