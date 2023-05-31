package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.CustomObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

fun loadObjectSpawns(
    customObjects: CustomObjects,
    storage: FileStorage = get(),
    path: String = getProperty("objectsPath")
) = timedLoad("object spawn") {
    customObjects.clear()
    val data: List<ObjectSpawn> = storage.loadType(path)
    val membersWorld = World.members
    for (spawn in data) {
        if (!membersWorld && spawn.members) {
            continue
        }
        val tile = Tile(spawn.x, spawn.y, spawn.plane)
        customObjects.spawn(spawn.id, tile, spawn.type, spawn.rotation)
    }
    data.size
}