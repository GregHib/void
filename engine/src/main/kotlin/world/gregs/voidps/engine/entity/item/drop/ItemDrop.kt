package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.random

/**
 * A [DropTable] [Drop] which when selected will produce an [Item]
 * @param id of the item to drop
 * @param amount of the item randomly selected to drop
 * @param chance the chance this item is selected compared to the overall [DropTable.roll]
 */
data class ItemDrop(
    val id: String,
    val amount: IntRange = 1..1,
    override val chance: Int = 1,
    override val predicate: ((Player) -> Boolean)? = null,
) : Drop {

    init {
        assert(chance > 0) { "Item $id must have a positive chance." }
    }

    fun toItem(): Item {
        if (id == "nothing" || id.isBlank()) {
            return Item.EMPTY
        }
        return Item(id, amount.random(random))
    }

    override fun print(indent: Int, multiplier: Double) = "${"  ".repeat(indent)}$id x${if (amount.first == amount.last) amount.first else "${amount.first}..${amount.last}"}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemDrop

        if (chance != other.chance) return false
        if (id != other.id) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chance
        result = 31 * result + id.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }


}
