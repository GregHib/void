package world.gregs.voidps.engine.entity.character.mode.move

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Area

data class AreaExited(
    override val character: Player,
    val name: String,
    val tags: Set<String>,
    val area: Area
) : SuspendableEvent, SuspendableContext<Player> {

    override val size = 3

    override suspend fun pause(ticks: Int) {
        Suspension.start(character, ticks)
    }

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "area_exit"
        1 -> name
        2 -> tags
        else -> null
    }
}

fun exitArea(area: String = "*", tag: String = "*", handler: suspend AreaExited.() -> Unit) {
    Events.handle<Player, AreaExited>("area_exit", area, tag) {
        handler.invoke(this)
    }
}