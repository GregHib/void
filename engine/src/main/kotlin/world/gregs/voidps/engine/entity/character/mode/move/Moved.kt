package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.dispatch.ListDispatcher
import world.gregs.voidps.engine.dispatch.MapDispatcher
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

/**
 * Entity moved between [from] and [to] tiles
 */
interface Moved {
    fun move(player: Player, from: Tile, to: Tile) {}
    fun move(npc: NPC, from: Tile, to: Tile) {}

    companion object : Moved {
        var playerDispatcher = ListDispatcher<Moved>()
        var npcDispatcher = MapDispatcher<Moved>("@Id", "")

        override fun move(player: Player, from: Tile, to: Tile) {
            for (instance in playerDispatcher.instances) {
                instance.move(player, from, to)
            }
        }

        override fun move(npc: NPC, from: Tile, to: Tile) {
            npcDispatcher.forEach(npc.id, "*") { instance ->
                instance.move(npc, from, to)
            }
        }
    }
}