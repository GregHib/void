package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class SurvivalExpert : Script {

    init {
        npcOperate("Talk-to", "survival_expert") {
            when (tutorialStage) {
                4 -> {
                    npc<Happy>("Hello there! I'm here to teach you how to survive out in the wilds.")
                    npc<Neutral>("Take this hatchet and tinderbox. Chop down one of these trees, then use the tinderbox on the logs to light a fire.")
                    inventory.add("bronze_hatchet")
                    inventory.add("tinderbox")
                    item("bronze_hatchet", "The Survival Expert gives you a bronze hatchet and a tinderbox.")
                    advanceTutorial(4)
                }
                9 -> {
                    npc<Happy>("Well done! Now let's try some fishing.")
                    npc<Neutral>("Take this small fishing net and use it on the fishing spots in the pond. Then cook what you catch on your fire.")
                    inventory.add("small_fishing_net")
                    item("small_fishing_net", "The Survival Expert gives you a small fishing net.")
                    advanceTutorial(9)
                }
                else -> {
                    val replaced = if (tutorialStage < 9) resupply("bronze_hatchet", "tinderbox") else resupply("small_fishing_net")
                    if (replaced) {
                        npc<Happy>("Lost your equipment? Here, take another.")
                        return@npcOperate
                    }
                    npc<Neutral>("Keep going, you're doing well. Just follow the instructions on your screen.")
                }
            }
        }
    }
}
