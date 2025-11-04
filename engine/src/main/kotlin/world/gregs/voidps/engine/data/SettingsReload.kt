package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

interface SettingsReload {

    fun settingsReload(handler: () -> Unit) {
        handlers.add(handler)
    }

    companion object : AutoCloseable {
        private val handlers = ObjectArrayList<() -> Unit>(5)

        fun now() {
            for (handler in handlers) {
                handler()
            }
        }

        override fun close() {
            handlers.clear()
        }
    }
}
