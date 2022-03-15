package world.gregs.voidps.engine.entity.item.floor

sealed class FloorItemState {
    object Private : FloorItemState()
    object Public : FloorItemState()
    object Removed : FloorItemState()
}