package content.entity.obj.spawn

import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

worldSpawn {
    loadObjectSpawns(objects)
}