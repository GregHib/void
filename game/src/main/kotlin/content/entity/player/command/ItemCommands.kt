@file:Suppress("UNCHECKED_CAST")

package content.entity.player.command

import content.social.trade.exchange.GrandExchange
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.*
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit

@Script
class ItemCommands : Api {

    val areas: AreaDefinitions by inject()
    val players: Players by inject()
    val exchange: GrandExchange by inject()
    val definitions: ItemDefinitions by inject()
    val enums: EnumDefinitions by inject()
    val itemDefinitions: ItemDefinitions by inject()
    val accounts: AccountDefinitions by inject()

    val alternativeNames = Object2ObjectOpenHashMap<String, String>()

    init {
        worldSpawn {
            for (id in 0 until definitions.size) {
                val definition = definitions.get(id)
                val list = (definition.extras as? MutableMap<String, Any>)?.remove("aka") as? List<String> ?: continue
                for (name in list) {
                    alternativeNames[name] = definition.stringId
                }
            }
        }

        adminCommand(
            "item",
            stringArg("item-id", autofill = itemDefinitions.ids.keys),
            intArg("item-amount", "number of items to spawn (e.g. 100, 10k, 5m, default 1)", optional = true),
            desc = "Spawn an item into your inventory",
            handler = ::itemSpawn,
        )

        adminCommand(
            "give",
            stringArg("player-name", autofill = accounts.displayNames.keys),
            stringArg("item-id", autofill = itemDefinitions.ids.keys),
            intArg("item-amount", "number of items to spawn (e.g. 100, 10k, 5m)", optional = true),
            desc = "Spawn an item into another players inventory",
            handler = ::targetSpawn,
        )

        adminCommand(
            "items",
            stringArg("item-id", autofill = itemDefinitions.ids.keys),
            stringArg("id", autofill = itemDefinitions.ids.keys, optional = true),
            stringArg("id", autofill = itemDefinitions.ids.keys, optional = true),
            stringArg("id", autofill = itemDefinitions.ids.keys, optional = true),
            stringArg("id", autofill = itemDefinitions.ids.keys, optional = true),
            desc = "Spawn multiple items at once",
            handler = ::itemSpawns,
        )
    }

    fun targetSpawn(player: Player, args: List<String>) {
        val name = args[0]
        val itemName = alternativeNames.getOrDefault(args.getOrNull(1), args[1])
        val amount = (args.getOrNull(2) ?: "1").toSILong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val target = players.get(name)
        if (target == null) {
            player.message("Couldn't find online player '$name'", ChatType.Console)
            return
        }
        spawn(player, target, definitions.get(itemName), amount)
    }

    fun itemSpawn(player: Player, args: List<String>) {
        val name = alternativeNames.getOrDefault(args.getOrNull(0), args[0])
        val definition = definitions.get(name)
        val amount = (args.getOrNull(1) ?: "1").toSILong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        spawn(player, player, definition, amount)
    }

    fun itemSpawns(player: Player, args: List<String>) {
        for (item in args) {
            val name = alternativeNames.getOrDefault(item, item)
            val definition = definitions.get(name)
            spawn(player, player, definition, 1)
        }
    }

    private fun spawn(source: Player, target: Player, definition: ItemDefinition, amount: Int) {
        val id = definition.stringId
        val charges = definition.getOrNull<Int>("charges")
        target.inventory.transaction {
            if (charges != null) {
                for (i in 0 until amount.coerceAtMost(28)) {
                    val index = inventory.freeIndex()
                    if (index == -1) {
                        break
                    }
                    set(index, Item(id, 1))
                    if (charges > 0) {
                        charge(target, index, charges)
                    }
                }
            } else {
                addToLimit(id, amount)
            }
        }
        if (target.inventory.transaction.error != TransactionError.None) {
            source.message(target.inventory.transaction.error.toString(), ChatType.Console)
        } else if (source != target) {
            source.message("Success", ChatType.Console)
        }
    }
}
