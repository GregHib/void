package content.area.misthalin.lumbridge


import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.suspend.SuspendableContext
import content.entity.player.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.refreshQuestJournal
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*

npcOperate("Talk-to", "duke_horacio") {
    player["hail_to_the_duke_baby_task"] = true
    npc<Neutral>("Greetings. Welcome to my castle.")
    when (player.quest("rune_mysteries")) {
        "unstarted" -> unstarted()
        "started" -> started()
        else -> completed()
    }
}

suspend fun SuspendableContext<Player>.started() {
    choice {
        option<Quiz>("What did you want me to do again?") {
            if (player.ownsItem("air_talisman")) {
                npc<Neutral>("Take that talisman I gave you to Sedridor at the Wizards' Tower. You'll find it south west of here, across the bridge from Draynor Village.")
                player<Happy>("Okay, will do.")
                return@option
            }
            npc<Quiz>("Did you take that talisman to Sedridor?")
            player<Sad>("No, I lost it.")
            npc<Neutral>("Ah, well that explains things. One of my servants found it outside, and it seemed too much of a coincidence that another would suddenly show up.")
            if (player.inventory.isFull()) {
                item("air_talisman", 600, "The Duke tries to hand you the talisman, but you don't have enough room to take it.")
                return@option
            }
            npc<Neutral>("Here, take it to the Wizards' Tower, south west of here. Please try not to lose it this time.")
            player.inventory.add("air_talisman")
            item("air_talisman", 600, "The Duke hands you the talisman.")
        }
        findMoney()
    }
}

suspend fun SuspendableContext<Player>.unstarted() {
    choice {
        option<Quiz>("Have you any quests for me?") {
            npc<Uncertain>("Well, I wouldn't describe it as a quest, but there is something I could use some help with.")
            player<Quiz>("What is it?")
            npc<Neutral>("We were recently sorting through some of the things stored down in the cellar, and we found this old talisman.")
            item("air_talisman", 600, "The Duke shows you a talisman.")
            npc<Neutral>("The Order of Wizards over at the Wizards' Tower have been on the hunt for magical artefacts recently. I wonder if this might be just the kind of thing they're after.")
            npc<Quiz>("Would you be willing to take it to them for me?")
            startQuest()
        }
        findMoney()
    }
}

suspend fun SuspendableContext<Player>.completed() {
    choice {
        option<Quiz>("Have you any quests for me?") {
            npc<Neutral>("The only job I had was the delivery of that talisman, so I'm afraid not.")
        }
        findMoney()
    }
}

suspend fun PlayerChoice.findMoney() : Unit = option<Quiz>("Where can I find money?") {
    npc<Neutral>("I've heard that the blacksmiths are prosperous amongst the peasantry. Maybe you could try your hand at that?")
}

suspend fun SuspendableContext<Player>.startQuest() {
    choice("Start the Rune Mysteries quest?") {
        option<Happy>("Sure, no problem.") {
            if (player.inventory.isFull()) {
                item("air_talisman", 600, "The Duke tries to hand you the talisman, but you don't have enough room to take it.")
                return@option
            }
            player["rune_mysteries"] = "started"
            player.inventory.add("air_talisman")
            npc<Neutral>("Thank you very much. You'll find the Wizards' Tower south west of here, across the bridge from Draynor Village. When you arrive, look for Sedridor. He is the Archmage of the wizards there.")
            player.refreshQuestJournal()
            item("air_talisman", 600, "The Duke hands you the talisman.")
        }
        option<Neutral>("Not right now.") {
            npc<Sad>("As you wish. Hopefully I can find someone else to help.")
        }
    }
}