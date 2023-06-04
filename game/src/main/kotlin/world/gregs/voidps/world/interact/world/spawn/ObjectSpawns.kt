package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.data.definition.extra.ObjectDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.GameMapObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

fun loadObjectSpawns(
    objects: GameObjects,
    storage: FileStorage = get(),
    path: String = getProperty("objectsPath"),
    definitions: ObjectDefinitions = get(),
) = timedLoad("object spawn") {
    objects.clear()
    val data: List<ObjectSpawn> = storage.loadType(path)
    val membersWorld = World.members
    for (spawn in data) {
        if (!membersWorld && spawn.members) {
            continue
        }
        val tile = Tile(spawn.x, spawn.y, spawn.plane)
        objects.add(tile, GameMapObject(definitions.get(spawn.id).id, spawn.type, spawn.rotation))
    }
    data.size
}