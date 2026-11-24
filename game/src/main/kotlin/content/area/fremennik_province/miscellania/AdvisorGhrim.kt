package content.area.fremennik_province.miscellania

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

/**
 * Advisor Ghrim after Throne of Miscellania.
 * "How is the kingdom faring?", Collect and Manage need kingdom management (approval, coffers and resources) which isn't implemented yet.
 * https://runescape.wiki/w/Transcript:Advisor_Ghrim
 */
class AdvisorGhrim : Script {

    init {
        npcOperate("Talk-to", "advisor_ghrim_2") {
            npc<Neutral>("Greetings, Your Royal Highness.")
            choice {
                if (questCompleted("blood_runs_deep")) {
                    option<Happy>("It's good to have King Vargas back.") {
                        npc<Neutral>("It certainly is, and I understand he has you to thank, your Majesty.")
                        npc<Neutral>("Perhaps, now that Queen Sigrid has moved in, he will not be tempted to go on any more 'adventures'.")
                    }
                } else if (questCompleted("glorious_memories")) {
                    option<Quiz>("What happened to King Vargas?") {
                        npc<Neutral>("His Majesty has just set out on his latest great adventure. I do not know when he will return, your Royal Highness.")
                    }
                }
                option<Quiz>("How do I manage the kingdom?") {
                    manage()
                }
                option<Quiz>("What am I meant to be doing again?") {
                    npc<Neutral>("Ruling the kingdom, Your Royal Highness.")
                    npc<Neutral>("And making sure that the king doesn't cause any more trouble.")
                }
            }
        }
    }

    private suspend fun Player.manage() {
        npc<Neutral>("Your loyal subjects work hard to collect resources for you. Every day they bring what they have collected to the castle. The resources which they collect are coal from the rocks to the north, maple logs from the trees to the east, herbs from the garden just outside the castle, and tuna and swordfish fished at the port to the south.")
        if (questCompleted("royal_trouble")) {
            npc<Neutral>("Etceteria has also pledged you teak and mahogany logs from their trees to the north, and seeds from their farm to the south. They are paid from the money in the coffers; each day, one-tenth of the money in the coffers is distributed to them. A maximum of 75k is paid to your subjects and the Etceterians, and the coffers can store up to 7.5 million gp.")
        } else {
            npc<Neutral>("They are paid from the money in the coffers; each day, one-tenth of the money in the coffers is distributed to them. A maximum of 75k is paid to your subjects, and the coffers can store up to 7.5 million gp.")
        }
        npc<Neutral>("Another thing which affects the amounts of resources collected is your approval rating. The more your subjects love you, the more they will be motivated to work hard. King Vargas would often go and help out his subjects in their daily tasks, when he was still able to do so, and they loved him dearly for it. Finally, you can choose how much of their efforts your subjects devote to each resource; of course, the harder they work at collecting one, the less time they have to spend collecting the others.")
        if (ownsItem("ghrims_book")) {
            return
        }
        npc<Quiz>("Have you got a copy of my book? It explains everything in more detail.")
        player<Neutral>("No, I don't...")
        if (!inventory.add("ghrims_book")) {
            inventoryFull()
            return
        }
        npc<Neutral>("Here, you can have this one, I have plenty spare.")
        player<Happy>("Thanks!")
    }
}
