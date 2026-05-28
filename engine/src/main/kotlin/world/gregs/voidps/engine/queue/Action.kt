package world.gregs.voidps.engine.queue

import world.gregs.voidps.engine.entity.character.Character

data class Action<C : Character>(
    val name: String,
    var remaining: Int,
    var priority: ActionPriority,
    var action: suspend C.() -> Unit,
) {
    var next: Action<C>? = null
    var previous: Action<C>? = null

    /**
     * Executes action once the delay has reached zero
     * @return if action was executed this call
     */
    fun process(): Boolean = --remaining <= 0

}
