package content.area.misthalin

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.event.Script
@Script
class Signposts {

    init {
        objectOperate("Read", "direction_signpost_*") {
            val locations = target.def.extras?.get("locations") as? ObjectArrayList<Object2ObjectOpenHashMap<String, String>> ?: return@objectOperate
        
            val location =
                locations.firstOrNull {
                    Tile(it.getOrDefault("x", "0").toInt(), it.getOrDefault("y", "0").toInt()) == target.tile
                } ?: return@objectOperate
        
            player.open("signpost_directions")
            player.interfaces.sendText("signpost_directions", "north", location.getOrDefault("north_text", ""))
            player.interfaces.sendText("signpost_directions", "east", location.getOrDefault("east_text", ""))
            player.interfaces.sendText("signpost_directions", "south", location.getOrDefault("south_text", ""))
            player.interfaces.sendText("signpost_directions", "west", location.getOrDefault("west_text", ""))
        }

    }

    @Suppress("UNCHECKED_CAST")
}
