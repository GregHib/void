package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs

class Bettamax(
    val npcs: NPCs,
) : Script {

    init {
        npcSpawn("wilbur") {
            val bettamax = npcs[tile.zone].first { it.id == "bettamax" }
            mode = Follow(this, bettamax)
            watch(bettamax)
        }
    }
}
