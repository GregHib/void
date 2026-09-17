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

            // Unstarted
            if(progress == 0){
                lines += "<navy>I can start this quest by speaking to <maroon>Gertrude<navy>."
                lines += "<navy>She can be found in a house south of the road leading west"
                lines += "<navy>out of <maroon>Varrock<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            // Couldn't find anything official, this is just extrapolating.
            lines += "<str>I helped Gertrude to find her lost cat,"

            // spoke_to_gertrude
            if(progress == 1) {
                lines += "<navy>I need to speak to <maroon>Gertrude<navy>'s sons,"
                lines += "<maroon>Shilop <navy>and <maroon>Wilough<navy>, in <maroon>Varrock Marketplace<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            lines += "<str>I spoke with Gertrude's sons,"
            lines += "<str>Shilop and Wilough, in Varrock Marketplace."

            // found_the_boys
            if(progress == 2) {
                lines += "<navy>I need to go to <maroon>Shilop <navy>and <maroon>Wilough<navy>'s secret hideout"
                lines += "<navy>in an abandoned <maroon>Lumber Mill<navy>, to the <maroon>north-east<navy> and find"
                lines += "<maroon>Fluffs<navy>, then return her to <maroon>Gertrude<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            // found_fluffs
            if(progress == 3) {
                lines += "<navy>I had a poke round the abandoned <maroon>Lumber Mill<navy> and found"
                lines += "<navy>Fluffs <maroon>up a ladder<navy>. I now need to return <maroon>Fluffs <navy>to <maroon>Gertrude<navy>."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            /**
             * attempt_fluffs_pickup
             */
            if(progress == 4) {
                if (!get("gertrudes_cat_fluffs_milk", false)){
                    lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>milk<navy> but she still won't come back."
                    lines += "<navy>Now I should feed her some <maroon>doogle sardines<navy>."
                    questJournal("Gertrude's Cat", lines)
                    return@questJournalOpen
                }
                if (!get("gertrudes_cat_fluffs_fed", false)) {
                    lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>doogle sardines<navy> but she still won't come back."
                    lines += "<navy>Now I should feed her some <maroon>milk<navy>."
                    questJournal("Gertrude's Cat", lines)
                    return@questJournalOpen
                }
                if(get("three_little_kittens_found", false)) {
                    lines += "<navy>I have found some <maroon>kittens<navy>. "
                    lines += "<navy>I think I should see whether <maroon>Fluffs<navy> is looking for them."
                    questJournal("Gertrude's Cat", lines)
                    return@questJournalOpen
                }
                if(get("gertrudes_cat_fluffs_milk", false) && get("gertrudes_cat_fluffs_fed", false)){
                    lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>milk<navy> and <maroon>doogle sardine,"
                    lines += "<navy>but she still won't come back. I should look around the <maroon>Lumbermill Yard<navy>"
                    lines += "<navy>for a reason for this behaviour."
                    questJournal("Gertrude's Cat", lines)
                    return@questJournalOpen
                }
                lines += "<navy>I had a poke round the abandoned <maroon>Lumber Mill<navy> and found"
                lines += "<maroon>Fluffs<navy> up <maroon>a ladder<navy>. I now need to return <maroon>Fluffs<navy> to"
                lines += "<maroon>Gertrude<navy>. "
                lines += "<navy>I think <maroon>Fluffs<navy> may be hungry or thirsty but I am not sure what"
                lines += "<navy>she wants; perhaps <maroon>Gertrude<navy> can help?"
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }
            /**
             * End of attempt_fluffs_pickup
             */

            lines += "<str>I fed her and returned her missing kitten,"

            // fluffs_returned
            if(progress == 5) {
                lines += "<navy>I returned the <maroon>kittens<navy> to <maroon>Fluffs<navy> who then ran away."
                lines += "<navy>I think I should see <maroon>Gertrude<navy> in her <maroon>house, west of Varrock<navy>,"
                lines += "<navy>and explain what has happened."
                questJournal("Gertrude's Cat", lines)
                return@questJournalOpen
            }

            // completed
            lines += "<str>Gertrude gave me my very own pet for a reward."
            lines += ""
            lines += "<red>QUEST COMPLETE!"
            questJournal("Gertrude's Cat", lines)
        }
    }
}