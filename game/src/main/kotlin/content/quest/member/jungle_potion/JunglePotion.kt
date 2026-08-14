package content.quest.member.jungle_potion

import content.quest.quest
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.inventory

class JunglePotion : Script {

    init {
        questJournalOpen("jungle_potion") {
            val lines = when (quest("jungle_potion")) {
                "started", "found_snake_weed" -> listOf(
                    "<str>I spoke to Trufitus, he needs to commune with the",
                    "<str>gods, he's asked me to help him by collecting herbs.",
                    "",
                    "<navy>I need to pick some fresh <maroon>Snakeweed <navy>for <maroon>Trufitus.",
                )
                "gave_snake_weed", "found_ardrigal" -> {
                    val list = mutableListOf(
                        "<str>I spoke to Trufitus, he needs to commune with the",
                        "<str>gods, he's asked me to help him by collecting herbs.",
                        "",
                        "<str>I've given Snakeweed to Trufitus.",
                    )
                    if (inventory.contains("clean_ardrigal")) {
                        list.add("<str>I picked some fresh Ardrigal for Trufitus.")
                        list.add("")
                        list.add("<navy>I need to give the <maroon>Ardrigal <navy>to <maroon>Trufitus.")
                    } else {
                        list.add("")
                        list.add("<navy>I need to pick some fresh <maroon>Ardrigal <navy>for <maroon>Trufitus.")
                    }
                    list
                }
                "gave_ardrigal", "found_sito_foil" -> {
                    val list = mutableListOf(
                        "<str>I spoke to Trufitus, he needs to commune with the",
                        "<str>gods, he's asked me to help him by collecting herbs.",
                        "",
                        "<str>I've given Snakeweed and Ardrigal to Trufitus.",
                    )
                    if (inventory.contains("clean_sito_foil")) {
                        list.add("<str>I picked some fresh Sito Foil for Trufitus.")
                        list.add("")
                        list.add("<navy>I need to give the <maroon>Sito Foil <navy>to <maroon>Trufitus.")
                    } else {
                        list.add("")
                        list.add("<navy>I need to pick some fresh <maroon>Sito Foil <navy>for <maroon>Trufitus.")
                    }
                    list
                }
                "gave_sito_foil", "found_volencia_moss" -> {
                    val list = mutableListOf(
                        "<str>I spoke to Trufitus, he needs to commune with the",
                        "<str>gods, he's asked me to help him by collecting herbs.",
                        "",
                        "<str>I've given Snakeweed, Ardrigal and Sito Foil",
                        "<str>to Trufitus.",
                        "",
                    )
                    if (inventory.contains("clean_volencia_moss")) {
                        list.add("<str>I picked some fresh Volencia Moss for Trufitus.")
                        list.add("")
                        list.add("<navy>I need to give the <maroon>Volencia Moss <navy>to <maroon>Trufitus.")
                    } else {
                        list.add("<navy>I need to pick some fresh <maroon>Volencia Moss <navy>for <maroon>Trufitus.")
                    }
                    list
                }
                "gave_volencia_moss", "found_rogues_purse" -> {
                    val list = mutableListOf(
                        "<str>I spoke to Trufitus, he needs to commune with the",
                        "<str>gods, he's asked me to help him by collecting herbs.",
                        "",
                        "<str>I've given Snakeweed, Ardrigal, Sito Foil",
                        "<str>and Volencia Moss to Trufitus.",
                        "",
                    )
                    if (inventory.contains("clean_rogues_purse")) {
                        list.add("<str>I picked some fresh Rogues Purse for Trufitus.")
                        list.add("")
                        list.add("<navy>I need to give the <maroon>Rogues Purse <navy>to <maroon>Trufitus.")
                    } else {
                        list.add("<navy>I need to pick some fresh <maroon>Rogues Purse <navy>for <maroon>Trufitus.")
                    }
                    list
                }
                "gave_rogues_purse" -> listOf(
                    "", // todo
                    "",
                    "",
                )
                "completed" -> listOf(
                    "<str>Trufitus Shakaya of Tai Bwo Wannai village needed",
                    "<str>some jungle herbs in order to make a potion which would",
                    "<str>help him commune with the gods. I collected five lots",
                    "<str>of jungle herbs for him and he was able to",
                    "<str>commune with the gods.",
                    "",
                    "<str>As a reward he showed me some herblore techniques. ",
                    "",
                    "<red>QUEST COMPLETE!",
                    "",
                )
                else -> listOf(
                    "<navy>I can start this quest by speaking to <maroon>Trufitus Shakaya",
                    "<navy>who lives in the main hut in <maroon> Tai Bwo Wannai",
                    "<navy>village on the island of <maroon>Karamja.",
                )
            }
            questJournal("Jungle Potion", lines)
        }
    }
}
