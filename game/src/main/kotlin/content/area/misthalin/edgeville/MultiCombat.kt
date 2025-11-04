package content.area.misthalin.edgeville

import content.area.wilderness.inMultiCombat
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

        variableSet("in_multi_combat") { _, _, to ->
            if (to == true) {
                interfaces.sendVisibility("area_status_icon", "multi_combat", true)
            } else if (to == null) {
                interfaces.sendVisibility("area_status_icon", "multi_combat", false)
            }
        }

        entered("*") {
            if (areaDefinitions.get(tile.zone).any { it.tags.contains("multi_combat") }) {
                set("in_multi_combat", true)
            }
        }

        exited("*") {
            if (inMultiCombat && areaDefinitions.get(tile.zone).none { it.tags.contains("multi_combat") }) {
                clear("in_multi_combat")
            }
        }
    }
}
