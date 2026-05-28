package content.entity.obj.canoe

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.suspend.Suspension

class CanoeStationMap : Script {

    init {
        interfaceClosed("canoe_stations_map") {
            suspension = null
        }

        interfaceOption("Select", "canoe_stations_map:travel_*") {
            val destination = it.component.removePrefix("travel_")
            (suspension as? Suspension.StringEntry)?.resume(destination)
        }
    }
}
