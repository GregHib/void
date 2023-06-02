package world.gregs.voidps.engine.entity.item.floor

import kotlinx.coroutines.CancellableContinuation
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
    val owner: String?
) : Entity {


    val value: Long
        get() = def.cost * amount.toLong()

    constructor(id: String, tile: Tile, amount: Int = 1, disappear: Int = -1, reveal: Int = -1, owner: String? = null) : this(id, tile, amount, Size.ONE, owner) {
        this.disappearTimer = disappear
        this.revealTimer = reveal
    }

    fun combine(other: FloorItem): Boolean {
        if (def.stackable == 0) {
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

    override val events: Events = Events(this)
    var disappearTimer: Int = -1
    var revealTimer: Int = -1

    @Deprecated("Temp")
    var botJobs: MutableSet<CancellableContinuation<Unit>>? = null

    lateinit var def: ItemDefinition

    var state: FloorItemState = if (owner == null) FloorItemState.Public else FloorItemState.Private

}