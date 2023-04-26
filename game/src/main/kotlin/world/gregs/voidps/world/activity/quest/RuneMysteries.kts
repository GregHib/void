package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.ChatBlue
import world.gregs.voidps.engine.client.ui.chat.ChatRed
import world.gregs.voidps.engine.client.ui.chat.Red
import world.gregs.voidps.engine.client.ui.chat.Strike
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on



on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 13}) { player: Player ->
    val lines = when (player.get("rune_mysteries", "unstarted")) {
        "completed" -> listOf(
                Strike { "I spoke to Duke Horacio in Lumbridge Castle. He told me" },
                Strike { "that he'd found a Strange Talisman in the Castle which" },
                Strike { "might be of use to the Order of Wizards at the Wizards'" },
                Strike { "Tower. He asked me to take it there and give it to a wizard" },
                Strike { "called Sedridor." },
                Strike { "I delivered the Strange Talisman to Sedridor in the" },
                Strike { "Wizards' Tower. He believed it might be key to discovering" },
                Strike { "a Teleportation Incantation to the lost Rune Essence Mine." },
                Strike { "He asked me to help confirm this by delivering a Package" },
                Strike { "to Aubury, an expert on Runecrafting." },
                Strike { "I delivered the Package to Aubury in Varrock. He confirmed" },
                Strike { "Sedridor's suspicions and asked me to take some" },
                Strike { "Research Notes back to him. I did so, and Sedridor used" },
                Strike { "them to discover the Teleportation Incantation to the lost" },
                Strike { "Rune Essence Mine. As a thank you for my help, he" },
                Strike { "granted me permission to use the Rune Essence Mine" },
                Strike { "whenever I please." },
                "",
                Red { "QUEST COMPLETE!" }
        )
        "started" -> {
            val list = mutableListOf(
                ChatBlue { "I spoke to  ${ChatRed { "Duke Horacio" }} ${ChatBlue { "in" }} ${ChatRed { "Lumbridge Castle. " }} ${ChatBlue { "He told me" }}" },
                ChatBlue { "that he'd found a ${ChatRed { "Strange Talisman" }} ${ChatBlue { "in the" }} ${ChatRed { "Castle" }} ${ChatBlue { "which" }}" },
                ChatBlue { "might be of use to the ${ChatRed { "Order of Wizards" }} ${ChatBlue { "at the" }} ${ChatRed { "Wizards'" }}" },
                ChatRed { "Tower. ${ChatBlue { " He asked me to take it there and give it to a wizard" }}" },
                ChatBlue { "called ${ChatRed { "Sedridor." }} ${ChatBlue { " I can find the" }} ${ChatRed { "Wizards' Tower" }} ${ChatBlue { "south west of" }}" },
                ChatRed { "Lumbridge${ChatBlue { ", across the bridge from " }} ${ChatRed { "Draynor Village." }} " }
            )

            if (!player.hasItem("air_talisman")) {
                list.add(ChatBlue { "If I lose the ${ChatRed { "Strange Talisman" }} ${ChatBlue { ", I'll need to ask" }} ${ChatRed { "Duke Horacio" }} ${ChatBlue { "for" }}" })
                list.add(ChatBlue { "another." })
            }
            list
        }
        "stage2" -> listOf(
            Strike { "I spoke to Duke Horacio in Lumbridge Castle. He told me" },
            Strike { "that he'd found a Strange Talisman in the Castle which" },
            Strike { "might be of use to the Order of Wizards at the Wizards'" },
            Strike { "Tower. He asked me to take it there and give it to a wizard" },
            Strike { "called Sedridor." },
            ChatBlue { "I delivered the ${ChatRed { "Strange Talisman" }} ${ChatBlue { "to" }} ${ChatRed { "Sedridor" }} ${ChatBlue { " in the" }}" },
            ChatBlue { "basement of the ${ChatRed { "Wizards' Tower." }} ${ChatBlue { " I should see what he can" }}" },
            ChatBlue { "tell me about it." }
        )
        "stage3" -> {
            val list = mutableListOf(
                Strike { "I spoke to Duke Horacio in Lumbridge Castle. He told me" },
                Strike { "that he'd found a Strange Talisman in the Castle which" },
                Strike { "might be of use to the Order of Wizards at the Wizards'" },
                Strike { "Tower. He asked me to take it there and give it to a wizard" },
                Strike { "called Sedridor." },
                ChatBlue { "I delivered the ${ChatRed { "Strange Talisman" }} ${ChatBlue { "to" }} ${ChatRed { "Sedridor" }} ${ChatBlue { "in the" }}" },
                ChatBlue { "basement of the ${ChatRed { "Wizards' Tower." }} ${ChatBlue { " He believes it might be" }}" },
                ChatBlue { "key to discovering a ${ChatRed { "Teleportation Incantation" }} ${ChatBlue { " to the lost" }}" },
                ChatRed { "Rune Essence Mine.${ChatBlue { " He asked me to help confirm this by" }}" },
                ChatBlue { "delivering a ${ChatRed { "Package" }} ${ChatBlue { " to" }} ${ChatRed { "Aubury" }} ${ChatBlue { ", an expert on" }}" },
                ChatRed { "Runecrafting.${ChatBlue { " I can find him in his" }} ${ChatRed { "Rune Shop" }} ${ChatBlue { " in south east" }}" },
                ChatRed { "Varrock." }
            )
            if (!player.hasItem("research_package_rune_mysteries")) {
                list.add(ChatBlue { "If I lose the ${ChatRed { "Package" }} ${ChatBlue { ", I'll need to ask" }} ${ChatRed { "Sedridor" }} ${ChatBlue { "for" }}" })
                list.add(ChatBlue { "another." })
            }
            list
        }
        "stage4" -> listOf(
            Strike { "I spoke to Duke Horacio in Lumbridge Castle. He told me" },
            Strike { "that he'd found a Strange Talisman in the Castle which" },
            Strike { "might be of use to the Order of Wizards at the Wizards'" },
            Strike { "Tower. He asked me to take it there and give it to a wizard" },
            Strike { "called Sedridor." },
            Strike { "I delivered the Strange Talisman to Sedridor in the" },
            Strike { "Wizards' Tower. He believed it might be key to discovering" },
            Strike { "a Teleportation Incantation to the lost Rune Essence Mine." },
            Strike { "He asked me to help confirm this by delivering a Package" },
            Strike { "to Aubury, an expert on Runecrafting." },
            ChatBlue { "I delivered the ${ChatRed { "Package" }} ${ChatBlue{ "to" }} ${ChatRed { "Aubury" }} ${ChatBlue{ "at his" }} ${ChatRed { "Rune Shop" }} ${ChatBlue{ "in" }}" },
            ChatBlue { "south east ${ChatRed { "Varrock" }} ${ChatBlue{ ". I should see what he can tell me about" }}" },
            ChatBlue { "the  ${ChatRed { "Teleportation Incantation." }}" },
        )
        "stage5" -> {
            val list = mutableListOf(
                Strike { "I spoke to Duke Horacio in Lumbridge Castle. He told me" },
                Strike { "that he'd found a Strange Talisman in the Castle which" },
                Strike { "might be of use to the Order of Wizards at the Wizards'" },
                Strike { "Tower. He asked me to take it there and give it to a wizard" },
                Strike { "called Sedridor." },
                Strike { "I delivered the Strange Talisman to Sedridor in the" },
                Strike { "Wizards' Tower. He believed it might be key to discovering" },
                Strike { "a Teleportation Incantation to the lost Rune Essence Mine." },
                Strike { "He asked me to help confirm this by delivering a Package" },
                Strike { "to Aubury, an expert on Runecrafting." },
                ChatBlue { "I delivered the ${ChatRed { "Package" }} ${ChatBlue { "to" }} ${ChatRed { "Aubury" }} ${ChatBlue { "at his" }} ${ChatRed { "Rune Shop" }} ${ChatBlue { "in" }}" },
                ChatBlue { "south east ${ChatRed { "Varrock" }} ${ChatBlue { ". He confirmed" }} ${ChatRed { "Sedridor's" }} ${ChatBlue { "suspicions" }}" },
                ChatBlue { "and asked me to take some ${ChatRed { "Research Notes" }} ${ChatBlue { ". back to him. I" }}" },
                ChatBlue { "can find ${ChatRed { "Sedridor" }} ${ChatBlue { "in the basement of the" }} ${ChatRed { "Wizards' Tower" }}." }
            )
            if (!player.hasItem("research_notes_rune_mysteries")) {
                list.add(ChatBlue { "If I lose the ${ChatRed { "Research Notes" }} ${ChatBlue { " I'll need to ask" }} ${ChatRed { "Aubury" }} ${ChatBlue { "for" }}" })
                list.add(ChatBlue { "some more." })
            }
            list
        }
        else -> listOf(
            ChatBlue { "I can start this quest by speaking to ${ChatRed { "Duke Horacio of" }}" },
            ChatRed { "Lumbridge ${ChatBlue{ "upstairs in" }} ${ChatRed { "Lumbridge Castle." }}" }
        )
    }
    player.sendQuestJournal("Rune Mysteries", lines)
}
