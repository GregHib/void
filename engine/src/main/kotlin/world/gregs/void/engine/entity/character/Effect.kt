package world.gregs.void.engine.entity.character

import kotlinx.serialization.Serializable
import world.gregs.void.engine.task.Task
import world.gregs.void.engine.task.TaskExecutor
import world.gregs.void.engine.task.delay
import world.gregs.void.utility.get

@Serializable
abstract class Effect(val effectType: String) {

    private var task: Task? = null

    fun removeSelf(character: Character, ticks: Long) {
        val executor: TaskExecutor = get()
        task = executor.delay(ticks) {
            character.effects.remove(this)
        }
    }

    open fun immune(character: Character): Boolean {
        return false
    }

    open fun onStart(character: Character) {
    }

    open fun onFinish(character: Character) {
        task?.cancel()
    }
}