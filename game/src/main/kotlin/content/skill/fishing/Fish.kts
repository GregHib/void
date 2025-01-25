package content.skill.fishing

import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.random

val minRespawnTick = 280
val maxRespawnTick = 530

npcSpawn("fishing_spot*") { npc ->
    val area: Area = npc["area"] ?: return@npcSpawn
    move(npc, area)
}

fun move(npc: NPC, area: Area) {
    npc.softQueue("spot_move", random.nextInt(minRespawnTick, maxRespawnTick)) {
        area.random(npc)?.let { tile ->
            npc.tele(tile)
        }
        move(npc, area)
    }
}