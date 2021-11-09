package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

class NPCSpawns(
    private val npcs: NPCs
) {
    private lateinit var spawns: Map<Region, List<NPCSpawn>>
    private val loaded = mutableSetOf<Region>()

    fun load(region: Region) {
        val spawns = spawns[region] ?: return
        if (loaded.contains(region)) {
            return
        }
        loaded.add(region)
        for (spawn in spawns) {
            npcs.add(spawn.id, spawn.tile, spawn.direction, spawn.delay)
        }
    }

    fun clear() {
        npcs.forEach { npcs.remove(it) }
        loaded.clear()
    }

    fun load(storage: FileStorage = get(), path: String = getProperty("npcSpawnsPath")): NPCSpawns {
        timedLoad("npc spawn") {
            val data: List<Map<String, Any>> = storage.load(path)
            val areas = data.map { NPCSpawn.fromMap(it) }
            this.spawns = areas.groupBy { it.tile.region }
            areas.size
        }
        return this
    }
}