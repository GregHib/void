package world.gregs.voidps.engine.entity.item.drop

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get

data class ItemDrop(
    val id: String,
    val amount: IntRange,
    override val chance: Int = 1,
    val members: Boolean = false,
    val predicate: ((Variables) -> Boolean)? = null
) : Drop {

    init {
        assert(chance > 0) { "Item must have a positive chance." }
    }

    fun toItem(definitions: ItemDefinitions = get()): Item {
        if (id == "nothing" || id.isBlank()) {
            return Item.EMPTY
        }
        return Item(id, amount.random(), definitions.get(id))
    }

    companion object {
        private val logger = InlineLogger()

        operator fun invoke(map: Map<String, Any>, itemDefinitions: ItemDefinitions? = null): ItemDrop {
            val id = map["id"] as String
            if (itemDefinitions != null && id != "nothing") {
                if (itemDefinitions.getOrNull(id) == null) {
                    logger.warn { "Invalid item id $id" }
                }
            }
            var predicate: ((Variables) -> Boolean)? = null
            if (map.containsKey("variable")) {
                val variable = map["variable"] as String
                if (map.containsKey("equals")) {
                    val value = map["equals"]
                    when (val default = map["default"]) {
                        is Int -> predicate = { it.get(variable, default) == value }
                        is String -> predicate = { it.get(variable, default) == value }
                        is Double -> predicate = { it.get(variable, default) == value }
                        is Long -> predicate = { it.get(variable, default) == value }
                        is Boolean -> predicate = { it.get(variable, default) == value }
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
                    predicate = { it.get(variable, default) in range }
                }
            }
            return ItemDrop(
                id = id,
                amount = map["amount"] as? IntRange ?: 1..1,
                chance = map["chance"] as? Int ?: 1,
                members = map["members"] as? Boolean ?: false,
                predicate = predicate
            )
        }
    }
}