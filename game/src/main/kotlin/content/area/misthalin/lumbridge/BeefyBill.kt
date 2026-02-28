package content.area.misthalin.lumbridge

import content.entity.npc.shop.openShop
import content.entity.player.bank.bank
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class BeefyBill : Script {

    init {
        npcOperate("Talk-to", "beefy_bill") {
            npc<Happy>("Beefy Bill at your service!")
            npc<Happy>("I can bank your beef, your cowhides and your flour, and I've got other stuff for trade. What's it to be?")
            choice("What would you like to say?") {
                trade()
                bankThings()
                option<Quiz>("Who are you?") {
                    npc<Neutral>("I'm Beefy Bill, specialist meat transporter and general merchant.")
                    npc<Neutral>("People bring me their beef, cowhides and flour, and I transport it all to the bank, keeping a mere 10% for my services. I also have stuff for sale.")
                    choice {
                        bankThings()
                        trade()
                        option<Neutral>("How do you pull your wagon?") {
                            npc<Laugh>("Oh, I don't pull it myself!<br>I use cattle to pull it for me.")
                            choice {
                                option<Quiz>("Isn't that disgusting?") {
                                    npc<Happy>("Oh, stop being naive! I'm not letting your petty personal ethics stand in the way of my right to run a successful business.")
                                    npc<Quiz>("Now, do you want my services or not?")
                                    choice {
                                        bankThings()
                                        trade()
                                        thinkItOver()
                                    }
                                }
                                bankThings()
                                trade()
                                thinkItOver()
                            }
                        }
                        thinkItOver()
                    }
                }
                thinkItOver()
            }
        }

        itemOnNPCOperate(npc = "beefy_bill") {
            when (it.item.id) {
                "cowhide", "pot_of_flour", "beef" -> transport(it.item.id)
                else -> npc<Neutral>("Sorry, I don't transport that sort of thing.")
            }
        }
    }

    private suspend fun Player.transport(item: String) {
        val count = inventory.count(item)
        choice {
            val cost = when {
                count < 12 -> 1
                count < 23 -> 2
                else -> 3
            }
            option("Bank ${count - cost}, Bill keeps $cost.") {
                inventory.transaction {
                    val link = link(bank)
                    remove(item, count)
                    link.add(item, count - cost)
                }
                when (inventory.transaction.error) {
                    is TransactionError.Full -> message("You don't have enough bank space to store that.")
                    TransactionError.None -> npc<Happy>("Pleasure doing business with ya, mate!")
                    else -> {}
                }
            }
            option("Forget it.")
        }
    }

    private fun ChoiceOption.trade() {
        option("Let's trade.") {
            openShop("beefy_bills_supplies")
        }
    }

    private fun ChoiceOption.bankThings() {
        option<Neutral>("I want you to bank things for me.") {
            npc<Happy>("Excellent. Just hand me the items, and I'll work out a price for you. I charge a 10% commission.")
        }
    }

    private fun ChoiceOption.thinkItOver() {
        option<Neutral>("I'll have a think about it.") {
            npc<Neutral>("Don't waste too much time thinking; time is money.")
        }
    }
}
