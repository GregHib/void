package world.gregs.voidps.engine.entity.character

import kotlinx.coroutines.Job
import world.gregs.voidps.engine.delay

abstract class Effect(val effectType: String) {

    private var task: Job? = null

    fun removeSelf(character: Character, ticks: Int) {
        task = delay(ticks) {
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