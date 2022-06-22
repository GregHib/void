package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players

class HitsTask(
    val players: Players,
    val npcs: NPCs
) : Runnable {
    override fun run() {
        for (npc in npcs) {
            if (npc.hits.isNotEmpty()) {
                for (hit in npc.hits) {
                    npc.events.emit(hit)
                }
                npc.hits.clear()
            }
        }
        for (player in players) {
            if (player.hits.isNotEmpty()) {
                for (hit in player.hits) {
                    player.events.emit(hit)
                }
                player.hits.clear()
            }
        }
    }
}