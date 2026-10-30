package content.area.karamja.brimhaven.agility_arena

import content.area.karamja.brimhaven.agility_arena.BrimhavenAgilityArena.Companion.arenaExp
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class PirateJackie : Script {

    init {
        npcOperate("Talk-to", "pirate_jackie_the_fruit_brimhaven") {
            player<Neutral>("Ahoy there!")
            npc<Neutral>("Ahoy!")
            choice {
                option<Neutral>("What is this place?") {
                    npc<Neutral>("Welcome to the Brimhaven Agility Arena!")
                    npc<Neutral>("If ye want to know more talk to Cap'n Izzy, he found it!")
                }
                option<Neutral>("What do you do?") {
                    npc<Neutral>("I be the Jack o' tickets. I exchange the tickets ye collect in the Agility Arena for more stuff. Ye can obtain more agility experience or some items ye won't find anywhere else!")
                    player<Neutral>("Sounds good!")
                }
                option<Neutral>("I'd like to trade in my tickets, please.") {
                    npc<Neutral>("Aye, ye be on the right track.")
                    open("brimhaven_arena_ticket_exchange")
                }
                option<Neutral>("See you later.")
            }
        }

        npcOperate("Trade", "pirate_jackie_the_fruit_brimhaven") {
            open("brimhaven_arena_ticket_exchange")
        }

        interfaceOption("Buy", "brimhaven_arena_ticket_exchange:*") {
            exchange(it.component)
        }

        interfaceOption("Close", "brimhaven_arena_ticket_exchange:close") {
            close("brimhaven_arena_ticket_exchange")
        }
    }

    fun Player.exchange(reward: String) {
        val tickets = Tables.intOrNull("agility_arena_rewards.$reward.tickets") ?: return
        val item = Tables.itemOrNull("agility_arena_rewards.$reward.item")
        if (inventory.count("agility_arena_ticket") < tickets) {
            val name = if (item == null) "Agility experience" else ItemDefinitions.get(item).name.removePrefix("Clean ").replaceFirstChar { it.uppercase() }
            message("$name costs $tickets tickets.")
            return
        }
        inventory.transaction {
            remove("agility_arena_ticket", tickets)
            if (item != null) {
                add(item)
            }
        }
        when (inventory.transaction.error) {
            TransactionError.None -> {}
            is TransactionError.Full -> {
                message("You don't have enough inventory space.")
                return
            }
            else -> return
        }
        if (item != null) {
            message("You have been granted a ${ItemDefinitions.get(item).name.removePrefix("Clean ").lowercase()}.")
            return
        }
        arenaExp(Tables.int("agility_arena_rewards.$reward.experience").toDouble())
        message("You have been granted some Agility experience!")
    }
}
