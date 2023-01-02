package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.watch

fun NPC.retreat(target: Character, distance: Int = 25) {
    action(ActionType.Movement) {
        while (isActive && tile.distanceTo(target.tile, target.size) <= distance) {
            movement.clear()
            watch(target)
            val opposite = tile.delta(target.tile)
            walkTo(tile.add(opposite))
            delay(5)
        }
    }
}