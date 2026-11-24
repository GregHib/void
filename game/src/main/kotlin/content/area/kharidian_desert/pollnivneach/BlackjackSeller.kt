package content.area.kharidian_desert.pollnivneach

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * https://runescape.wiki/w/Transcript:Blackjack_seller
 */
class BlackjackSeller : Script {

    init {
        // The rest of his dialogue continues the Rogue Trader activity, which isn't implemented
        npcOperate("Talk-to", "blackjack_seller_pollnivneach_2") {
            npc<Neutral>("Hello. Could I interest you in a blackjack?")
            npc<Quiz>("Ah, it's you again $name. Tell me how are the blackjack sales going?")
            player<Neutral>("Not too bad, Ali was well impressed by the reaction and demand they had initially but he's a little concerned about how the sales have dropped off of recent.")
            player<Quiz>("Maybe you could produce a different range of products?")
            npc<Neutral>("I'm not too sure, I'm quite happy to stick with what I've got unless, I had a good reason to change.")
        }

        npcOperate("Trade", "blackjack_seller_pollnivneach_2") {
            npc<Neutral>("I've only got one model in stock at the moment. It's a willow black jack which I'd be willing to let go for 600 coins.")
            choice {
                option<Neutral>("I'll take it.") {
                    inventory.transaction {
                        remove("coins", 600)
                        add("willow_blackjack")
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> {}
                        is TransactionError.Full -> inventoryFull()
                        else -> {
                            message("You can not afford that.")
                            player<Shifty>("Can I get that on credit?")
                            npc<Angry>("Get out of here!")
                        }
                    }
                }
                option<Neutral>("No thanks.")
            }
        }
    }
}
