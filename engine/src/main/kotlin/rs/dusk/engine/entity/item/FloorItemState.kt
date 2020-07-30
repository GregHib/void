package rs.dusk.engine.entity.item

sealed class FloorItemState {
    object Private : FloorItemState()
    object Public : FloorItemState()
    object Removed : FloorItemState()
}