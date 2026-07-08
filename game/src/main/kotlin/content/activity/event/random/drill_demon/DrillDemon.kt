package content.activity.event.random.drill_demon

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.activity.event.random.rewardCostumeOrCoins
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Drill Demon random event: Sergeant Damien whisks the player to his exercise yard and barks out an
 * exercise. Each of the four mats shows one exercise on its sign (a varbit); the player must use the
 * mat matching the order. Four correct exercises earns a piece of the camouflage outfit (or coins).
 * https://runescape.wiki/w/Random_events?oldid=3667851#Drill_Demon
 */
class DrillDemon : Script {

    init {
        RandomEvents.register("drill_demon") { startEvent() }

        npcOperate("Talk-to", "sergeant_damien") {
            if (get<String>("random_event") != "drill_demon") {
                message("They aren't interested in talking to you.")
                return@npcOperate
            }
            order(wrong = false)
        }

        objectOperate("Use", "drill_demon_mat_*") { (mat) ->
            if (get<String>("random_event") != "drill_demon") {
                message("You can't do that right now.")
                return@objectOperate
            }
            val exercise = get("drill_demon_sign_${mat.id.removePrefix("drill_demon_mat_")}", 0)
            face(mat.tile)
            anim(EXERCISES[exercise].anim)
            delay(EXERCISE_TICKS)
            // 0 (jog) is a valid exercise, so read with a 0 default - the random_event guard above
            // already rules out an unset value here.
            val correct = exercise == get("drill_demon_task", 0)
            if (correct) {
                inc("drill_demon_correct")
            }
            if (get("drill_demon_correct", 0) >= REQUIRED) {
                finish()
            } else {
                assignRound()
                order(wrong = !correct)
            }
        }
    }

    private suspend fun Player.startEvent() {
        set("drill_demon_correct", 0)
        assignRound()
        mysteriousOldMan()
        kidnap(YARD)
        npc<Neutral>("sergeant_damien", "Move yourself private! Follow my orders and you may, just may, leave here in a fit state for my corps!")
        order(wrong = false)
    }

    /** Shuffle the four exercises across the mats' signs and pick the one the player must perform. */
    private fun Player.assignRound() {
        val exercises = EXERCISES.indices.shuffled(random)
        for (mat in 1..EXERCISES.size) {
            set("drill_demon_sign_$mat", exercises[mat - 1])
        }
        set("drill_demon_task", EXERCISES.indices.random(random))
    }

    private suspend fun Player.order(wrong: Boolean) {
        val prefix = if (wrong) "Wrong exercise, worm! " else ""
        npc<Neutral>("sergeant_damien", "$prefix${EXERCISES[get("drill_demon_task", 0)].order} private!")
    }

    private suspend fun Player.finish() {
        npc<Neutral>("sergeant_damien", "Well I'll be, you actually did it $name. Now take this and get yourself out of my sight.")
        rewardCostumeOrCoins("camo_helmet", "camo_top", "camo_bottoms", coins = 500)
        clear("drill_demon_task")
        clear("drill_demon_correct")
        RandomEvents.complete(this)
    }

    private data class Exercise(val anim: String, val order: String)

    companion object {
        private const val REQUIRED = 4
        private const val EXERCISE_TICKS = 4
        private val YARD = Tile(3163, 4820)

        // Index order matches the sign varbit values: 0 jog, 1 sit-ups, 2 push-ups, 3 star jumps.
        private val EXERCISES = listOf(
            Exercise("drill_demon_jog", "Get yourself over there and jog on that mat"),
            Exercise("drill_demon_situp", "Get on that mat and give me sit ups"),
            Exercise("drill_demon_pushup", "Drop and give me push ups on that mat"),
            Exercise("drill_demon_starjump", "I want to see you on that mat doing star jumps"),
        )
    }
}
