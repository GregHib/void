package rs.dusk.engine.model.entity.item

sealed class FloorItemState {
    object Private : FloorItemState()
    object Public : FloorItemState()
    object Removed : FloorItemState()
}