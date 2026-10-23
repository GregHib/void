package content.quest.member.gertrudes_cat

import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script

// Source: https://runescape.wiki/w/Transcript:Gertrude%27s_Cat/Journal
// https://www.youtube.com/watch?v=_pZcnvI1l_Q from 2011 using a mix of RS3 and OSRS transcript and going as best as I can off of memory and videos.
const val GERTRUDES_CAT = "gertrudes_cat"

class GertrudesCat : Script {
    init {
        questJournalOpen(GERTRUDES_CAT) {
            val stage = questStage(GERTRUDES_CAT)
            val lines = mutableListOf<String>()

            // Unstarted
            if (stage == 0) {
                lines += "<navy>I can start this quest by speaking to <maroon>Gertrude<navy>. She can be found in"
                lines += "<navy>a house south of the road leading <maroon>west out of Varrock<navy>."
            } else if (stage in 1..4) {
                lines += "<str>I can start this quest by speaking to Gertrude. She can be found in"
                lines += "<str>a house south of the road leading west out of Varrock."
            }

            // spoke_to_gertrude
            if (stage == 1) {
                lines += "<navy>I need to speak to Gertrude's sons, <maroon>Shilop and Wilough<navy>, in <maroon>Varrock"
                lines += "<maroon>Marketplace<navy>."
            } else if (stage in 2..4) {
                lines += "<str>I need to speak to Gertrude's sons, Shilop and Wilough, in Varrock"
                lines += "<str>Marketplace."
            }

            // found_the_boys
            if (stage == 2) {
                lines += "<navy>I need to go to <maroon>Shilop <navy>and <maroon>Wilough<navy>'s secret hideout in an abandoned"
                lines += "<maroon>Lumber Mill<navy>, to the <maroon>north-east<navy> and find <maroon>Fluffs<navy>, then return"
                lines += "<navy>her to <maroon>Gertrude<navy>."
            }

            // found_fluffs
            if (stage == 3) {
                lines += "<navy>I had a poke round the abandoned <maroon>Lumber Mill<navy> and found"
                lines += "<navy>Fluffs <maroon>up a ladder<navy>. I now need to return <maroon>Fluffs <navy>to <maroon>Gertrude<navy>."
            } else if (stage >= 4) {
                lines += "<str>I had a poke round the abandoned Lumber Mill and found"
                lines += "<str>Fluffs up a ladder. I now need to return Fluffs to Gertrude."
            }

            // attempt_fluffs_pickup
            if (stage == 4) {
                val milk = get("gertrudes_cat_fluffs_milk", false)
                val food = get("gertrudes_cat_fluffs_fed", false)
                when {
                    get("three_little_kittens_found", false) -> {
                        lines += "<navy>I have found some <maroon>kittens<navy>. "
                        lines += "<navy>I think I should see whether <maroon>Fluffs<navy> is looking for them."
                    }
                    milk && food -> {
                        lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>milk<navy> and <maroon>doogle sardine<navy>,"
                        lines += "<navy>but she still won't come back. I should look around the"
                        lines += "<maroon>Lumbermill Yard<navy> for a reason for this behaviour."
                    }
                    milk -> {
                        lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>milk<navy> but she still won't come back."
                        lines += "<navy>Now I should feed her some <maroon>doogle sardines<navy>."
                    }
                    food -> {
                        lines += "<navy>I found <maroon>Fluffs<navy> and fed her some <maroon>doogle sardines<navy>"
                        lines += "<navy>but she still won't come back. Now I should feed her some <maroon>milk<navy>."
                    }
                    else -> {
                        lines += "<navy>I think <maroon>Fluffs<navy> may be hungry or thirsty but I am not sure what"
                        lines += "<navy>she wants; perhaps <maroon>Gertrude<navy> can help?"
                    }
                }
            }

            // fluffs_returned
            if (stage == 5) {
                lines += "<navy>I returned the <maroon>kittens<navy> to <maroon>Fluffs<navy> who then ran away."
                lines += "<navy>I think I should see <maroon>Gertrude<navy> in her <maroon>house, west of Varrock<navy>,"
                lines += "<navy>and explain what has happened."
            }

            // completed
            if (stage == 6) {
                lines += "<str>I helped Gertrude to find her lost cat, Fluffs, by finding Fluffs's"
                lines += "<str>kittens. With her kittens safe, Fluffs returned to Gertrude."
                lines += "<str>Gertrude gave me my very own pet for a reward."
                lines += ""
                lines += "<red>QUEST COMPLETE!"
            }

            questJournal("Gertrude's Cat", lines)
        }
    }
}
