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
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.random

/**
 * Certer random event: one of the certificate brothers appears beside the player and nags until
 * spoken to. He shows an item and three descriptions (interface 184); picking the description that
 * matches the item earns a roll of the gem/coin reward table, a wrong pick earns nothing. Ignoring
 * him applies the note-and-teleport penalty.
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
        val (item, description) = ITEMS.entries.random(random)
        val answer = (1..3).random(random)
        set("certer_answer", answer)
        val falses = FALSE_OPTIONS.shuffled(random).iterator()
        open("certer_identify")
        for (option in 1..3) {
            interfaces.sendText("certer_identify", "option_$option", if (option == answer) description else falses.next())
        }
        interfaces.sendItem("certer_identify", "item", Item(item))
    }

    private fun Player.nagLines() = listOf(
        "Greetings $name, I need your help.",
        "ehem... Hello $name, please talk to me!",
        "Hello, are you there $name?",
        "It's really rude to ignore someone, $name!",
        "No-one ignores me!",
    )

    companion object {
        private val ITEMS = mapOf(
            "bowl" to "A bowl.",
            "raw_shrimps" to "A fish.",
            "raw_bass" to "A bass.",
            "bronze_sword" to "A sword.",
            "bronze_battleaxe" to "A battleaxe.",
            "bronze_med_helm" to "A helmet.",
            "bronze_kiteshield" to "A kiteshield.",
            "shears" to "A pair of shears.",
            "spade" to "A shovel.",
            "gold_ring" to "A ring.",
            "gold_necklace" to "A necklace.",
        )
        private val FALSE_OPTIONS = listOf(
            "An axe.",
            "An arrow.",
            "A pair of boots.",
            "A pair of gloves.",
            "A staff.",
            "A bow.",
            "A feather.",
        )
    }
}
