package content.area.kharidian_desert.kalphite_lair

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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random

class WeirdOldMan : Script {
    init {
        npcOperate("Talk-to", "weird_old_man") {
            when (random.nextInt(3)) {
                0 -> {
                    npc<Shifty>("Pst, wanna hear a secret?")
                    choice {
                        option<Happy>("Sure!") {
                            npc<Shock>("They've got six legs... SIX!")
                        }
                        option<Neutral>("No thanks.")
                    }
                }
                1 -> aaa()
                2 -> {
                    npc<Laugh>("She likes to be tickled hehehehe.")
                    player<Confused>("Okay...")
                }
            }
        }
    }

    private suspend fun Player.aaa() {
        npc<Scared>("AAAAAAAAARRRRRRGGGGGHHHHHHHH!")
        choice {
            option<Quiz>("What's wrong?") {
                aaa()
            }
            option<Confused>("I'll leave you to it then...")
        }
    }
}
