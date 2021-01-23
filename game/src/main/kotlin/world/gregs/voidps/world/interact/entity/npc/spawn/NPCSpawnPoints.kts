package world.gregs.voidps.world.interact.entity.npc.spawn

import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.RegionLoaded
import world.gregs.voidps.engine.tick.Startup
import world.gregs.voidps.utility.getProperty
import world.gregs.voidps.utility.inject

val files: FileLoader by inject()
val bus: EventBus by inject()

data class NPCSpawnPoint(val id: Int, val tile: Tile, val direction: Direction = Direction.NONE)

val spawns: MutableMap<Region, MutableList<NPCSpawnPoint>> = mutableMapOf()

Startup then {
    val path: String = getProperty("npcsPath")
    val points: Array<NPCSpawnPoint> = files.load(path)
    points.forEach { spawn ->
        val list = spawns.getOrPut(spawn.tile.region) { mutableListOf() }
        list.add(spawn)
    }
}

RegionLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { spawn ->
        val npc = bus.emit(
            NPCSpawn(
                spawn.id,
                spawn.tile,
                spawn.direction
            )
        )
    }
}