package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns
import world.gregs.voidps.world.interact.world.spawn.loadObjectSpawns

val npcs: NPCs by inject()

worldSpawn {
    loadObjectSpawns(get())
    loadNpcSpawns(npcs)
}