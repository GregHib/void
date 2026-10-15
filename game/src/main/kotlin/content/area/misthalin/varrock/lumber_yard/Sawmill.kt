package content.area.misthalin.varrock.lumber_yard

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

class Sawmill : Script {
    init {
        interfaceOption("*", "sawmill:*_planks") {
            val type = Rows.getOrNull("planks.${it.component.removeSuffix("_planks")}") ?: return@interfaceOption
            val logs = type.item("log")
            val amount = when (it.option) {
                "Buy <col=ff9040>1</col>" -> 1
                "Buy <col=ff9040>5</col>" -> 5
                "Buy <col=ff9040>10</col>" -> 10
                "Buy <col=ff9040>x</col>" -> intEntry("Enter amount:").coerceIn(0, inventory.count(logs))
                else -> inventory.count(logs)
            }
            val price = Tables.int("sawmill_prices.$logs.cost")
            val plank = type.item("plank")
            inventory.transaction {
                remove("coins", price * amount)
                remove(logs, amount)
                add(plank, amount)
            }
            close("sawmill")
            when (val error = inventory.transaction.error) {
                is TransactionError.Deficient -> if (error.item == "coins") {
                    npc<Neutral>("Those planks cost $price coins. You don't have enough money for all of them.")
                } else {
                    npc<Neutral>("You'll need to bring me some more logs.")
                }
                TransactionError.None -> {
                    val tile = Tile(3302, 3491)
                    val sawdust = FloorItems.firstOrNull(tile, "sawdust")
                    if (sawdust == null) {
                        FloorItems.add(tile, "sawdust", revealTicks = 100, disappearTicks = 200)
                    }
                }
                else -> {}
            }
        }
    }
}
