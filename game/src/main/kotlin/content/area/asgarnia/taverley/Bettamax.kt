package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.Id
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class Bettamax : Api {

    val npcs: NPCs by inject()

    @Id("wilbur")
    override fun spawn(npc: NPC) {
        val bettamax = npcs[npc.tile.zone].first { it.id == "bettamax" }
        npc.mode = Follow(npc, bettamax)
        npc.watch(bettamax)
    }
}
