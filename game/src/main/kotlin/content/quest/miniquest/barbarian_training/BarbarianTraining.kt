package content.quest.miniquest.barbarian_training

import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue

/**
 * Each section of Otto's training goes through the same stages.
 */
object BarbarianTraining {
    const val NOT_STARTED = 0
    const val STARTED = 1
    const val DONE = 2
    const val REPORTED = 3

    const val NOT_STARTED_MESSAGE = "You must begin the relevant section of Otto Godblessed's barbarian training."
}

/**
 * Training [section] stage, one of [BarbarianTraining.NOT_STARTED], [BarbarianTraining.STARTED], [BarbarianTraining.DONE] or [BarbarianTraining.REPORTED]
 */
fun Player.barbarianTraining(section: String): Int = this["barbarian_training_$section", 0]

fun Player.barbarianTrainingStarted(section: String): Boolean = barbarianTraining(section) >= BarbarianTraining.STARTED

/**
 * Completes the task for [section] if Otto has told the player about it.
 */
fun Player.barbarianTaskDone(section: String) {
    if (barbarianTraining(section) != BarbarianTraining.STARTED) {
        return
    }
    set("barbarian_training_$section", BarbarianTraining.DONE)
    queue("barbarian_training") {
        statement("You feel you have learned more of barbarian ways. Otto might wish to talk to you more.")
    }
}
