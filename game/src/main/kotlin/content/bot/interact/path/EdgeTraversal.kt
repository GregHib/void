package content.bot.interact.path

import content.bot.interact.navigation.graph.Edge
import world.gregs.voidps.engine.entity.character.player.Player

class EdgeTraversal {
    fun blocked(player: Player, edge: Edge) : Boolean {
        return edge.requirements.any { !it.has(player) }
    }
}