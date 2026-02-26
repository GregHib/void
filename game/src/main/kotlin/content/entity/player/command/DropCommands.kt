package content.entity.player.command

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.chat.toSIInt
import world.gregs.voidps.engine.client.ui.chat.toSIPrefix
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTable
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.item.drop.TableType
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.timer.TICKS
import java.util.concurrent.TimeUnit
import kotlin.collections.iterator
import kotlin.system.measureTimeMillis

class DropCommands(val tables: DropTables) : Script {

    init {
        modCommand(
            "chance",
            stringArg("drop-table-id", autofill = tables.tables.keys),
            desc = "Display chances for every item in a drop table",
            handler = ::chance,
        )

        modCommand(
            "sim",
            stringArg("drop-table-id", autofill = tables.tables.keys),
            intArg("drop-count", desc = "Number of drops to simulate", optional = true),
            desc = "Simulate any amount of drops from a drop-table/boss",
            handler = ::simulate,
        )
    }

    fun simulate(player: Player, args: List<String>) {
        val name = args.first()
        val count = (args.getOrNull(1) ?: "1").toSIInt()
        val table = tables.get(name) ?: tables.get("${name}_drop_table")
        val title = "${count.toSIPrefix()} '${name.removeSuffix("_drop_table")}' drops"
        if (table == null) {
            player.message("No drop table found for '$name'")
            return
        }
        if (count < 0) {
            player.message("Simulation count has to be more than 0.")
            return
        }
        if (player.hasClock("search_delay")) {
            player.message("Requests too quick, try again in ${TICKS.toSeconds(player.remaining("search_delay"))} seconds.")
            return
        }
        player.start("search_delay", 5)
        player.message("Simulating $title")
        if (count > 100_000) {
            player.message("Calculating...")
        }
        GlobalScope.launch {
            val inventory = Inventory.debug(capacity = 100, id = "")
            coroutineScope {
                val time = measureTimeMillis {
                    (0 until count).chunked(1_000_000).map { numbers ->
                        async {
                            val temp = Inventory.debug(capacity = 100)
                            val list = InventoryDelegate(temp)
                            for (i in numbers) {
                                table.roll(list = list, player = player)
                            }
                            temp
                        }
                    }.forEach {
                        if (!it.await().moveAll(inventory)) {
                            println("Failed to move all simulated drops to inventory")
                        }
                    }
                }
                if (time > 0) {
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(time)
                    player.message("Simulation took ${if (seconds > 1) "${seconds}s" else "${time}ms"}")
                }
            }
            val alch: (Item) -> Long = {
                it.amount * it.def.cost.toLong()
            }
            val exchange: (Item) -> Long = {
                it.amount * it.def["price", it.def.cost].toLong()
            }
            val chances = mutableMapOf<ItemDrop, Double>()
            collectChances(player, table, chances)
            val itemChances = chances.map { it.key.id to it }.toMap()
            val sortByPrice = false
            try {
                if (sortByPrice) {
                    inventory.sortedByDescending { exchange(it) }
                } else {
                    inventory.sortedByDescending { it.amount.toLong() }
                }
                World.queue("drop_sim") {
                    var alchValue = 0L
                    var exchangeValue = 0L
                    for (item in inventory.items) {
                        if (item.isNotEmpty()) {
                            alchValue += alch(item)
                            exchangeValue += exchange(item)
                            val (drop, chance) = itemChances[item.id] ?: continue
                            player.message("${item.id} 1/${(count / (item.amount / drop.amount.first.toDouble())).toInt()} (1/${chance.toInt()} real)")
                        }
                    }
                    player.message("Alch price: ${alchValue.toDigitGroupString()}gp (${alchValue.toSIPrefix()})")
                    player.message("Exchange price: ${exchangeValue.toDigitGroupString()}gp (${exchangeValue.toSIPrefix()})")
                    player.interfaces.open("shop")
                    player["free_inventory"] = -1
                    player["main_inventory"] = 510
                    player.interfaceOptions.unlock("shop", "stock", 0 until inventory.size * 6, "Info")
                    for ((index, item) in inventory.items.withIndex()) {
                        player["amount_$index"] = item.amount
                    }
                    player.sendInventory(inventory, id = 510)
                    player.interfaces.sendVisibility("shop", "store", false)
                    player.interfaces.sendText("shop", "title", "$title - ${alchValue.toDigitGroupString()}gp (${alchValue.toSIPrefix()})")
                }
            } catch (e: Exception) {
                player.close("shop")
            }
        }
    }

    fun Inventory.sortedByDescending(block: (Item) -> Long) {
        transaction {
            val items = items.clone()
            clear()
            items.sortedByDescending(block).forEachIndexed { index, item ->
                set(index, item)
            }
        }
    }

    fun chance(player: Player, args: List<String>) {
        val content = args[0]
        val table = tables.get(content) ?: tables.get("${content}_drop_table")
        if (table == null) {
            player.message("No drop table found for '$content'")
            return
        }
        if (player.hasClock("search_delay")) {
            return
        }
        player.start("search_delay", 1)
        val chances = mutableMapOf<ItemDrop, Double>()
        collectChances(player, table, chances)
        for ((drop, chance) in chances) {
            val amount = when {
                drop.amount.first == drop.amount.last && drop.amount.first > 1 -> "(${drop.amount.first})"
                drop.amount.first != drop.amount.last && drop.amount.first > 1 -> "(${drop.amount.first}-${drop.amount.last})"
                else -> ""
            }
            player.message("${drop.id} $amount - 1/${chance.toInt()}")
        }
    }

    fun collectChances(player: Player, table: DropTable, map: MutableMap<ItemDrop, Double>, multiplier: Double = 1.0) {
        for (drop in table.drops) {
            if (drop is ItemDrop) {
                val chance = drop.chance(table) * multiplier
                map[drop] = chance
            } else if (drop is DropTable) {
                val chance = if (table.type == TableType.First && drop.chance > 0) table.roll / drop.chance.toDouble() else 1.0
                collectChances(player, drop, map, chance)
            }
        }
    }

    fun ItemDrop.chance(table: DropTable): Double {
        if (table.type == TableType.All) {
            return 1.0
        }
        if (chance <= 0) {
            return 0.0
        }
        return table.roll / chance.toDouble()
    }

    private class InventoryDelegate(
        private val inventory: Inventory,
        private val list: MutableList<ItemDrop> = mutableListOf(),
    ) : MutableList<ItemDrop> by list {
        override fun add(element: ItemDrop): Boolean {
            if (!inventory.add(element.id, element.amount.random()) && element.id != "nothing") {
                println("Failed to add $element")
            }
            return true
        }
    }
}
