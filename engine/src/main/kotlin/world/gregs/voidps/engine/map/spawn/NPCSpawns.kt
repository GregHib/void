package world.gregs.voidps.engine.map.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty

fun loadNpcSpawns(npcs: NPCs, storage: FileStorage = get(), path: String = getProperty("npcSpawnsPath")) {
    timedLoad("npc spawn") {
        val data: List<Map<String, Any>> = storage.load(path)
        val areas = data.map { NPCSpawn.fromMap(it) }
        for (spawn in areas) {
            npcs.add(spawn.id, spawn.tile, spawn.direction, spawn.delay)
        }
        areas.size
    }
}