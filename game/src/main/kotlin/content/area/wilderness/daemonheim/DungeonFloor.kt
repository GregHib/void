package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import kotlin.math.min

class DungeonFloor : Script {
    init {
        interfaceOpened("dungeon_floor") {
            val members = dungeonMembers
            val highest = members.maxOf { maxFloor(it) }
            for (member in members) {
                update(member, members, highest)
            }
        }

        interfaceClosed("dungeon_floor") {
            clear("dungeon_temp_floor")
        }

        interfaceOption("Select-floor", "dungeon_floor:select_*") {
            if (inDungeoneering) {
                return@interfaceOption
            }
            val floor = it.component.removePrefix("select_").toInt()
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader can select the floor. To lead your own party, use the 'Form party' option on the party details interface when you are not in a party.")
                return@interfaceOption
            }
            for (member in dungeonMembers) {
                if (floor > maxFloor(member)) {
                    if (member != this) {
                        // https://www.youtube.com/watch?v=YrKF-soguEI
                        message("<red_orange>${member.name} is unable to access that floor. Use the bars on the floor select interface to see which floors your party can access.")
                    }
                    return@interfaceOption
                }
            }
            interfaces.sendText("dungeon_floor", "floor", floor.toString())
            set("dungeon_temp_floor", floor)
        }

        interfaceOption("Confirm", "dungeon_floor:confirm") {
            if (inDungeoneering) {
                closeMenu()
                return@interfaceOption
            }
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader change these settings.")
                return@interfaceOption
            }
            val floor: Int = get("dungeon_temp_floor") ?: return@interfaceOption
            for (member in dungeonMembers) {
                member["dungeoneering_party_floor"] = floor
            }
            closeMenu()
        }
    }

    private fun update(member: Player, members: List<Player>, highest: Int) {
        for ((index, player) in members.withIndex()) {
            val maxProgress = maxFloor(player)
            member.interfaces.sendPosition("dungeon_floor", "p$index", y = 10 * maxProgress + 1)
            for (floor in 1..highest) {
                member.interfaces.sendVisibility("dungeon_floor", "p${index}_cf$floor", player["dungeon_floor_${floor}_complete", false])
                member.interfaces.sendVisibility("dungeon_floor", "p${index}_f$floor", floor <= maxProgress)
                member.interfaces.sendVisibility("dungeon_floor", "select_$floor", floor <= maxProgress)
            }
        }
    }

    companion object {
        fun floorAtLevel(player: Player): Int {
            val level = player.levels.getMax(Skill.Dungeoneering)
            return ((level + 1) / 2).coerceAtMost(if (World.members) 60 else 35)
        }
        fun maxFloor(player: Player): Int {
            val level = floorAtLevel(player)
            val unlocked = player["dungeoneering_floor_unlocked", 1]
            return min(level, unlocked)
        }
    }
}
