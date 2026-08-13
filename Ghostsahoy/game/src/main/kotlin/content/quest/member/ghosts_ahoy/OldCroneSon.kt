package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class OldCroneSon : Script {
    init {
        npcOperate("Talk-To", "ahoy_oldman") {
            when (val stage = get("ahoy_subquest_toyboat", 0)) {
                0 -> shipwreckIntro()
                1 -> handOverBoat()
                2, 3 -> postBoat(stage)
            }
        }
    }

    private suspend fun Player.shipwreckIntro() {
        player<Neutral>("What are you doing on this shipwreck?")
        npc<Neutral>("Shipwreck?!? Not shipwreck, surely not! Just in port, that's all!")
        player<Neutral>("Don't be silly - half the ship's missing!")
        npc<Neutral>("No no no - the captain's just waiting for the wind to change, then we're off!")
        player<Neutral>("You mean this skeleton sitting here in this chair?")
        npc<Neutral>("You must show more respect to the Captain.")
    }

    private suspend fun Player.handOverBoat() {
        val hasRepairedBoat = inventory.contains("model_ship_silk")
        choice {
            option<Neutral>("What is your name?") {
                npc<Neutral>("I don't remember. Everyone around here just calls me 'boy'.")
                player<Neutral>("You're the cabin boy?!?")
                npc<Neutral>("Yes, and proud of it.")
            }
            option<Neutral>("Can I have the key to the chest?") {
                npc<Neutral>("Hang on, let me ask the Captain ...")
                statement("The old man cocks an ear towards the Pirate Captain's skeleton.")
                if (hasCorrectBoat()) {
                    grantKey()
                } else {
                    npc<Neutral>("The Captain says no.")
                }
            }
            if (hasRepairedBoat) {
                option<Neutral>("Is this your toy boat?") {
                    item(item = "model_ship_silk", text = "The old man inspects the toy boat.")
                    if (hasCorrectBoat()) {
                        npc<Neutral>(
                            "My word - so it is!!! I never thought I would see it again!! Where " +
                                "did you get it from?",
                        )
                        player<Neutral>("Your mother gave it to me to pass on to you.")
                        npc<Neutral>("My mother? She still lives?")
                        player<Neutral>("Yes, in a shack to the west of here.")
                        npc<Neutral>("After all these years ...")
                        player<Neutral>("Can I have the key to the chest, then?")
                        npc<Neutral>("Hang on, let me ask the Captain ...")
                        statement("The old man cocks an ear towards the Pirate Captain's skeleton.")
                        grantKey()
                    } else if (inventory.contains("model_ship")) {
                        npc<Neutral>("No - I made a toy boat a long while ago, but that one had a flag.")
                    } else {
                        npc<Neutral>(
                            "No - I made a toy boat a long while ago, but the colours on the " +
                                "flag were different.",
                        )
                    }
                }
            }
        }
    }

    private suspend fun Player.grantKey() {
        set("ahoy_subquest_toyboat", 2)
        inventory.remove("model_ship")
        inventory.remove("model_ship_silk")
        addOrDrop("chest_key_ghosts_ahoy")
        npc<Neutral>("The Captain says yes.")
        item(item = "chest_key_ghosts_ahoy", text = "The old man gives you the chest key.")
    }

    private suspend fun Player.postBoat(stage: Int) {
        if (ownsItem("chest_key_ghosts_ahoy") || stage == 3) {
            player<Neutral>("How is it going?")
            npc<Neutral>("Wonderful, wonderful! Mother's coming to get me!")
        } else {
            player<Quiz>("I've lost my key to the chest - do you have another one?")
            npc<Neutral>(
                "What? But the chest is only just over there! How on RuneScape did you lose it " +
                    "in that short distance?",
            )
            player<Sad>("Sorry, I won't do it again...")
            addOrDrop("chest_key_ghosts_ahoy")
            item(item = "chest_key_ghosts_ahoy", text = "The old man grudgingly gives you another chest key.")
        }
    }

    private fun Player.hasCorrectBoat(): Boolean = get("ahoy_mast_top", 0) == get("ahoy_toy_top", 0) &&
        get("ahoy_mast_skull", 0) == get("ahoy_toy_skull", 0) &&
        get("ahoy_mast_bottom", 0) == get("ahoy_toy_bottom", 0)
}
