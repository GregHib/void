package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * A [DropTable] [Drop] which when selected will produce an [Item]
 * @param id of the item to drop
 * @param amount of the item randomly selected to drop
 * @param chance the chance this item is selected compared to the overall [DropTable.roll]
 */
data class ItemDrop(
    val id: String,
    val amount: IntRange,
    override val chance: Int = 1,
    val predicate: ((Player) -> Boolean)? = null
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

    companion object {
        operator fun invoke(
            id: String = "",
            min: Int = 1,
            max: Int = 1,
            chance: Int = 1,
            members: Boolean? = null,
            owns: String? = null,
            lacks: String? = null,
            variable: String? = null,
            eq: Any? = null,
            default: Any? = null,
            within: IntRange? = null,
            negated: Boolean = false,
        ): ItemDrop {
            var predicate: ((Player) -> Boolean)? = null
            if (owns != null || lacks != null) {
                predicate = { (owns == null || ownsItem(it, owns)) && (lacks == null || !ownsItem(it, lacks)) }
            } else if (variable != null) {
                if (negated) {
                    if (eq != null) {
                        when (default) {
                            is Int -> predicate = { it[variable, default] != eq }
                            is String -> predicate = { it[variable, default] != eq }
                            is Double -> predicate = { it[variable, default] != eq }
                            is Long -> predicate = { it[variable, default] != eq }
                            is Boolean -> predicate = { it[variable, default] != eq }
                            else -> when (eq) {
                                is Int -> predicate = { it.get<Int>(variable) != eq }
                                is String -> predicate = { it.get<String>(variable) != eq }
                                is Double -> predicate = { it.get<Double>(variable) != eq }
                                is Long -> predicate = { it.get<Long>(variable) != eq }
                                is Boolean -> predicate = { it.get<Boolean>(variable) != eq }
                                else -> {}
                            }
                        }
                    } else if (within != null) {
                        predicate = { it[variable, default ?: -1] !in within }
                    }
                } else {
                    if (eq != null) {
                        when (default) {
                            is Int -> predicate = { it[variable, default] == eq }
                            is String -> predicate = { it[variable, default] == eq }
                            is Double -> predicate = { it[variable, default] == eq }
                            is Long -> predicate = { it[variable, default] == eq }
                            is Boolean -> predicate = { it[variable, default] == eq }
                            else -> when (eq) {
                                is Int -> predicate = { it.get<Int>(variable) == eq }
                                is String -> predicate = { it.get<String>(variable) == eq }
                                is Double -> predicate = { it.get<Double>(variable) == eq }
                                is Long -> predicate = { it.get<Long>(variable) == eq }
                                is Boolean -> predicate = { it.get<Boolean>(variable) == eq }
                                else -> {}
                            }
                        }
                    } else if (within != null) {
                        predicate = { it[variable, default ?: -1] in within }
                    }
                }
            } else if (members != null) {
                predicate = { World.members == members }
            }
            val amount = min..max
            return ItemDrop(id, amount, chance, predicate)
        }

        private val inventories = listOf("inventory", "worn_equipment", "bank")

        fun ownsItem(player: Player, item: String): Boolean {
            for (inventory in inventories) {
                val items = player.inventories.inventory(inventory).items
                if (items.any { wildcardEquals(item, it.id) }) {
                    return true
                }
            }
            return false
        }
    }
}