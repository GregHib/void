package content.quest.free.prince_ali_rescue

import content.entity.player.modal.tab.questJournalOpen
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory

val escapeKit = listOf(
    Item("pink_skirt"),
    Item("bronze_key_prince_ali_rescue"),
    Item("wig_blonde"),
)

questJournalOpen("prince_ali_rescue") {
    val lines = when (player.quest("prince_ali_rescue")) {
        "osman" -> listOf(
            "<navy>I spoke to <maroon>Chancellor Hassan<navy>, the <maroon>Chancellor<navy> to the <maroon>Emir of Al",
            "<maroon>Kharid<navy>, in the <maroon>Al Kharid Palace<navy>. He asked for my help with an",
            "<navy>urgent matter, and directed me to speak to <maroon>Osman<navy>, <maroon>Al",
            "<maroon>Kharid's Spymaster<navy>, just outside the <maroon>Palace<navy>.",
        )
        "leela" -> {
            if (player.inventory.contains("key_print")) {
                listOf(
                    "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                    "<str>the Al Kharid Palace. He asked for my help with an urgent",
                    "<str>matter, and directed me to speak to Osman, Al Kharid's",
                    "<str>Spymaster.",
                    "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
                    "<str>that Prince Ali, the Emir's heir, was captured by a group of",
                    "<str>Bandits and taken to an Abandoned Jail east of Draynor",
                    "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
                    "<str>suggested I speak with Leela in Draynor Village.",
                    "<navy>To free <maroon>Prince Ali<navy>, I need to create him a disguise to make him",
                    "<navy>look like <maroon>Lady Keli<navy>, the leader of the <maroon>Bandits<navy>. I also need to",
                    "<navy>make a copy of the key to his cell.",
                    "",
                    "<navy>According to <maroon>Leela<navy>, I need a <maroon>Blonde Wig<navy>, a <maroon>Pink Skirt<navy> and some",
                    "<navy><maroon>Skin Paste<navy> for the disguise. Apparently there's an <maroon>Old Sailor<navy>",
                    "<navy>living in <maroon>Draynor Village<navy> who might be able to make a <maroon>Wig<navy> for",
                    "<navy>me to then dye. A <maroon>Pink Skirt<navy> can be purchased from a <maroon>Clothes",
                    "<maroon>Shop<navy>. As for the <maroon>Skin Paste<navy>, <maroon>Leela<navy> thinks a local <maroon>Witch<navy> could",
                    "<navy>make me some.",
                    "",
                    "<navy>I took an <maroon>Imprint<navy> of the <maroon>Cell Key<navy> using some <maroon>Soft Clay<navy>. I should",
                    "<navy>take it to <maroon>Osman<navy> along with a <maroon>Bronze Bar<navy> so that he can make",
                    "<navy>us a copy.",
                )
            } else if (player.inventory.contains(escapeKit)) {
                listOf(
                    "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                    "<str>the Al Kharid Palace. He asked for my help with an urgent",
                    "<str>matter, and directed me to speak to Osman, Al Kharid's",
                    "<str>Spymaster.",
                    "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
                    "<str>that Prince Ali, the Emir's heir, was captured by a group of",
                    "<str>Bandits and taken to an Abandoned Jail east of Draynor",
                    "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
                    "<str>suggested I speak with Leela in Draynor Village.",
                    "<navy>To free <maroon>Prince Ali<navy>, I have created him a disguise to make him",
                    "<navy>look like <maroon>Lady Keli<navy>, the leader of the <maroon>Bandits<navy>. I have also made",
                    "<navy>a copy of the key to his cell. I should speak with <maroon>Leela<navy> outside",
                    "<navy>the <maroon>Abandoned Jail<navy> and let her know that I've done all of this.",
                )
            } else if (player["prince_ali_rescue_key_given", false]) {
                // TODO lost key / incomplete kit
                emptyList()
            } else if (player["prince_ali_rescue_key_made", false]) {
                listOf(
                    "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                    "<str>the Al Kharid Palace. He asked for my help with an urgent",
                    "<str>matter, and directed me to speak to Osman, Al Kharid's",
                    "<str>Spymaster.",
                    "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
                    "<str>that Prince Ali, the Emir's heir, was captured by a group of",
                    "<str>Bandits and taken to an Abandoned Jail east of Draynor",
                    "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
                    "<str>suggested I speak with Leela in Draynor Village.",
                    "<navy>To free <maroon>Prince Ali<navy>, I need to create him a disguise to make him",
                    "<navy>look like <maroon>Lady Keli<navy>, the leader of the <maroon>Bandits<navy>. I also need to",
                    "<navy>make a copy of the key to his cell.",
                    "",
                    "<navy>According to <maroon>Leela<navy>, I need a <maroon>Blonde Wig<navy>, a <maroon>Pink Skirt<navy> and some",
                    "<navy><maroon>Skin Paste<navy> for the disguise. Apparently there's an <maroon>Old Sailor<navy>",
                    "<navy>living in <maroon>Draynor Village<navy> who might be able to make a <maroon>Wig<navy> for",
                    "<navy>me to then dye. A <maroon>Pink Skirt<navy> can be purchased from a <maroon>Clothes",
                    "<maroon>Shop<navy>. As for the <maroon>Skin Paste<navy>, <maroon>Leela<navy> thinks a local <maroon>Witch<navy> could",
                    "<navy>make me some.",
                    "",
                    "<navy>I took an <maroon>Imprint<navy> of the <maroon>Cell Key<navy> and gave it to <maroon>Osman<navy>. He will",
                    "<navy>use it to make a copy, which he will send over once it is ready.",
                )
            } else if (player["prince_ali_rescue_leela", false]) {
                listOf(
                    "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                    "<str>the Al Kharid Palace. He asked for my help with an urgent",
                    "<str>matter, and directed me to speak to Osman, Al Kharid's",
                    "<str>Spymaster.",
                    "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
                    "<str>that Prince Ali, the Emir's heir, was captured by a group of",
                    "<str>Bandits and taken to an Abandoned Jail east of Draynor",
                    "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
                    "<str>suggested I speak with Leela in Draynor Village.",
                    "<navy>To free <maroon>Prince Ali<navy>, I need to create him a disguise to make him",
                    "<navy>look like <maroon>Lady Keli<navy>, the leader of the <maroon>Bandits<navy>. I also need to",
                    "<navy>make a copy of the key to his cell.",
                    "",
                    "<navy>According to <maroon>Leela<navy>, I need a <maroon>Blonde Wig<navy>, a <maroon>Pink Skirt<navy> and some",
                    "<navy><maroon>Skin Paste<navy> for the disguise. Apparently there's an <maroon>Old Sailor<navy>",
                    "<navy>living in <maroon>Draynor Village<navy> who might be able to make a <maroon>Wig<navy> for",
                    "<navy>me to then dye. A <maroon>Pink Skirt<navy> can be purchased from a <maroon>Clothes",
                    "<maroon>Shop<navy>. As for the <maroon>Skin Paste<navy>, <maroon>Leela<navy> thinks a local <maroon>Witch<navy> could",
                    "<navy>make me some.",
                    "",
                )
            } else {
                listOf(
                    "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                    "<str>the Al Kharid Palace. He asked for my help with an urgent",
                    "<str>matter, and directed me to speak to Osman, Al Kharid's",
                    "<str>Spymaster.",
                    "<navy>I spoke to <maroon>Osman<navy> outside the <maroon>Al Kharid Palace<navy>. He informed me",
                    "<navy>that <maroon>Prince Ali<navy>, the <maroon>Emir's<navy> heir, was captured by a group of",
                    "<navy><maroon>Bandits<navy> and taken to an <maroon>Abandoned Jail<navy> east of <maroon>Draynor",
                    "<maroon>Village<navy>. <maroon>Osman<navy> asked for my help in rescuing <maroon>Prince Ali<navy>, and",
                    "<navy>suggested I speak with <maroon>Leela<navy>, who I can find spying on the",
                    "<navy><maroon>Jail<navy>.",
                    ""
                )
            }
        }
        "guard" -> listOf(
            "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
            "<str>the Al Kharid Palace. He asked for my help with an urgent",
            "<str>matter, and directed me to speak to Osman, Al Kharid's",
            "<str>Spymaster.",
            "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
            "<str>that Prince Ali, the Emir's heir, was captured by a group of",
            "<str>Bandits and taken to an Abandoned Jail east of Draynor",
            "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
            "<str>suggested I speak with Leela in Draynor Village.",
            "<str>With help from Osman and Leela, I created a disguise to make",
            "<str>Prince Ali look like Lady Keli, the leader of the Bandits. I also",
            "<str>made a copy of the key to his cell.",
            "<navy>Before I can free <maroon>Prince Ali<navy>, I need to deal with his <maroon>Personal",
            "<maroon>Guard<navy>. <maroon>Leela<navy> suggested I speak with the <maroon>Guard<navy> to try and",
            "<navy>determine any weaknesses he might have.",
        )
        "joe_beer", "joe_beers" -> listOf(
            "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
            "<str>the Al Kharid Palace. He asked for my help with an urgent",
            "<str>matter, and directed me to speak to Osman, Al Kharid's",
            "<str>Spymaster.",
            "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
            "<str>that Prince Ali, the Emir's heir, was captured by a group of",
            "<str>Bandits and taken to an Abandoned Jail east of Draynor",
            "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
            "<str>suggested I speak with Leela in Draynor Village.",
            "<str>With help from Osman and Leela, I created a disguise to make",
            "<str>Prince Ali look like Lady Keli, the leader of the Bandits. I also",
            "<str>made a copy of the key to his cell.",
            "<navy>To stop <maroon>Prince Ali's Personal Guard<navy> from being a problem, I",
            "<navy>gave him some <maroon>Beer<navy> to get him drunk. The last thing I need to",
            "<navy>do is deal with <maroon>Lady Keli<navy>. <maroon>Leela<navy> might know how I can do this.",
        )
        "keli_tied_up" -> listOf() // TODO kali tied up
        "prince_ali_disguise" -> listOf(
            "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
            "<str>the Al Kharid Palace. He asked for my help with an urgent",
            "<str>matter, and directed me to speak to Osman, Al Kharid's",
            "<str>Spymaster.",
            "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
            "<str>that Prince Ali, the Emir's heir, was captured by a group of",
            "<str>Bandits and taken to an Abandoned Jail east of Draynor",
            "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
            "<str>suggested I speak with Leela in Draynor Village.",
            "<str>With help from Osman and Leela, I created a disguise to make",
            "<str>Prince Ali look like Lady Keli, the leader of the Bandits. I also",
            "<str>made a copy of the key to his cell.",
            "<str>To stop Prince Ali's Personal Guard from being a problem, I",
            "<str>gave him some Beer to get him drunk.",
            "<str>To get Lady Keli out of the way, I tied her up and put her in a",
            "<str>Cupboard.",
            "<navy>With <maroon>Lady Keli<navy> dealt with, I was able to free <maroon>Prince Ali<navy> and get",
            "<navy>him to safety. I should now return to <maroon>Chancellor Hassan<navy> in the",
            "<navy><maroon>Al Kharid Palace<navy>.",
        )
        "completed" -> listOf(
            "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
            "<str>the Al Kharid Palace. He asked for my help with an urgent",
            "<str>matter, and directed me to speak to Osman, Al Kharid's",
            "<str>Spymaster.",
            "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
            "<str>that Prince Ali, the Emir's heir, was captured by a group of",
            "<str>Bandits and taken to an Abandoned Jail east of Draynor",
            "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
            "<str>suggested I speak with Leela in Draynor Village.",
            "<str>With help from Osman and Leela, I created a disguise to make",
            "<str>Prince Ali look like Lady Keli, the leader of the Bandits. I also",
            "<str>made a copy of the key to his cell.",
            "<str>To stop Prince Ali's Personal Guard from being a problem, I",
            "<str>gave him some Beer to get him drunk.",
            "<str>To get Lady Keli out of the way, I tied her up and put her in a",
            "<str>Cupboard.",
            "<str>With Lady Keli dealt with, I was able to free Prince Ali and get",
            "<str>him to safety. I returned to Al Kharid, where Hassan rewarded",
            "<str>me for my work.",
            "",
            "<col=ff0000>QUEST COMPLETE!",
        )
        else -> emptyList()
    }
    player.questJournal("Prince Ali Rescue", lines)
}