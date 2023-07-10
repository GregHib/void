package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 13 }) { player: Player ->
    val lines = when (player["rune_mysteries", "unstarted"]) {
        "completed" -> listOf(
            "<str>I spoke to Duke Horacio in Lumbridge Castle. He told me",
            "<str>that he'd found a Strange Talisman in the Castle which",
            "<str>might be of use to the Order of Wizards at the Wizards'",
            "<str>Tower. He asked me to take it there and give it to a wizard",
            "<str>called Sedridor.",
            "<str>I delivered the Strange Talisman to Sedridor in the",
            "<str>Wizards' Tower. He believed it might be key to discovering",
            "<str>a Teleportation Incantation to the lost Rune Essence Mine.",
            "<str>He asked me to help confirm this by delivering a Package",
            "<str>to Aubury, an expert on Runecrafting.",
            "<str>I delivered the Package to Aubury in Varrock. He confirmed",
            "<str>Sedridor's suspicions and asked me to take some",
            "<str>Research Notes back to him. I did so, and Sedridor used",
            "<str>them to discover the Teleportation Incantation to the lost",
            "<str>Rune Essence Mine. As a thank you for my help, he",
            "<str>granted me permission to use the Rune Essence Mine",
            "<str>whenever I please.",
            "",
            "<red>QUEST COMPLETE!"
        )
        "started" -> {
            val list = mutableListOf(
                "<navy>I spoke to <maroon>Duke Horacio<navy> in <maroon>Lumbridge Castle.<navy> He told me",
                "<navy>that he'd found a <maroon>Strange Talisman<navy> in the <maroon>Castle<navy> which",
                "<navy>might be of use to the <maroon>Order of Wizards<navy> at the <maroon>Wizards'",
                "<maroon>Tower. He asked me to take it there and give it to a wizard",
                "<navy>called <maroon>Sedridor.<navy> I can find the <maroon>Wizards' Tower<navy> south west of",
                "<maroon>Lumbridge, across the bridge from <maroon>Draynor Village."
            )

            if (!player.hasItem("air_talisman")) {
                list.add("<navy>If I lose the <maroon>Strange Talisman<navy> , I'll need to ask <maroon>Duke Horacio<navy> for")
                list.add("<navy>another.")
            }
            list
        }
        "talisman_delivered" -> listOf(
            "<str>I spoke to Duke Horacio in Lumbridge Castle. He told me",
            "<str>that he'd found a Strange Talisman in the Castle which",
            "<str>might be of use to the Order of Wizards at the Wizards'",
            "<str>Tower. He asked me to take it there and give it to a wizard",
            "<str>called Sedridor.",
            "<navy>I delivered the <maroon>Strange Talisman<navy> to <maroon>Sedridor<navy> in the",
            "<navy>basement of the <maroon>Wizards' Tower.<navy> I should see what he can",
            "<navy>tell me about it."
        )
        "research_package" -> {
            val list = mutableListOf(
                "<str>I spoke to Duke Horacio in Lumbridge Castle. He told me",
                "<str>that he'd found a Strange Talisman in the Castle which",
                "<str>might be of use to the Order of Wizards at the Wizards'",
                "<str>Tower. He asked me to take it there and give it to a wizard",
                "<str>called Sedridor.",
                "<navy>I delivered the <maroon>Strange Talisman<navy> to <maroon>Sedridor<navy> in the",
                "<navy>basement of the <maroon>Wizards' Tower.<navy> He believes it might be",
                "<navy>key to discovering a <maroon>Teleportation Incantation<navy> to the lost",
                "<maroon>Rune Essence Mine.<navy> He asked me to help confirm this by",
                "<navy>delivering a <maroon>Package<navy> to <maroon>Aubury<navy> , an expert on",
                "<maroon>Runecrafting.<navy> I can find him in his <maroon>Rune Shop<navy> in south east",
                "<maroon>Varrock."
            )
            if (!player.hasItem("research_package_rune_mysteries")) {
                list.add("<navy>If I lose the <maroon>Package<navy> , I'll need to ask <maroon>Sedridor<navy> for")
                list.add("<navy>another.")
            }
            list
        }
        "package_delivered" -> listOf(
            "<str>I spoke to Duke Horacio in Lumbridge Castle. He told me",
            "<str>that he'd found a Strange Talisman in the Castle which",
            "<str>might be of use to the Order of Wizards at the Wizards'",
            "<str>Tower. He asked me to take it there and give it to a wizard",
            "<str>called Sedridor.",
            "<str>I delivered the Strange Talisman to Sedridor in the",
            "<str>Wizards' Tower. He believed it might be key to discovering",
            "<str>a Teleportation Incantation to the lost Rune Essence Mine.",
            "<str>He asked me to help confirm this by delivering a Package",
            "<str>to Aubury, an expert on Runecrafting.",
            "<navy>I delivered the <maroon>Package<navy> to <maroon>Aubury<navy> at his <maroon>Rune Shop<navy> in",
            "<navy>south east <maroon>Varrock<navy> . I should see what he can tell me about",
            "<navy>the <maroon>Teleportation Incantation.",
        )
        "research_notes" -> {
            val list = mutableListOf(
                "<str>I spoke to Duke Horacio in Lumbridge Castle. He told me",
                "<str>that he'd found a Strange Talisman in the Castle which",
                "<str>might be of use to the Order of Wizards at the Wizards'",
                "<str>Tower. He asked me to take it there and give it to a wizard",
                "<str>called Sedridor.",
                "<str>I delivered the Strange Talisman to Sedridor in the",
                "<str>Wizards' Tower. He believed it might be key to discovering",
                "<str>a Teleportation Incantation to the lost Rune Essence Mine.",
                "<str>He asked me to help confirm this by delivering a Package",
                "<str>to Aubury, an expert on Runecrafting.",
                "<navy>I delivered the <maroon>Package<navy> to <maroon>Aubury<navy> at his <maroon>Rune Shop<navy> in",
                "<navy>south east <maroon>Varrock<navy> . He confirmed <maroon>Sedridor's<navy> suspicions",
                "<navy>and asked me to take some <maroon>Research Notes<navy> . back to him. I",
                "<navy>can find <maroon>Sedridor<navy> in the basement of the <maroon>Wizards' Tower<navy>."
            )
            if (!player.hasItem("research_notes_rune_mysteries")) {
                list.add("<navy>If I lose the <maroon>Research Notes<navy> I'll need to ask <maroon>Aubury<navy> for")
                list.add("<navy>some more.")
            }
            list
        }
        else -> listOf(
            "<navy>I can start this quest by speaking to <maroon>Duke Horacio of",
            "<maroon>Lumbridge<navy>upstairs in <maroon>Lumbridge Castle."
        )
    }
    player.sendQuestJournal("Rune Mysteries", lines)
}
