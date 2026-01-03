package content.area.kandarin.barbarian_outpost

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class BarbarianGuard : Script {

    init {
        npcOperate("Talk-to", "barbarian_guard") {
            when (quest("alfred_grimhands_barcrawl")) {
                "unstarted" -> startBarCrawl()
                "completed" -> toggleVialSmashing()
                else -> if (get("barcrawl_signatures", emptyList<String>()).size == 10) {
                    questComplete()
                } else {
                    checkProcess()
                }
            }
        }
    }

    suspend fun Player.startBarCrawl() {
        npc<Quiz>("Oi, whaddya want?")
        choice {
            option<Neutral>("I want to come through this gate.") {
                npc<Quiz>("Barbarians only. Are you a barbarian? You don't look like one.")
                choice {
                    option<Neutral>("Hmm, yep you've got me there.")
                    option<Neutral>("Looks can be deceiving, I am in fact a barbarian.") iAm@{
                        if (inventory.isFull()) {
                            npc<Neutral>("Are you, now? Free up some space so you can carry something, and we'll see how barbarian you really are.")
                            return@iAm
                        }
                        npc<Neutral>("If you're a barbarian you need to be able to drink like one. We barbarians like a good drink.")
                        npc<Happy>("I have the perfect challenge for you... The Alfred Grimhand Barcrawl! First completed by Alfred Grimhand.")
                        if (inventory.add("barcrawl_card")) {
                            set("alfred_grimhands_barcrawl", "signatures")
                            item("barcrawl_card", 400, "The guard hands you a Barcrawl card.")
                        }
                        npc<Neutral>("Take that card to each of the bars named on it. The bartenders will know what it means. We're kinda well known.")
                        npc<Neutral>("They'll give you their strongest drink and sign your card. When you've done all that, we'll be happy to let you in.")
                    }
                }
            }
            option<Neutral>("I want some money.") {
                npc<Neutral>("Do I look like a bank to you?")
            }
        }
    }

    suspend fun Player.toggleVialSmashing() {
        if (get("vial_smashing", false)) {
            npc<Quiz>("'Ello friend. I see you're drinking like a barbarian - do you want to stop smashing your vials when you finish them?")
            choice {
                option<Neutral>("Yes please, I want to stop smashing my vials.") {
                    set("vial_smashing", false)
                    message("Vial smashing is now turned off.")
                    npc<Neutral>("You're a funny sort of barbarian! But okay, you will no longer smash your vials as you drink your potions.")
                }
                option<Neutral>("No thank you, I like smashing them.") {
                    npc<Neutral>("That's a proper barbarian spirit, that is.")
                    message("Vial smashing is now turned off.")
                }
            }
        } else {
            npc<Quiz>("'Ello friend. Do you want me to show you how to smash your vials when you finish drinking them?")
            choice {
                option<Neutral>("Yes please, I want to smash my vials.") {
                    set("vial_smashing", true)
                    message("Vial smashing is now turned on.")
                    npc<Neutral>("It's all part of drinking like a barbarian! Okay, you will now smash your vials as you drink your potions.")
                }
                option<Neutral>("No thank you, I'd rather keep my vials.")
            }
        }
    }

    suspend fun Player.questComplete() {
        npc<Quiz>("So, how's the Barcrawl coming along?")
        player<Drunk>("I tink I jusht 'bout done dem all... but I losht count...")
        if (inventory.remove("barcrawl_card")) {
            item("barcrawl_card", 400, "You give the card to the barbarian.")
            set("alfred_grimhands_barcrawl", "completed")
            clear("barcrawl_signatures")
        }
        npc<Neutral>("Yep that seems fine, you can come in now. I never learned to read, but you look like you've drunk plenty. Also, one more thing...")
        npc<Neutral>("Since you drink like a barbarian, I can show you how to smash your vials when you finish them. Do you want to do that?")
        choice {
            option<Neutral>("Yes please, I want to smash my vials.") {
                npc<Neutral>("It's all part of drinking like a barbarian! Okay, you will now smash your vials as you drink your potions.")
                message("Vial smashing is now turned on.")
            }
            option<Neutral>("No thank you, I'd rather keep my vials.")
        }
    }

    suspend fun Player.checkProcess() {
        npc<Quiz>("So, how's the Barcrawl coming along?")
        if (ownsItem("barcrawl_card")) {
            player<Disheartened>("I haven't finished it yet.")
            npc<Neutral>("Well come back when you have, you lightweight.")
            return
        }
        player<Disheartened>("I've lost my barcrawl card...")
        if (inventory.isFull()) {
            npc<Quiz>("What are you like? You're gonna have to free up some space so I can give you another one.")
            return
        }
        npc<Quiz>("What are you like? You're gonna have to start all over now.")
        if (inventory.add("barcrawl_card")) {
            clear("barcrawl_signatures")
            item("barcrawl_card", 400, "The guard hands you a Barcrawl card.")
        }
    }
}
