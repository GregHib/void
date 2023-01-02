package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.tick.Job

/**
 * An [Item] with physical location
 */
data class FloorItem(
    override var tile: Tile,
    val id: String,
    var amount: Int = 1,
    override val size: Size = Size.ONE,
    val owner: String? = null
) : InteractiveEntity {

    override val events: Events = Events(this)
    override var values: Values? = null

    fun visible(player: Player): Boolean {
        return state == FloorItemState.Public || (state == FloorItemState.Private && player.name == owner)
    }

    lateinit var def: ItemDefinition

    var state: FloorItemState = if (owner == null) FloorItemState.Public else FloorItemState.Private

    var disappear: Job? = null
}