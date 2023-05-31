package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

fun loadNpcSpawns(
    npcs: NPCs,
    storage: FileStorage = get(),
    path: String = getProperty("npcSpawnsPath")
) {
    timedLoad("npc spawn") {
        npcs.clear()
        val data: List<NPCSpawn> = storage.loadType(path)
        val membersWorld = World.members
        for (spawn in data) {
            if (!membersWorld && spawn.members) {
                continue
            }
            val tile = Tile(spawn.x, spawn.y, spawn.plane)
            npcs.add(spawn.id, tile, spawn.direction, spawn.delay)
        }
        data.size
    }
}