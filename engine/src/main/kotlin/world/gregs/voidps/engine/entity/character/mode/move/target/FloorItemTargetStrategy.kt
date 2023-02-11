package world.gregs.voidps.engine.entity.character.mode.move.target

import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.hasEffect
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

    override fun reached(interact: Interact): Boolean {
        if (interact.character.tile.within(tile, 1) && (interact.steps.isEmpty() || interact.character.hasEffect("frozen"))) {
            return super.reached(interact) || DefaultTargetStrategy.reached(interact)
        }
        return DefaultTargetStrategy.reached(interact)
    }
}