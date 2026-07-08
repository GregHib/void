package content.activity.event.random.drill_demon

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.activity.event.random.rewardCostumeOrCoins
import content.entity.gfx.areaGfx
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
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
            if (!get("drill_demon_ready", false)) {
                npc<Neutral>("sergeant_damien", "I haven't given you the order yet, worm!", largeHead = true)
                return@npcOperate
            }
            order(wrong = false)
        }

        objectOperate("Use", "drill_demon_mat_*") { (mat) ->
            if (get<String>("random_event") != "drill_demon") {
                message("You can't do that right now.")
                return@objectOperate
            }
            if (!get("drill_demon_ready", false)) {
                // Jumping on a mat before the order is given is a wasted, uncredited attempt.
                npc<Neutral>("sergeant_damien", "I haven't given you the order yet, worm!", largeHead = true)
                return@objectOperate
            }
            val value = get("drill_demon_sign_${mat.id.removePrefix("drill_demon_mat_")}", 0)
            val exercise = EXERCISES.getValue(value)
            walkOverDelay(Tile(mat.tile.x, YARD.y)) // stand on the mat before exercising
            face(Direction.SOUTH)
            sound(exercise.sound)
            delay(2)
            anim(exercise.anim)
            delay(EXERCISE_TICKS)
            val correct = value == get("drill_demon_task", 0)
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
        clear("drill_demon_ready")
        assignRound(reveal = false)
        mysteriousOldMan()
        kidnap(YARD)
        message("Follow Sergeant Damien's orders!")
        delay(ORDER_DELAY) // give the player a few seconds before barking the first order
        set("drill_demon_ready", true)
        order(wrong = false)
    }

    /**
     * Shuffle the four exercises across the mats' signs and pick the one the player must perform.
     * When [reveal] is set a puff of smoke sweeps across the signs from sign 1 to sign 4, each half a
     * tick apart (the signs themselves all change this tick; only the puffs are staggered client-side).
     */
    private fun Player.assignRound(reveal: Boolean = true) {
        val exercises = EXERCISES.keys.shuffled(random)
        set("drill_demon_task", EXERCISES.keys.random(random))
        for (sign in 1..exercises.size) {
            set("drill_demon_sign_$sign", exercises[sign - 1])
            if (reveal) {
                areaGfx("drill_demon_sign_change", SIGN_TILES[sign - 1], delay = (sign - 1) * HALF_TICK)
            }
        }
    }

    private suspend fun Player.order(wrong: Boolean) {
        val task = EXERCISES.getValue(get("drill_demon_task", 1))
        val prefix = if (wrong) "Wrong exercise, worm! " else ""
        npc<Neutral>("sergeant_damien", "$prefix${task.order} private!", largeHead = true)
        item(task.sign, "Go to <red>this mat</col> and ${task.action}!")
    }

    private suspend fun Player.finish() {
        npc<Neutral>("sergeant_damien", "Well I'll be, you actually did it $name. Now take this and get yourself out of my sight.", largeHead = true)
        rewardCostumeOrCoins("camo_helmet", "camo_top", "camo_bottoms", coins = 500)
        clear("drill_demon_ready")
        clear("drill_demon_task")
        clear("drill_demon_correct")
        RandomEvents.complete(this)
    }

    private data class Exercise(val anim: String, val sound: String, val order: String, val sign: String, val action: String)

    companion object {
        private const val REQUIRED = 4
        private const val EXERCISE_TICKS = 4
        private const val ORDER_DELAY = 8 // ~5 seconds before the first order
        private const val HALF_TICK = 15 // client ticks; a game tick is 30
        private val YARD = Tile(3163, 4820)

        // The sign board sits two tiles north of each mat (mat y + 2).
        private val SIGN_TILES = listOf(Tile(3160, 4821), Tile(3162, 4821), Tile(3164, 4821), Tile(3166, 4821))

        // Sign varbit values, 1-based so a sign is never the cleared 0 default (0 = no sign shown).
        private val EXERCISES = mapOf(
            1 to Exercise("drill_demon_jog", "drill_demon_jog", "Get yourself over there and jog on that mat", "run", "jog on the spot"),
            2 to Exercise("drill_demon_situp", "drill_demon_situp", "Get on that mat and give me sit ups", "sit_up", "do some sit-ups"),
            3 to Exercise("drill_demon_pushup", "drill_demon_pushup", "Drop and give me push ups on that mat", "push_up", "do some push-ups"),
            4 to Exercise("drill_demon_starjump", "drill_demon_starjump", "I want to see you on that mat doing star jumps", "star_jump", "do some star jumps"),
        )
    }
}
