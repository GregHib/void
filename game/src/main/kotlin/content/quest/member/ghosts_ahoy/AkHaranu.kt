package content.quest.member.ghosts_ahoy

import content.entity.npc.shop.openShop
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class AkHaranu : Script {
    init {
        npcOperate("Talk-To", "ahoy_akharanu") {
            val stage = get("ahoy_subquest_bow", 0)
            when {
                stage == 8 -> postExchange()
                stage >= 1 -> exchangePhase()
                else -> introPhase()
            }
        }

        npcOperate("Trade", "ahoy_akharanu") {
            openShop("ak_haranus_exotic_shop")
        }
    }

    private suspend fun Player.introPhase() {
        player<Neutral>("It's nice to see a human face around here.")
        npc<Neutral>("My name Ak-Haranu. I am trader, come from many far across sea in east.")
        player<Neutral>(
            "You come from the lands of the East? Do you have anything that can help me " +
                "translate a book that is scribed in your language?",
        )
        npc<Neutral>(
            "Ak-Haranu may help you. A translation manual I have, much good for reading " +
                "Eastern language.",
        )
        player<Neutral>("How much do you want for it?")
        npc<Neutral>(
            "Ak-Haranu not want money for this book, as is such small thing. But there may " +
                "be something you could do for me. I am big admirer of Robin, Master Bowman. " +
                "He staying in village inn.",
        )
        player<Neutral>("What would you like me to do?")
        npc<Neutral>(
            "Please get Master Bowman sign an oak longbow for me, so Ak-Haranu can show " +
                "family and friends when returning home and become much admired. Then I give " +
                "you book in exchange.",
        )
        choice {
            option("Okay, wait here - I'll get you your bow.") {
                player<Neutral>("Okay, wait here - I'll get you your bow.")
                set("ahoy_subquest_bow", 1)
            }
            option<Neutral>("Sorry, I have too much to do at the moment.")
        }
    }

    private suspend fun Player.exchangePhase() {
        when {
            inventory.contains("signed_oak_bow") -> {
                player<Neutral>("I have your signed longbow for you.")
                npc<Neutral>("Ah, can it be true? You have obtained bow from Master Bowman?")
                player<Neutral>("He was more than happy to oblige (cough). Here you are.")
                inventory.remove("signed_oak_bow")
                inventory.add("translation_manual")
                set("ahoy_subquest_bow", 8)
                item(
                    item = "signed_oak_bow",
                    text = "Ak-Haranu gives you a translation manual in return for the signed oak longbow.",
                )
                npc<Neutral>("May honour be bestowed upon you and your family!")
            }
            inventory.contains("oak_longbow") -> {
                npc<Neutral>("How is ${if (male) "Master" else "Mistress"} $name getting on with longbow?")
                player<Neutral>("I'll let you know.")
            }
            else -> {
                player<Neutral>("Have you got an oak longbow that I can get Robin to sign for you?")
                npc<Neutral>(
                    "No, Ak-Haranu afraid that no longbow in supply at moment. You must make " +
                        "one or buy one.",
                )
            }
        }
    }

    private suspend fun Player.postExchange() {
        if (ownsItem("translation_manual") || get("ahoy_given_manual", false)) {
            player<Neutral>(
                "Thank you for the translation manual, Ak-Haranu - it may save many souls before long.",
            )
            npc<Neutral>("And Ak-Haranu thanks you for kind gift of longbow.")
        } else {
            player<Neutral>("I'm sorry, I seem to have lost the translation manual you gave me.")
            npc<Neutral>(
                "Ah, no worry, my friend. For the gift of longbow Ak-Haranu would give a thousand books.",
            )
            inventory.add("translation_manual")
            item(item = "translation_manual", text = "Ak-Haranu gives you another translation manual.")
        }
    }
}
