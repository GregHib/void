package content.bot.fact

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.type.Tile

sealed interface Predicate<T> {
    fun test(value: T): Boolean

    data class IntRange(val min: Int? = null, val max: Int? = null) : Predicate<Int> {
        override fun test(value: Int): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }

    data class IntEquals(val value: Int) : Predicate<Int> {
        override fun test(value: Int) = value == this.value
    }

    data class DoubleRange(val min: Double? = null, val max: Double? = null) : Predicate<Double> {
        override fun test(value: Double): Boolean {
            if (min != null && value < min) return false
            if (max != null && value > max) return false
            return true
        }
    }

    data class DoubleEquals(val value: Double) : Predicate<Double> {
        override fun test(value: Double) = value == this.value
    }

    data class InArea(val name: String) : Predicate<Tile> {
        override fun test(value: Tile) = value in Areas[name]
    }

    object BooleanTrue : Predicate<Boolean> {
        override fun test(value: Boolean) = value
    }

    object BooleanFalse : Predicate<Boolean> {
        override fun test(value: Boolean) = !value
    }

    data class StringEquals(val value: String) : Predicate<String> {
        override fun test(value: String) = value == this.value
    }

    data class TileEquals(val x: Int? = null, val y: Int? = null, val level: Int? = null) : Predicate<Tile> {
        override fun test(value: Tile): Boolean {
            if (x != null && value.x != x) return false
            if (y != null && value.y != y) return false
            if (level != null && value.level != level) return false
            return true
        }
    }

    data class Within(val x: Int, val y: Int, val level: Int, val radius: Int) : Predicate<Tile> {
        override fun test(value: Tile) = value.within(x, y, level, radius)
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
    }
}