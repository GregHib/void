package content.entity.player

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.type.Tile

@Script
class ForceMovement : Api {

    override fun move(player: Player, from: Tile, to: Tile) {
        val block: () -> Unit = player.remove("force_walk") ?: return
        block.invoke()
    }

    override fun move(npc: NPC, from: Tile, to: Tile) {
        val block: () -> Unit = npc.remove("force_walk") ?: return
        block.invoke()
    }

}
