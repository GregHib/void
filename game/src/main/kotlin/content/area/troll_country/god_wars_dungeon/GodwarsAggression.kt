package content.area.troll_country.god_wars_dungeon

import content.entity.combat.killer
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.type.random

class GodwarsAggression : Script {

    val areas: AreaDefinitions by inject()
    val dungeon = areas["godwars_dungeon_multi_area"]

    init {
        npcSpawn(handler = ::randomHuntMode)

        entered("godwars_dungeon_multi_area") {
            open("godwars_overlay")
            set("gods", equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet())
        }

        interfaceOpened("godwars_overlay") {
            sendVariable("armadyl_killcount")
            sendVariable("bandos_killcount")
            sendVariable("saradomin_killcount")
            sendVariable("zamorak_killcount")
            sendVariable("godwars_darkness")
        }

        exited("godwars_dungeon_multi_area") {
            close("godwars_overlay")
            if (get("logged_out", false)) {
                return@exited
            }
            set("godwars_darkness", false)
            clear("armadyl_killcount")
            clear("bandos_killcount")
            clear("saradomin_killcount")
            clear("zamorak_killcount")
        }

        itemAdded(inventory = "worn_equipment") { (item) ->
            val god = item.def.getOrNull<String>("god") ?: return@itemAdded
            if (tile in dungeon) {
                get<MutableSet<String>>("gods")!!.add(god)
            }
        }

        itemRemoved(inventory = "worn_equipment") {
            if (tile in dungeon) {
                set("gods", equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet())
            }
        }

        huntPlayer(mode = "godwars_aggressive") { target ->
            interactPlayer(target, "Attack")
        }

        huntNPC(mode = "zamorak_aggressive") { target ->
            interactNpc(target, "Attack")
        }

        huntNPC(mode = "anti_zamorak_aggressive") { target ->
            interactNpc(target, "Attack")
        }

        npcDeath {
            val killer = killer
            if (killer is NPC) {
                randomHuntMode(this)
            } else if (killer is Player) {
                val god = def["god", ""]
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
