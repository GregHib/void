package content.quest.member.lost_city

import content.entity.player.bank.bank
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.carriesItem

class LostCity : Script {
    init {
        questJournalOpen("lost_city") {
            val lines = when (quest("lost_city")) {
                "completed" -> listOf(
                    "<str>According to one of the adventurers in Lumbridge Swamp the",
                    "<str>entrance to Zanaris is somewhere around there.",
                    "<str>I found a Leprechaun hiding in a nearby tree.",
                    "<str>He told me that the entrance to Zanaris is in the shed in",
                    "<str>Lumbridge swamp but only if I am carrying a Dramen Staff.",
                    "<str>The Dramen Tree was guarded by a powerful Tree Spirit.",
                    "<str>I cut a branch from the tree and crafted a Dramen Staff.",
                    "<str>With the mystical Dramen Staff in my possession I was able to",
                    "<str>enter Zanaris through the shed in Lumbridge swamp.",
                    "",
                    "<red>QUEST COMPLETE!",
                )
                "started" -> listOf(
                    "<str>According to one of the adventurers in Lumbridge Swamp, ",
                    "<str>the entrance to Zanaris is somewhere around there.",
                    "<navy>Apparently, there is a <maroon>leprechaun <navy>hiding in a tree nearby ",
                    "<navy>who can tell me how to enter the <maroon>lost city of Zanaris.",
                    "",
                )
                "find_staff", "tree_spirit" -> listOf(
                    "<str>According to one of the adventurers in Lumbridge Swamp the",
                    "<str>entrance to Zanaris is somewhere around there.",
                    "<str>I found a Leprechaun hiding in a nearby tree.",
                    "<navy>He told me that the entrance to <maroon>Zanaris <navy>is in the <maroon>shed <navy>in ",
                    "<maroon>Lumbridge Swamp<navy>, but only if I am carrying a <maroon>dramen staff.",
                    "<navy>I can find a <maroon>dramen tree <navy>in a cave somewhere on <maroon>Entrana",
                    "",
                )
                "spirit_killed" -> listOf(
                    "<str>According to one of the adventurers in Lumbridge Swamp the",
                    "<str>entrance to Zanaris is somewhere around there.",
                    "<str>I found a Leprechaun hiding in a nearby tree.",
                    "<str>He told me that the entrance to Zanaris is in the shed in",
                    "<str>Lumbridge swamp but only if I am carrying a Dramen Staff.",
                    "<str>The Dramen Tree was guarded by a powerful Tree Spirit.",
                    if (bank.contains("dramen_branch") || carriesItem("dramen_branch")) {
                        "<navy>I should use a <maroon>knife <navy>to craft the <maroon>branch <navy>from the tree into a <maroon>staff."
                    } else {
                        "<navy>With the <maroon>spirit <navy>defeated, I can cut a <maroon>branch <navy>from the dramen tree."
                    },
                    "",
                )
                "crafted_staff" -> listOf(
                    "<str>According to one of the adventurers in Lumbridge Swamp the",
                    "<str>entrance to Zanaris is somewhere around there.",
                    "<str>I found a Leprechaun hiding in a nearby tree.",
                    "<str>He told me that the entrance to Zanaris is in the shed in",
                    "<str>Lumbridge swamp but only if I am carrying a Dramen Staff.",
                    "<str>The Dramen Tree was guarded by a powerful Tree Spirit.",
                    "<str>I cut a branch from the tree and crafted a Dramen Staff.",
                    "<navy>I should enter <maroon>Zanaris <navy>by going to the <maroon>shed <navy>in <maroon>Lumbridge Swamp ",
                    "<navy>while wielding a <maroon>dramen staff.",
                    "",
                )
                else -> listOf(
                    "<navy>I can start this quest by speaking to the <maroon>adventurers <navy>in the ",
                    "<navy>south part of <maroon>Lumbridge Swamp.",
                )
            }
            questJournal("Lost City", lines)
        }
    }
}
