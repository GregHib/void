package content.minigame.barrows

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class StrangeOldMan : Script {
    init {
        npcOperate("Talk-to", "strange_old_man") {
            when(random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Knock knock.")
                    player<Quiz>("Who's there?")
                    npc<Laugh>("A big scary monster! HAHAHAHAHAHAHAHAHAHA!")
                    player<Confused>("Okay...")
                }
                1 -> npc<Happy>("Dig, dig, dig.")
                2 -> {
                    npc<Shifty>("Wanna hear a secret?")
                    player<Confused>("What?")
                    npc<Shock>("They're not normal!")
                    player<Confused>("Right...")
                }
                else -> {
                    npc<Scared>("AAAAAAAAARRRRRRGGGGGHHHHHHHH!")
                    player<Quiz>("What's wrong?")
                    npc<Scared>("AAAAAAAAARRRRRRGGGGGHHHHHHHH!")
                }
            }
            choice {
                option<Quiz>("Can you tell me more about the brothers?") {
                    npc<Happy>("The brothers? His faithful servants! Book tell you more! Read it! Read it!")
                }
                option<Confused>("I'll leave you to it then...")
            }
        }
    }
}