package content.area.misthalin.lumbridge

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.suspend.SuspendableContext
import content.quest.quest
import content.quest.refreshQuestJournal
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.startQuest

npcOperate("Talk-to", "father_aereck") {
    when (player.quest("the_restless_ghost")) {
        "unstarted" -> {
            npc<Happy>("Welcome to the church of holy Saradomin.")
            choice {
                option<Quiz>("Who's Saradomin?") {
                    npc<Surprised>("Surely you have heard of the god, Saradomin?")
                    npc<Neutral>("He who creates the forces of goodness and purity in this world? I cannot believe your ignorance!")
                    npc<Neutral>("This is the god with more followers than any other! ...At least in this part of the world.")
                    npc<Neutral>("He who created this world along with his brothers Guthix and Zamorak?")
                    choice {
                        option<Neutral>("Oh, THAT Saradomin...") {
                            npc<Uncertain>("There... is only one Saradomin...")
                            player<Neutral>("Yeah... I, uh, thought you said something else.")
                        }
                        option<Neutral>("Oh, sorry. I'm not from this world.") {
                            npc<Surprised>("...")
                            npc<Neutral>("That's... strange.")
                            npc<Neutral>("I thought things not from this world were all... You know. Slime and tentacles.")
                            choice {
                                option<Neutral>("You don't understand. This is an online game!") {
                                    npc<Uncertain>("I... beg your pardon?")
                                    player<Neutral>("Never mind.")
                                }
                                option<Happy>("I am - do you like my disguise?") {
                                    npc<Surprised>("Aargh! Avaunt foul creature from another dimension! Avaunt! Begone in the name of Saradomin!")
                                    player<Happy>("Ok, ok, I was only joking...")
                                }
                            }
                        }
                    }
                }
                option<Happy>("Nice place you've got here.") {
                    npc<Happy>("It is, isn't it? It was built over 230 years ago.")
                }
                option<Happy>("I'm looking for a quest.") {
                    npc<Happy>("That's lucky, I need someone to do a quest for me.")
                    if (startQuest("the_restless_ghost")) {
                        player["the_restless_ghost"] = "started"
                        player.refreshQuestJournal()
                        player<Happy>("Okay, let me help then.")
                        npc<Happy>("Thank you. The problem is, there is a ghost in the church graveyard. I would like you to get rid of it.")
                        npc<Happy>("If you need any help, my friend Father Urhney is an expert on ghosts.")
                        npc<Happy>("I believe he is currently living as a hermit in Lumbridge swamp. He has a little shack in the far west of the swamps.")
                        npc<Neutral>("Exit the graveyard through the south gate to reach the swamp. I'm sure if you told him that I sent you he'd be willing to help.")
                        npc<Happy>("My name is Father Aereck by the way. Pleased to meet you.")
                        player<Happy>("Likewise.")
                        npc<Neutral>("Take care travelling through the swamps, I have heard they can be quite dangerous.")
                        player<Happy>("I will, thanks.")
                    } else {
                        player<Neutral>("Actually, I don't have time right now.")
                        npc<Sad>("Oh well. If you do have some spare time on your hands, come back and talk to me.")
                    }
                }
            }
        }
        "started" -> started()
        "ghost" -> ghost()
        "mining_spot" -> miningSpot()
        "found_skull" -> foundSkull()
        else -> completed()
    }
}

suspend fun SuspendableContext<Player>.started() {
    npc<Neutral>("Have you got rid of the ghost yet?")
    player<Sad>("I can't find Father Urhney at the moment.")
    npc<Happy>("Well, you can get to the swamp he lives in by going south through the cemetery.")
    npc<Happy>("You'll have to go right into the far western depths of the swamp, near the coastline. That is where his house is.")
}

suspend fun SuspendableContext<Player>.ghost() {
    npc<Neutral>("Have you got rid of the ghost yet?")
    player<Neutral>("I had a talk with Father Urhney. He has given me this funny amulet to talk to the ghost with.")
    npc<Uncertain>("I always wondered what that amulet was... Well, I hope it's useful. Tell me when you get rid of the ghost!")
}

suspend fun SuspendableContext<Player>.miningSpot() {
    npc<Neutral>("Have you got rid of the ghost yet?")
    player<Neutral>("I've found out that the ghost's corpse has lost its skull. If I can find the skull, the ghost should leave.")
    npc<Neutral>("That WOULD explain it.")
    npc<Neutral>("Hmmmmm. Well, I haven't seen any skulls.")
    player<Uncertain>("Yes, I think a warlock has stolen it.")
    npc<Angry>("I hate warlocks.")
    npc<Happy>("Ah well, good luck!")
}

suspend fun SuspendableContext<Player>.foundSkull() {
    if (player.holdsItem("ghostspeak_amulet")) {
        npc<Neutral>("Have you got rid of the ghost yet?")
        player<Happy>("I've finally found the ghost's skull!")
        npc<Happy>("Great! Put it in the ghost's coffin and see what happens!")
    } else {
        npc<Neutral>("Have you got rid of the ghost yet?")
        player<Sad>("Well, I found the ghost's skull but then lost it.")
        npc<Neutral>("Don't worry, I'm sure you'll find it again.")
    }
}

suspend fun SuspendableContext<Player>.completed() {
    npc<Happy>("Thank you for getting rid of that awful ghost for me! May Saradomin always smile upon you!")
    player<Happy>("I'm looking for a new quest.")
    npc<Happy>("Sorry, I only had the one quest.")
}