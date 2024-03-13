package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.interfaceSlot
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.world.activity.bank.ownsItem

interfaceSlot(component = "journals", id = "quest_journals", itemSlot = 8) {
    val lines = when (player.quest("the_knights_sword")) {
        "completed" -> listOf(
            "<str>Thurgo needed a picture of the sword before he could",
            "<str>start work on a replacement. I took him a portrait of it.",
            "<str>After bringing Thurgo two iron bars and some blurite ore",
            "<str>he made me a fine replica of Sir Vyvin's Sword, which I",
            "<str>returned to the Squire for a reward.",
            "",
            "<red>QUEST COMPLETE!",
        )
        "started" -> listOf(
            "<str>I told the Squire I would help him to replace the sword he",
            "<str>had lost. It could only be made by an Imcando Dwarf.",
            "<navy>The Squire suggests I speak to <maroon>Reldo <navy>in the <maroon>Varrock Palace",
            "<maroon>Library<navy> for information about the <maroon>Imcando Dwarves.",
        )
        "find_thurgo" -> listOf(
            "<str>I told the Squire I would help him to replace the sword he",
            "<str>had lost. It could only be made by an Imcando Dwarf.",
            "<navy>Reldo couldn't give me much information about the",
            "<maroon>Imcando <navy>except a few live on the <maroon>southern peninsula of",
            "<maroon>Asgarnia, <navy>they dislike strangers, and LOVE <maroon>redberry pies.",
        )
        "happy_thurgo" -> listOf(
            "<str>I told the Squire I would help him to replace the sword he",
            "<str>had lost. It could only be made by an Imcando Dwarf.",
            "<str>I found an Imcando Dwarf named Thurgo thanks to",
            "<str>information provided by Reldo. He wasn't very talkative",
            "<str>until I gave him a Redberry pie, which he gobbled up.",
            "<navy>He will help me now I have gained his trust through <maroon>pie.",
        )
        "picture" -> listOf(
            "<str>I told the Squire I would help him to replace the sword he",
            "<str>had lost. It could only be made by an Imcando Dwarf.",
            "<str>I found an Imcando Dwarf named Thurgo thanks to",
            "<str>information provided by Reldo. He wasn't very talkative",
            "<str>until I gave him a Redberry pie, which he gobbled up.",
            "<maroon>Thurgo <navy>needs a <maroon>picture of the sword <navy>before he can help.",
            "<navy>I should probably ask the <maroon>Squire <navy>about obtaining one.",
        )
        "cupboard" -> {
            val list = mutableListOf(
                "<str>I told the Squire I would help him to replace the sword he",
                "<str>had lost. It could only be made by an Imcando Dwarf.",
                "<str>I found an Imcando Dwarf named Thurgo thanks to",
                "<str>information provided by Reldo. He wasn't very talkative",
                "<str>until I gave him a Redberry pie, which he gobbled up.",
                "<str>Thurgo needed a picture of the sword to replace.",
            )
            if (player.holdsItem("portrait") || player.ownsItem("portrait")) {
                list.add("<navy>I now have a picture of the <maroon>Knight's Sword <navy>- I should take it")
                list.add("<navy>to <maroon>Thurgo <navy>so that he can duplicate it.")
            } else {
                list.add("<navy>The Squire told me about a <maroon>portrait <navy>of Sir Vyvin's father")
                list.add("<navy>which has a <maroon>picture of the sword <navy>in <maroon>Sir Vyvin's room.")
            }
            list
        }
        "blurite_sword" -> {
            val list = mutableListOf(
                "<str>I told the Squire I would help him to replace the sword he",
                "<str>had lost. It could only be made by an Imcando Dwarf.",
                "<str>I found an Imcando Dwarf named Thurgo thanks to",
                "<str>information provided by Reldo. He wasn't very talkative",
                "<str>until I gave him a Redberry pie, which he gobbled up.",
                "<str>Thurgo needed a picture of the sword before he could",
                "<str>start work on a replacement. I took him a portrait of it.",

                )
            if (player.holdsItem("blurite_sword") || player.ownsItem("blurite_sword")) {
                list.add("<str>Thurgo has now smithed me a replica of Sir Vyvin's sword.")
                list.add("")
                list.add("<navy>I should return it to the <maroon>Squire <navy>for my <maroon>reward.")
            } else {
                list.add("<navy>According to <maroon>Thurgo <navy>to make a <maroon>replica sword <navy>he will need")
                list.add("<maroon>two Iron Bars <navy>and some <maroon>Blurite Ore. Blurite Ore <navy>can only be")
                list.add("<navy>found <maroon>deep in the caves below Thurgo's house<navy>. I should")
                list.add("<navy>prepare myself to fend off Ice giants.")
            }
            list
        }
        else -> listOf(
            "<navy>I can start this quest by speaking to the <maroon>Squire <navy>in the",
            "<navy>courtyard of the <maroon>White Knight's Castle <navy>in <maroon>southern Falador",
            "<navy>To complete this quest I need:",
            "<maroon>Level 10 Mining",
            "<navy>and to be unafraid of <maroon>Level 57 Ice Warriors."
        )
    }
    player.sendQuestJournal("The Knight's Sword", lines)
}