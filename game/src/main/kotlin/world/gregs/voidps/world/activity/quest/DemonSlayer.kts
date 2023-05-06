package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 2 }) { player: Player ->
    val lines = when (player["demon_slayer", "unstarted"]) {
        "unstarted" -> listOf(
            "<navy>I can start this quest by speaking to the <maroon>Gypsy<navy> in the <maroon>tent",
            "<navy>in <maroon>Varrock's main square.",
            "",
            "<navy>I must be able to defeat a level 27 <maroon>apocalyptic demon<navy>!"
        )
        "wally_cutscene", "sir_prysin" -> listOf(
            "<str>I spoke to Aris in Varrock Square who saw my future.",
            "<str>Unfortunately it involved killing a demon who nearly",
            "<str>destroyed Varrock over 150 years ago.",
            "",
            "<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.",
            "<navy>I should ask <maroon>Sir Prysin<navy> in <maroon>Varrock Palace<navy> where it is."
        )
        "key_hunt" -> {
            val list = mutableListOf(
                "<str>I spoke to Aris in Varrock Square who saw my future.",
                "<str>Unfortunately it involved killing a demon who nearly",
                "<str>destroyed Varrock over 150 years ago.",
                "",
                "<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.",
                "<maroon>Sir Prysin<navy> needs <maroon>3 keys<navy> before he can give me <maroon>Silverlight<navy>.",
                "",
            )

            list.add("<navy>The <maroon>1st Key<navy> was dropped down the <maroon>palace kitchen drains<navy>.")
            if (player["demon_slayer_drain_dislodged", false]) {
                list.add("<navy>I have washed the key down the drain into the sewer. I")
                list.add("<navy>should go down there to fetch it. The <maroon>sewer entrance<navy> could")
                list.add("<navy>be found somewhere just <maroon>outside<navy> the <maroon>palace courtyard<navy> to")
                list.add("<maroon>the east.")
            } else {
                list.add("<navy>Maybe some water can dislodge it.")
            }

            list.add("<navyThe <maroon>2nd Key<navy> is with Captain Rovin in Varrock Palace.")

            list.add("<navyThe <maroon>3rd Key<navy> is with Wizard Traiborn at the Wizards' Tower,")
            list.add("<navy>south of Draynor Village.")

            list
        }
        else -> listOf()
    }
    player.sendQuestJournal("Demon Slayer", lines)
}