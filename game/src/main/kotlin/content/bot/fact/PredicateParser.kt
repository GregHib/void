package content.bot.fact

import world.gregs.voidps.type.Tile

sealed class PredicateParser<T> {
    open val required: Set<String> = emptySet()
    open val optional: Set<String> = emptySet()
    abstract fun parse(map: Map<String, Any>): Predicate<T>?

    object IntegerParser : PredicateParser<Int>() {
        override val optional = setOf("min", "max", "equals")

        override fun parse(map: Map<String, Any>): Predicate<Int>? {
            if (map.containsKey("min") || map.containsKey("max")) {
                return Predicate.IntRange(map["min"] as? Int, map["max"] as? Int)
            } else if (map.containsKey("equals")) {
                return Predicate.IntEquals(map["equals"] as Int)
            }
            return null
        }
    }

    object BooleanParser : PredicateParser<Boolean>() {
        override val required = setOf("equals")

        override fun parse(map: Map<String, Any>) = if (map["equals"] as Boolean) {
            Predicate.BooleanTrue
        } else {
            Predicate.BooleanFalse
        }
    }

    object TileParser : PredicateParser<Tile>() {
        override val optional = setOf("x", "y", "level")

        override fun parse(map: Map<String, Any>): Predicate<Tile> {
            return Predicate.TileEquals(map["x"] as? Int, map["y"] as? Int, map["level"] as? Int)
        }
    }

    companion object {
        val parsers = mapOf(
            "inventory_space" to IntegerParser,
            "inventory" to IntegerParser,
            "carries" to IntegerParser,
            "banked" to IntegerParser,
            "equips" to IntegerParser,
            "variable" to IntegerParser,
            "clock" to IntegerParser,
            "interface_open" to BooleanParser,
            "has_timer" to BooleanParser,
            "has_queue" to BooleanParser,
            "tile" to TileParser,
            "combat" to IntegerParser,
            "skill" to IntegerParser,
        )
    }
}

