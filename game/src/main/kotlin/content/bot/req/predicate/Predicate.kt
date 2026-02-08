package content.bot.req.predicate

import content.bot.req.RequirementEvaluator
import content.bot.req.fact.ItemView
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirements
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.type.Tile

sealed class Predicate<T> {
    abstract fun test(player: Player, value: T): Boolean
    open val children: Set<Predicate<*>> = emptySet()
    open val evaluator: RequirementEvaluator<T>? = null

    data class IntRange(val min: Int? = null, val max: Int? = null) : Predicate<Int>() {
        override fun test(player: Player, value: Int): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }

    data class IntEquals(val value: Int) : Predicate<Int>() {
        override fun test(player: Player, value: Int) = value == this.value
    }

    data class DoubleRange(val min: Double? = null, val max: Double? = null) : Predicate<Double>() {
        override fun test(player: Player, value: Double): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }

    data class DoubleEquals(val value: Double) : Predicate<Double>() {
        override fun test(player: Player, value: Double) = value == this.value
    }

    data class InArea(val name: String) : Predicate<Tile>() {
        override val evaluator = RequirementEvaluator.TileEval
        override fun test(player: Player, value: Tile) = value in Areas[name]
    }

    object BooleanTrue : Predicate<Boolean>() {
        override fun test(player: Player, value: Boolean) = value
    }

    object BooleanFalse : Predicate<Boolean>() {
        override fun test(player: Player, value: Boolean) = !value
    }

    data class StringEquals(val value: String) : Predicate<String>() {
        override fun test(player: Player, value: String) = value == this.value
    }

    data class TileEquals(val x: Int? = null, val y: Int? = null, val level: Int? = null) : Predicate<Tile>() {
        override val evaluator = RequirementEvaluator.TileEval
        override fun test(player: Player, value: Tile): Boolean {
            if (x != null && value.x != x) return false
            if (y != null && value.y != y) return false
            if (level != null && value.level != level) return false
            return true
        }
    }

    data class Within(val x: Int, val y: Int, val level: Int, val radius: Int) : Predicate<Tile>() {
        override val evaluator = RequirementEvaluator.TileEval
        override fun test(player: Player, value: Tile) = value.within(x, y, level, radius)
    }

    data class InventoryItems(val entries: List<Entry>) : Predicate<ItemView>() {
        data class Entry(
            val filter: Predicate<Item>,
            val amount: Predicate<Int>,
        )
        override val evaluator = RequirementEvaluator.InventoryEval
        override val children = entries.map { it.amount }.toSet() + entries.map { it.filter }.toSet()

        override fun test(player: Player, value: ItemView): Boolean {
            for ((filter, amount) in entries) {
                val count = value.count { item -> filter.test(player, item) }
                if (!amount.test(player, count)) {
                    return false
                }
            }
            return true
        }
    }

    data class AnyItem(private val ids: Set<String>) : Predicate<Item>() {
        override fun test(player: Player, value: Item) = value.id in ids
    }

    object EquipableItem : Predicate<Item>() {
        override fun test(player: Player, value: Item) = player.hasRequirements(value)
    }

    object UsableItem : Predicate<Item>() {
        override fun test(player: Player, value: Item) = player.hasRequirementsToUse(value)
    }

    data class EqualsItem(private val id: String) : Predicate<Item>() {
        override fun test(player: Player, value: Item) = value.id == id
    }

    data class AllOf<T>(override val children: Set<Predicate<T>>) : Predicate<T>() {
        override fun test(player: Player, value: T) = children.all { it.test(player, value) }
    }

    data class AnyOf<T>(override val children: Set<Predicate<T>>) : Predicate<T>() {
        override fun test(player: Player, value: T) = children.any { it.test(player, value) }
    }

    companion object {
        fun parseInt(map: Map<String, Any>): Predicate<Int>? = when {
            map.containsKey("min") || map.containsKey("max") -> IntRange(map["min"] as? Int, map["max"] as? Int)
            map.containsKey("equals") -> {
                when (val value = map["equals"]) {
                    is Int -> IntEquals(value)
                    else -> error("Unsupported equals type: '${value?.let { it::class.simpleName }}'")
                }
            }
            else -> null
        }

        fun parseDouble(map: Map<String, Any>): Predicate<Double>? = when {
            map.containsKey("min") || map.containsKey("max") -> DoubleRange(map["min"] as? Double, map["max"] as? Double)
            map.containsKey("equals") -> {
                when (val value = map["equals"]) {
                    is Double -> DoubleEquals(value)
                    else -> error("Unsupported equals type: '${value?.let { it::class.simpleName }}'")
                }
            }
            else -> null
        }

        fun parseBool(map: Map<String, Any>): Predicate<Boolean>? {
            val equals = map["equals"] ?: return null
            if (equals !is Boolean) {
                error("Unsupported equals type: '${equals.let { it::class.simpleName }}'")
            }
            return if (equals) BooleanTrue else BooleanFalse
        }

        fun parseString(map: Map<String, Any>): Predicate<String>? {
            val equals = map["equals"] ?: return null
            if (equals !is String) {
                error("Unsupported equals type: '${equals.let { it::class.simpleName }}'")
            }
            return StringEquals(equals)
        }

        fun parseTile(map: Map<String, Any>): Predicate<Tile>? = when {
            map.containsKey("id") -> InArea(map["id"] as String)
            map.containsKey("x") || map.containsKey("y") || map.containsKey("level") -> TileEquals(map["x"] as? Int, map["y"] as? Int, map["level"] as? Int)
            else -> null
        }

        fun parseItems(items: List<Map<String, Any>>): InventoryItems {
            val entries = mutableListOf<InventoryItems.Entry>()
            for (item in items) {
                val filter = itemFilter(item)
                val counter = parseInt(item) ?: IntRange(min = 1)
                entries.add(InventoryItems.Entry(filter, counter))
            }
            return InventoryItems(entries)
        }

        private fun itemFilter(item: Map<String, Any>): Predicate<Item> {
            require(item.containsKey("id")) { "Item must have field 'id' in map $item" }
            val id = item["id"] as String
            var filter = if (id.contains(",")) {
                val ids = id.split(",")
                AnyItem(
                    ids.flatMap { id ->
                        if (id.any { char -> char == '*' || char == '#' }) {
                            Wildcards.get(id, Wildcard.Item)
                        } else {
                            setOf(id)
                        }
                    }.toSet(),
                )
            } else if (id.any { it == '*' || it == '#' }) {
                AnyItem(Wildcards.get(id, Wildcard.Item))
            } else {
                EqualsItem(id)
            }
            if (item.containsKey("usable") && item["usable"] as Boolean) {
                // TODO lookup values from custom configs e.g. firemaking.level
                filter = AllOf(setOf(filter, UsableItem))
            } else if (item.containsKey("equipable") && item["equipable"] as Boolean) {
                filter = AllOf(setOf(filter, EquipableItem))
            }
            return filter
        }
    }
}
