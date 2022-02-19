package world.gregs.voidps.engine.entity.obj

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.definition.ObjectDefinitions
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

@Suppress("UNCHECKED_CAST")
fun loadObjectSpawns(
    customObjects: CustomObjects,
    definitions: ObjectDefinitions,
    storage: FileStorage = get(),
    path: String = getProperty("objectsPath")
) = timedLoad("object spawn") {
    val data: Array<Map<String, Any>> = storage.load(path)
    for (it in data) {
        val tile = Tile.fromMap(it["tile"] as Map<String, Any>)
        val id = definitions.get(it["id"] as Int).stringId
        customObjects.spawn(id, tile, it["type"] as Int, it["rotation"] as Int)
    }
    data.size
}