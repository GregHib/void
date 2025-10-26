package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character

interface InteractionType {
    fun hasOperate(): Boolean
    fun hasApproach(): Boolean
    fun operate()
    fun approach()
}