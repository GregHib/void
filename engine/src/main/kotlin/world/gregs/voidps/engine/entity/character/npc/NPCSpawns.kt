package world.gregs.voidps.engine.entity.character.npc

import org.koin.dsl.module
import world.gregs.voidps.engine.TimedLoader
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.map.area.SpawnArea
import world.gregs.voidps.engine.map.region.Region

val npcSpawnModule = module {
    single(createdAtStart = true) {
        NPCSpawns(get(), get()).run(getProperty("npcsPath"))
    }
}

class NPCSpawns(
    private val factory: NPCFactory,
    private val files: FileLoader
) : TimedLoader<NPCSpawns>("npc spawn") {

    private lateinit var spawns: Map<Region, List<SpawnArea>>

    override fun load(args: Array<out Any?>): NPCSpawns {
        val path = args.first() as String
        val spawns: MutableMap<Region, MutableList<SpawnArea>> = mutableMapOf()
        val points: Array<SpawnArea> = files.load(path)
        points.forEach { spawn ->
            val list = spawns.getOrPut(spawn.area.region) { mutableListOf() }
            list.add(spawn)
            count++
        }
        this.spawns = spawns
        return this
    }

    fun load(region: Region) {
        val areas = spawns[region] ?: return
        for (area in areas) {
            for (spawn in area.spawns) {
                repeat(spawn.limit) {
                    factory.spawn(spawn.name, area.area, spawn.direction)
                }
            }
        }
    }
}