package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SpiritMosquito : Script {
    init {
        npcOperate("Interact", "spirit_mosquito_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("You have lovely ankles.")
                    player<Happy>("Am I meant to be pleased by that?")
                    npc<Neutral>("Thin skin. Your delicious blood is easier to get too.")
                    player<Happy>("I knew I couldn't trust you.")
                    npc<Neutral>("Oh come on, you won't feel a thing...")
                }
                1 -> {
                    npc<Neutral>("How about that local sports team?")
                    player<Happy>("Which one? The gnomeball team?")
                    npc<Neutral>("I must confess: I have no idea.")
                    player<Happy>("Why did you ask, then?")
                    npc<Neutral>("I was just trying to be friendly.")
                    player<Happy>("Just trying to get to my veins, more like!")
                }
                2 -> {
                    npc<Neutral>("Have you ever tasted pirate blood?")
                    player<Happy>("Why would I drink pirate blood?")
                    npc<Neutral>("How about dwarf blood?")
                    player<Happy>("I don't think you quite understand...")
                    npc<Neutral>("Gnome blood, then?")
                }
                3 -> {
                    npc<Neutral>("I'm soooo hungry!")
                    player<Happy>("What would you like to eat?")
                    npc<Neutral>("Well, if you're not too attached to your elbow...")
                    player<Happy>("You can't eat my elbow! You don't have teeth!")
                    npc<Neutral>("Tell me about it. Cousin Nigel always makes fun of me. Calls me 'No-teeth'.")
                }
            }
        }
    }
}
