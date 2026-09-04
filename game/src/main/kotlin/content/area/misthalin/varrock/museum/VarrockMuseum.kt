package content.area.misthalin.varrock.museum

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open

class VarrockMuseum : Script {
    init {
        objectOperate("Look-at", "vm_museum_wallmounted_map_0*") {
            when (tile.level) {
                0 -> open("vm_map_digsite")
                1 -> open("vm_map_timeline1")
                2 -> open("vm_map_timeline2")
                else -> open("vm_map_basement")
            }
        }

        itemOption("Look-at", "museum_map") {
            open("vm_map_digsite")
        }

        interfaceOption("Stairs to Natural History exhibit", "vm_map_digsite:lvl0_level_down_1*") {
            open("vm_map_basement")
        }

        interfaceOption("Stairs to Timeline exhibit", "vm_map_digsite:lvl0_level_up*,vm_map_timeline2:lvl2_level_down*") {
            open("vm_map_timeline1")
        }

        interfaceOption("Stairs to Dig Site exhibit", "vm_map_basement:basement_level_up_1*,vm_map_timeline1:lvl1_level_down*") {
            open("vm_map_digsite")
        }

        interfaceOption("Stairs to Timeline exhibit", "vm_map_timeline1:lvl1_level_up*") {
            open("vm_map_timeline2")
        }
    }
}
