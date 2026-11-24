package content.minigame.pest_control

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script

class TerryGord : Script {
    init {
        // https://runescape.wiki/w/Transcript:Terry_Gord
        npcOperate("Talk-to", "terry_gord_pest_control") {
            if (questCompleted("quiet_before_the_swarm")) {
                npc<Sad>("I heard about the landing party. All my friends...and the Commodore...")
                player<Quiz>("Does it make you think twice about wanting to become a Void Knight?")
                npc<Sad>("Yes and no. I want to become one to honour their memory and keep the fight going, but to lose so many...")
                return@npcOperate
            }
            npc<Neutral>("Hi, how can I help you?")
            choice("What do you want to say?") {
                option<Quiz>("Who are you?") {
                    npc<Neutral>("I'm Terry.")
                    player<Quiz>("Are you a Void Knight?")
                    npc<Sad>("No. I want to be. My father was, but when he died fighting the pests, my mother forbade me from joining up.")
                }
                option<Quiz>("What is this place?") {
                    npc<Neutral>("This is the Void Knight Outpost. The Void Knights keep us all safe from the pests that are trying to invade. You should talk to one of them if you want to know more.")
                }
                option<Neutral>("I'm fine, thanks.")
            }
        }
    }
}
