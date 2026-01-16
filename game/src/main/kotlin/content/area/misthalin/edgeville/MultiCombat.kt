package content.area.misthalin.edgeville

import content.area.wilderness.inMultiCombat
import content.quest.instanceOffset
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions

class MultiCombat(val areaDefinitions: AreaDefinitions) : Script {

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
            val offset = instanceOffset()
            if (areaDefinitions.get(tile.minus(offset).zone).any { it.tags.contains("multi_combat") }) {
                set("in_multi_combat", true)
            }
        }

        exited("*") {
            val offset = instanceOffset()
            if (inMultiCombat && areaDefinitions.get(tile.minus(offset).zone).none { it.tags.contains("multi_combat") }) {
                clear("in_multi_combat")
            }
        }
    }
}
