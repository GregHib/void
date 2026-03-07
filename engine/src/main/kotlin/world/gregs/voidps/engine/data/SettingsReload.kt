package world.gregs.voidps.engine.data

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.Script

interface SettingsReload {

    fun settingsReload(handler: () -> Unit) {
        Script.checkLoading()
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
