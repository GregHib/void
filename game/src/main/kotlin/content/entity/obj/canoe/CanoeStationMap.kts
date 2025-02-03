package content.entity.obj.canoe

import content.entity.sound.playSound
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

val stations = listOf(
    "lumbridge",
    "champions_guild",
    "barbarian_village",
    "edgeville",
    "wilderness_pond"
)

val distance = listOf(1, 2, 3, 4)

val destinations = listOf(
    Tile(0, 0),
    Tile(3199, 3344),
    Tile(0, 0),
    Tile(0, 0),
    Tile(0, 0),
)

interfaceOpen("canoe_stations_map") { player ->
    val canoe = "dugout"
    val station = "lumbridge"
    val index = stations.indexOf(station)
    val distance = distance[1]

    for (i in stations.indices) {
        val reachable = i in index - distance..index + distance
        val name = stations[i]
        val here = i == index
        player.interfaces.sendVisibility(id, "you_are_here_$name", here)
        player.interfaces.sendVisibility(id, "${name}_group", !here && reachable)
        player.interfaces.sendVisibility(id, "wilderness_warning", canoe == "waka" && reachable)
    }
}

val objects: GameObjects by inject()

interfaceOption("Select", "travel_*", "canoe_stations_map") {
    val destination = component.removePrefix("travel_")
    val station = "lumbridge"
    if (destination == station) {
        return@interfaceOption
    }
    val distance = distance[1]
    val index = stations.indexOf(station)
    val targetIndex = stations.indexOf(destination)
    if (targetIndex !in index - distance..index + distance) {
        return@interfaceOption
    }
    player.tele(destinations[targetIndex])
    player.message("You arrive at the Champions' Guild.<br>Your canoe sinks into the water after the hard journey.", type = ChatType.Filter)
    player["canoe_station_base"] = 0
    objects.add("a_sinking_canoe_log", tile = Tile(3205, 3334), rotation = 1, ticks = 3)
    player.playSound("canoe_sink")
}