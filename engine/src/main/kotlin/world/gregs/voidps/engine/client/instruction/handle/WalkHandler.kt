package world.gregs.voidps.engine.client.instruction.handle

import org.rsmod.pathfinder.PathFinder
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.mode.MovementMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : InstructionHandler<Walk>() {

    private val players: Players by inject()
    private val collisions: Collisions by inject()

    override fun validate(player: Player, instruction: Walk) {
        val pf = PathFinder(flags = get<Collisions>().data, useRouteBlockerFlags = true)
        val route = pf.findPath(
            player.tile.x,
            player.tile.y,
            instruction.x,
            instruction.y,
            player.tile.plane,
            srcSize = 1,
            destWidth = 1,
            destHeight = 1)
        player.watch(null)
        player.interact.clear()
        player.events.clearSuspend()
        player.mode = MovementMode(route, player)
    }

}