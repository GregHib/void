package rs.dusk.world.entity.npc

import rs.dusk.engine.data.file.FileLoader
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Startup
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.factory.NPCFactory
import rs.dusk.utility.getProperty
import rs.dusk.utility.inject

val npcs: NPCFactory by inject()
val files: FileLoader by inject()

data class Spawn(val id: Int, val x: Int, val y: Int, val plane: Int = 0, val direction: Direction = Direction.NONE)

Startup then {
    val path: String = getProperty("npcSpawnsPath")
    val spawns: Array<Spawn> = files.load(path)
    spawns.forEach {
        npcs.spawn(it.id, it.x, it.y, it.plane, it.direction)
    }
}