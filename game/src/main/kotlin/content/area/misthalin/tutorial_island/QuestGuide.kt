package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class QuestGuide : Script {

    init {
        npcOperate("Talk-to", "quest_guide") {
            when (tutorialStage) {
                24 -> {
                    npc<Happy>("Greetings, traveller. I am the Quest Guide, and I'm here to tell you about quests.")
                    npc<Neutral>("Click on the flashing question mark icon to open your quest journal. It lists every quest, and how far through each one you are.")
                    advanceTutorial(24)
                }
                26 -> {
                    npc<Neutral>("Quests are set by people all over the world. Completing them earns you rewards and quest points.")
                    npc<Happy>("That's everything from me. Climb down the ladder to meet the Mining Instructor.")
                    advanceTutorial(26)
                }
                else -> npc<Neutral>("Check your quest journal whenever you want to know what to do next.")
            }
        }
    }
}
