package world.gregs.voidps.engine.entity.character.npc

import org.koin.dsl.module
import world.gregs.voidps.engine.data.file.FileLoader
import world.gregs.voidps.engine.flatGroupBy
import world.gregs.voidps.engine.map.area.SpawnArea
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getProperty

val npcSpawnModule = module {
    single(createdAtStart = true) {
        NPCSpawns(get())
    }
}

class NPCSpawns(
    private val factory: NPCFactory
) {

    private lateinit var spawns: Map<Region, List<SpawnArea>>

    fun load(region: Region) {
        val areas = spawns[region] ?: return
        for (area in areas) {
            if (area.spawned) {
                continue
            }
            area.spawned = true
            for (spawn in area.spawns) {
                repeat(spawn.limit) {
                    factory.spawn(spawn.name, area.area, spawn.direction)
                }
            }
        }
    }

    init {
        load()
    }

    fun load() = timedLoad("npc spawn") {
        val data: Array<SpawnArea> = get<FileLoader>().load(getProperty("npcsPath"))
        this.spawns = data.flatGroupBy { it.area.regions }
        data.size
    }

}