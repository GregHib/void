package content.area.misthalin

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.Tile

@Suppress("UNCHECKED_CAST")
objectOperate("Read", "direction_signpost_*") {
    val locations = target.def.extras?.get("locations")!! as ObjectArrayList<Object2ObjectOpenHashMap<String, String>>

    val location =
        locations.firstOrNull {
            Tile(it["x"]!!.toInt(), it["y"]!!.toInt()) == target.tile
        } ?: return@objectOperate

    player.open("signpost_directions")
    player.interfaces.sendText("signpost_directions", "north", location["north_text"]!!)
    player.interfaces.sendText("signpost_directions", "east", location["east_text"]!!)
    player.interfaces.sendText("signpost_directions", "south", location["south_text"]!!)
    player.interfaces.sendText("signpost_directions", "west", location["west_text"]!!)
}
