package content.skill.fishing

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.random

@Script
class Fish : Api {

    val minRespawnTick = 280
    val maxRespawnTick = 530

    override fun spawn(npc: NPC) {
        if (npc.id.startsWith("fishing_spot")) {
            val area: Area = npc["area"] ?: return
            move(npc, area)
        }
    }

    fun move(npc: NPC, area: Area) {
        npc.softQueue("spot_move", random.nextInt(minRespawnTick, maxRespawnTick)) {
            area.random(npc)?.let { tile ->
                npc.tele(tile)
            }
            move(npc, area)
        }
    }
}
