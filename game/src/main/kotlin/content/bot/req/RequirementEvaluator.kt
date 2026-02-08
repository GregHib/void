package content.bot.req

import content.bot.behaviour.setup.Deficit
import content.bot.behaviour.setup.Deficit.MissingInventory
import content.bot.req.fact.Fact
import content.bot.req.fact.ItemView
import content.bot.req.predicate.Predicate
import content.bot.req.predicate.Predicate.IntEquals
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import kotlin.collections.plusAssign

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
                val entries = mutableListOf<Deficit.Entry>()
                collect(player, fact, predicate) { filter, needed -> entries += Deficit.Entry(filter, needed) }
                return listOf(MissingInventory(entries))
            } else if (fact is Fact.EquipmentItems && predicate is Predicate.InventoryItems) {
                val entries = mutableListOf<Deficit.Entry>()
                collect(player, fact, predicate) { filter, needed -> entries += Deficit.Entry(filter, needed) }
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
