package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.path.strat.DistanceFromTargetStrategy

fun NPC.retreat(target: Character, distance: Int = 25) {
    action(ActionType.Movement) {
        val strategy = DistanceFromTargetStrategy(target, distance)
        while (isActive && !strategy.reached(tile, size)) {
            movement.clear()
            watch(target)
            val opposite = tile.delta(strategy.target.tile)
            walkTo(tile.add(opposite))
            delay(5)
        }
    }
}