package content.bot.fact

import content.bot.fact.Deficit.MissingInventory
import content.bot.fact.Predicate.IntEquals
import world.gregs.voidps.engine.entity.character.player.Player
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

    object InventoryEval : RequirementEvaluator<ItemView>() {
        override fun evaluate(player: Player, fact: Fact<ItemView>, predicate: Predicate<ItemView>): List<Deficit> {
            if (predicate is Predicate.InventoryItems) {
                val entries = mutableListOf<MissingInventory.Entry>()
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
                        entries += MissingInventory.Entry(entry.filter, needed)
                    }
                }
                return listOf(MissingInventory(entries))
            }
            return emptyList()
        }
    }
}