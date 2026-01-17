package content.quest.free.the_restless_ghost

import content.entity.player.bank.bank
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory

class FatherUrhney(val floorItems: FloorItems) : Script {

    init {
        npcOperate("Talk-to", "father_urhney") {
            npc<Frustrated>("Go away! I'm meditating!")
            choice {
                option<Idle>("Well, that's friendly.") {
                    npc<Frustrated>("I SAID go AWAY.")
                    player<Idle>("Okay, okay... sheesh, what a grouch.")
                }
                if (quest("the_restless_ghost") == "started") {
                    option<Happy>("Father Aereck sent me to talk to you.") {
                        npc<Angry>("I suppose I'd better talk to you then. What problems has he got himself into this time?")
                        choice {
                            option<Idle>("He's got a ghost haunting his graveyard.") {
                                ghost()
                            }
                            option<Quiz>("You mean he gets himself into lots of problems?") {
                                npc<Idle>("Yeah. For example, when we were trainee priests he kept on getting stuck up bell ropes.")
                                npc<Angry>("Anyway. I don't have time for chitchat. What's his problem THIS time?")
                                player<Idle>("He's got a ghost haunting his graveyard.")
                                ghost()
                            }
                        }
                    }
                }
                val stage = quest("the_restless_ghost")
                if (stage == "ghost" || stage == "mining_spot" || stage == "found_skull" || stage == "completed") {
                    option<Happy>("I've lost the Amulet of Ghostspeak.") {
                        statement("Father Urhney sighs.")
                        if (holdsItem("ghostspeak_amulet")) {
                            npc<Angry>("What are you talking about? I can see you've got it with you!")
                            return@option
                        }
                        if (bank.contains("ghostspeak_amulet")) {
                            npc<Angry>("You come here wasting my time... Has it even occurred to you that you've got it stored somewhere? Now GO AWAY!")
                            return@option
                        }
                        if (inventory.isFull()) {
                            npc<Angry>("How careless can you get? Those things aren't easy to come by you know! Now clear some space in your inventory and I'll give you another one.")
                        } else {
                            npc<Angry>("How careless can you get? Those things aren't easy to come by you know! It's a good job I've got a spare.")
                            inventory.add("ghostspeak_amulet")
                            item("ghostspeak_amulet", 200, "Father Urhney hands you an amulet.")
                            set("i_cant_hear_dead_people_task", true)
                            npc<Angry>("Be more careful this time.")
                            player<Idle>("Okay, I'll try to be.")
                        }
                    }
                }
                option<Idle>("I've come to repossess your house.") {
                    npc<Shock>("Under what grounds???")
                    choice {
                        option<Idle>("Repeated failure on mortgage repayments.") {
                            npc<Frustrated>("What?")
                            npc<Frustrated>("But... I don't have a mortgage! I built this house myself!")
                            player<Idle>("Sorry. I must have got the wrong address. All the houses look the same around here.")
                            npc<Frustrated>("What? What houses? What ARE you talking about???")
                            player<Idle>("Never mind.")
                        }
                        option<Disheartened>("I don't know. I just wanted this house...") {
                            npc<Frustrated>("Oh... go away and stop wasting my time!")
                        }
                    }
                }
            }
        }

        npcOperate("Pickpocket", "father_urhney") {
            message("<red>You don't want to dip into those pockets without good reason.")
            message("<red>They're holy ...and filthy.")
        }
    }

    suspend fun Player.ghost() {
        npc<Angry>("Oh, the silly fool.")
        npc<Angry>("I leave town for just five months, and ALREADY he can't manage.")
        npc<Disheartened>("(sigh)")
        npc<Angry>("Well, I can't go back and exorcise it. I vowed not to leave this place. Until I had done a full two years of prayer and meditation.")
        npc<Idle>("Tell you what I can do though; take this amulet.")
        set("the_restless_ghost", "ghost")
        if (inventory.isFull()) {
            floorItems.add(tile, "ghostspeak_amulet", disappearTicks = 300, owner = this)
        } else {
            inventory.add("ghostspeak_amulet")
        }
        item("ghostspeak_amulet", 200, "Father Urhney hands you an amulet.")
        npc<Idle>("It is an Amulet of Ghostspeak.")
        npc<Idle>("So called, because when you wear it you can speak to ghosts. A lot of ghosts are doomed to be ghosts because they have left some important task uncompleted.")
        npc<Idle>("Maybe if you know what this task is, you can get rid of the ghost. I'm not making any guarantees mind you, but it is the best I can do right now.")
        player<Idle>("Thank you. I'll give it a try!")
    }
}
