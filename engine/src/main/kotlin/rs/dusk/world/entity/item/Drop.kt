package rs.dusk.world.entity.item

import rs.dusk.engine.event.Event
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.item.FloorItem

/**
 * Spawns a floor item
 * Note: Not concerned with where the item is coming from
 * @param item The floor item to spawn
 * @param revealTicks Number of ticks before the item is revealed to all
 * @param disappearTicks Number of ticks before the item is removed
 * @param owner The index of the owner of the item
 */
data class Drop(
    val item: FloorItem,
    val revealTicks: Int = -1,
    val disappearTicks: Int = -1,
    val owner: Int = -1) : Event() {
    companion object : EventCompanion<Drop>
}