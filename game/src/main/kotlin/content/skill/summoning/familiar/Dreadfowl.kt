package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Dreadfowl : Script {
    init {
        npcOperate("Interact", "dreadfowl_familiar") {
            when (random.nextInt(3)) {
                0 -> {
                    npc<Neutral>("Attack! Fight! Annihilate!")
                    player<Happy>("It always worries me when you're so happy saying that.")
                }
                1 -> {
                    npc<Neutral>("Can it be fightin' time, please?")
                    player<Happy>("Look, I'll find something for you to fight, just give me a second.")
                }
                2 -> {
                    npc<Neutral>("I want to fight something.")
                    player<Happy>("I'll find something for you in a minute - just be patient.")
                }
            }
        }
    }
}
