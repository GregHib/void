package content.activity.event.random.certer

import content.activity.event.random.RandomEvents
import content.activity.event.random.rewardCerterLoot
import content.activity.event.random.startInPlaceEvent
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

/**
 * Certer random event: one of the certificate brothers appears beside the player and nags until
 * spoken to. He describes an item and shows three item models on interface 184; picking the model
 * that matches his description earns a roll of the gem/coin reward table, a wrong pick earns nothing.
 * Ignoring him applies the note-and-teleport penalty.
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
            npc<Happy>("giles", "Ah, hello, $name. Could you please help me identify this?")
            openPuzzle()
        }

        interfaceOption("Select", "certer_identify:option_*") {
            val correct = it.component == "option_${get("certer_answer", 0)}"
            close("certer_identify")
            if (correct) {
                npc<Happy>("giles", "Thank you, I hope you like your present. I must be leaving now though.")
                rewardCerterLoot()
            } else {
                npc<Neutral>("giles", "Sorry, I don't think so.")
            }
            RandomEvents.completeInPlace(this)
        }
    }

    private fun Player.startEvent() {
        val giles = startInPlaceEvent("giles", nagLines()) ?: return
        giles.anim("emote_bow")
    }

    private fun Player.openPuzzle() {
        val answer = ITEMS.random(random)
        val options = (FALSE_OPTIONS.shuffled(random).take(2) + answer.description).shuffled(random)
        set("certer_answer", options.indexOf(answer.description) + 1)
        open("certer_identify")
        interfaces.sendModel("certer_identify", "item", answer.model) // the item to identify
        for ((index, option) in options.withIndex()) {
            interfaces.sendText("certer_identify", "option_${index + 1}", option)
        }
    }

    private fun Player.nagLines() = listOf(
        "Greetings $name, I need your help.",
        "ehem... Hello $name, please talk to me!",
        "Hello, are you there $name?",
        "It's really rude to ignore someone, $name!",
        "No-one ignores me!",
    )

    private data class Candidate(val description: String, val model: Int)

    companion object {
        // The item model shown in the box and the matching description shown as an option.
        private val ITEMS = listOf(
            Candidate("A bowl?", 2807),
            Candidate("A fish?", 2590),
            Candidate("A bass?", 2355),
            Candidate("A sword?", 2604),
            Candidate("A battleaxe?", 2778),
            Candidate("A helmet?", 2833),
            Candidate("A kiteshield?", 2339),
            Candidate("A pair of shears?", 2620),
            Candidate("A shovel?", 7304),
            Candidate("A ring?", 2784),
            Candidate("A necklace?", 2506),
        )
        private val FALSE_OPTIONS = listOf(
            "An axe?",
            "An arrow?",
            "A pair of boots?",
            "A pair of gloves?",
            "A staff?",
            "A bow?",
            "A feather?",
        )
    }
}
