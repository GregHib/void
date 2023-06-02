package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile

/**
 * An [Item] with physical location
 * Not a data class to prevent hash conflicts in lists
 */
class FloorItem private constructor(
    val id: String,
    override var tile: Tile,
    var amount: Int,
    override val size: Size,
    var owner: Int
) : Entity {

    constructor(id: String, tile: Tile, amount: Int = 1, disappear: Int = -1, reveal: Int = -1, owner: Int = 0) : this(id, tile, amount, Size.ONE, owner) {
        this.disappearTimer = disappear
        this.revealTimer = reveal
    }

    override val events: Events = Events(this)
    lateinit var def: ItemDefinition
    var disappearTimer: Int = -1
    var revealTimer: Int = -1

    val value: Long
        get() = def.cost * amount.toLong()

    fun combine(other: FloorItem): Boolean {
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

}