package content.bot.fact

import content.bot.fact.Deficit.MissingInventory
import content.bot.fact.Predicate.IntEquals
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

/**
 * Evaluates [Requirement]'s to produce known [Deficit]'s
 */
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
            if (fact is Fact.InventoryItems && predicate is Predicate.InventoryItems) {
                val entries = mutableListOf<MissingInventory.Entry>()
                collect(player, fact, predicate) { filter, needed -> entries += MissingInventory.Entry(filter, needed) }
                return listOf(MissingInventory(entries))
            } else if (fact is Fact.EquipmentItems && predicate is Predicate.InventoryItems) {
                val entries = mutableListOf<Predicate<Item>>()
                collect(player, fact, predicate) { filter, _ -> entries += filter }
                return listOf(Deficit.MissingEquipment(entries))
            }
            return emptyList()
        }

        private fun collect(player: Player, fact: Fact<ItemView>, predicate: Predicate.InventoryItems, block: (Predicate<Item>, Int) -> Unit) {
            val value = fact.getValue(player)
            for (entry in predicate.entries) {
                val have = value.count { entry.filter.test(player, it) }
                if (entry.amount.test(player, have)) {
                    continue
                }
                val needed = when (entry.amount) {
                    is Predicate.IntRange -> entry.amount.min!! - have
                    is IntEquals -> entry.amount.value - have
                    else -> continue
                }
                if (needed > 0) {
                    block.invoke(entry.filter, needed)
                }
            }
        }
    }
}