package content.entity.obj.canoe

import world.gregs.voidps.engine.client.ui.event.interfaceOpen

val stations = mapOf(
    12342 to "edgeville",
    12850 to "lumbridge",
    12852 to "champions_guild",
    12341 to "barbarian_village",
)

interfaceOpen("canoe_stations_map") { player ->
    val canoe = ""
    val location = stations[player.tile.region.id]
    val canTravelToWildy = false

    for (station in stations.values) {
        val here = station == location
        player.interfaces.sendVisibility(id, "you_are_here_$station", here)
        player.interfaces.sendVisibility(id, station, !here)
    }
    player.interfaces.sendVisibility(id, "wilderness_warning", canTravelToWildy)
}