package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "father_urhney") {
    npc<Angry>("Go away! I'm meditating!")
    choice {
        option<Talking>("Well, that's friendly.") {
            npc<Angry>("I SAID go AWAY.")
            player<Talking>("Okay, okay... sheesh, what a grouch.")
        }
        option<Talking>("I've come to repossess your house.") {
            npc<Surprised>("Under what grounds???")
            choice {
                option<Talking>("Repeated failure on mortgage repayments.") {
                    npc<Angry>("What?")
                    npc<Angry>(" But... I don't have a mortgage! I built this house myself!")
                    player<Talking>(" Sorry. I must have got the wrong address. All the houses look the same around here.")
                    npc<Angry>("What? What houses? What ARE you talking about???")
                    player<Talking>("Never mind.")
                }
                option<Sad>("I don't know. I just wanted this house...") {
                    npc<Angry>("Oh... go away and stop wasting my time!")
                }
            }
        }
    }
}

npcOperate("Pickpocket", "father_urhney") {
    player.message("<red>You don't want to dip into those pockets without good reason.")
    player.message("<red>They're holy ...and filthy.")
}