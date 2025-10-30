package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.*
import world.gregs.voidps.engine.inject

class MultiCombat : Script {

    val areaDefinitions: AreaDefinitions by inject()

    init {
        npcSpawn {
            for (def in areaDefinitions.get(tile.zone)) {
                if (def.tags.contains("multi_combat")) {
                    this["in_multi_combat"] = true
                    break
                }
            }
        }

        variableSet("in_multi_combat") { player, _, _, to ->
            if (to == true) {
                player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
            } else if (to == null) {
                player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
            }
        }

        enterArea(tag = "multi_combat") {
            player["in_multi_combat"] = true
        }

        exitArea(tag = "multi_combat") {
            player.clear("in_multi_combat")
        }
    }
}
