package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.map.Tile

data class FloorItemTargetStrategy(
    private val floorItem: FloorItem
) : TargetStrategy {
    override val bitMask = 0
    override val tile: Tile
        get() = floorItem.tile
    override val size: Size
        get() = floorItem.size
    override val rotation = 0
    override val exitStrategy = 10
    override val width: Int = 1
    override val height: Int = 1

    override fun reached(character: Character): Boolean {
        if (character.tile.within(tile, 1) && (character.steps.isEmpty() || character.hasClock("movement_delay"))) {
            return super.reached(character) || DefaultTargetStrategy.reached(character)
        }
        return DefaultTargetStrategy.reached(character)
    }
}