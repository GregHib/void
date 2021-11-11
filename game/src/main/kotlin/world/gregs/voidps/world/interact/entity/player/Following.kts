package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.utility.inject

val path: PathFinder by inject()

on<PlayerOption>({ option == "Follow" }) { player: Player ->
    player.watch(target)
    player.action(ActionType.Follow) {
        val handler = target.events.on<Player, ActionStarted>({ shouldDisengage(type) }) {
            cancel()
        }
        try {
            while (true) {
                if (!player.reached(target)) {
                    player.movement.clear()
                    path.find(player, Path(target.followTarget), false)
                }
                delay()
            }
        } finally {
            player.watch(null)
            target.events.remove(handler)
        }
    }
}

fun Player.reached(target: Player): Boolean {
    return target.followTarget.reached(tile, size)
}

fun shouldDisengage(type: ActionType): Boolean {
    return type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout
}