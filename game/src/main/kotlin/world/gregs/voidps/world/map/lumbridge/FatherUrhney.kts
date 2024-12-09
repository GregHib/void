package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

npcOperate("Talk-to", "father_urhney") {
    npc<Frustrated>("Go away! I'm meditating!")
    choice {
        option<Neutral>("Well, that's friendly.") {
            npc<Frustrated>("I SAID go AWAY.")
            player<Neutral>("Okay, okay... sheesh, what a grouch.")
        }
        option<Happy>("Father Aereck sent me to talk to you.", filter = { player.quest("the_restless_ghost") == "started" }) {
            npc<Angry>("I suppose I'd better talk to you then. What problems has he got himself into this time?")
            choice {
                option<Neutral>("He's got a ghost haunting his graveyard.") {
                    ghost()
                }
                option<Quiz>("You mean he gets himself into lots of problems?") {
                    npc<Neutral>("Yeah. For example, when we were trainee priests he kept on getting stuck up bell ropes.")
                    npc<Angry>("Anyway. I don't have time for chitchat. What's his problem THIS time?")
                    player<Neutral>("He's got a ghost haunting his graveyard.")
                    ghost()
                }
            }
        }
        option<Happy>("I've lost the Amulet of Ghostspeak.", filter = {
            val stage = player.quest("the_restless_ghost")
            stage == "ghost" || stage == "mining_spot" || stage == "found_skull" || stage == "completed"
        }) {
            statement("Father Urhney sighs.")
            if (player.holdsItem("ghostspeak_amulet")) {
                npc<Angry>("What are you talking about? I can see you've got it with you!")
                return@option
            }
            if (player.bank.contains("ghostspeak_amulet")) {
                npc<Angry>("You come here wasting my time... Has it even occurred to you that you've got it stored somewhere? Now GO AWAY!")
                return@option
            }
            if (player.inventory.isFull()) {
                npc<Angry>("How careless can you get? Those things aren't easy to come by you know! Now clear some space in your inventory and I'll give you another one.")
            } else {
                npc<Angry>("How careless can you get? Those things aren't easy to come by you know! It's a good job I've got a spare.")
                player.inventory.add("ghostspeak_amulet")
                item("ghostspeak_amulet", 200, "Father Urhney hands you an amulet.")
                player["i_cant_hear_dead_people_task"] = true
                npc<Angry>("Be more careful this time.")
                player<Neutral>("Okay, I'll try to be.")
            }
        }
        option<Neutral>("I've come to repossess your house.") {
            npc<Surprised>("Under what grounds???")
            choice {
                option<Neutral>("Repeated failure on mortgage repayments.") {
                    npc<Frustrated>("What?")
                    npc<Frustrated>("But... I don't have a mortgage! I built this house myself!")
                    player<Neutral>("Sorry. I must have got the wrong address. All the houses look the same around here.")
                    npc<Frustrated>("What? What houses? What ARE you talking about???")
                    player<Neutral>("Never mind.")
                }
                option<Sad>("I don't know. I just wanted this house...") {
                    npc<Frustrated>("Oh... go away and stop wasting my time!")
                }
            }
        }
    }
}


val floorItems: FloorItems by inject()

suspend fun CharacterContext.ghost() {
    npc<Angry>("Oh, the silly fool.")
    npc<Angry>("I leave town for just five months, and ALREADY he can't manage.")
    npc<Sad>("(sigh)")
    npc<Angry>("Well, I can't go back and exorcise it. I vowed not to leave this place. Until I had done a full two years of prayer and meditation.")
    npc<Neutral>("Tell you what I can do though; take this amulet.")
    player["the_restless_ghost"] = "ghost"
    if (player.inventory.isFull()) {
        floorItems.add(player.tile, "ghostspeak_amulet", disappearTicks = 300, owner = player)
    } else {
        player.inventory.add("ghostspeak_amulet")
    }
    item("ghostspeak_amulet", 200, "Father Urhney hands you an amulet.")
    npc<Neutral>("It is an Amulet of Ghostspeak.")
    npc<Neutral>("So called, because when you wear it you can speak to ghosts. A lot of ghosts are doomed to be ghosts because they have left some important task uncompleted.")
    npc<Neutral>("Maybe if you know what this task is, you can get rid of the ghost. I'm not making any guarantees mind you, but it is the best I can do right now.")
    player<Neutral>("Thank you. I'll give it a try!")
}

npcOperate("Pickpocket", "father_urhney") {
    player.message("<red>You don't want to dip into those pockets without good reason.")
    player.message("<red>They're holy ...and filthy.")
}