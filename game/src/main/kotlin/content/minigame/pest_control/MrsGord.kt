package content.minigame.pest_control

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class MrsGord : Script {
    init {
        // https://runescape.wiki/w/Transcript:Mrs_Gord
        npcOperate("Talk-to", "mrs_gord_pest_control") {
            if (questCompleted("quiet_before_the_swarm")) {
                npc<Sad>("I heard about the landing party. All those young people...and the Commodore...")
                player<Quiz>("I guess you're glad you stopped Terry from becoming a knight, huh?")
                npc<Sad>("Yes, but now he knows a pain that won't leave him for the rest of his life. Maybe I should reconsider, let him train...it might help take his mind off things.")
                return@npcOperate
            }
            npc<Neutral>("Hi, how can I help you?")
            choice("What do you want to say?") {
                option<Quiz>("Who are you?") {
                    npc<Sad>("I'm a widow of one of the Void Knights. My husband died protecting the world from the pest invasion.")
                }
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This is the Void Knight Outpost. The Void Knights keep us all safe from the pests that are trying to invade. You should talk to one of them if you want to know more.")
                }
                option<Neutral>("I'm fine, thanks.")
            }
        }
    }
}
