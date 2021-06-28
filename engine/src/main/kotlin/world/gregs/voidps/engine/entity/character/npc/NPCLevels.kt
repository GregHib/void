package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class NPCLevels(private val def: NPCDefinition) : Levels.Level {
    override fun getMaxLevel(skill: Skill): Int {
        return when (skill) {
            Skill.Constitution -> def["hitpoints", 10]
            else -> 1
        }
    }
}