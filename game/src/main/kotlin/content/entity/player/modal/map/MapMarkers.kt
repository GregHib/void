package content.entity.player.modal.map

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

object MapMarkers {
    fun add(player: Player, tile: Tile, type: String = "yellow", text: String = "") {
        for (i in 1..5) {
            if (player.contains("world_map_marker_$i")) {
                continue
            }
            player["world_map_marker_$i"] = tile.id
            player["world_map_marker_text_$i"] = text
            player["world_map_marker_type_$i"] = type
            break
        }
    }

    fun remove(player: Player, tile: Tile, type: String = "yellow", text: String = "") {
        for (i in 1..5) {
            if (!player.contains("world_map_marker_$i")) {
                continue
            }
            val marker = player["world_map_marker_$i", -1]
            if (marker == tile.id && type == player["world_map_marker_type_$i", "yellow"]) {
                player.clear("world_map_marker_$i")
                player.clear("world_map_marker_text_$i")
                player.clear("world_map_marker_type_$i")
            }
            break
        }
    }
}
