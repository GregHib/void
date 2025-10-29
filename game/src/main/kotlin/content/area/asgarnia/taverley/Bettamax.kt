package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class Bettamax : Api {

    val npcs: NPCs by inject()

    init {
        npcSpawn("wilbur") { npc ->
            val bettamax = npcs[npc.tile.zone].first { it.id == "bettamax" }
            npc.mode = Follow(npc, bettamax)
            npc.watch(bettamax)
        }
    }
}
