package world.gregs.voidps.world.map.lumbridge

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.interact.dialogue.Frustrated
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.Surprised
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "father_urhney") {
    npc<Frustrated>("Go away! I'm meditating!")
    choice {
        option<Neutral>("Well, that's friendly.") {
            npc<Frustrated>("I SAID go AWAY.")
            player<Neutral>("Okay, okay... sheesh, what a grouch.")
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

npcOperate("Pickpocket", "father_urhney") {
    player.message("<red>You don't want to dip into those pockets without good reason.")
    player.message("<red>They're holy ...and filthy.")
}