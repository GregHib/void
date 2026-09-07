package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
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
                    recap()
                }
            }
        }
    }

    /** The stage text tells the player they can ask for a recap at any time. */
    private suspend fun Player.recap() {
        npc<Neutral>("Chop a tree with your hatchet for logs, then use your tinderbox on them to light a fire.")
        npc<Neutral>("Use your net on a fishing spot to catch shrimp, and cook them on a fire once you've caught some.")
        npc<Neutral>("Your skills panel shows how much experience each of those has earned you.")
    }
}
