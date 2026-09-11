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
            // Couldn't find anything official, this is just extrapolating.
            lines += "<str>I have talked to Gertrude about her cat. She is very upset."

            if(progress == 1) {
                lines += "<navy>I need to speak to <maroon>Gertrude<navy>'s sons,"
                lines += "<maroon>Shilop <navy>and <maroon>Wilough<navy>, in <maroon>Varrock Marketplace<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            lines += "<str>I spoke with Gertrude's sons,"
            lines += "<str>Shilop and Wilough, in Varrock Marketplace."

            if(progress == 2) {
                lines += "<navy>I need to go to <maroon>Shilop <navy>and <maroon>Wilough<navy>'s secret hideout"
                lines += "<navy>in an abandoned <maroon>Lumber Mill<navy>, to the <maroon>north-east<navy> and find"
                lines += "<maroon>Fluffs<navy>, then return her to <maroon>Gertrude<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            if(progress == 3) {
                //found_fluffs = 3,
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            if(progress == 4) {
                //fed_fluffs = 4,
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            if(progress == 5) {
                //returned_fluffs = 5,
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            // complete = 6
            questJournal("Gertrude's Cat", lines)
        }
    }
}