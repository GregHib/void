package content.activity.event.random.sandwich_lady

import content.activity.event.random.RandomEvents
import content.activity.event.random.startInPlaceEvent
import content.entity.combat.hit.directHit
import content.entity.combat.inCombat
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

/**
 * Sandwich Lady random event: she appears beside the player pushing a refreshment tray and nags
 * until spoken to. Talking opens her tray (interface 297); picking the food she offered hands it
 * over, picking any other knocks the player out (notes their items and teleports them away).
 * https://runescape.wiki/w/Random_events?oldid=3667851#Sandwich_Lady
 */
class SandwichLady : Script {

    init {
        RandomEvents.register("sandwich_lady") { startEvent() }

        npcOperate("Talk-to", "sandwich_lady") { (lady) ->
            if (lady.owner != this) {
                npc<Happy>("sandwich_lady", "This is for ${lady.owner?.name ?: "someone else"}, not you!")
                return@npcOperate
            }
            val food = get<String>("sandwich_lady_food") ?: return@npcOperate
            if (inCombat) {
                // No time for the tray mid-fight; she just hands over the right one.
                serve(food)
                return@npcOperate
            }
            npc<Happy>("sandwich_lady", "You look hungry to me. I tell you what - have a ${description(food)} on me.")
            interfaces.sendText("sandwich_lady_select", "title", "Have a ${description(food)} for free!")
            open("sandwich_lady_select")
        }

        interfaceOption("Choose refreshment", "sandwich_lady_select:*") {
            val food = get<String>("sandwich_lady_food") ?: return@interfaceOption
            val lady = lady()
            close("sandwich_lady_select")
            if (it.component == food) {
                serve(food)
            } else {
                knockOut(lady)
            }
        }
    }

    private fun Player.startEvent() {
        val food = FOODS.random(random)
        set("sandwich_lady_food", food)
        val lady = startInPlaceEvent("sandwich_lady", nagLines(), nagInterval = 30) ?: return
        set("sandwich_lady_npc", lady.index)
        lady.say("Sandwich delivery for $name!")
    }

    private suspend fun Player.knockOut(lady: NPC?) {
        message("The sandwich lady knocks you out and you wake up somewhere... different.")
        lady?.say("Hey, I didn't say you could have that!")
        lady?.anim("sandwich_lady_knockout") // swings the baguette
        lady?.let { directHit(it, 3) }
        anim("human_death")
        open("fade_out")
        delay(3)
        RandomEvents.noteAndTeleport(this)
        clearAnim()
        open("fade_in")
    }

    private fun Player.lady(): NPC? = NPCs.indexed(get("sandwich_lady_npc", -1))?.takeIf { it.id == "sandwich_lady" }

    private suspend fun Player.serve(food: String) {
        message("The sandwich lady gives you a ${description(food)}!")
        addOrDrop(food)
        npc<Happy>("sandwich_lady", "Hope that fills you up!")
        // Clearing the event state removes the following NPC on its next tick.
        RandomEvents.completeInPlace(this)
    }

    private fun Player.nagLines() = listOf(
        "All types of sandwiches, $name.",
        "Come on $name, I made these specifically!",
        "You better start showing some manners!",
        "You think I made these just for fun?!!?",
    )

    private fun description(food: String) = food.replace('_', ' ')

    companion object {
        private val FOODS = listOf(
            "baguette",
            "triangle_sandwich",
            "square_sandwich",
            "roll",
            "meat_pie",
            "doughnut",
            "chocolate_bar",
        )
    }
}
