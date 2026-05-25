package content.skill.dungeoneering

import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed class DungeonDoor {
    object Normal : DungeonDoor()
    object Guardian : DungeonDoor()
    data class Blocked(val skill: Skill, val level: Int) : DungeonDoor() {
        companion object {
            val skills = setOf(Skill.Runecrafting, Skill.Strength, Skill.Mining, Skill.Firemaking, Skill.Magic, Skill.Prayer, Skill.Woodcutting, Skill.Smithing, Skill.Crafting, Skill.Attack, Skill.Thieving, Skill.Summoning, Skill.Herblore, Skill.Farming, Skill.Construction)
        }
    }
    data class Locked(val key: String, val depth: Int) : DungeonDoor()
}
