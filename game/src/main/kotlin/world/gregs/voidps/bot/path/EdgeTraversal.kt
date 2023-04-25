package world.gregs.voidps.bot.path

import world.gregs.voidps.bot.navigation.graph.Edge
import world.gregs.voidps.engine.entity.character.player.Player

class EdgeTraversal {
    fun blocked(player: Player, edge: Edge) : Boolean {
        return edge.requirements.any { !it.has(player) }
    }
}