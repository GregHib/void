package world.gregs.voidps.world.interact.entity.effect

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop

val none = object : CollisionStrategy {
    override fun canMove(tileFlag: Int, blockFlag: Int): Boolean {
        return true
    }
}

on<TimerStart>({ timer == "no_clip" }) { character: Character ->
    character["old_collision"] = character.collision
    character.collision = none
}

on<TimerStop>({ timer == "no_clip" }) { character: Character ->
    character.collision = character.remove("old_collision") ?: return@on
}