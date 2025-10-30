package content.area.misthalin

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Tile

class Signposts : Script {

    init {
        @Suppress("UNCHECKED_CAST")
        objectOperate("Read", "direction_signpost_*") { (target) ->
            val locations = target.def.extras?.get("locations") as? ObjectArrayList<Object2ObjectOpenHashMap<String, String>> ?: return@objectOperate

            val location =
                locations.firstOrNull {
                    Tile(it.getOrDefault("x", "0").toInt(), it.getOrDefault("y", "0").toInt()) == target.tile
                } ?: return@objectOperate

            open("signpost_directions")
            interfaces.sendText("signpost_directions", "north", location.getOrDefault("north_text", ""))
            interfaces.sendText("signpost_directions", "east", location.getOrDefault("east_text", ""))
            interfaces.sendText("signpost_directions", "south", location.getOrDefault("south_text", ""))
            interfaces.sendText("signpost_directions", "west", location.getOrDefault("west_text", ""))
        }
    }
}
