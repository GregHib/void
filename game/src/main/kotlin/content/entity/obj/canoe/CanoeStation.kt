package content.entity.obj.canoe

import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.StringSuspension
import world.gregs.voidps.engine.suspend.SuspendableContext
import kotlin.math.abs

private val stations = listOf(
    "lumbridge",
    "champions_guild",
    "barbarian_village",
    "edgeville",
    "wilderness_pond",
)

private val distance = mapOf(
    "log" to 1,
    "dugout" to 2,
    "stable_dugout" to 3,
    "waka" to 4,
)

private const val INTERFACE_ID = "canoe_stations_map"

internal suspend fun Player.canoeStationMap(canoe: String, station: String): String? {
    check(open(INTERFACE_ID)) { "Unable to open canoe station map for $this" }
    val index = stations.indexOf(station)
    val distance = distance[canoe]!!

    for (i in stations.indices) {
        val reachable = i in index - distance..index + distance
        val name = stations[i]
        val here = i == index
        interfaces.sendVisibility(INTERFACE_ID, "you_are_here_$name", here)
        interfaces.sendVisibility(INTERFACE_ID, "${name}_group", !here && reachable)
    }
    val selection = StringSuspension.get(this)
    close(INTERFACE_ID)
    if (selection == station) {
        return null
    }
    val targetIndex = stations.indexOf(selection)
    if (targetIndex !in index - distance..index + distance) {
        return null
    }
    return selection
}

internal suspend fun Player.canoeTravel(canoe: String, station: String, destination: String) {
    check(open("canoe_travel")) { "Unable to open canoe travel map for $this" }
    sendScript(
        "model_swapper",
        InterfaceDefinition.pack(758, 3),
        when (canoe) {
            "stable_dugout" -> 40514
            "waka" -> 40515
            "log" -> 40516
            "dugout" -> 40517
            else -> throw UnsupportedOperationException("No canoe type found for: $canoe")
        },
    )
    val distance = abs(stations.indexOf(destination) - stations.indexOf(station))
    interfaces.sendAnimation("canoe_travel", "model", "${station}_to_$destination")
    delay(distance + 2)
    close("canoe_travel")
}
