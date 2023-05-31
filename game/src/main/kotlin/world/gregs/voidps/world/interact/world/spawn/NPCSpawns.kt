package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

fun loadNpcSpawns(npcs: NPCs, storage: FileStorage = get(), path: String = getProperty("npcSpawnsPath")) {
    timedLoad("npc spawn") {
        val data: List<NPCSpawn> = storage.load(path)
        val membersWorld = World.members
        var total = 0
        for (spawn in data) {
            if (!membersWorld && spawn.members) {
                continue
            }
            total++
            npcs.add(spawn.id, Tile(spawn.x, spawn.y, spawn.plane), spawn.direction, spawn.delay)
        }
        total
    }
}