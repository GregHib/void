package content.quest.free.prince_ali_rescue

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.tab.questJournalOpen
import content.entity.sound.sound
import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.FontDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory

class PrinceAliRescue : Script {

    val fontDefinitions: FontDefinitions by inject()
    val disguise = listOf(
        Item("pink_skirt"),
        Item("wig_blonde"),
        Item("paste"),
    )

    init {
        objectOperate("Open", "draynor_prison_door_closed") {
            if (player.inventory.contains("bronze_key_prince_ali_rescue") || player.quest("prince_ali_rescue") == "prince_ali_disguise") {
                when (player.quest("prince_ali_rescue")) {
                    "keli_tied_up", "prince_ali_disguise" -> {
                        player.sound("unlock")
                        enterDoor(target)
                    }
                    "joe_beers" -> statement("You'll need to deal with Lady Keli before freeing the Prince.")
                    else -> statement("You'll need to deal with Lady Keli and the guard before freeing the Prince.")
                }
            } else {
                player.sound("locked")
                player.message("The gate is locked.")
            }
        }

        questJournalOpen("prince_ali_rescue") {
            val lines = when (player.quest("prince_ali_rescue")) {
                "osman" -> listOf(
                    "<navy>I spoke to <maroon>Chancellor Hassan<navy>, the <maroon>Chancellor<navy> to the <maroon>Emir of Al",
                    "<maroon>Kharid<navy>, in the <maroon>Al Kharid Palace<navy>. He asked for my help with an",
                    "<navy>urgent matter, and directed me to speak to <maroon>Osman<navy>, <maroon>Al",
                    "<maroon>Kharid's Spymaster<navy>, just outside the <maroon>Palace<navy>.",
                )
                "leela" -> {
                    if (!player["prince_ali_rescue_leela", false]) {
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
                            "",
                        )
                    } else {
                        val list = mutableListOf(
                            "<str>I spoke to Hassan, the Chancellor to the Emir of Al Kharid, in",
                            "<str>the Al Kharid Palace. He asked for my help with an urgent",
                            "<str>matter, and directed me to speak to Osman, Al Kharid's",
                            "<str>Spymaster.",
                            "<str>I spoke to Osman outside the Al Kharid Palace. He informed me",
                            "<str>that Prince Ali, the Emir's heir, was captured by a group of",
                            "<str>Bandits and taken to an Abandoned Jail east of Draynor",
                            "<str>Village. Osman asked for my help in rescuing Prince Ali, and",
                            "<str>suggested I speak with Leela in Draynor Village.",
                        )
                        val disguise = player.inventory.contains(disguise)
                        val key = player["prince_ali_rescue_key_given", false]
                        val string = buildString {
                            append("To free <maroon>Prince Ali<navy>, ")
                            append(if (disguise) "I have created" else "I need to create")
                            append(" him a disguise to make him look like <maroon>Lady Keli<navy>, the leader of the <maroon>Bandits<navy>. ")
                            append(if (key) "I have also made" else "I also need to make")
                            append(" a copy of the key to his cell.")

                            if (disguise && key) {
                                append(" I should speak with <maroon>Leela<navy> outside the <maroon>Abandoned Jail<navy> and let her know that I've done all of this.")
                            }
                        }
                        val font = fontDefinitions.get("p12_full")
                        for (line in font.splitLines(string, width = 350)) {
                            list.add("<navy>$line")
                        }
                        if (!disguise || !key) {
                            list.add("")
                            list.add("<navy>According to <maroon>Leela<navy>, I need a <maroon>Blonde Wig<navy>, a <maroon>Pink Skirt<navy> and some")
                            list.add("<navy><maroon>Skin Paste<navy> for the disguise. Apparently there's an <maroon>Old Sailor<navy>")
                            list.add("<navy>living in <maroon>Draynor Village<navy> who might be able to make a <maroon>Wig<navy> for")
                            list.add("<navy>me to then dye. A <maroon>Pink Skirt<navy> can be purchased from a <maroon>Clothes")
                            list.add("<maroon>Shop<navy>. As for the <maroon>Skin Paste<navy>, <maroon>Leela<navy> thinks a local <maroon>Witch<navy> could")
                            list.add("<navy>make me some.")
                        }
                        list.add("")
                        if (player.inventory.contains("key_print")) {
                            list.add("<navy>I took an <maroon>Imprint<navy> of the <maroon>Cell Key<navy> using some <maroon>Soft Clay<navy>. I should")
                            list.add("<navy>take it to <maroon>Osman<navy> along with a <maroon>Bronze Bar<navy> so that he can make")
                            list.add("<navy>us a copy.")
                        } else if (player["prince_ali_rescue_key_made", false] && !player["prince_ali_rescue_key_given", false]) {
                            list.add("<navy>I took an <maroon>Imprint<navy> of the <maroon>Cell Key<navy> and gave it to <maroon>Osman<navy>. He will")
                            list.add("<navy>use it to make a copy, which he will send over once it is ready.")
                        }
                        list
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
                "joe_beer" -> listOf(
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
                    "<maroon>Guard<navy>. Luckily, it seems the <maroon>Guard<navy> has a love for <maroon>Beer<navy> If I",
                    "<navy>bring him some, I should be able to get him drunk.",
                )
                "joe_beers" -> listOf(
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
                "keli_tied_up" -> listOf(
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
                    "<navy>To get <maroon>Lady Keli<navy> out of the way, I tied her up and put her in a",
                    "<navy><maroon>Cupboard<navy>. I can now free <maroon>Prince Ali<navy>. I'll need to make sure I",
                    "<navy>give him his disguise when I do.",
                )
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
                else -> listOf("<navy>I can start his quest by talking to <maroon>Chancellor Hassan<navy> in <maroon>Al Kharid Palace<navy>.")
            }
            player.questJournal("Prince Ali Rescue", lines)
        }
    }
}
