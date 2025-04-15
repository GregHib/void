package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject

interface EventDispatcher {
    fun <E : Event> emit(event: E): Boolean {
        debug(this, event)
        return when (event) {
            else -> Events.events.emit(this, event)
        }
    }

    fun <E : SuspendableEvent> emit(event: E): Boolean {
        debug(this, event)
        return when(event) {
            is Command -> Command.handle(this, event)
            else -> Events.events.emit(this, event)
        }
    }

    val key: String
        get() = when (this) {
            is NPC -> "npc"
            is FloorItem -> "floor_item"
            is GameObject -> "object"
            is Player -> "player"
            is World -> "world"
            else -> ""
        }

    val identifier: String
        get() = when (this) {
            is NPC -> id
            is FloorItem -> id
            is GameObject -> id
            is Player -> "player"
            is World -> "world"
            else -> ""
        }

    companion object {
        private val logger = InlineLogger()
        private fun debug(dispatcher: EventDispatcher, event: Event) {
            if (dispatcher is Player) {
                if (dispatcher.contains("bot")) {
                    Events.events.all?.invoke(dispatcher, event)
                }
                if (dispatcher["debug", false]) {
                    logger.debug { "Event: $dispatcher - ${event.debug(dispatcher)}" }

                }
            }
        }
    }
}