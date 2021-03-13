package world.gregs.voidps.engine.entity.character.npc

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.area.Area
import world.gregs.voidps.engine.map.area.Polygon
import world.gregs.voidps.engine.map.area.Rectangle
import world.gregs.voidps.engine.map.region.Region

val npcSpawnModule = module {
    single(createdAtStart = true) {
        NPCSpawns(getProperty("npcsPath"), get(), get())
    }
}

class NPCSpawns(
    path: String,
    private val loader: NPCLoader,
    private val files: FileLoader
) {

    private val spawns = loadSpawns(path)

    fun load(region: Region) {
        val areas = spawns[region] ?: return
        for (area in areas) {
            for (spawn in area.spawns) {
                repeat(spawn.limit) {
                    loader.spawn(spawn.name, area.area, spawn.direction)
                }
            }
        }
    }

    private fun loadSpawns(path: String): Map<Region, List<NPCSpawnArea>> {
        val spawns: MutableMap<Region, MutableList<NPCSpawnArea>> = mutableMapOf()
        val points: Array<NPCSpawnArea> = files.load(path)
        points.forEach { spawn ->
            val list = spawns.getOrPut(spawn.area.region) { mutableListOf() }
            list.add(spawn)
        }
        return spawns
    }

    private data class NPCSpawnBuilder(
        val name: String,
        val area: SpawnArea,
        val spawns: List<NPCSpawnArea.NPCSpawn>,
        val delay: Int = 60
    ) {

        data class SpawnArea(val x: IntArray, val y: IntArray, val plane: Int = 0)

        fun build() = NPCSpawnArea(
            name = name,
            area = area.let {
                if (it.x.size <= 2) {
                    Rectangle(it.x.first(), it.y.first(), it.x.last(), it.y.last())
                } else {
                    Polygon(it.x, it.y, it.plane)
                }
            },
            spawns = spawns,
            delay = delay
        )
    }

    @JsonDeserialize(builder = NPCSpawnBuilder::class)
    private data class NPCSpawnArea(
        val name: String,
        val area: Area,
        val spawns: List<NPCSpawn>,
        var delay: Int = 0
    ) {
        data class NPCSpawn(
            val name: String,
            val weight: Int = 1,
            val limit: Int = 1,
            val direction: Direction = Direction.NONE
        )
    }
}