package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

@Suppress("UNCHECKED_CAST")
fun loadObjectSpawns(
    customObjects: CustomObjects,
    definitions: ObjectDefinitions,
    storage: FileStorage = get(),
    path: String = getProperty("objectsPath")
) = timedLoad("object spawn") {
    val data: Array<Map<String, Any>> = storage.load(path)
    val membersWorld = World.members
    for (it in data) {
        if (!membersWorld && it["members"] != false) {
            continue
        }
        val tile = Tile.fromMap(it["tile"] as Map<String, Any>)
        val id = definitions.get(it["id"] as Int).stringId
        customObjects.spawn(id, tile, it["type"] as Int, it["rotation"] as Int)
    }
    data.size
}