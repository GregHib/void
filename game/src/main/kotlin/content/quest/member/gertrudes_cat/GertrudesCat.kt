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
            lines += "<str>I helped Gertrude to find her lost cat,"

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
                lines += "<navy>I had a poke round the abandoned <maroon>Lumber Mill<navy> and found"
                lines += "<navy>Fluffs <maroon>up a ladder<navy>. I now need to return <maroon>Fluffs <navy>to <maroon>Gertrude<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            if(progress == 4) {
                lines += "<navy>I had a poke round the abandoned <maroon>Lumber Mill<navy> and found"
                lines += "<maroon>Fluffs<navy> up <maroon>a ladder<navy>. I now need to return <maroon>Fluffs<navy> to"
                lines += "<maroon>Gertrude<navy>. "
                lines += "<navy>I think <maroon>Fluffs<navy> may be hungry or thirsty but I am not sure what"
                lines += "<navy>she wants; perhaps <maroon>Gertrude<navy> can help?"
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            if (get("gertrudes_cat_fluffs_milk", false)){
                lines += "<navy>I have fed the cat milk."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            if (get("gertrudes_cat_fluffs_fed", false)) {
                lines += "<navy>I have fed the cat a doogle sardine"
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            if(get("gertrudes_cat_fluffs_milk", false) && get("gertrudes_cat_fluffs_fed", false)){
                lines += "<navy>I still need to <maroon>get her to follow me home."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            lines += "<str>I fed it and returned her missing kitten,"

            if(progress == 5) {
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            // complete
            lines += "<str>Gertrude gave me my very own pet for a reward."
            lines += ""
            lines += "<red>QUEST COMPLETE!"
            questJournal("Gertrude's Cat", lines)
        }
    }
}