package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timer.TimerQueue

/**
 * An [Item] with physical location
 */
data class FloorItem(
    override var tile: Tile,
    val id: String,
    var amount: Int = 1,
    override val size: Size = Size.ONE,
    val owner: String? = null
) : Entity {

    override val events: Events = Events(this)
    override var values: Values? = null
    val timers = TimerQueue(events)
    @Deprecated("Temp")
    var disappearTimer: Int = -1
    @Deprecated("Temp")
    var revealTimer: Int = -1

    lateinit var def: ItemDefinition

    var state: FloorItemState = if (owner == null) FloorItemState.Public else FloorItemState.Private
}