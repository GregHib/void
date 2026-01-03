package content.area.fremennik_province.neitiznot

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class ThakkradSigmundson : Script {

    init {
        npcOperate("Talk-to", "thakkrad_sigmundson") {
            npc<Neutral>("Thank you for leading the Burgher's militia against the Troll King. Now that the trolls are leaderless I have repaired the bridge to the central isle for you as best I can.")
            player<Quiz>("Thanks Thakkrad. Does that mean I have access to the runite ores on that island?")
            npc<Neutral>("Yes, you should be able to mine runite there if you wish.")
        }

        npcOperate("Craft-goods", "thakkrad_sigmundson") {
            choice("What can I help you with?") {
                option("Cure my yak hide, please.") {
                    cureHide()
                }
                option<Neutral>("Nothing, thanks.") {
                    npc<Neutral>("See you later. You won't find anyone else who can cure yak-hide.")
                }
            }
        }

        itemOnNPCOperate("yak_hide", "thakkrad_sigmundson") {
            cureHide()
        }
    }

    suspend fun Player.cureHide() {
        player<Neutral>("Cure my yak hide please.")
        npc<Neutral>("I will cure yak-hide for a fee of 5 gp per hide.")
        choice("How many hides do you want cured?") {
            option("Cure all my hides.") {
                cure(inventory.count("yak_hide"))
            }
            option("Cure one hide.") {
                cure(1)
            }
            option("Cure no hide.") {
                npc<Neutral>("Bye.")
            }
            option<Quiz>("Can you cure any type of leather?") {
                npc<Confused>("Other types of leather? Why would you need any other type of leather?")
                player<Neutral>("I'll take that as a no then.")
            }
        }
    }

    suspend fun Player.cure(amount: Int) {
        if (!inventory.contains("yak_hide")) {
            npc<Neutral>("You have no yak-hide to cure.")
            return
        }
        inventory.transaction {
            val removed = removeToLimit("yak_hide", amount)
            remove("coins", removed * 5)
            add("cured_yak_hide", removed)
        }
        when (inventory.transaction.error) {
            is TransactionError.Deficient -> npc<Neutral>("You don't have enough gold to pay me!.")
            TransactionError.None -> npc<Neutral>("There you go.")
            else -> {}
        }
    }
}
