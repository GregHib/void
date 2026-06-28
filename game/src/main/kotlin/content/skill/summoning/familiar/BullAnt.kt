package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.effect.energy.energyPercent
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class BullAnt : Script {
    init {
        npcOperate("Interact", "bull_ant_familiar") {
            if (energyPercent() < 12) {
                npc<Neutral>("What's the matter, Private? Not enjoying the run?")
                player<Happy>("Sir...wheeze...yes Sir!")
                npc<Neutral>("Not enjoying the run? You need more training biped?")
                player<Happy>("Sir, no Sir! Sir, I'm enjoying the run a great deal, Sir!")
                npc<Neutral>("Then hop to, Private!")
                player<Happy>("Sir, yes Sir!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("All right you worthless biped, fall in!")
                    player<Happy>("Sir, yes Sir!")
                    npc<Neutral>("We're going to work you so hard your boots fall off, understood?")
                    player<Happy>("Sir, yes Sir!")
                    npc<Neutral>("Carry on Private!")
                }
                1 -> {
                    npc<Neutral>("Aten...hut!")
                    player<Happy>("Sir, Private Player reporting for immediate active duty, Sir!")
                    npc<Neutral>("As you were, Private!")
                }
                2 -> {
                    npc<Neutral>("I can't believe they stuck me with you...")
                    player<Happy>("Buck up, Sir, it's not that bad.")
                    npc<Neutral>("Stow that, Private, and get back to work!")
                    player<Happy>("Sir, yes Sir!")
                }
                3 -> {
                    npc<Neutral>("What in the name of all the layers of the abyss do you think you're doing, biped?")
                    player<Happy>("Sir, nothing Sir!")
                    npc<Neutral>("Well double-time it, Private, whatever it is!")
                    player<Happy>("Sir, yes Sir!")
                }
            }
        }
    }
}
