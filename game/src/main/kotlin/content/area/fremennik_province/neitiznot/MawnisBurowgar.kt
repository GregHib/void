package content.area.fremennik_province.neitiznot

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class MawnisBurowgar : Script {

    init {
        npcOperate("Talk-to", "mawnis_burowgar*") {
            if (!questCompleted("fremennik_isles")) {
                return@npcOperate
            }
            npc<Happy>("It makes me proud to know that the helm of my ancestors will be worn in battle.")
            npc<Happy>("I thank you on behalf of all my kinsmen $name.")
            if (ownsItem("helm_of_neitiznot")) {
                return@npcOperate
            }
            player<Shifty>("Ah yes, about that beautiful helmet.")
            npc<Confused>("You mean the priceless heirloom that I gave to you as a sign of my trust and gratitude?")
            player<Shifty>("Err yes, that one. I may have mislaid it.")
            npc<Confused>("It's a good job I have alert and loyal men who notice when something like this is left lying around and picks it up.")
            npc<Confused>("I'm afraid I'm going to have to charge you a 50,000GP handling cost.")
            choice("Pay 50,000GP to recover your helmet?") {
                option("Yes, that would be fine.") {
                    inventory.transaction {
                        remove("coins", 50_000)
                        add("helm_of_neitiznot")
                    }
                    when (inventory.transaction.error) {
                        TransactionError.None -> {
                            item("helm_of_neitiznot", "The Burgher hands you his crown.")
                            npc<Happy>("Please be more careful with it in the future.")
                        }
                        else -> npc<Neutral>("You don't have enough gold right now.")
                    }
                }
                option("No, that's too much.") {
                    npc<Neutral>("Okay. Come back later if you change your mind.")
                }
            }
        }
    }
}
