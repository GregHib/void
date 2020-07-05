package rs.dusk.world.entity.npc

import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.factory.NPCFactory
import rs.dusk.engine.model.world.Region
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.model.world.map.MapLoaded
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject

val npcs: NPCFactory by inject()
val files: FileLoader by inject()

data class NPCSpawn(val id: Int, val tile: Tile, val direction: Direction = Direction.NONE)

val spawns: MutableMap<Region, MutableList<NPCSpawn>> = mutableMapOf()

Startup then {
    val path: String = getProperty("npcSpawnsPath")
    val points: Array<NPCSpawn> = files.load(path)
    points.forEach { spawn ->
        val list = spawns.getOrPut(spawn.tile.region) { mutableListOf() }
        list.add(spawn)
    }
}

MapLoaded then {
    val spawns = spawns[region] ?: return@then
    spawns.forEach { spawn ->
        npcs.spawn(spawn.id, spawn.tile.x, spawn.tile.y, spawn.tile.plane, spawn.direction)
    }
}