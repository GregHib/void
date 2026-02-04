package content.bot.fact

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.type.Tile
import kotlin.collections.map

sealed interface Condition {
    fun check(player: Player): Boolean
    fun keys(): Set<String>
    fun priority(): Int

    data class Equals<T>(val fact: Fact<T>, val value: T) : Condition {
        override fun check(player: Player) = fact.getValue(player) == value
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class AtLeast(val fact: Fact<Int>, val min: Int) : Condition {
        override fun check(player: Player) = fact.getValue(player) >= min
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class AtMost(val fact: Fact<Int>, val max: Int) : Condition {
        override fun check(player: Player) = fact.getValue(player) <= max
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class Range(val fact: Fact<Int>, val min: Int, val max: Int) : Condition {
        override fun check(player: Player) = fact.getValue(player) in min..max
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class Within(val fact: Fact<Tile>, val tile: Tile, val radius: Int) : Condition {
        override fun check(player: Player) = fact.getValue(player).within(tile, radius)
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class Area(val fact: Fact<Tile>, val area: String) : Condition { // TODO make fact always PlayerTile?
        override fun check(player: Player) = fact.getValue(player) in Areas[area]
        override fun priority() = fact.priority
        override fun keys() = setOf("enter:$area")
    }

    data class OneOf<T>(val fact: Fact<T>, val values: Set<T>) : Condition {
        override fun check(player: Player) = fact.getValue(player) in values
        override fun priority() = fact.priority
        override fun keys() = fact.keys()
    }

    data class Not(val inner: Condition) : Condition {
        override fun check(player: Player) = !inner.check(player)
        override fun priority() = inner.priority()
        override fun keys() = inner.keys()
    }

    data class All(val conditions: List<Condition>) : Condition {
        override fun check(player: Player) = conditions.all { it.check(player) }
        override fun priority() = conditions.first().priority()
        override fun keys() = conditions.flatMap { it.keys() }.toSet()
    }

    data class Any(val conditions: List<Condition>) : Condition {
        override fun check(player: Player) = conditions.any { it.check(player) }
        override fun priority() = conditions.first().priority()
        override fun keys() = conditions.flatMap { it.keys() }.toSet()
    }

    data class Reference(
        val type: String = "",
        val id: String = "",
        val value: kotlin.Any? = null,
        val default: kotlin.Any? = null,
        val min: Int? = null,
        val max: Int? = null,
        val references: Map<String, String> = emptyMap(),
    ) : Condition {
        override fun check(player: Player) = false
        override fun priority() = -1
        override fun keys() = emptySet<String>()
    }

    class Clone(val id: String) : Condition {
        override fun check(player: Player) = false
        override fun priority() = -1
        override fun keys() = emptySet<String>()
    }

    companion object {
        fun split(id: String, min: Int?, max: Int?, wildcard: Wildcard, fact: (String) -> Fact<Int>): Condition = when {
            id.contains(",") -> Any(id.split(",").flatMap { individual ->
                if (individual.any { char -> char == '*' || char == '#' }) {
                    Wildcards.get(individual, wildcard).map { resolved -> range(fact(resolved), min, max) }
                } else {
                    listOf(range(fact(individual), min, max))
                }
            })
            id.any { char -> char == '*' || char == '#' } -> Any(Wildcards.get(id, wildcard).map { resolved -> range(fact(resolved), min, max) })
            else -> range(fact(id), min, max)
        }

        fun range(fact: Fact<Int>, min: Int?, max: Int?, greaterThan: Boolean = true) = when {
            min != null && max != null -> Range(fact, min, max)
            min != null -> AtLeast(fact, min)
            max != null -> AtMost(fact, max)
            else -> if (greaterThan) AtLeast(fact, 1) else AtMost(fact, 1)
        }
    }

}