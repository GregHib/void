package content.area.morytania.port_phasmatys

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.member.ghosts_ahoy.checkGhostspeak
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class GhostDisciple : Script {

    init {
        npcOperate("Talk-to", "ahoy_disciple") {
            if (!checkGhostspeak()) {
                return@npcOperate
            }
            if (get("ectofuntus_charges", 0) > 0) {
                collect()
                return@npcOperate
            }
            talk()
        }

        npcOperate("Collect", "ahoy_disciple") {
            collect()
        }
    }

    private suspend fun Player.collect() {
        val amount = get("ectofuntus_charges", 0) * TOKENS_PER_WORSHIP
        if (amount <= 0) {
            npc<Neutral>("I'm sorry, but you haven't earned any.")
            return
        }
        if (inventory.isFull() && !inventory.contains("ecto_token")) {
            npc<Neutral>("I have $amount ectotokens waiting for you mortal, but you do not have room in your inventory for them.")
            return
        }
        set("ectofuntus_charges", 0)
        addOrDrop("ecto_token", amount)
        npc<Neutral>("Certainly, mortal. Here's $amount ectotokens.")
    }

    private suspend fun Player.talk() {
        if (tile.level > 0) {
            npc<Neutral>("Put bones of any type into the machine's hopper, and then turn the handle to grind them. You will need a pot to empty the machine of ground up bones.")
            return
        }
        npc<Neutral>("This is the Ectofuntus, the most marvellous creation of Necrovarus, our glorious leader.")
        options()
    }

    private suspend fun Player.options() {
        choice {
            option<Quiz>("What is the Ectofuntus?") {
                npc<Neutral>("It provides the power to keep us ghosts from passing over into the next plane of existence.")
                player<Quiz>("And how does it work?")
                npc<Neutral>("You have to pour a bucket of ectoplasm into the fountain, a pot of ground bones, and then worship at the Ectofuntus. A unit of unholy power will then be created.")
                options()
            }
            option<Quiz>("Where do I get ectoplasm from?") {
                npc<Neutral>("Necrovarus sensed the power bubbling beneath our feet, and we delved long and deep beneath Port Phasmatys, until we found a pool of natural ectoplasm. You may find it by using the trapdoor over there.")
                options()
            }
            option<Quiz>("How do I grind bones?") {
                npc<Neutral>("There is a bone grinding machine upstairs. Put bones of any type into the machine's hopper, and then turn the handle to grind them. You will need a pot to empty the machine of ground up bones.")
                options()
            }
            option<Quiz>("How do I receive Ectotokens?") {
                npc<Neutral>("We disciples keep track of how many units of power have been produced. Just talk to us once you have generated some and we will reimburse you with the correct amount of Ectotokens.")
                player<Quiz>("How do I generate units of power?")
                npc<Neutral>("You have to pour a bucket of ectoplasm into the fountain and then worship at the Ectofuntus with a pot of ground bones. This will create a unit of unholy power.")
                options()
            }
            option("Thanks for your time.")
        }
    }

    companion object {
        /** Ecto-tokens awarded for each worship at the Ectofuntus. */
        private const val TOKENS_PER_WORSHIP = 5
    }
}
