package content.quest.member.gertrudes_cat

import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script

// Source: https://runescape.wiki/w/Transcript:Gertrude%27s_Cat/Journal
// https://www.youtube.com/watch?v=_pZcnvI1l_Q from 2011 using a mix of RS3 and OSRS transcript and going as best as I can off of memory and videos.
const val GERTRUDES_CAT_STRING_NAME = "gertrudes_cat"
class GertrudesCat : Script {
    init {
        questJournalOpen(GERTRUDES_CAT_STRING_NAME) {
            val progress = questStage(GERTRUDES_CAT_STRING_NAME)
            val lines = mutableListOf<String>()

            if(progress == 0){
                lines += "<navy>I can start this quest by speaking to <maroon>Gertrude<navy>."
                lines += "<navy>She can be found in a house south of the road leading west"
                lines += "<navy>out of <maroon>Varrock<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            questJournal("Gertrude's Cat", lines)
        }
    }
}