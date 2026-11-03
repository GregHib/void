package content.area.kharidian_desert.nardah

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

/**
 * Ali the Wise hears about the Muspah after Tale of the Muspah.
 * https://runescape.wiki/w/Transcript:Wahisietel
 */
class AliTheWise : Script {

    init {
        npcOperate("Talk-to", "ali_the_wise_2") {
            jhallan()
        }
    }

    private suspend fun Player.jhallan() {
        val senntisten = questCompleted("temple_at_senntisten")
        if (!senntisten) {
            player<Quiz>("So this is your place?")
            npc<Happy>("Humble but homely, I think. It suits me well. How good to see you again!")
        }
        player<Neutral>("You told me how to thaw out the Muspah. I thought I should keep my part of the bargain.")
        npc<Happy>("Oh! So you have some information for me? I must record this!")
        player<Neutral>("To start with, the Muspah wasn't what it appeared to be.")
        player<Neutral>("It turned out to be alive all along, and once I'd freed it, it... changed.")
        player<Neutral>("It told me it was a Mahjarrat. It was quite a shock to suddenly see its head was a skull.")
        npc<Shock>("The Muspah creature was a Mahjarrat in disguise?")
        player<Neutral>("Yes, he's called Jhallan. He was incredibly weak and wanted to hibernate under the Mahjarrat ritual site.")
        player<Neutral>("I found him somewhere peaceful and put him back to sleep. Apparently the ritual will rejuvenate any Mahjarrat nearby.")
        if (senntisten) {
            npc<Neutral>("Well, we know where to find him again, should we ever need to.")
            return
        }
        npc<Neutral>("We know where to find him again, should we ever need to.")
        npc<Neutral>("Well, that creature in the ice has given me much to think about.")
        npc<Neutral>("I've started to research these Mahjarrat; I'm worried they may be a threat to us humans. We need to collaborate to gather as much information as possible.")
    }
}
