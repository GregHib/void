package world.gregs.voidps.world.interact.entity.effect

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.collision.strategy.NoCollision
import world.gregs.voidps.engine.utility.inject

val none: NoCollision by inject()

on<EffectStart>({ effect == "no_clip" }) { character: Character ->
    character["old_collision"] = character.collision
    character.collision = none
}

on<EffectStop>({ effect == "no_clip" }) { character: Character ->
    character.collision = character.remove("old_collision") ?: return@on
}