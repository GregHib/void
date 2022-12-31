package world.gregs.voidps.engine.client.instruction.handle

import org.rsmod.pathfinder.SmartPathFinder
import world.gregs.voidps.engine.client.instruction.InstructionHandler
import world.gregs.voidps.engine.entity.character.move.toMutableRoute
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.instruct.Walk

class WalkHandler : InstructionHandler<Walk>() {

    override fun validate(player: Player, instruction: Walk) {
        val pf = SmartPathFinder(flags = get<Collisions>().data, useRouteBlockerFlags = true)
        val route = pf.findPath(
            player.tile.x,
            player.tile.y,
            instruction.x,
            instruction.y,
            player.tile.plane,
            srcSize = 1,
            destWidth = 1,
            destHeight = 1).toMutableRoute()
        player.movement.queueRouteTurns(route)
//        player.walkTo(
//            target = player.tile.copy(instruction.x, instruction.y),
//            cancelAction = player.action.type != ActionType.Making
//        )
    }

}