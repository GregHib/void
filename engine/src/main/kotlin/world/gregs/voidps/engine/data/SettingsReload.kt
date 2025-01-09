package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

object SettingsReload : Event {

    override val notification: Boolean = true

    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "settings_reload"
        else -> null
    }
}

fun settingsReload(handler: suspend SettingsReload.(Player) -> Unit) {
    Events.handle("settings_reload", handler = handler)
}
