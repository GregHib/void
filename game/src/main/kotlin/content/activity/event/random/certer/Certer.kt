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
        val options = ITEMS.shuffled(random).take(3)
        val answer = options.random(random)
        set("certer_answer", options.indexOf(answer) + 1)
        open("certer_identify")
        interfaces.sendText("certer_identify", "prompt", "Which of these is ${answer.description}?")
        for ((index, option) in options.withIndex()) {
            interfaces.sendModel("certer_identify", "option_${index + 1}", option.model)
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
        // The description the certer reads out and the item model shown as an option.
        private val ITEMS = listOf(
            Candidate("a bowl", 2807),
            Candidate("a fish", 2590),
            Candidate("a bass", 2355),
            Candidate("a sword", 2604),
            Candidate("a battleaxe", 2778),
            Candidate("a helmet", 2833),
            Candidate("a kiteshield", 2339),
            Candidate("a pair of shears", 2620),
            Candidate("a shovel", 7304),
            Candidate("a ring", 2784),
            Candidate("a necklace", 2506),
        )
    }
}
