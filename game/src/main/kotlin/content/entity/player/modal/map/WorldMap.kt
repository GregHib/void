package content.entity.player.modal.map

import content.entity.effect.frozen
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.network.client.instruction.WorldMapClick
import world.gregs.voidps.network.login.protocol.encode.updateInterface
import world.gregs.voidps.engine.event.Script
@Script
class WorldMap {

    val definitions: InterfaceDefinitions by inject()
    
    init {
        interfaceOpen("world_map") { player ->
            updateMap(player)
            if (player.steps.isNotEmpty()) {
                player.softTimers.start("world_map_check")
            }
            player.sendVariable("world_map_hide_player_location")
            player.sendVariable("world_map_hide_links")
            player.sendVariable("world_map_hide_labels")
            player.sendVariable("world_map_hide_tooltips")
            player.sendVariable("world_map_marker_custom")
            player.interfaceOptions.unlockAll("world_map", "key_list", 0..182)
        }

        timerStart("world_map_check") {
            interval = 5
        }

        timerTick("world_map_check") { player ->
            updateMap(player)
            if (player.steps.isEmpty() || !player.hasOpen("world_map")) {
                cancel()
            }
        }

        interfaceOption("Re-sort key", "order", "world_map") {
            player["world_map_list_order"] = when (player["world_map_list_order", "categorised"]) {
                "categorised" -> "traditional"
                "traditional" -> "alphabetical"
                "alphabetical" -> "categorised"
                else -> "categorised"
            }
        }

        interfaceOption(id = "world_map", component = "key_list") {
            when (itemSlot) {
                1 -> player.toggle("world_map_hide_player_location")
                4 -> player.toggle("world_map_hide_links")
                12 -> player.toggle("world_map_hide_labels")
                16 -> player.toggle("world_map_hide_tooltips")
                19 -> player["world_map_marker_custom"] = 0
            }
        }

        interfaceOption("Clear marker", "marker", "world_map") {
            player["world_map_marker_custom"] = 0
        }

        interfaceOption(component = "world_map", id = "toplevel*") {
            if (player.frozen) {
                player.message("You cannot do this at the moment.") // TODO proper message
            } else {
                player.open("world_map")
            }
        }

        interfaceOption(component = "close", id = "world_map") {
            // Mechanics are unknown, would need tracking last interface to handle inside Interfaces.kt
            player.client?.updateInterface(definitions.get(player.interfaces.gameFrame).id, 2)
            player.open(player.interfaces.gameFrame, close = false)
        }

        instruction<WorldMapClick> { player ->
            if (player.hasClock("world_map_double_click") && player["previous_world_map_click", 0] == tile) {
                player["world_map_marker_custom"] = tile
            }
            player["previous_world_map_click"] = tile
            player.start("world_map_double_click", 1)
        }

    }

    fun updateMap(player: Player) {
        val tile = player.tile.id
        player["world_map_centre"] = tile
        player["world_map_marker_player"] = tile
    }
    
}
