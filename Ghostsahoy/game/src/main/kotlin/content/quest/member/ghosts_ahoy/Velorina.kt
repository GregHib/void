package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.inventory

class Velorina : Script {
    init {
        npcOperate("Talk-To", "ahoy_velorina") {
            if (!checkGhostspeak()) return@npcOperate
            when (ghosts_ahoy) {
                0 -> intro()
                1 -> notSpoken()
                2 -> {
                    player<Sad>("I'm sorry, but Necrovarus will not let you go.")
                    necrovarusRefused()
                }
                3 -> oldCroneOptions()
                4 -> itemRequestOptions()
                5 -> {
                    npc<Neutral>("How is it going?")
                    player<Neutral>("I found your old friend. She still lives, and has agreed to perform an enchantment for me that will enable me to command Necrovarus to set you free.")
                    npc<Neutral>("Oh, kind ${if (male) "sir" else "lady"} - you are the answer to all our prayers!")
                }
                6 -> {
                    npc<Neutral>("How is it going?")
                    if (ownsItem("ghostspeak_amulet_enchanted")) {
                        player<Neutral>("I have had the Amulet of Ghostspeak enchanted, which I shall use to command Necrovarus to set you free.")
                    } else {
                        player<Neutral>(
                            "I have had the Amulet of Ghostspeak enchanted, but I used it foolishly. I shall return " +
                                "to the old crone to have it enchanted again - then I shall command Necrovarus to set you free.",
                        )
                    }
                    npc<Neutral>("Oh, kind ${if (male) "sir" else "lady"} - you are the answer to all our prayers!")
                }
                7 -> rewardScene()
                8 -> postQuestOptions()
            }
        }
    }

    private suspend fun Player.intro() {
        npc<Neutral>(
            "Take pity on me, please - eternity stretches out before me and I am helpless in " +
                "its grasp.",
        )
        choice {
            option<Quiz>("Why, what is the matter?") {
                wailExplanation()
                explainHistory()
            }
            option("Sorry, I'm scared of ghosts.") {
                player<Scared>("Leave me alone - I'm scared of ghosts!!!")
            }
        }
    }

    private suspend fun Player.wailExplanation() {
        npc<Neutral>("Oh, I'm sorry - I was just wailing out loud. I didn't mean to scare you.")
        player<Neutral>(
            "No, that's okay - it takes more than a ghost to scare me. What is wrong?",
        )
    }

    private suspend fun Player.explainHistory() {
        npc<Neutral>("Do you know the history of our town?")
        choice {
            option<Neutral>("Yes, I do. It is a very sad story.") {
                hookOffer()
            }
            option<Neutral>("No - could you tell me?") {
                fullHistory()
                hookOffer()
            }
        }
    }

    private suspend fun Player.fullHistory() {
        npc<Neutral>("Do you know why ghosts exist?")
        player<Neutral>(
            "A ghost is a soul left in limbo, unable to pass over to the next world; they " +
                "might have something left to do in this world that torments them, or they " +
                "might just have died in a state of confusion.",
        )
        npc<Neutral>(
            "Yes, that is normally the case. But here in Port Phasmatys, we of this town once " +
                "chose freely to become ghosts!",
        )
        player<Shock>("Why on earth would you do such a thing?")
        npc<Neutral>(
            "It is a long story. Many years ago, this was a thriving port, a trading centre " +
                "to the eastern lands of Gielinor. We became rich on the profits made from " +
                "the traders that came across the eastern seas.",
        )
        npc<Neutral>("We were very happy... until Lord Drakan noticed us.")
        npc<Neutral>(
            "He sent unholy creatures to demand that a blood-tithe be paid to the Lord " +
                "Vampyre, as is required from all in the domain of Morytania. We had no " +
                "choice but to agree to his demands.",
        )
        npc<Neutral>(
            "As the years went by, our numbers dwindled and many spoke of abandoning the " +
                "town for safer lands. Then, Necrovarus came to us.",
        )
        npc<Neutral>(
            "He came from the eastern lands, but of more than that, little is known. Some say " +
                "he was once a mage, some say a priest. Either way, he was in possession of " +
                "knowledge totally unknown to even the most learned among us.",
        )
        npc<Neutral>(
            "Necrovarus told us that he had been brought by a vision he'd had of an " +
                "underground source of power. He inspired us to delve beneath the town, " +
                "promising us the power to repel the vampyres.",
        )
        npc<Neutral>(
            "Deep underneath Phasmatys, we found a pool of green slime that Necrovarus called " +
                "ectoplasm. He showed us how to build the Ectofuntus, which would turn the " +
                "ectoplasm into the power he had promised us.",
        )
        npc<Neutral>(
            "Indeed, this Ectopower did repel the vampyres; they would not enter Phasmatys " +
                "once the Ectofuntus began working. But little did we know that we had " +
                "exchanged one evil for yet another - Ectopower.",
        )
        npc<Neutral>(
            "Little by little, we began to lose any desire for food or water, and our desire " +
                "for the Ectopower grew until it dominated our thoughts entirely. Our bodies " +
                "shrivelled and, one by one, we died.",
        )
        npc<Neutral>(
            "The Ectofuntus and the power it emanates keeps us here as ghosts - some, like " +
                "myself, content to remain in this world; some becoming tortured souls who " +
                "we do not allow to pass our gates.",
        )
        npc<Neutral>(
            "We would be able to pass over into the next world but Necrovarus has used his " +
                "power to create a psychic barrier, preventing us.",
        )
        npc<Neutral>("We must remain here for all eternity, even unto the very end of the world.")
        player<Sad>("That's a very sad story.")
    }

    private suspend fun Player.hookOffer() {
        npc<Neutral>("Would you help us obtain our release into the next world?")
        choice {
            option("Yes.") {
                ghosts_ahoy = 1
                player<Neutral>("Yes, of course I will. Tell me what you want me to do.")
                npc<Neutral>("Oh, thank you!")
                explainTask()
            }
            option("No.") {
                player<Neutral>("I'm sorry, but it isn't really my problem.")
                npc<Neutral>(
                    "No, you're right - it's our own fault. If you do change your mind " +
                        "though, please come back and help us. We will be here ... forever.",
                )
            }
        }
    }

    private suspend fun Player.explainTask() {
        npc<Neutral>(
            "Necrovarus will not listen to those of us who are already dead. He might rethink " +
                "his position if someone with a mortal soul pleaded our cause.",
        )
        npc<Neutral>("If he declines, there may yet be another way.")
    }

    private suspend fun Player.notSpoken() {
        npc<Neutral>("I sense that you have not yet spoken to Necrovarus.")
        player<Neutral>("No, I was just getting to that.")
        npc<Neutral>("Well, I suppose we do have all the time in the world...")
    }

    private suspend fun Player.necrovarusRefused() {
        npc<Neutral>("I feared as much. His spirit is a thing of fire and wrath.")
        player<Quiz>("You spoke of another way.")
        npc<Neutral>(
            "It is only a small chance. During the building of the Ectofuntus one of " +
                "Necrovarus's disciples spoke out against him. It is such a long time ago I " +
                "cannot remember her name, although I knew her as a friend.",
        )
        npc<Neutral>(
            "She fled before the Ectofuntus took control over us, but being a disciple of " +
                "Necrovarus she would have been privy to many of his darkest secrets. She " +
                "may know of a way to aid us without Necrovarus.",
        )
        ghosts_ahoy = 3
        oldCroneOptions()
    }

    private suspend fun Player.oldCroneOptions() {
        choice {
            option<Quiz>("Do you know where this woman can be found?") {
                npc<Neutral>(
                    "I have a vision of a small wooden shack, the land it was built on the " +
                        "unholy soil of Morytania. I sense the sea is very close, and that " +
                        "there looms castles to the west and the east.",
                )
            }
            option<Quiz>("If it was such a long time ago, won't she be dead already?") {
                npc<Neutral>(
                    "She was a friend of mine. Had she died, I would have felt her spirit " +
                        "pass over to the next world, though I may not follow.",
                )
            }
        }
    }

    private suspend fun Player.itemRequestOptions() {
        choice {
            option<Quiz>("Do you know where I can find the Book of Haricanto?") {
                npc<Neutral>(
                    "Nobody knows what has happened to the Book. It was stolen when our port " +
                        "was raided by pirates many years ago, and never seen since.",
                )
            }
            option<Quiz>("Do you know where I can find the Robes of Necrovarus?") {
                npc<Neutral>(
                    "I imagine they are still worn by his mortal body, which now lies in a " +
                        "coffin inside the Temple.",
                )
            }
            option<Quiz>("I need something to translate the Book of Haricanto.") {
                npc<Neutral>(
                    "I don't really know. You could try asking some of the traders from the " +
                        "East - they might be able to help you.",
                )
            }
        }
    }

    private suspend fun Player.rewardScene() {
        npc<Neutral>(
            "You don't need to tell me $name- I sensed the removal of Necrovarus's psychic barrier!",
        )
        player<Neutral>("Only happy to help out.")
        npc<Neutral>("Here, take this as a thank you for the service that you have given us.")
        item(item = "ectophial", text = "Velorina gives you a vial of bright green ectoplasm.")
        npc<Neutral>(
            "This is an Ectophial. If you ever want to come back to Port Phasmatys, empty " +
                "this on the floor beneath your feet, and you will be instantly teleported " +
                "to the temple - the source of its power.",
        )
        npc<Neutral>("Remember that once the Ectophial has been used you need to refill it from the Ectofuntus.")
        sendGhostsAhoyReward()
    }

    private suspend fun Player.postQuestOptions() {
        choice {
            option<Neutral>("I thought you were going to pass over to the next world.") {
                npc<Neutral>(
                    "All in good time, $name. We stand forever in your debt, and will " +
                        "certainly put in a good word for you when we pass over.",
                )
            }
            option<Neutral>("Can I have another Ectophial?") {
                when {
                    inventory.isFull() -> npc<Sad>("No, you've got nowhere to hold it.")
                    ownsItem("ectophial") || ownsItem("ectophial_empty") ->
                        npc<Neutral>("Really? You've already got an Ectophial.")
                    else -> {
                        npc<Neutral>("Of course you can, you have helped us more than we could ever have hoped.")
                        addOrDrop("ectophial")
                        item(item = "ectophial", text = "Velorina gives you a vial of bright green ectoplasm.")
                    }
                }
            }
        }
    }
}
