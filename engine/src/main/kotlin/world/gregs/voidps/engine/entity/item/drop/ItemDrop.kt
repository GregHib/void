package world.gregs.voidps.engine.entity.item.drop

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.wildcardEquals

data class ItemDrop(
    val id: String,
    val amount: IntRange,
    override val chance: Int = 1,
    val members: Boolean = false,
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
        private val logger = InlineLogger()

        operator fun invoke(
            id: String = "",
            min: Int = 1,
            max: Int = 1,
            chance: Int = 1,
            members: Boolean = false,
            owns: String? = null,
            lacks: String? = null,
            variable: String? = null,
            eq: Any? = null,
            default: Any? = null,
            within: String? = null,
        ): ItemDrop {
            var predicate: ((Player) -> Boolean)? =null
            if (owns != null || lacks != null) {
                predicate = { (owns == null || ownsItem(it, owns)) && (lacks == null || !ownsItem(it, lacks)) }
            } else if (variable != null) {
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
                    val range = within.toIntRange(inclusive = true)
                    predicate = { it[variable, default ?: -1] in range }
                }
            }
            val amount = min..max
            return ItemDrop(id, amount, chance, members, predicate)
        }

        operator fun invoke(map: Map<String, Any>, itemDefinitions: ItemDefinitions? = null): ItemDrop {
            val id = map["id"] as String
            if (itemDefinitions != null && id != "nothing") {
                if (itemDefinitions.getOrNull(id) == null) {
                    logger.warn { "Invalid drop id $id" }
                }
            }
            var predicate: ((Player) -> Boolean)? = null
            if (map.containsKey("variable")) {
                val variable = map["variable"] as String
                if (map.containsKey("equals")) {
                    val value = map["equals"]
                    when (val default = map["default"]) {
                        is Int -> predicate = { it[variable, default] == value }
                        is String -> predicate = { it[variable, default] == value }
                        is Double -> predicate = { it[variable, default] == value }
                        is Long -> predicate = { it[variable, default] == value }
                        is Boolean -> predicate = { it[variable, default] == value }
                        else -> when (value) {
                            is Int -> predicate = { it.get<Int>(variable) == value }
                            is String -> predicate = { it.get<String>(variable) == value }
                            is Double -> predicate = { it.get<Double>(variable) == value }
                            is Long -> predicate = { it.get<Long>(variable) == value }
                            is Boolean -> predicate = { it.get<Boolean>(variable) == value }
                        }
                    }
                } else if (map.containsKey("within")) {
                    val range = (map["within"] as String).toIntRange(inclusive = true)
                    val default = map.getOrDefault("default", -1)
                    predicate = { it[variable, default] in range }
                }
            } else if (map.containsKey("owns") || map.containsKey("lacks")) {
                val owns = map["owns"] as? String
                val lacks = map["lacks"] as? String
                predicate = { (owns == null || ownsItem(it, owns)) && (lacks == null || !ownsItem(it, lacks)) }
            }
            return ItemDrop(
                id = id,
                amount = map["amount"] as? IntRange ?: map["charges"] as? IntRange ?: 1..1,
                chance = map["chance"] as? Int ?: 1,
                members = map["members"] as? Boolean ?: false,
                predicate = predicate
            )
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