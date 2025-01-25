package world.gregs.voidps.world.interact.world

import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.world.spawn.loadNpcSpawns

val npcs: NPCs by inject()
val definitions: NPCDefinitions by inject()

worldSpawn {
    loadNpcSpawns(npcs)
}

adminCommand("reload") {
    if (content == "npcs") {
        definitions.load()
        val npcs: NPCs = get()
        loadNpcSpawns(npcs)
    }
}