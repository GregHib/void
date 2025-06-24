package content.area.morytania.slayer_tower

import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.objectDespawn
import world.gregs.voidps.engine.entity.objectSpawn
import world.gregs.voidps.engine.inject

val objects: GameObjects by inject()

objectSpawn("slayer_tower_door_*_opened") { obj ->
    val statue = if (obj.id == "slayer_tower_door_west_opened") {
        objects[obj.tile.add(-2, -2), "slayer_tower_statue"]
    } else {
        objects[obj.tile.add(1, -2), "slayer_tower_statue"]
    } ?: return@objectSpawn
    statue.anim("slayer_tower_statue_stand")
}

objectDespawn("slayer_tower_door_*_opened") { obj ->
    val statue = if (obj.id == "slayer_tower_door_west_opened") {
        objects[obj.tile.add(-2, -2), "slayer_tower_statue"]
    } else {
        objects[obj.tile.add(1, -2), "slayer_tower_statue"]
    } ?: return@objectDespawn
    statue.anim("slayer_tower_statue_hide")
}