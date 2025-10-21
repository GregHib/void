package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character

interface InteractionType {
    fun hasOperate(character: Character): Boolean
    fun hasApproach(character: Character): Boolean
    fun operate(character: Character, target: Entity)
    fun approach(character: Character, target: Entity)
}