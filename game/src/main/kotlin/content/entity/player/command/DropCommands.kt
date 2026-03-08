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
import world.gregs.voidps.engine.entity.character.player.name
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
            val map = mutableMapOf<ItemDrop, Int>()
            coroutineScope {
                val time = measureTimeMillis {
                    (0 until count).chunked(1_000_000).map { numbers ->
                        async {
                            val list = mutableListOf<ItemDrop>()
                            for (i in numbers) {
                                table.roll(list = list, player = player)
                            }
                            list
                        }
                    }.forEach {
                        for (drop in it.await()) {
                            map[drop] = (map[drop] ?: 0) + 1
                        }
                    }
                }
                val s = System.currentTimeMillis()
                for ((drop, amount) in map) {
                    var total = 0L
                    for (i in 1..amount) {
                        total += drop.amount.random()
                    }
                    inventory.add(drop.id, total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
                }
                player.message("Finished total in ${(System.currentTimeMillis() - s)}ms")
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
            val itemChances = mutableMapOf<String, Double>()
            collectChances(player, table, itemChances)
            val sortByPrice = false
            try {
                if (sortByPrice) {
                    inventory.sortedByDescending { exchange(it) }
                } else {
                    inventory.sortedByDescending { it.amount.toLong() }
                }
                World.queue("drop_sim_${player.name}") {
                    for ((drop, c) in map.toList().sortedBy { it.first.id }) {
                        if (drop.id == "nothing" || drop.amount.last == 0) {
                            continue
                        }
                        val quantity = if (drop.amount.first == drop.amount.last) drop.amount.first else "${drop.amount.first}-${drop.amount.last}"
                        val chance = itemChances["${drop.id} x$quantity"] ?: continue
                        val real = (1 / chance).toInt()
                        val rate = (1 / (c / count.toDouble())).toInt()
                        if (rate != real) {
                            player.message("${drop.id} x$quantity rate=1/$rate (real=1/$real)")
                        } else {
                            player.message("${drop.id} x$quantity rate=1/$rate")
                        }
                    }
                    var alchValue = 0L
                    var exchangeValue = 0L
                    for (item in inventory.items) {
                        if (item.isNotEmpty()) {
                            alchValue += alch(item)
                            exchangeValue += exchange(item)
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
        val chances = mutableMapOf<String, Double>()
        collectChances(player, table, chances)
        for ((drop, chance) in chances) {
            player.message("$drop - 1/${(1 / chance).toInt()}")
        }
    }

    fun collectChances(player: Player, table: DropTable, map: MutableMap<String, Double>, multiplier: Double = 1.0) {
        for (drop in table.drops) {
            if (drop is ItemDrop) {
                val chance = drop.chance(table) * multiplier
                val quantity = if (drop.amount.first == drop.amount.last) drop.amount.first else "${drop.amount.first}-${drop.amount.last}"
                val id = "${drop.id} x$quantity"
                map[id] = map.getOrPut(id) { 0.0 } + chance
            } else if (drop is DropTable) {
                val chance = multiplier * if (table.type == TableType.First && drop.chance > 0) drop.chance / table.roll.toDouble() else 1.0
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
        return chance / table.roll.toDouble()
    }
}
