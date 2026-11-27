package content.quest.member.waterfall_quest

import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script

class WaterfallQuest : Script {
    init {
        questJournalOpen("waterfall_quest") {
            val lines = mutableListOf<String>()
            val stage = questStage("waterfall_quest")
            if (stage == 0) {
                lines.add("<navy>I can start this quest by speaking to <maroon>Almera<navy> in her house next to")
                lines.add("<navy>the <maroon>Baxtorian Falls<navy>.")
            } else if (stage == 1) {
                lines.add("<navy>I spoke to <maroon>Almera<navy> in a house close to the Baxtorian waterfall. Her")
                lines.add("<navy>son was missing so I offered to help find him. The boy, <maroon>Hudon's<navy>")
                lines.add("<navy>looking for treasure in the waterfall. She said I could use the raft")
                lines.add("<navy>at the back of the house to try and find him.")
            } else if (stage >= 2) {
                lines.add("<str>I spoke to Almera in a house close to the Baxtorian waterfall. Her")
                lines.add("<str>son was missing so I offered to help find him. The boy, Hudon's")
                lines.add("<str>looking for treasure in the waterfall.")
                lines.add("")
            }
            if (stage == 2) {
                lines.add("<navy>I found Hudon a short raft ride down the river. He is convinced")
                lines.add("<navy>there is treasure here somewhere. Maybe I need to do a little")
                lines.add("<navy>research.")
            } else if (stage >= 3) {
                lines.add("<str>I found Hudon a short raft ride down the river. He is convinced")
                lines.add("<str>there is treasure here somewhere. Maybe I need to do a little")
                lines.add("<str>research.")
                lines.add("")
            }
            if (stage >= 3) {
                lines.add("<str>I found a book in the tourist office about Baxtorian. The book tells")
                lines.add("<str>of a sad love story about two elf lovers. It ends with Baxtorian")
                lines.add("<str>withdrawing to his home under the waterfall after his wife dies. It")
                lines.add("<str>is told that only Glarial could enter their home.")
                lines.add("")
            }
            if (stage == 3) {
                lines.add("<navy>The book also mentions <maroon>Glarial's tomb<navy> and a pebble, it appears that")
                lines.add("<navy>the pebble is used to enter the tomb.")
                lines.add("<navy>From what I understand <maroon>Glarial's pebble<navy> was hidden in a cave under")
                lines.add("<navy>the <maroon>Tree Gnome Village<navy> by <maroon>Golrie's<navy> ancestors.")
            } else if (stage == 4) {
                lines.add("<navy>The book also mentions <maroon>Glarial's tomb<navy> and a pebble, it appears that")
                lines.add("<navy>the pebble is used to enter the tomb.")
                lines.add("<str>From what I understand Glarial's pebble was hidden in a cave under")
                lines.add("<str>the Tree Gnome Village by Golrie's ancestors.")
            } else if (stage >= 5) {
                lines.add("<str>The book also mentions Glarial's tomb and a pebble, it appears that")
                lines.add("<str>the pebble is used to enter the tomb. From what I understand")
                lines.add("<str>Glarial's pebble was hidden in a cave under the Tree Gnome Village")
                lines.add("<str>by Golrie's ancestors.")
                lines.add("")
            }
            if (stage == 5) {
                lines.add("<navy>I have managed to enter the tomb of Glarial. There must be")
                lines.add("<navy>something that will help me down here.")
                lines.add("")
            } else if (stage == 6) {
                lines.add("<str>I have managed to enter the tomb of Glarial. There must be")
                lines.add("<str>something that will help me down here.")
                lines.add("")
            } else if (stage in 7..9) {
                lines.add("<str>I have managed to enter the tomb of Glarial. There must be")
                lines.add("<str>something that will help me down here.")
                lines.add("<str>Inside the tomb I found Glarial's amulet and ashes.")
                lines.add("")
            } else if (stage >= 10) {
                lines.add("<str>I have managed to enter the tomb of Glarial. There must be")
                lines.add("<str>something that will help me down here.")
                lines.add("<str>Inside the tomb I found Glarial's amulet.")
                lines.add("")
            }

            if (stage == 5) {
                lines.add("<navy>Inside the tomb I found Glarial's amulet.")
            } else if (stage == 6) {
                lines.add("<navy>Inside the tomb I found Glarial's amulet and ashes. I wonder if I")
                lines.add("<navy>can find my way into Baxtorian's home through the waterfall?")
            } else if (stage == 7) {
                lines.add("<navy>I finally got access to the derelict home of Baxtorian. The door")
                lines.add("<navy>must have been keyed to Glarial's amulet. I should explore the")
                lines.add("<navy>caverns and see what I can find.")
            } else if (stage >= 8) {
                lines.add("<str>I finally got access to the derelict home of Baxtorian. The door")
                lines.add("<str>must have been keyed to Glarial's amulet.")
                lines.add("")
            }

            if (stage == 8) {
                lines.add("<navy>I have found a room containing the <maroon>Chalice of Eternity<navy>. The chalice")
                lines.add("<navy>is suspended in midair just out of reach. There are six pillars")
                lines.add("<navy>surrounding it, perhaps I should look at them more closely.")
                lines.add("")
            } else if (stage >= 9) {
                lines.add("<str>I have found a room containing the Chalice of Eternity. The chalice")
                lines.add("<str>is suspended in midair just out of reach.")
                lines.add("")
            }

            if (stage == 8) {
                lines.add("<navy>Putting runes on the pillars activated some ancient magic. Maybe I")
                lines.add("<navy>should try to finish casting the prehistoric spell. I think those")
                lines.add("<navy>statues at the end of the hall are the key.")
            } else if (stage == 9) {
                lines.add("<str>Putting runes on the pillars activated some ancient magic. Maybe I")
                lines.add("<str>should try to finish casting the prehistoric spell. I think those")
                lines.add("<str>statues at the end of the hall are the key.")
                lines.add("")
            } else if (stage >= 10) {
                lines.add("<str>Putting runes on the pillars activated some ancient magic. Maybe I")
                lines.add("<str>should try to finish casting the prehistoric spell.")
                lines.add("")
            }

            if (stage == 9) {
                lines.add("<navy>By putting Glarial's amulet on her statue I finished a spell that")
                lines.add("<navy>made the floor raise. Now I'm able to get to the chalice.")
            } else if (stage >= 10) {
                lines.add("<str>By putting Glarial's amulet on her statue I finished a spell that")
                lines.add("<str>made the floor raise. Now I'm able to get to the chalice.")
                lines.add("")
            }
            if (stage >= 10) {
                lines.add("<str>Using the Glarial's ashes as a counterweight, I was able to remove")
                lines.add("<str>the treasure that had been left in the chalice.")
                lines.add("")
            }
            if (stage == 10) {
                lines.add("<white>QUEST COMPLETE!")
            }
            questJournal("Waterfall Quest", lines)
        }
    }
}
