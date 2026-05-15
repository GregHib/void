package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.inDungeoneering
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class DungeonFloor : Script {
    init {
        interfaceOpened("dungeon_floor") {
            val members = dungeonMembers
            var highest = 0
            val max = IntArray(members.size)
            val completed = IntArray(members.size)
            for (i in members.indices) {
                val member = members[i]
                val maxProgress = member["dungeoneering_floor", 1]
                val currentProgress = member["dungeoneering_current_progress", 0]
                max[i] = maxProgress
                completed[i] = currentProgress
                if (maxProgress > highest) {
                    highest = maxProgress
                }
            }
            for (member in members) {
                update(member, highest, max, completed)
            }
        }

        interfaceClosed("dungeon_floor") {
            clear("dungeon_temp_floor")
        }

        interfaceOption("Select-floor", "dungeon_floor:select_*") {
            val floor = it.component.removePrefix("select_").toInt()
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader can select the floor. To lead your own party, use the 'Form party' option on the party details interface when you are not in a party.")
                return@interfaceOption
            }
            for (member in dungeonMembers) {
                if (floor > member["dungeoneering_floor", 1]) {
                    if (member != this) {
                        message("<red_orange>${member.name} is unable to access that floor. Use the bars on the floor select interface to see which floors your party can access.")
                    }
                    return@interfaceOption
                }
            }
            interfaces.sendText("dungeon_floor", "floor", floor.toString())
            set("dungeon_temp_floor", floor)
        }

        interfaceOption("Confirm", "dungeon_floor:confirm") {
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

    private fun update(member: Player, highest: Int, maxes: IntArray, completes: IntArray) {
        val selectable = !member.inDungeoneering
        for (player in 0 until 5) {
            val max = maxes.getOrNull(player)
            val completed = completes.getOrNull(player)
            if (max == null || completed == null) {
                member.interfaces.sendVisibility("dungeon_floor", "p$player", true)
                continue
            }
            member.interfaces.sendPosition("dungeon_floor", "p$player", y = 10 * max + 1)
            for (floor in 1..highest) {
                member.interfaces.sendVisibility("dungeon_floor", "p${player}_cf$floor", floor <= completed)
                member.interfaces.sendVisibility("dungeon_floor", "p${player}_f$floor", floor <= max)
                member.interfaces.sendVisibility("dungeon_floor", "select_$floor", selectable && floor <= max)
            }
        }
    }
}
