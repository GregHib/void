package world.gregs.voidps.engine.path.traverse

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.path.TraversalStrategy

class EdgeTraversal : TraversalStrategy {
    fun blocked(player: Player, edge: Edge) : Boolean = false
}