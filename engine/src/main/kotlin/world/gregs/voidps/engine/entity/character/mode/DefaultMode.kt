package world.gregs.voidps.engine.entity.character.mode

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Wander.Companion.wanders
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface DefaultMode {

    /**
     * The mode a npc returns to whenever it's idle.
     * @return null falls back to [Wander] or standing still.
     */
    fun npcDefaultMode(id: String = "*", handler: NPC.() -> Mode?) {
        Script.checkLoading()
        Wildcards.find(id, Wildcard.Npc) { match ->
            handlers.getOrPut(match) { mutableListOf() }.add(handler)
        }
    }

    companion object : AutoCloseable {
        private val handlers = Object2ObjectOpenHashMap<String, MutableList<NPC.() -> Mode?>>(10)

        fun get(npc: NPC): Mode? {
            for (handler in handlers[npc.id] ?: emptyList()) {
                return handler(npc) ?: continue
            }
            for (handler in handlers["*"] ?: emptyList()) {
                return handler(npc) ?: continue
            }
            if (wanders(npc)) {
                return Wander(npc)
            }
            return null
        }

        override fun close() {
            handlers.clear()
        }
    }
}
