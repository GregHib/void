package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.item.floor.FloorItem

interface TargetFloorItemContext<C: Character> : CharacterContext<C> {
    val target: FloorItem
}