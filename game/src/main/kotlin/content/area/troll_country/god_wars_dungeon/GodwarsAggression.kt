package content.area.troll_country.god_wars_dungeon

import content.entity.combat.killer
import content.entity.death.npcDeath
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random

class GodwarsAggression : Script {

    val areas: AreaDefinitions by inject()
    val dungeon = areas["godwars_dungeon_multi_area"]

    init {
        npcSpawn(block = ::randomHuntMode)

        enterArea("godwars_dungeon_multi_area") {
            player.open("godwars_overlay")
            player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
        }

        interfaceOpen("godwars_overlay") {
            sendVariable("armadyl_killcount")
            sendVariable("bandos_killcount")
            sendVariable("saradomin_killcount")
            sendVariable("zamorak_killcount")
            sendVariable("godwars_darkness")
        }

        exitArea("godwars_dungeon_multi_area") {
            player.close("godwars_overlay")
            if (logout) {
                return@exitArea
            }
            player["godwars_darkness"] = false
            player.clear("armadyl_killcount")
            player.clear("bandos_killcount")
            player.clear("saradomin_killcount")
            player.clear("zamorak_killcount")
        }

        itemAdded(inventory = "worn_equipment") { player ->
            val god = item.def.getOrNull<String>("god") ?: return@itemAdded
            if (player.tile in dungeon) {
                player.get<MutableSet<String>>("gods")!!.add(god)
            }
        }

        itemRemoved(inventory = "worn_equipment") { player ->
            if (player.tile in dungeon) {
                player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
            }
        }

        huntPlayer(mode = "godwars_aggressive") { npc ->
            npc.interactPlayer(target, "Attack")
        }

        huntNPC(mode = "zamorak_aggressive") { npc ->
            npc.interactNpc(target, "Attack")
        }

        huntNPC(mode = "anti_zamorak_aggressive") { npc ->
            npc.interactNpc(target, "Attack")
        }

        npcDeath { npc ->
            val killer = npc.killer
            if (killer is NPC) {
                randomHuntMode(npc)
            } else if (killer is Player) {
                val god = npc.def["god", ""]
                if (god != "") {
                    killer.inc("${god}_killcount")
                }
            }
        }
    }

    fun randomHuntMode(npc: NPC) {
        if (npc.tile in dungeon && (npc.def["hunt_mode", ""] == "zamorak_aggressive" || npc.def["hunt_mode", ""] == "anti_zamorak_aggressive")) {
            npc.huntMode = if (random.nextBoolean()) npc.def["hunt_mode"] else "godwars_aggressive"
        }
    }
}
