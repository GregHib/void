package content.quest.free.demon_slayer

import world.gregs.voidps.engine.client.ui.interfaceSlot
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import content.quest.quest
import content.quest.questJournal

interfaceSlot(component = "journals", id = "quest_journals", itemSlot = 2) {
    val lines = when (player.quest("demon_slayer")) {
        "unstarted" -> listOf(
            "<navy>I can start this quest by speaking to the <maroon>Gypsy<navy> in the <maroon>tent",
            "<navy>in <maroon>Varrock's main square.",
            "",
            "<navy>I must be able to defeat a level 27 <maroon>apocalyptic demon<navy>!"
        )
        "sir_prysin", "key_hunt" -> {
            val list = mutableListOf(
                "<str>I spoke to Aris in Varrock Square who saw my future.",
                "<str>Unfortunately it involved killing a demon who nearly",
                "<str>destroyed Varrock over 150 years ago.",
            )
            if (player["demon_slayer_silverlight", false]) {
                list.add("<str>I reclaimed the magical sword Silverlight from Sir Prysin.")
                list.add("")
                list.add("<navy>Now I should go to the stone circle south of the city and")
                list.add("<navy>destroy <maroon>Delrith<navy> using <maroon>Silverlight<navy>!.")
            } else {
                list.add("")
                list.add("<navy>To defeat the <maroon>demon<navy> I need the magical sword <maroon>Silverlight<navy>.")
                if (player.quest("demon_slayer") == "sir_prysin") {
                    list.add("<navy>I should ask <maroon>Sir Prysin<navy> in <maroon>Varrock Palace<navy> where it is.")
                } else {
                    val prysin = player.inventory.contains("silverlight_key_sir_prysin")
                    val rovin = player.inventory.contains("silverlight_key_captain_rovin")
                    val traiborn = player.inventory.contains("silverlight_key_wizard_traiborn")
                    if (prysin && rovin && traiborn) {
                        list.add("<navy>Now I have all <maroon>3 keys<navy> I should go and speak to <maroon>Sir Prysin")
                        list.add("<navy>and collect the magical sword <maroon>Silverlight<navy> from him.")
                    } else {
                        list.add("<maroon>Sir Prysin<navy> needs <maroon>3 keys<navy> before he can give me <maroon>Silverlight<navy>.")
                        list.add("")
                        listKeys(player, list, prysin, rovin, traiborn)
                    }
                }
            }
            list
        }
        "completed" -> listOf(
            "<str>I spoke to Aris in Varrock Square who saw my future.",
            "<str>Unfortunately it involved killing a demon who nearly",
            "<str>destroyed Varrock over 150 years ago.",
            "<str>I reclaimed the magical sword Silverlight from Sir Prysin.",
            "<str>Using its power I managed to destroy the demon Delrith",
            "<str>like the great hero Wally did many years before.",
            "<red>QUEST COMPLETE!"
        )
        else -> listOf()
    }
    player.questJournal("Demon Slayer", lines)
}

fun listKeys(
    player: Player,
    list: MutableList<String>,
    prysin: Boolean,
    rovin: Boolean,
    traiborn: Boolean
) {
    if (prysin) {
        list.add("<str>I have the 1st Key with me.")
    } else {
        list.add("<navy>The <maroon>1st Key<navy> was dropped down the <maroon>palace kitchen drains<navy>.")
        if (player["demon_slayer_drain_dislodged", false]) {
            list.add("<navy>I have washed the key down the drain into the sewer. I")
            list.add("<navy>should go down there to fetch it. The <maroon>sewer entrance<navy> could")
            list.add("<navy>be found somewhere just <maroon>outside<navy> the <maroon>palace courtyard<navy> to")
            list.add("<maroon>the east.")
        } else {
            list.add("<navy>Maybe some water can dislodge it.")
        }
    }

    if (rovin) {
        list.add("<str>I have the 2nd Key with me.")
    } else {
        list.add("<navyThe <maroon>2nd Key<navy> is with Captain Rovin in Varrock Palace.")
    }

    if (traiborn) {
        list.add("<str>I have the 3rd Key with me.")
    } else {
        list.add("<navy>The <maroon>3rd Key<navy> is with Wizard Traiborn at the Wizards' Tower,")
        list.add("<navy>south of Draynor Village.")
        val bones = player["demon_slayer_bones", -1]
        if (bones != -1) {
            list.add("<maroon>Traiborn<navy> needs <maroon>${bones}<navy> more <maroon>bones<navy>.")
        }
    }
}