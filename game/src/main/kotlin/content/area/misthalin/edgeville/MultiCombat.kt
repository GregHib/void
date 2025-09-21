package content.area.misthalin.edgeville

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.*
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
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

    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (key == "in_multi_combat") {
            if (to == true) {
                player.interfaces.sendVisibility("area_status_icon", "multi_combat", true)
            } else if (to == null) {
                player.interfaces.sendVisibility("area_status_icon", "multi_combat", false)
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
    }
}
