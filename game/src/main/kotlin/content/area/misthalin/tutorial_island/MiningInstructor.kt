package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class MiningInstructor : Script {

    init {
        npcOperate("Talk-to", "mining_instructor") {
            when (tutorialStage) {
                28 -> {
                    npc<Happy>("Hello there. This is where we teach Mining and Smithing.")
                    npc<Neutral>("Prospect those rocks to find out what ore they hold. Start with the tin, then the copper.")
                    advanceTutorial(28)
                }
                31 -> {
                    npc<Neutral>("Now you know what's in them, you'll want to get it out. Take this pickaxe and mine some tin and some copper.")
                    inventory.add("bronze_pickaxe")
                    item("bronze_pickaxe", "The Mining Instructor gives you a bronze pickaxe.")
                    advanceTutorial(31)
                }
                35 -> {
                    npc<Happy>("A fine bronze bar. Now take this hammer and use the bar on the anvil to smith a dagger.")
                    inventory.add("hammer")
                    item("hammer", "The Mining Instructor gives you a hammer.")
                    advanceTutorial(35)
                }
                else -> {
                    val replaced = when {
                        tutorialStage >= 36 -> resupply("bronze_pickaxe", "hammer")
                        tutorialStage >= 32 -> resupply("bronze_pickaxe")
                        else -> false
                    }
                    if (replaced) {
                        npc<Neutral>("Lost your tools? Here's a replacement.")
                        return@npcOperate
                    }
                    recap()
                }
            }
        }
    }

    /** The stage text tells the player they can ask for a recap at any time. */
    private suspend fun Player.recap() {
        npc<Neutral>("Prospecting a rock tells you which ore it holds. Mining it with a pickaxe gets you the ore itself.")
        npc<Neutral>("Use the ore on the furnace to smelt it into a bar - copper and tin together make bronze.")
        npc<Neutral>("Then use the bar on an anvil while carrying a hammer, and choose what you'd like to smith.")
    }
}
