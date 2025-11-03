package content.entity.obj.canoe

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.suspend.StringSuspension

class CanoeStationMap : Script {

    init {
        interfaceClose("canoe_stations_map") {
            dialogueSuspension = null
        }

        interfaceOption("Select", "canoe_stations_map:travel_*") {
            val destination = it.component.removePrefix("travel_")
            (dialogueSuspension as? StringSuspension)?.resume(destination)
        }
    }
}
