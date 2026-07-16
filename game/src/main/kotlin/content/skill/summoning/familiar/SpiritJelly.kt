package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class SpiritJelly : Script {
    init {
        npcOperate("Interact", "spirit_jelly_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Play play play play!")
                    player<Happy>("The only game I have time to play is the 'Staying Very Still' game.")
                    npc<Neutral>("But that game is soooooo booooooring...")
                    player<Happy>("How about we use the extra house rule, that makes it the 'Staying Very Still and Very Quiet' game.")
                    npc<Neutral>("Happy happy! I love new games!")
                }
                1 -> {
                    npc<Neutral>("It's playtime now!")
                    player<Happy>("Okay, how about we play the 'Staying Very Still' game.")
                    npc<Neutral>("But that game is booooooring...")
                    player<Happy>("If you win then you can pick the next game, how about that?")
                    npc<Neutral>("Happy happy!")
                }
                2 -> {
                    npc<Neutral>("Can we go over there now, please please please pleeeeeease?")
                    player<Happy>("Go over where?")
                    npc<Neutral>("I dunno, someplace fun, please please please!")
                    player<Happy>("Okay, but first, let's play the 'Sitting Very Still' game.")
                    npc<Neutral>("But that game is booooooring...")
                    player<Happy>("Well, if you win we can go somewhere else, okay?")
                    npc<Neutral>("Happy happy!")
                }
                3 -> {
                    npc<Neutral>("What game are we playing now?")
                    player<Happy>("It's called the 'Staying Very Still' game.")
                    npc<Neutral>("This game is booooooring...")
                    player<Happy>("Hey, all that moping doesn't look very still to me.")
                    npc<Neutral>("I never win at this game...")
                    player<Happy>("You know what? I think I'll not count it this one time")
                    npc<Neutral>("Happy happy! You're the best friend ever!")
                }
            }
        }
    }
}
