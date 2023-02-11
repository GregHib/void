package world.gregs.voidps.bot

import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.addEvent
import world.gregs.voidps.network.Instruction

data class Bot(val player: Player) : Entity by player {

    val botEvents = Events(this)

    var step: Instruction? = null

}

@JvmName("onBot")
inline fun <reified E : Event> onBot(noinline condition: E.(Bot) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, noinline block: suspend E.(Bot) -> Unit) =
    addEvent(condition, priority, block)