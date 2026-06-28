package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class VoidTorcher : Script {
    init {
        npcOperate("Interact", "void_torcher_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    player<Happy>("You okay there, spinner?")
                    npc<Neutral>("I not spinner!")
                    player<Happy>("Sorry, splatter?")
                    npc<Neutral>("I not splatter either!")
                    player<Happy>("No, wait, I meant defiler.")
                    npc<Neutral>("I torcher!")
                    player<Happy>("Hehe, I know. I was just messing with you.")
                    npc<Neutral>("Grr. Don't be such a pest.")
                }
                1 -> {
                    npc<Neutral>("'T' is for torcher, that's good enough for me... 'T' is for torcher, I'm happy you can see.")
                    player<Happy>("You're just a bit weird, aren't you?")
                }
                2 -> {
                    npc<Neutral>("Burn, baby, burn! Torcher inferno!")
                    player<Happy>("*Wibble*")
                }
                3 -> {
                    npc<Neutral>("So hungry... must devour...")
                    player<Happy>("*Gulp* Er, yeah, I'll find you something to eat in a minute.")
                    npc<Neutral>("Is flesh-bag scared of torcher?")
                    player<Happy>("No, no. I, er, always look like this... honest.")
                }
            }
        }
    }
}
