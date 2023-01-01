package world.gregs.voidps.world.interact.entity.effect

import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on

val none = object : CollisionStrategy {
    override fun canMove(tileFlag: Int, blockFlag: Int): Boolean {
        return true
    }
}

on<EffectStart>({ effect == "no_clip" }) { character: Character ->
    character["old_collision"] = character.collision
    character.collision = none
}

on<EffectStop>({ effect == "no_clip" }) { character: Character ->
    character.remove<CollisionStrategy>("old_collision")?.let { collision ->
        character.collision = collision
    }
}