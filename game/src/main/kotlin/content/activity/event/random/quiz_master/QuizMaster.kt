package content.activity.event.random.quiz_master

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Goofy
import content.entity.player.dialogue.Hysterics
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Quiz Master random event: the Quiz Master whisks the player to his studio for a game of
 * "Odd One Out". Three golden models are shown on interface 191; the player picks the one that
 * doesn't belong with the other two. Four correct answers win a choice of 1000 coins or a random
 * item. Wrong answers cost nothing but a turn.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Quiz_Master
 */
class QuizMaster : Script {

    init {
        RandomEvents.register("quiz_master") { startEvent() }

        interfaceOption("Select", "dialogue_macro_quiz_show:model_*") {
            if (get<String>("random_event") != "quiz_master") {
                return@interfaceOption
            }
            answer(it.component.removePrefix("model_").toInt())
        }
    }

    private suspend fun Player.startEvent() {
        set("quiz_correct", 0)
        quizHerald()
        kidnap(ROOM)
        face(QUIZ_MASTER)
        intro()
        openQuestion()
    }

    private suspend fun Player.quizHerald() {
        val herald = NPCs.addRandom("quiz_master", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("quiz_master", tile, ticks = 25, owner = this)
        herald.watch(this)
        herald.say("Hey $name! It's your lucky day!")
        delay(2)
    }

    private suspend fun Player.intro() {
        npc<Hysterics>("quiz_master", "WELCOME to the GREATEST QUIZ SHOW in the whole of RuneScape: <col=8A0808>O D D</col> <col=8A088A>O N E</col> <col=08088A>O U T</col>")
        player<Confused>("I'm sure I didn't ask to take part in a quiz show...")
        npc<Goofy>("quiz_master", "Please welcome our newest contestant: <col=FF0000>$name</col>! Just pick the O D D  O N E  O U T. Four questions right, and then you win!")
    }

    private fun Player.openQuestion() {
        val set = SETS.random(random)
        val answer = set[0]
        val models = set.toList().shuffled(random)
        set("quiz_answer", models.indexOf(answer) + 1) // 1-based slot of the odd one out
        interfaces.sendModel("dialogue_macro_quiz_show", "model_1", models[0])
        interfaces.sendModel("dialogue_macro_quiz_show", "model_2", models[1])
        interfaces.sendModel("dialogue_macro_quiz_show", "model_3", models[2])
        open("dialogue_macro_quiz_show")
        // The cache marks these models non-clickable, so send the access mask that lets the client
        // report a click on each one; without it the interface just sits there with no way to answer.
        for (slot in 1..3) {
            interfaceOptions.unlockAll("dialogue_macro_quiz_show", "model_$slot")
        }
    }

    private suspend fun Player.answer(slot: Int) {
        close("dialogue_macro_quiz_show")
        if (slot == get("quiz_answer", 0)) {
            if (inc("quiz_correct") >= REQUIRED) {
                win()
                return
            }
            npc<Goofy>("quiz_master", "Wow, you're a smart one! You're absolutely RIGHT! Okay, next question!")
        } else {
            npc<Hysterics>("quiz_master", "WRONG! That's just WRONG! Okay, next question!")
        }
        openQuestion()
    }

    private suspend fun Player.win() {
        npc<Hysterics>("quiz_master", "<col=08088A>CONGRATULATIONS!</col> You are a <col=8A0808>WINNER</col>! Please choose your <col=08088A>PRIZE</col>!")
        choice {
            option("1000 Coins") { giveOrDrop("coins", 1000) }
            option("Random Item") {
                val row = Tables.get("random_event_quiz").rows().random(random)
                giveOrDrop(row.item("item"), row.int("amount"))
            }
        }
        clear("quiz_answer")
        clear("quiz_correct")
        RandomEvents.complete(this)
        message("Welcome back.")
    }

    private fun Player.giveOrDrop(item: String, amount: Int) {
        if (!inventory.add(item, amount)) {
            FloorItems.add(tile, item, amount, disappearTicks = 300, owner = this)
        }
    }

    companion object {
        private const val REQUIRED = 4
        private val ROOM = Tile(1952, 4766, 1)
        private val QUIZ_MASTER = Tile(1952, 4768, 1)

        // Golden models (8828-8837) grouped so the first of each triple is the odd one out.
        // 28 battleaxe, 29 salmon, 30 trout, 31 necklace, 32 shield, 33 helm, 34 ring,
        // 35 secateurs, 36 sword, 37 trowel.
        private val SETS = listOf(
            intArrayOf(8828, 8829, 8829), // weapon vs two fish
            intArrayOf(8831, 8837, 8835), // jewellery vs two gardening tools
            intArrayOf(8830, 8832, 8833), // fish vs two pieces of armour
            intArrayOf(8835, 8834, 8831), // gardening tool vs two pieces of jewellery
            intArrayOf(8837, 8836, 8828), // gardening tool vs two weapons
        )
    }
}
