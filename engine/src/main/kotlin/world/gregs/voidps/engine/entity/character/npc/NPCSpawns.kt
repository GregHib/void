package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.timedLoad

/**
 * Adds the npcs from the last [NPCSpawnLoader] run to the world.
 */
fun loadNpcSpawns() {
    timedLoad("npc spawn") {
        NPCs.clear()
        val spawns = NPCSpawnLoader.spawns
        for (spawn in spawns) {
            NPCs.add(spawn.id, spawn.tile, spawn.direction)
        }
        spawns.size
    }
}
