package content.activity.event.random.surprise_exam

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Surprise Exam random event: the Mysterious Old Man teleports the player into Mr Mordaut's classroom.
 * Talking to Mordaut opens a "what comes next?" pattern quiz (interface 103) - pick the item that
 * belongs with the three shown. Three correct answers pass the exam; Mordaut then names one of four
 * coloured doors, and leaving through it returns the player with a Book of Knowledge (an XP lamp).
 * https://runescape.wiki/w/Random_events?oldid=3667851#Surprise_Exam
 */
class SurpriseExam : Script {

    init {
        RandomEvents.register("surprise_exam") { startEvent() }

        npcOperate("Talk-to", "mr_mordau") { (mordaut) ->
            if (get<String>("random_event") != "surprise_exam") {
                npc<Neutral>("mr_mordau", "I'm rather busy, please don't interrupt my class.", largeHead = true)
                return@npcOperate
            }
            face(mordaut.tile)
            val door = get<String>("surprise_exam_door")
            if (door != null) {
                npc<Neutral>("mr_mordau", "Well done! Please exit through the ${DOOR_TEXT[door]} door.", largeHead = true)
            } else {
                npc<Neutral>("mr_mordau", "Please answer these questions for me.", largeHead = true)
                openQuestion()
            }
        }

        // The answer icons are option buttons on a main-window interface.
        interfaceOption("Select", "surprise_exam_pattern:option_*") {
            if (get<String>("random_event") != "surprise_exam") {
                return@interfaceOption
            }
            answer(it.component.removePrefix("option_").toInt()) // option_1..4
        }

        objectOperate("Open", "exam_door_*") { (door) ->
            if (get<String>("random_event") != "surprise_exam") {
                return@objectOperate
            }
            when (get<String>("surprise_exam_door")) {
                null -> statement("I should probably speak with Mr. Mordaut first.")
                door.id -> finish()
                else -> statement("The door won't budge. Perhaps I should ask for directions.")
            }
        }

        itemOption("Read", "book_of_knowledge") { (item, slot) ->
            val skill = skillLamp()
            if (inventory.remove(slot, item.id)) {
                exp(skill, levels.getMax(skill) * XP_PER_LEVEL)
                statement("You feel more knowledgeable about ${skill.name.lowercase()}.")
            }
        }
    }

    private suspend fun Player.startEvent() {
        set("surprise_exam_correct", 0)
        clear("surprise_exam_door")
        mysteriousOldMan()
        kidnap(CLASSROOM)
        message("Speak to Mr Mordaut to begin your exam.")
    }

    /** Show three icons of one category plus four options (the matching item among decoys). */
    private fun Player.openQuestion() {
        val set = SETS.random(random)
        val shuffled = set.shuffled(random)
        val correct = shuffled.random(random)
        val index = shuffled.indexOf(correct) // which option slot holds the answer
        val pattern = shuffled.filter { it != correct }
        val decoy = SETS.filter { it != set }.random(random)
        set("surprise_exam_answer", index + 1) // 1-based to dodge the set-clears-0 gotcha
        for (i in 1..3) {
            interfaces.sendItem("surprise_exam_pattern", "pattern_$i", ItemDefinitions.get(pattern[i - 1]).id)
        }
        for (slot in 0..3) {
            val item = if (slot == index) correct else decoy[slot]
            interfaces.sendItem("surprise_exam_pattern", "option_${slot + 1}", ItemDefinitions.get(item).id)
        }
        open("surprise_exam_pattern")
        // Send the access mask so the client reports clicks on the answer icons; the cache marks the
        // option enabled but doesn't send it, so without this the icons don't respond.
        for (slot in 1..4) {
            interfaceOptions.unlockAll("surprise_exam_pattern", "option_$slot", 0..0)
        }
    }

    private suspend fun Player.answer(option: Int) {
        close("surprise_exam_pattern")
        if (option == get("surprise_exam_answer", 0)) {
            if (inc("surprise_exam_correct") >= REQUIRED) {
                set("surprise_exam_door", DOORS.random(random))
                npc<Neutral>("mr_mordau", "Excellent work! You've passed. Please exit through the ${DOOR_TEXT[get<String>("surprise_exam_door")]} door.", largeHead = true)
                return
            }
            npc<Neutral>("mr_mordau", "Excellent work! Now for another...", largeHead = true)
        } else {
            npc<Neutral>("mr_mordau", "I'm afraid that isn't correct. Now for another...", largeHead = true)
        }
        openQuestion()
    }

    private fun Player.finish() {
        message("You've passed the exam!")
        addOrDrop("book_of_knowledge")
        clear("surprise_exam_answer")
        clear("surprise_exam_correct")
        clear("surprise_exam_door")
        RandomEvents.complete(this)
        jingle("surprise_exam_passed")
    }

    companion object {
        private const val REQUIRED = 3
        private const val XP_PER_LEVEL = 15.0
        private val CLASSROOM = Tile(1886, 5025)

        private val DOOR_TEXT = mapOf(
            "exam_door_red" to "red cross",
            "exam_door_blue" to "blue star",
            "exam_door_purple" to "purple circle",
            "exam_door_green" to "green square",
        )
        private val DOORS = DOOR_TEXT.keys.toList()

        private val SETS = listOf(
            listOf("gardening_trowel", "secateurs", "seed_dibber", "rake"),
            listOf("salmon", "shark", "trout", "shrimps"),
            listOf("bronze_sword", "wooden_shield", "bronze_med_helm", "adamant_battleaxe"),
            listOf("fly_fishing_rod", "barbarian_rod", "small_fishing_net", "harpoon"),
        )
    }
}
