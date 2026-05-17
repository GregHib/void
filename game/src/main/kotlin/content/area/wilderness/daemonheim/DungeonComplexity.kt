package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class DungeonComplexity : Script {
    private val types = listOf(
        "combat",
        "cooking",
        "firemaking",
        "woodcutting",
        "fishing",
        "weapons",
        "mining",
        "runecrafting",
        "textiles",
        "hunter",
        "armour",
        "seeds",
        "herblore",
        "thieving",
        "summoning",
        "construction",
    )

    init {
        interfaceOpened("dungeon_complexity") {
            val selected = get("dungeoneering_party_complexity", 1)
            select(selected)
        }

        interfaceClosed("dungeon_complexity") {
            clear("dungeon_temp_complexity")
        }

        interfaceOption("Select-complexity", "dungeon_complexity:complexity_*") {
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader can select the complexity level. To lead your own party, use the 'Form Party' option on the party details interface when you are not in a party.")
                return@interfaceOption
            }
            val index = it.component.removePrefix("complexity_").toInt()
            for (member in dungeonMembers) {
                if (index > member["dungeoneering_complexity", 1]) {
                    if (member == this) {
                        message("<red_orange>To access complexity level $index, you must complete one dungeon floor on complexity level ${index - 1}.")
                    } else {
                        message("<red_orange>${member.name} is unable to access that complexity level. Use the select complexity interface to see which complexity levels your party can access.")
                    }
                    return@interfaceOption
                }
            }
            select(index)
        }

        interfaceOption("Confirm", "dungeon_complexity:confirm") {
            if (dungeonLeader != this) {
                message("<red_orange>Only the party leader can change these settings.")
                return@interfaceOption
            }
            val complexity: Int = get("dungeon_temp_complexity") ?: return@interfaceOption
            for (member in dungeonMembers) {
                member["dungeoneering_party_complexity"] = complexity
            }
            closeMenu()
        }
    }

    private fun Player.select(complexity: Int) {
        if (get("dungeon_temp_complexity", 0) == complexity) {
            return
        }
        for (i in 1..6) {
            interfaces.sendVisibility("dungeon_complexity", "complexity_${i}_selected", i == complexity)
        }
        val penalty = if (complexity == 6) 0 else 55 - (complexity * 5)
        interfaces.sendText("dungeon_complexity", "xp_penalty", "$penalty% XP Penalty")
        val amount = when (complexity) {
            1 -> 1
            2 -> 5
            3 -> 10
            else -> types.size
        }
        for (i in types.indices) {
            val type = types[i]
            val active = i <= amount
            val component = InterfaceDefinitions.getComponent("dungeon_complexity", "${type}_sprite")!!
            interfaces.sendColour("dungeon_complexity", "${type}_text", if (active) 0xe2e2a2 else 0x4e4e4e)
            interfaces.sendSprite(
                "dungeon_complexity",
                "${type}_sprite",
                if (active) {
                    component.defaultImage
                } else if (component.defaultImage == 3183) {
                    3184
                } else {
                    component.defaultImage + 15
                },
            )
        }
        interfaces.sendText("dungeon_complexity", "complexity", complexity.toString())
        set("dungeon_temp_complexity", complexity)
    }
}
