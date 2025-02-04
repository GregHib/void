package content.entity.obj.canoe

import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.suspend.StringSuspension

interfaceClose("canoe_stations_map") { player ->
    player.dialogueSuspension = null
}

interfaceOption("Select", "travel_*", "canoe_stations_map") {
    val destination = component.removePrefix("travel_")
    (player.dialogueSuspension as? StringSuspension)?.resume(destination)
}