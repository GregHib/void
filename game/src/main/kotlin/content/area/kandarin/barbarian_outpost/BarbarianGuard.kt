package content.area.kandarin.barbarian_outpost

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

@Script
class BarbarianGuard {

    init {
        npcOperate("Talk-to", "barbarian_guard") {
            when (player.quest("alfred_grimhands_barcrawl")) {
                "unstarted" -> startBarCrawl()
                "completed" -> toggleVialSmashing()
                else -> if (player["barcrawl_signatures", emptyList<String>()].size == 10) {
                    questComplete()
                } else {
                    checkProcess()
                }
            }
        }
    }

    suspend fun NPCOption<Player>.startBarCrawl() {
        npc<Quiz>("Oi, whaddya want?")
        choice {
            option<Talk>("I want to come through this gate.") {
                npc<Quiz>("Barbarians only. Are you a barbarian? You don't look like one.")
                choice {
                    option<Talk>("Hmm, yep you've got me there.")
                    option<Talk>("Looks can be deceiving, I am in fact a barbarian.") iAm@{
                        if (player.inventory.isFull()) {
                            npc<Talk>("Are you, now? Free up some space so you can carry something, and we'll see how barbarian you really are.")
                            return@iAm
                        }
                        npc<Talk>("If you're a barbarian you need to be able to drink like one. We barbarians like a good drink.")
                        npc<Happy>("I have the perfect challenge for you... The Alfred Grimhand Barcrawl! First completed by Alfred Grimhand.")
                        if (player.inventory.add("barcrawl_card")) {
                            player["alfred_grimhands_barcrawl"] = "signatures"
                            item("barcrawl_card", 400, "The guard hands you a Barcrawl card.")
                        }
                        npc<Talk>("Take that card to each of the bars named on it. The bartenders will know what it means. We're kinda well known.")
                        npc<Talk>("They'll give you their strongest drink and sign your card. When you've done all that, we'll be happy to let you in.")
                    }
                }
            }
            option<Talk>("I want some money.") {
                npc<Talk>("Do I look like a bank to you?")
            }
        }
    }

    suspend fun NPCOption<Player>.toggleVialSmashing() {
        if (player["vial_smashing", false]) {
            npc<Quiz>("'Ello friend. I see you're drinking like a barbarian - do you want to stop smashing your vials when you finish them?")
            choice {
                option<Talk>("Yes please, I want to stop smashing my vials.") {
                    player["vial_smashing"] = false
                    player.message("Vial smashing is now turned off.")
                    npc<Talk>("You're a funny sort of barbarian! But okay, you will no longer smash your vials as you drink your potions.")
                }
                option<Talk>("No thank you, I like smashing them.") {
                    npc<Talk>("That's a proper barbarian spirit, that is.")
                    player.message("Vial smashing is now turned off.")
                }
            }
        } else {
            npc<Quiz>("'Ello friend. Do you want me to show you how to smash your vials when you finish drinking them?")
            choice {
                option<Talk>("Yes please, I want to smash my vials.") {
                    player["vial_smashing"] = true
                    player.message("Vial smashing is now turned on.")
                    npc<Talk>("It's all part of drinking like a barbarian! Okay, you will now smash your vials as you drink your potions.")
                }
                option<Talk>("No thank you, I'd rather keep my vials.")
            }
        }
    }

    suspend fun NPCOption<Player>.questComplete() {
        npc<Quiz>("So, how's the Barcrawl coming along?")
        player<Drunk>("I tink I jusht 'bout done dem all... but I losht count...")
        if (player.inventory.remove("barcrawl_card")) {
            item("barcrawl_card", 400, "You give the card to the barbarian.")
            player["alfred_grimhands_barcrawl"] = "completed"
            player.clear("barcrawl_signatures")
        }
        npc<Talk>("Yep that seems fine, you can come in now. I never learned to read, but you look like you've drunk plenty. Also, one more thing...")
        npc<Talk>("Since you drink like a barbarian, I can show you how to smash your vials when you finish them. Do you want to do that?")
        choice {
            option<Talk>("Yes please, I want to smash my vials.") {
                npc<Talk>("It's all part of drinking like a barbarian! Okay, you will now smash your vials as you drink your potions.")
                player.message("Vial smashing is now turned on.")
            }
            option<Talk>("No thank you, I'd rather keep my vials.")
        }
    }

    suspend fun NPCOption<Player>.checkProcess() {
        npc<Quiz>("So, how's the Barcrawl coming along?")
        if (player.ownsItem("barcrawl_card")) {
            player<Sad>("I haven't finished it yet.")
            npc<Talk>("Well come back when you have, you lightweight.")
            return
        }
        player<Sad>("I've lost my barcrawl card...")
        if (player.inventory.isFull()) {
            npc<Quiz>("What are you like? You're gonna have ot free up some space so I can give you another one.")
            return
        }
        npc<Quiz>("What are you like? You're gonna have to start all over now.")
        if (player.inventory.add("barcrawl_card")) {
            player.clear("barcrawl_signatures")
            item("barcrawl_card", 400, "The guard hands you a Barcrawl card.")
        }
    }
}
