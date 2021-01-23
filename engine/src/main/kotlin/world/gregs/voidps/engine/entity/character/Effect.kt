package world.gregs.voidps.engine.entity.character

import kotlinx.serialization.Serializable
import world.gregs.voidps.engine.task.Task
import world.gregs.voidps.engine.task.TaskExecutor
import world.gregs.voidps.engine.task.delay
import world.gregs.voidps.utility.get

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