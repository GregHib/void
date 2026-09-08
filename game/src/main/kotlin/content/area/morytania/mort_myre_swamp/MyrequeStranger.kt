package content.area.morytania.mort_myre_swamp

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.member.myreque.myrequeStage
import content.quest.questComplete
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.AuditLog

class MyrequeStranger : Script {
    init {
        npcOperate("Talk-to", "stranger_3") {
            val progress = questStage("in_search_of_the_myreque")
            if (progress !in myrequeStage("delivered_weapons")..myrequeStage("escaped_to_canifis")) {
                return@npcOperate npc<Confused>("I think we've been through this before haven't we?")
            }
            npc<Quiz>("Hello friend? What can I do for you?")
            player<Neutral>("Oh sorry, I thought you were someone else.")
            if (progress != myrequeStage("escaped_to_canifis")) {
                return@npcOperate
            }
            npc<Quiz>("Who did you think I was?")
            player<Angry>("I thought you were that dirty murderer Vanstrom.")
            npc<Shock>("Woah, it sounds like you've got a score to settle!")
            player<Angry>("I definitely have!")
            completeQuest()
        }
    }

    private fun Player.completeQuest() {
        jingle("quest_complete_1")
        exp(Skill.Attack, 600.0)
        exp(Skill.Defence, 600.0)
        exp(Skill.Strength, 600.0)
        exp(Skill.Constitution, 600.0)
        exp(Skill.Crafting, 600.0)
        inc("quest_points", 2)
        AuditLog.event(this, "quest_completed", "in_search_of_the_myreque")
        set("in_search_of_the_myreque", "completed")
        refreshQuestJournal()
        questComplete(
            "In Search of the Myreque",
            "2 Quest Points",
            "600 Attack XP",
            "600 Defence XP",
            "600 Strength XP",
            "600 Constitution XP",
            "600 Crafting XP",
            "A quick route to Mort'ton",
            item = "steel_sword",
        )
    }
}
