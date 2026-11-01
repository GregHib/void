package content.area.misthalin.wizards_tower.runecrafting_guild

import content.entity.player.dialogue.type.intEntry
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class RunecraftingGuildRewards : Script {
    init {
        // https://youtu.be/01EWnc60K6o?t=510
        // message("You may only buy a maximum of 50 items at a time.")
        // message("You do not have enough tokens to buy that item.")
        interfaceOpened("rc_guild_rewards") {
            tab(Tab.Inventory)
            interfaces.sendText("rc_guild_rewards", "token_text", "Tokens: ${inventory.count("runecrafting_guild_tokens")}")
        }

        interfaceOption("Buy 1", "rc_guild_rewards:*") {
            buy(it.component, 1)
        }

        interfaceOption("Buy X", "rc_guild_rewards:*") {
            var amount = intEntry("Enter amount:") // TODO proper message
            if (amount > 50) {
                message("You may only buy a maximum of 50 items at a time.") // https://youtu.be/01EWnc60K6o?t=501
                amount = 50
            }
            buy(it.component, amount)
        }

        interfaceOption("Confirm", "rc_guild_rewards:confirm") {
            confirm()
        }
    }

    private fun Player.confirm() {
        val item = ItemDefinitions.get(get("gop_reward_item", -1)).stringId
        val amount = get("gop_reward_amount", 0)
        val cost = 10 // TODO tokens from enum 1680?
        inventory.transaction {
            remove("runecrafting_guild_tokens", cost * amount)
            add(item, amount)
        }
        when (inventory.transaction.error) {
            is TransactionError.Deficient -> message("You do not have enough tokens to buy that item.")
            is TransactionError.Full -> TODO()
            TransactionError.None -> message("Your purchase has been added to your inventory.")
            else -> {}
        }
    }

    private fun Player.buy(item: String, amount: Int) {
        val cost = 10 // TODO tokens from enum 1680?
        val tokens = inventory.count("runecrafting_guild_tokens")
        if (cost * amount < tokens) {
            message("You do not have enough tokens to buy that item.") // https://youtu.be/01EWnc60K6o?t=512
            return
        }
        val def = ItemDefinitions.get(item)
        set("gop_reward_item", def.id)
        set("gop_reward_amount", amount)
        interfaces.sendText("rc_guild_rewards", "reward_text", "${def.name} ($amount)")
    }
}
