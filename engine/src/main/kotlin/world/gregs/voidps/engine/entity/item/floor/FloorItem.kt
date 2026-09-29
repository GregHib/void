package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

/**
 * An [Item] with physical location
 * Not a data class to prevent hash conflicts in lists
 * @param revealTicks number of ticks until the item will be revealed to all players
 * @param disappearTicks number of ticks after [revealTicks] when the item will be removed
 */
class FloorItem(
    override var tile: Tile,
    val id: String,
    var amount: Int = 1,
    revealTicks: Int = FloorItems.NEVER,
    disappearTicks: Int = FloorItems.NEVER,
    val charges: Int = 0,
    var owner: String? = null,
) : Entity {

    /**
     * Number of ticks the item will stay public before being removed
     */
    var disappearTicks: Int = disappearTicks
        private set

    /**
     * Tracks which stage of life the item is in:
     *  > 0 - private, counting down until revealed to all players
     *  < 0 - counting up until removed
     *  = 0 - permanent, stays in its current stage indefinitely
     */
    var lifecycle: Int = 0
        private set

    init {
        reset(revealTicks, disappearTicks)
    }

    /**
     * Restart the items lifecycle with new timers
     */
    fun reset(revealTicks: Int, disappearTicks: Int) {
        this.disappearTicks = disappearTicks
        lifecycle = if (owner != null && revealTicks > 0) revealTicks else disappearing()
    }

    private fun disappearing(): Int = if (disappearTicks < 0) 0 else -disappearTicks.coerceAtLeast(1)

    /**
     * Advances the [lifecycle] by one tick
     * @return the [lifecycle] stage which just finished: [REVEAL], [REMOVE] or [NONE]
     */
    fun tick(): Int = when {
        lifecycle > 0 -> if (--lifecycle == 0) {
            lifecycle = disappearing()
            REVEAL
        } else {
            NONE
        }
        lifecycle < 0 -> if (++lifecycle == 0) REMOVE else NONE
        else -> NONE
    }

    val def: ItemDefinition
        get() = ItemDefinitions.get(id)

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

    override fun toString(): String = "FloorItem(id=$id, tile=$tile, amount=$amount, lifecycle=$lifecycle, disappear=$disappearTicks, charges=$charges, owner=$owner)"

    companion object {
        const val NONE = 0
        const val REVEAL = 1
        const val REMOVE = 2
    }
}
