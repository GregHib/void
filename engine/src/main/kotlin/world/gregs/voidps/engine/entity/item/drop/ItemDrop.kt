package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

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
        assert(chance > 0) { "Item must have a positive chance." }
    }

    fun toItem(): Item {
        if (id == "nothing" || id.isBlank()) {
            return Item.EMPTY
        }
        return Item(id, amount.random())
    }
}
