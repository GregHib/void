package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionStarted
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

fun Character.follow(target: Character, targetStrategy: TileTargetStrategy = if (target is Player) target.followTarget else target.interactTarget) {
    watch(target)
    action(ActionType.Follow) {
        val handler = target.events.on<Character, ActionStarted>({ type == ActionType.Teleport || type == ActionType.Climb || type == ActionType.Logout || type == ActionType.Dying }) {
            cancel()
        }
        try {
            while (isActive) {
                if (!targetStrategy.reached(tile, size)) {
                    movement.set(targetStrategy)
                }
                delay()
            }
        } finally {
            watch(null)
            target.events.remove(handler)
        }
    }
}