package content.quest.member.eagles_peak

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

/**
 * Nickolaus at his camp near Eagles' Peak after the quest.
 * https://runescape.wiki/w/Transcript:Nickolaus
 */
class Nickolaus : Script {

    init {
        npcOperate("Talk-to", "nickolaus_4") {
            player<Happy>("Hello again Nickolaus.")
            npc<Happy>("Ah, hello again. Can I help at all?")
            choice {
                option<Quiz>("I was just wondering how you got on with those eagles.") {
                    npc<Happy>("Ah wonderfully. They're actually most cooperative creatures once they get used to you. With some rope I'm sure you could find some way to tame them... maybe even fly on them.")
                }
                option("No thanks. I was just dropping by.") {
                    player<Neutral>("No thanks. I was just dropping by, wanted to make sure you hadn't been abducted again.")
                    npc<Neutral>("No such luck I'm afraid, but I'm sure I'll make another discovery soon enough. I always seem to.")
                }
            }
        }
    }
}
