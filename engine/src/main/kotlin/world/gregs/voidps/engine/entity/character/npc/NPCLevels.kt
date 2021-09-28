package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class NPCLevels(private val def: NPCDefinition) : Levels.Level {
    override fun getMaxLevel(skill: Skill): Int {
        return when (skill) {
            Skill.Constitution -> def["hitpoints", 10]
            Skill.Attack -> def["att", 1]
            Skill.Strength -> def["str", 1]
            Skill.Defence -> def["def", 1]
            Skill.Range -> def["range", 1]
            Skill.Magic -> def["mage", 1]
            else -> 1
        }
    }
}