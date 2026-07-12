package content.activity.event.random.freaky_forester

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.rewardCostumePoint
import content.entity.combat.killer
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Freaky Forester random event: the player is whisked to the forester's clearing and told to kill
 * a pheasant with a specific number of tails. Only the assigned pheasant yields the correct raw
 * pheasant; handing it back rewards a random event gift.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Freaky_Forester
 */
class FreakyForester : Script {

    init {
        RandomEvents.register("freaky_forester") { startEvent() }

        npcOperate("Talk-to", "freaky_forester") {
            if (get<String>("random_event") != "freaky_forester") {
                message("The forester is too busy to talk to you.")
                return@npcOperate
            }
            foresterDialogue()
        }

        // One pheasant per trip: don't let the player farm the clearing.
        canAttack("pheasant_*") {
            if (carriesRawPheasant()) {
                message("You don't need to attack any more pheasants.")
                false
            } else {
                true
            }
        }

        npcDeath("pheasant_*") { death ->
            val killer = killer as? Player ?: return@npcDeath
            if (killer.get<String>("random_event") != "freaky_forester") {
                return@npcDeath
            }
            death.dropItems = false
            // The pheasant drops the raw bird for the killer to pick up and hand to the forester.
            val correct = tailCount(id) == killer.get("freaky_forester_task", 0)
            val bird = if (correct) "raw_pheasant" else "raw_pheasant_incorrect"
            FloorItems.add(tile, bird, revealTicks = FloorItems.NEVER, disappearTicks = 300, owner = killer)
        }
    }

    private suspend fun Player.startEvent() {
        if (!contains("freaky_forester_task")) {
            // Keep the assigned pheasant on a relog resume.
            set("freaky_forester_task", random.nextInt(1, TAILS + 1))
        }
        kidnap(CLEARING)
        talkWith(NPCs.find(tile.regionLevel, "freaky_forester"))
        giveTask()
    }

    private suspend fun Player.foresterDialogue() {
        when {
            inventory.contains("raw_pheasant") -> {
                // Dialogue first: walking off cancels the handler, so only take the bird
                // once the suspending lines are done and the reward is guaranteed.
                npc<Happy>("Thanks, $name, you may leave the area now.")
                reward()
                inventory.remove("raw_pheasant")
                clear("freaky_forester_task")
                anim("teleport_modern")
                sound("teleport")
                gfx("teleport_modern")
                delay(3)
                RandomEvents.complete(this)
                anim("teleport_land_modern")
                gfx("teleport_land_modern")
                sound("teleport_land")
            }
            inventory.contains("raw_pheasant_incorrect") -> {
                npc<Neutral>("That's not the right one.")
                inventory.remove("raw_pheasant_incorrect")
            }
            else -> giveTask()
        }
    }

    private suspend fun Player.giveTask() {
        val tails = get("freaky_forester_task", 1)
        npc<Neutral>(
            "Hey there $name. Can you kill the ${TAIL_WORDS[tails]} tailed pheasant please. " +
                "Bring me the raw pheasant when you're done.",
        )
    }

    private suspend fun Player.reward() {
        npc<Happy>("Please take this gift as a reward for your help, many thanks!")
        addOrDrop("random_event_gift")
        rewardCostumePoint("lederhosen")
    }

    private fun Player.carriesRawPheasant() = inventory.contains("raw_pheasant") || inventory.contains("raw_pheasant_incorrect")

    private fun tailCount(pheasantId: String) = pheasantId.removePrefix("pheasant_").substringBefore("_").toIntOrNull() ?: 0

    companion object {
        private const val TAILS = 4
        private val TAIL_WORDS = arrayOf("", "one", "two", "three", "four")
        private val CLEARING = Tile(2601, 4777)
    }
}
