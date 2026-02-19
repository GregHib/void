package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class AbyssalLurker : Script {
    init {
        npcOperate("Interact", "abyssal_lurker_familiar") {
            when (random.nextInt(4)) {
                0 -> {
                    npc<Sad>("Craaw...<br>(Djrej gf'jg sgshe...)")
                    player<Quiz>("What? Are we in danger, or something?")
                }
                1 -> {
                    npc<Quiz>("Craweeeeee?<br>(G-harrve shelmie?)")
                    player<Quiz>("What? Do you want something?")
                }
                2 -> {
                    npc<Neutral>("Craaw craw!<br>(Jehjfk l'ekfh skjd.)")
                    player<Quiz>("What? Is there somebody down an old well, or something?")
                }
                3 -> {
                    npc<Happy>("Craaw, screeeee!<br>(To poshi v'kaa!)")
                    player<Quiz>("What? Is that even a language?")
                }
            }
        }
    }
}
