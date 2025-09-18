package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.variable.variableSet
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class MultiCombat : Api {

    val areaDefinitions: AreaDefinitions by inject()

    override fun spawn(npc: NPC) {
        for (def in areaDefinitions.get(npc.tile.zone)) {
            if (def.tags.contains("multi_combat")) {
                npc["in_multi_combat"] = true
                break
            }
        }
    }

    init {
        enterArea(tag = "multi_combat") {
            player["in_multi_combat"] = true
        }

        exitArea(tag = "multi_combat") {
            player.clear("in_multi_combat")
        }

        variableSet("in_multi_combat", to = true) { player ->
            player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
        }

        variableSet("in_multi_combat", to = null) { player ->
            player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
        }
    }
}
