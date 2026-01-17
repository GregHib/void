package content.entity.npc

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.social.trade.lend.Loan.getSecondsRemaining
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player

class Banker : Script {

    init {
        npcApproach("Talk-to", "banker*") {
            approachRange(2)
            npc<Quiz>("Good day. How may I help you?")
            val loanReturned = getSecondsRemaining(this, "lend_timeout") < 0
            val collection = false

            if (loanReturned) {
                npc<Neutral>("Before we go any further, I should inform you that an item you lent out has been returned to you.")
            } else if (collection) {
                npc<Neutral>("Before we go any further, I should inform you that you have items ready for collection from the Grand Exchange.")
            }
            menu()
        }

        objectOperate("Use", "bank_chest_*") {
            open("bank")
        }

        objectOperate("Use", "bank_booth_*", arrive = false) { (target) ->
            val banker = NPCs.first { it.def.name == "Banker" }
            talkWith(banker)
            menu()
        }

        npcApproach("Bank", "banker*") {
            approachRange(2)
            open("bank")
        }

        npcApproach("Collect", "banker*") {
            approachRange(2)
            open("collection_box")
        }
    }

    suspend fun Player.menu() {
        choice {
            option("I'd like to access my bank account, please.", block = { open("bank") })
            option("I'd like to check my PIN settings.", block = { open("bank_pin") })
            option("I'd like to see my collection box.", block = { open("collection_box") })
            option("I'd like to see my Returned Items box.", block = { open("returned_items") })
            option("What is this place?") {
                npc<Neutral>("This is a branch of the Bank of ${Settings["server.name"]}. We have branches in many towns.")
                choice {
                    option("And what do you do?") {
                        npc<Neutral>("We will look after your items and money for you. Leave your valuables with us if you want to keep them safe.")
                        achievement()
                    }
                    option("Didn't you used to be called the Bank of Varrock?") {
                        npc<Neutral>("Yes we did, but people kept on coming into our branches outside of Varrock and telling us that our signs were wrong. They acted as if we didn't know what town we were in or something.")
                        achievement()
                    }
                }
            }
        }
    }

    fun Player.achievement() {
        if (!get("you_can_bank_on_us_task", false)) {
            set("you_can_bank_on_us_task", true)
            addVarbit("task_reward_items", "red_dye")
        }
    }
}
