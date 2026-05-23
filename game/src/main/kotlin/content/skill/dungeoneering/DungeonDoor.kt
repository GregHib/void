package content.skill.dungeoneering

import world.gregs.voidps.engine.entity.character.player.skill.Skill

sealed class DungeonDoor {
    object UnallocatedDoor : DungeonDoor() {
        override fun toString() = "UnallocatedDoor"
    }
    object NormalDoor : DungeonDoor() {
        override fun toString() = "NormalDoor"
    }
    object GuardianDoor : DungeonDoor() {
        override fun toString() = "GuardianDoor"
    }
    data class SkillDoor(val skill: Skill, val level: Int) : DungeonDoor() {
        companion object {
            val skills = setOf(Skill.Runecrafting, Skill.Strength, Skill.Mining, Skill.Firemaking, Skill.Magic, Skill.Prayer, Skill.Woodcutting, Skill.Smithing, Skill.Crafting, Skill.Attack, Skill.Thieving, Skill.Summoning, Skill.Herblore, Skill.Farming, Skill.Construction)
        }
    }
    data class KeyDoor(val key: String) : DungeonDoor()
    data class PuzzleDoor(val puzzle: String) : DungeonDoor()
}