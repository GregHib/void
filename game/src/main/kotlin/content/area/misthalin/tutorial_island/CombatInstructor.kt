package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class CombatInstructor : Script {

    init {
        npcOperate("Talk-to", "combat_instructor") {
            when (tutorialStage) {
                39 -> {
                    npc<Happy>("Hello there! Ready to learn how to fight?")
                    npc<Neutral>("First you'll need to wield a weapon. Open your worn equipment tab and take a look at what you're carrying.")
                    advanceTutorial(39)
                }
                43 -> {
                    npc<Neutral>("Good. A dagger is quick, but a sword hits harder and a shield keeps you alive. Take these and equip them both.")
                    inventory.add("bronze_sword")
                    inventory.add("wooden_shield")
                    item("bronze_sword", "The Combat Instructor gives you a bronze sword and a wooden shield.")
                    advanceTutorial(43)
                }
                49 -> {
                    npc<Happy>("Nicely done. Not every fight should be up close, though.")
                    npc<Neutral>("Take this shortbow and these arrows, then kill another rat from a distance.")
                    inventory.add("shortbow")
                    inventory.add("bronze_arrow", 50)
                    item("shortbow", "The Combat Instructor gives you a shortbow and some arrows.")
                    advanceTutorial(49)
                }
                else -> {
                    val replaced = when {
                        tutorialStage >= 49 -> resupply("shortbow") or resupply("bronze_arrow", 50)
                        tutorialStage >= 43 -> resupply("bronze_sword", "wooden_shield")
                        tutorialStage >= 41 -> resupply("bronze_dagger")
                        else -> false
                    }
                    if (replaced) {
                        npc<Neutral>("You'll not get far unarmed. Take these.")
                        return@npcOperate
                    }
                    npc<Neutral>("Keep practising. The rats in the cage won't hurt you.")
                }
            }
        }
    }
}
