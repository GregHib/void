package content.entity.npc.spawn

import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject

val npcs: NPCs by inject()

worldSpawn {
    loadNpcSpawns(npcs)
}