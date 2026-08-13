package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer

class Gravingas : Script {

    private val noAmulet = listOf(
        "Woooo wo woo wooooo!",
        "Wooo wooo Wooooooooo!",
        "Wo woooo wooooooo!",
        "Woo Wooooooooo wooo wooo wo wooo woo!",
        "Wooooo wo woooooo - woooooo wo wooo!",
        "Wooo wooooooo, Wooooo woooooo o woooo!",
        "Wooo wo wo wooooo wooooo, woo wo woooo wo wooooooooo!",
        "Woo'o wooo wooooo - woooooo wo woooooo!",
    )

    private val amulet = listOf(
        "Power to the Ghosts!!",
        "United we conquer - divided we fall!!",
        "Rise together, Ghosts without a cause!!",
        "Let Necrovarus know that we want out!!",
        "Down with Necrovarus!!",
        "We shall overcome!!",
        "Don't stay silent - victory in numbers!!",
        "Rise up my fellow ghosts, and we shall be victorious!!",
    )

    init {
        npcOperate("Talk-To", "ahoy_ghost_protestor") {
            if (!checkGhostspeak()) return@npcOperate
            val petition = get("ahoy_signaturecounter", 0)

            when {
                ghosts_ahoy == 7 -> {
                    player<Neutral>(
                        "Why are you still protesting? I have commanded Necrovarus to let you " +
                            "and your friends do as you like! There's no need for this anymore!",
                    )
                    npc<Neutral>("There's always a need for a healthy interest in politics.")
                }
                ghosts_ahoy < 4 -> {
                    npc<Neutral>(
                        "Will you join with me and protest against the evil ban of Necrovarus " +
                            "and his disciples?",
                    )
                    if (ghosts_ahoy < 1) {
                        player<Neutral>("I'm sorry, I don't really think I should get involved.")
                        npc<Neutral>("Ah, the youth of today - so apathetic to politics.")
                        return@npcOperate
                    }
                    askToJoin()
                }
                petition == 31 || ghosts_ahoy > 4 -> {
                    npc<Neutral>("So have you presented the petition to Necrovarus?")
                    player<Sad>("Yes. He burned it.")
                    npc<Neutral>("That's exactly what I thought he would do.")
                    player<Quiz>(
                        "Well, if you knew that he would do that, why have I been wasting my " +
                            "time running around after ghosts for signatures?",
                    )
                    npc<Neutral>("It never hurts to get involved in politics.")
                }
                petition > 0 -> petitionStatus(petition)
                else -> {
                    npc<Neutral>(
                        "Will you join with me and protest against the evil desires of " +
                            "Necrovarus and his disciples?",
                    )
                    askToJoin()
                }
            }
        }

        npcSpawn("protester_standardspeak_multi,protester_ghostspeak_multi") {
            softTimers.start("protester_standardspeak_multi,protester_ghostspeak_multi")
        }

        npcTimerStart("protester_standardspeak_multi,protester_ghostspeak_multi") {
            14
        }

        npcTimerTick("protester_standardspeak_multi,protester_ghostspeak_multi") {
            say(if (id == "protester_standardspeak_multi") amulet.random() else noAmulet.random())
            Timer.CONTINUE
        }
    }

    private suspend fun Player.askToJoin() {
        choice {
            option<Neutral>("After hearing Velorina's story I will be happy to help out.") {
                addOrDrop("petition_form")
                set("ahoy_signaturecounter", 1)
                npc<Neutral>(
                    "Excellent, excellent!! Here - take this petition form, and try and get " +
                        "10 signatures from the townsfolk.",
                )
            }
            option<Neutral>("I'm sorry, I don't really think I should get involved.")
        }
    }

    private suspend fun Player.petitionStatus(petition: Int) {
        if (!inventory.contains("petition_form")) {
            if (ownsItem("petition_form")) {
                player<Neutral>("I'm sorry, I seem to have lost my petition form.")
                npc<Neutral>("Are you sure? Have another look in the bank - you might be mistaken.")
            } else {
                player<Neutral>("I'm sorry, I seem to have lost my petition form.")
                if (inventory.isFull()) {
                    npc<Neutral>(
                        "Blown away in the sea breeze, hey? Oh well, can't be helped. I'd " +
                            "give you another one, but you look like you're carrying too much already.",
                    )
                } else {
                    set("ahoy_signaturecounter", 1)
                    addOrDrop("petition_form")
                    npc<Neutral>(
                        "Blown away in the sea breeze, hey? Oh well, can't be helped. Here's " +
                            "another one, but you'll have to start from scratch again.",
                    )
                }
            }
            return
        }
        when (petition) {
            11 -> npc<Neutral>("You've got them all! Now go and present it to Necrovarus!!")
            10 -> npc<Neutral>("Very nearly there!! Only 1 more signature to go.")
            1 -> npc<Neutral>("Come on - you haven't even started yet! You need 10 more signatures.")
            else -> npc<Neutral>("Not doing bad I see! You need ${11 - petition} more signatures.")
        }
    }
}
