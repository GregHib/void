package content.area.asgarnia.taverley

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs

class Bettamax : Script {

    init {
        npcSpawn("wilbur") {
            val bettamax = NPCs.at(tile.zone).first { it.id == "bettamax" }
            mode = Follow(this, bettamax)
            watch(bettamax)
        }
    }
}
