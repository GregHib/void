package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC

fun NPC.retreat(target: Character, distance: Int = 25) {
    /*action(ActionType.Movement) {
        while (isActive && tile.distanceTo(target.tile, target.size) <= distance) {
//            movement.clear()
            watch(target)
            val opposite = tile.delta(target.tile)
            walkTo(tile.add(opposite))
            pause(5)
        }
    }*/
}