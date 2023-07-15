package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.variable.VariableSet
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop

val quests = setOf(
    // free
    "cooks_assistant",
    "demon_slayer",
    "dorics_quest",
    "the_knights_sword",
    "rune_mysteries"
    // members
)

on<InterfaceOpened>({ id == "quest_journals" }) { player: Player ->
    player.interfaceOptions.unlock(id, "journals", 0 until 201, "View")
    player.sendVariable("quest_points")
    player.sendVariable("quest_points_total") //set total quest points available in variables.yml
    player.sendVariable("unstable_foundations")
    for (quest in quests) {
        player.sendVariable(quest)
    }
}

on<VariableSet>({ quests.contains(key) }) { player: Player ->
    player.softTimers.start("refresh_quest_journal")
}

on<TimerStart>({ timer == "refresh_quest_journal" }) { _: Player ->
    interval = 1
}

on<TimerStop>({ timer == "refresh_quest_journal" }) { player: Player ->
    player.refreshQuestJournal()
}

on<Registered> { player: Player ->
    player.clearCamera()
}