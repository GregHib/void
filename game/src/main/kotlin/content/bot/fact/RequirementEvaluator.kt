package content.bot.fact

import content.bot.fact.Predicate.IntEquals
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

sealed class RequirementEvaluator<T> {
    abstract fun evaluate(player: Player, fact: Fact<T>, predicate: Predicate<T>): List<Deficit>

    object TileEval : RequirementEvaluator<Tile>() {
        override fun evaluate(player: Player, fact: Fact<Tile>, predicate: Predicate<Tile>): List<Deficit> {
            if (fact is Fact.PlayerTile && predicate is Predicate.InArea) {
                val value = fact.getValue(player)
                if (!predicate.test(player, value)) {
                    return listOf(Deficit.NotInArea(predicate.name))
                }
            }
            return emptyList()
        }
    }

    object IntEvaluator : RequirementEvaluator<Int>() {
        override fun evaluate(player: Player, fact: Fact<Int>, predicate: Predicate<Int>): List<Deficit> {
            return when (fact) {
                is Fact.EquipCount if predicate is Predicate.IntRange -> listOf(Deficit.MissingItem(Predicate.EqualsItem(fact.id), predicate.min!!))
                is Fact.InventoryCount if predicate is Predicate.IntRange -> listOf(Deficit.MissingItem(Predicate.EqualsItem(fact.id), predicate.min!!))
                is Fact.EquipCount if predicate is IntEquals -> listOf(Deficit.MissingItem(Predicate.EqualsItem(fact.id), predicate.value))
                is Fact.InventoryCount if predicate is IntEquals -> listOf(Deficit.MissingItem(Predicate.EqualsItem(fact.id), predicate.value))
                else -> emptyList()
            }
        }
    }

    object InventoryEval : RequirementEvaluator<Array<Item>>() {
        override fun evaluate(player: Player, fact: Fact<Array<Item>>, predicate: Predicate<Array<Item>>): List<Deficit> {
            if (predicate is Predicate.InventoryItems) {
                val deficits = mutableListOf<Deficit>()
                val value = fact.getValue(player)
                for (entry in predicate.entries) {
                    val have = value.count { entry.filter.test(player, it) }
                    if (entry.count.test(player, have)) {
                        continue
                    }
                    val needed = when (entry.count) {
                        is Predicate.IntRange -> entry.count.min!! - have
                        is IntEquals -> entry.count.value - have
                        else -> continue
                    }
                    if (needed > 0) {
                        deficits += Deficit.MissingItem(entry.filter, needed)
                    }
                }
                return deficits
            }
            return emptyList()
        }
    }
}