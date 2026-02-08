package content.bot.behaviour.setup

import content.bot.action.BotAction
import content.bot.req.Requirement
import content.bot.req.fact.Fact
import content.bot.req.predicate.Predicate
import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory

/**
 * Missing setup [Requirement]'s which can be produce dynamic [Resolver]'s
 */
sealed interface Deficit {
    fun resolve(player: Player): Resolver?

    data class NotInArea(val area: String) : Deficit {
        override fun resolve(player: Player): Resolver = Resolver("go_to_$area", -1, actions = listOf(BotAction.GoTo(area)))
    }

    data class MissingEquipment(val entries: List<Predicate<Item>>) : Deficit {
        override fun resolve(player: Player): Resolver? {
            val entries = entries.toMutableList()
            val actions = mutableListOf<BotAction>()
            val uniqueName = StringBuilder()
            for (item in player.inventory.items) {
                if (item.isEmpty()) {
                    continue
                }
                val iterator = entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (!entry.test(player, item)) {
                        continue
                    }
                    iterator.remove()
                    uniqueName.append("_${item.id}")
                    actions.add(BotAction.InterfaceOption("Equip", "inventory:inventory:${item.id}"))
                }
            }
            val spaceNeeded = withdraw(actions, player, entries, uniqueName)
            if (actions.isNotEmpty()) {
                return Resolver(
                    "withdraw$uniqueName",
                    weight = 20,
                    setup = listOf(
                        Requirement(Fact.InventorySpace, Predicate.IntRange(spaceNeeded)),
                    ),
                    actions = actions,
                )
            }
            return null
        }

        private fun withdraw(actions: MutableList<BotAction>, player: Player, entries: MutableList<Predicate<Item>>, uniqueName: StringBuilder): Int {
            if (entries.isEmpty()) {
                return 0
            }
            var spaceNeeded = 0
            actions.add(BotAction.GoToNearest("bank"))
            actions.add(BotAction.InteractObject("Use-quickly", "bank_booth*", success = Requirement(Fact.InterfaceOpen("bank"), Predicate.BooleanTrue)))
            for (item in player.bank.items) {
                if (item.isEmpty()) {
                    continue
                }
                val iterator = entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (!entry.test(player, item)) {
                        continue
                    }
                    iterator.remove()
                    spaceNeeded++
                    uniqueName.append("_${item.id}")
                    actions.add(BotAction.InterfaceOption("Withdraw-1", "bank:inventory:${item.id}"))
                }
            }
            if (spaceNeeded > 0) {
                if (player.inventory.spaces < spaceNeeded) {
                    actions.add(2, BotAction.InteractObject("Deposit carried items", "bank:carried", success = Requirement(Fact.InventorySpace, Predicate.IntEquals(28))))
                }
                actions.add(BotAction.CloseInterface)
            }
            return spaceNeeded
        }
    }

    data class MissingInventory(val entries: List<Entry>) : Deficit {
        data class Entry(val filter: Predicate<Item>, val needed: Int)

        override fun resolve(player: Player): Resolver? {
            var spaceNeeded = 0
            val actions = mutableListOf(
                BotAction.GoToNearest("bank"),
                BotAction.InteractObject("Use-quickly", "bank_booth*", success = Requirement(Fact.InterfaceOpen("bank"), Predicate.BooleanTrue)),
            )
            val uniqueName = StringBuilder()
            for (item in player.bank.items) {
                if (item.isEmpty()) {
                    continue
                }
                for (entry in entries) {
                    if (!entry.filter.test(player, item)) {
                        continue
                    }
                    val needed = entry.needed
                    spaceNeeded += needed
                    uniqueName.append("_${item.id}")
                    if (needed == 1 || needed == 5 || needed == 10) {
                        actions.add(BotAction.InterfaceOption("Withdraw-$needed", "bank:inventory:${item.id}"))
                    } else {
                        BotAction.InterfaceOption("Withdraw-X", "bank:inventory:${item.id}")
                        BotAction.IntEntry(needed)
                    }
                }
            }
            if (spaceNeeded > 0) {
                if (player.inventory.spaces < spaceNeeded) {
                    actions.add(2, BotAction.InteractObject("Deposit carried items", "bank:carried", success = Requirement(Fact.InventorySpace, Predicate.IntEquals(28))))
                }
                actions.add(BotAction.CloseInterface)
                return Resolver(
                    "withdraw_$uniqueName",
                    weight = 20,
                    setup = listOf(
                        Requirement(Fact.InventorySpace, Predicate.IntRange(spaceNeeded)),
                    ),
                    actions = actions,
                )
            }
            return null
        }
    }
}
