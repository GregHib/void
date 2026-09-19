package content.minigame.mage_training_arena

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * The Rewards Guardian trades Pizazz Points from all four rooms for wands, infinity robes, runes
 * and the Bones to Peaches spell.
 */
class MageTrainingArenaRewards(private val inventoryDefinitions: InventoryDefinitions) : Script {

    init {
        npcOperate("Talk-to", "rewards_guardian") {
            if (!get("mage_training_arena_started", false)) {
                npc<Neutral>("Greetings. Have you spoken to my fellow Guardian downstairs?")
                player<Neutral>("Nope.")
                npc<Neutral>("Well, you need to talk to him first.")
                return@npcOperate
            }
            player<Neutral>("Hi.")
            npc<Neutral>("Greetings. What wisdom do you seek?")
            menu()
        }

        npcOperate("Trade-with", "rewards_guardian") {
            if (!get("mage_training_arena_started", false)) {
                npc<Neutral>("Have you spoken to my fellow guardian downstairs?")
                player<Neutral>("Nope.")
                npc<Neutral>("Well, you need to talk to him first.")
                return@npcOperate
            }
            openRewards()
        }

        interfaceOption("Value", "mage_training_arena_rewards:stock") { (item) ->
            val row = reward(item.id) ?: return@interfaceOption
            val coins = row.intOrNull("coins") ?: 0
            if (coins > 0) {
                message("The ${item.def.name.lowercase()} costs $coins gold coins.")
                return@interfaceOption
            }
            message("The ${item.def.name} costs ${row.int("telekinetic")} Telekinetic, ${row.int("alchemist")} Alchemist,")
            message("${row.int("enchanting")} Enchantment and ${row.int("graveyard")} Graveyard Pizazz Points.")
        }

        interfaceOption("Buy", "mage_training_arena_rewards:stock") { (item) ->
            val row = reward(item.id) ?: return@interfaceOption
            buy(this, row)
        }

        interfaceOption("Examine", "mage_training_arena_rewards:stock") { (item) ->
            message(item.def.getOrNull("examine") ?: return@interfaceOption, ChatType.ItemExamine)
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Neutral>("Who are you?") {
                npc<Neutral>("Me? I'm here to grant you rewards for any of the Pizazz Points you may have earned in this training arena. Like my fellow Guardians, I am part of the arena and live to ensure its safe running.")
                player<Neutral>("I see.")
                menu()
            }
            option<Neutral>("Can I trade my Pizazz Points please?") {
                npc<Neutral>("Why of course.")
                openRewards()
            }
            option<Neutral>("Got anything else I can buy?") {
                npc<Neutral>("Well, we do stock a special book that you may be interested in, which provides a comprehensive guide to this training arena. It costs 200gp. Would like one?")
                choice {
                    option<Neutral>("Yes please.") {
                        buy(this, Tables.get("mta_rewards").rows().first { it.item("item") == "arena_book" })
                    }
                    option<Neutral>("No thanks.")
                }
            }
        }
    }

    private fun Player.openRewards() {
        val id = "mage_training_arena"
        val new = !inventories.contains(id)
        val inventory = inventories.inventory(id)
        if (new) {
            val definition = inventoryDefinitions.get(id)
            for (index in 0 until definition.length) {
                val itemId = definition.ids?.getOrNull(index) ?: continue
                val amount = definition.amounts?.getOrNull(index) ?: continue
                inventory.transaction { set(index, Item(ItemDefinitions.get(itemId).stringId, amount)) }
            }
        }
        open("mage_training_arena_rewards")
        sendInventory(id)
        interfaceOptions.unlockAll("mage_training_arena_rewards", "stock", 0 until inventory.size)
    }

    private suspend fun buy(player: Player, row: RowDefinition) {
        val item = row.item("item")
        val coins = row.intOrNull("coins") ?: 0
        if (coins > 0) {
            if (!player.inventory.remove("coins", coins)) {
                player.message("You don't have enough coins.")
                return
            }
            player.addOrDrop(item)
            AuditLog.event(player, "mta_reward", item, coins)
            return
        }
        if (item == "bones_to_peaches_spell" && player["bones_to_peaches", false]) {
            player.message("You already unlocked that spell.")
            return
        }
        for (room in PizazzPoints.rooms) {
            if (PizazzPoints.get(player, room) < row.int(room)) {
                player.message("You cannot afford that item.")
                return
            }
        }
        val required = row.itemOrNull("requires")
        val previous = if (required == null) {
            null
        } else if (player.inventory.contains(required)) {
            player.inventory
        } else if (player.equipment.contains(required)) {
            player.equipment
        } else if (player.inventory.contains("master_wand") || player.equipment.contains("master_wand")) {
            null
        } else {
            player.message("You don't have the required wand to buy this upgrade.")
            return
        }
        if (item != "bones_to_peaches_spell") {
            player.inventory.transaction {
                if (previous === player.inventory && required != null) {
                    remove(required)
                } else if (previous != null && required != null) {
                    link(previous).remove(required)
                }
                add(item)
            }
            when (player.inventory.transaction.error) {
                is TransactionError.Full -> {
                    player.message("You don't have enough inventory space.")
                    return
                }
                TransactionError.None -> {}
                else -> return
            }
        }
        for (room in PizazzPoints.rooms) {
            PizazzPoints.remove(player, room, row.int(room))
        }
        AuditLog.event(player, "mta_reward", item, row.int("telekinetic"), row.int("alchemist"), row.int("enchanting"), row.int("graveyard"))
        if (item == "bones_to_peaches_spell") {
            player["bones_to_peaches"] = true
            player.statement("The Guardian teaches you how to use the Bones to Peaches spell!")
        }
    }

    private fun reward(item: String): RowDefinition? = Tables.get("mta_rewards").rows().firstOrNull { it.item("item") == item }
}
