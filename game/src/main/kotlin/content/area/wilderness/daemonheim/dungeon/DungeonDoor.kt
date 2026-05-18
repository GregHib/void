package content.area.wilderness.daemonheim.dungeon

import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Zone

sealed class DungeonDoor {
    data object Normal : DungeonDoor()
    data object Guardian : DungeonDoor()
    data class SkillDoor(val skill: Skill) : DungeonDoor() {
        companion object {
            val skills = setOf(Skill.Runecrafting, Skill.Strength, Skill.Mining, Skill.Firemaking, Skill.Magic, Skill.Prayer, Skill.Woodcutting, Skill.Smithing, Skill.Crafting, Skill.Attack, Skill.Thieving, Skill.Summoning, Skill.Herblore, Skill.Farming, Skill.Construction)
        }
    }
    data class Locked(val shape: KeyShape, val colour: KeyColour) : DungeonDoor()
}

data class DungeonRoom(
    val boss: Boolean = false,
    val critical: Boolean = false,
    var key: String? = null,
    val doors: Array<DungeonDoor?> = arrayOfNulls(4),
    val children: Array<DungeonRoom?> = arrayOfNulls(4),
) {
    var rotation: Direction = Direction.NONE
    var zone: Zone = Zone.EMPTY

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DungeonRoom

        if (boss != other.boss) return false
        if (critical != other.critical) return false
        if (key != other.key) return false
        if (!doors.contentEquals(other.doors)) return false
        if (!children.contentEquals(other.children)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = boss.hashCode()
        result = 31 * result + critical.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + doors.contentHashCode()
        result = 31 * result + children.contentHashCode()
        return result
    }
}

fun generate() {
    val boss = DungeonRoom(boss = true, critical = true)
    // 95% chance of locked door

    /*
        while(stack) {
            val room = stack()
            for(door in room) {
                pick(door)
                pick(door.room)
                stack.add(door.room)
            }
        }
     */
}
/*
    [straight_rooms]
    key_tiles = "list<tile>"
    npcs = "list<tile>"
    Skills ??
    Complexity?
 */
enum class RoomShape { // FIXME this won't work because some skill rooms have a direction i.e. need to be on the side with the solvable part of the puzzle
    Straight,
    End,
    Corner,
    Junction,
    Cross
}

fun main() {
    val doors = Array<DungeonDoor>(256) { DungeonDoor.Normal }
}

enum class KeyShape  {
    Corner,
    Crescent,
    Diamond,
    Pentagon,
    Rectangle,
    Shield,
    Triangle,
    Wedge
}

enum class KeyColour {
    Blue,
    Crimson,
    Gold,
    Green,
    Orange,
    Purple,
    Silver,
    Yellow
}