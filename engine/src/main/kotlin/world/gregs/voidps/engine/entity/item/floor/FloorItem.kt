package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.type.Tile

/**
 * An [Item] with physical location
 * Not a data class to prevent hash conflicts in lists
 */
class FloorItem(
    override var tile: Tile,
    val id: String,
    var amount: Int = 1,
    var revealTicks: Int = -1,
    var disappearTicks: Int = -1,
    var owner: String? = null
) : Entity, EventDispatcher {

    override val events: Events = Events(this)
    lateinit var def: ItemDefinition

    val value: Long
        get() = def.cost * amount.toLong()

    /**
     * Adds [other] items amount to this item.
     */
    fun merge(other: FloorItem): Boolean {
        if (def.stackable != 1) {
            return false
        }
        val stack = amount
        val combined = stack + other.amount
        // Overflow should add as separate item
        if (stack xor combined and (other.amount xor combined) < 0) {
            return false
        }
        amount = combined
        return true
    }

    /**
     * Reveal when owned items countdown reaches 0
     */
    fun reveal(): Boolean = owner != null && revealTicks >= 0 && --revealTicks == 0

    /**
     * Reveal when public items countdown reaches 0
     */
    fun remove(): Boolean = revealTicks <= 0 && (disappearTicks == 0 || disappearTicks > 0 && --disappearTicks == 0)

    override fun toString(): String {
        return "FloorItem(id='$id', tile=$tile, amount=$amount, disappear=$disappearTicks, reveal=$revealTicks, owner=$owner)"
    }
}