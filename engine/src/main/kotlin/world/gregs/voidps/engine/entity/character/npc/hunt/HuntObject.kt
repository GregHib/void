package world.gregs.voidps.engine.entity.character.npc.hunt

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Event

data class HuntObject(
    val mode: String,
    val target: GameObject
) : Event