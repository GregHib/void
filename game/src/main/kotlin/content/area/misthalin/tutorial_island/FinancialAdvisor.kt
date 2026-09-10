package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class FinancialAdvisor : Script {

    init {
        npcOperate("Talk-to", "financial_advisor") {
            when (tutorialStage) {
                54 -> {
                    npc<Happy>("Hello, and welcome to the bank of the future!")
                    npc<Neutral>("Your money pouch carries your coins for you, so you'll never need to make room for them in your backpack.")
                    npc<Neutral>("Head through the next door to meet Brother Brace.")
                    advanceTutorial(54)
                }
                else -> npc<Neutral>("Look after your coins and they'll look after you.")
            }
        }
    }
}
