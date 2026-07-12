package content.activity.event.random.certer

import content.activity.event.random.RandomEvents
import content.activity.event.random.startInPlaceEvent
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

/**
 * Certer random event: one of the certificate brothers appears beside the player and nags until
 * spoken to. He shows an item (interface 184) and three descriptions to choose from; picking the
 * one that matches the item earns a random event gift, a wrong pick earns nothing.
 * Ignoring him applies the note-and-teleport penalty.
 *
 * The interface builds itself from varbits: its load script reads the three option descriptions
 * (keys into enum 2224) and the item model (a key into enum 2225) - so we just set those.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Certer
 */
class Certer : Script {

    init {
        RandomEvents.register("certer") { startEvent() }

        npcOperate("Talk-to", "giles") { (giles) ->
            if (giles.owner != this) {
                message("They aren't interested in talking to you.")
                return@npcOperate
            }
            npc<Happy>("Ah, hello, $name. Could you please help me identify this?")
            openPuzzle()
        }

        interfaceOption("Select", "certer_identify:option_*") {
            val correct = it.component.removePrefix("option_").toInt() == get("certer_answer", 0)
            close("certer_identify")
            val giles = NPCs.findOrNull(tile.regionLevel) { npc -> npc.id == "giles" && npc.owner == this }
            if (giles != null) {
                talkWith(giles)
                if (correct) {
                    npc<Happy>("Thank you, I hope you like your present. I must be leaving now though.")
                } else {
                    npc<Neutral>("Sorry, I don't think so.")
                }
            }
            if (correct) {
                addOrDrop("random_event_gift")
            }
            RandomEvents.completeInPlace(this)
        }
    }

    private fun Player.startEvent() {
        val giles = startInPlaceEvent("giles", nagLines())
        giles.anim("emote_bow")
    }

    private fun Player.openPuzzle() {
        val answer = (0 until ITEMS).random(random)
        val options = ((0 until ITEMS).filter { it != answer }.shuffled(random).take(2) + answer).shuffled(random)
        set("certer_answer", options.indexOf(answer) + 1)
        set("certer_desc_1", options[0])
        set("certer_desc_2", options[1])
        set("certer_desc_3", options[2])
        // Show the model matching the answer's description (enum 2225: model key = (desc + 2) % 9).
        set("certer_model_enum", 0)
        set("certer_model_key", (answer + 2) % ITEMS)
        // Present the model upright at a sensible size.
        set("certer_rotation_x", 5)
        set("certer_rotation_y", 4)
        set("certer_scale", 10)
        set("certer_spin", 0)
        open("certer_identify")
    }

    private fun Player.nagLines() = listOf(
        "Greetings $name, I need your help.",
        "ehem... Hello $name, please talk to me!",
        "Hello, are you there $name?",
        "It's really rude to ignore someone, $name!",
        "No-one ignores me!",
    )

    companion object {
        private const val ITEMS = 9 // enum 2224 holds nine item descriptions (keys 0-8)
    }
}
