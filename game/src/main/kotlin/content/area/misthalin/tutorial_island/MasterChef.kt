package content.area.misthalin.tutorial_island

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem

class MasterChef : Script {

    init {
        npcOperate("Talk-to", "master_chef") {
            when (tutorialStage) {
                15 -> {
                    npc<Happy>("Hello there! I'm the cook. There's more to cooking than throwing a fish on a fire, you know.")
                    npc<Neutral>("Take this pot of flour and bucket of water. Use them together to make dough, then cook the dough on my range.")
                    giveIngredients()
                    advanceTutorial(15)
                }
                16, 17 -> replaceIngredients()
                else -> npc<Neutral>("Use the flour with the water to make dough, then cook it on my range.")
            }
        }
    }

    /**
     * Burning the bread consumes the dough, so the chef has to hand out more - the stage text
     * tells the player he will.
     */
    private suspend fun Player.replaceIngredients() {
        if (carriesItem("bread_dough")) {
            npc<Neutral>("You've got your dough. Cook it on the range over there.")
            return
        }
        npc<Happy>("Lost your ingredients? Not to worry, here's some more.")
        giveIngredients()
    }

    private suspend fun Player.giveIngredients() {
        resupply("pot_of_flour", "bucket_of_water")
        item("pot_of_flour", "The cook gives you a pot of flour and a bucket of water.")
    }
}
