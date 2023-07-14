package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.entity.item.floor.FloorItem

interface TargetFloorItemContext : CharacterContext {
    val target: FloorItem
}