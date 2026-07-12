package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class VoidSpinner : Script {
    init {
        npcOperate("Interact", "void_spinner_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Let's go play hide an' seek!")
                    player<Happy>("Okay, you hide and I'll come find you.")
                    npc<Neutral>("You'll never find me!")
                    player<Happy>("What a disaster that would be...")
                }
                1 -> {
                    npc<Neutral>("My mummy told me I was clever.")
                    player<Happy>("Aren't you meant to be the essence of a spinner? How do you have a mother?")
                    npc<Neutral>("What you mean, 'essence'?")
                    player<Happy>("Never mind, I don't think it matters.")
                    npc<Neutral>("My logimical powers has proved me smarterer than you!")
                }
                2 -> {
                    npc<Neutral>("I'm coming to tickle you!")
                    player<Happy>("No! You've got so many tentacles!")
                    npc<Neutral>("I'm coming to tickle you!")
                    player<Happy>("Aieee!")
                }
                3 -> {
                    npc<Neutral>("Where's the sweeties?")
                    player<Happy>("They are wherever good spinners go.")
                    npc<Neutral>("Yay for me!")
                }
            }
        }
    }
}
