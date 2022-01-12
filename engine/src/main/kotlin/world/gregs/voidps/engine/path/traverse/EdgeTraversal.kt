package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.nav.Edge

class EdgeTraversal {
    fun blocked(player: Player, edge: Edge) : Boolean {
        return edge.requirements.any { !it.has(player) }
    }
}