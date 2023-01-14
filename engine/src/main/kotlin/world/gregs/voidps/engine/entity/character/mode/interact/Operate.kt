package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.entity.character.mode.interact.option.Option
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.event.addEvent

data class Operate<T : Any>(val target: T, val optionData: Option, val partial: Boolean) : SuspendableEvent()

@JvmName("onOperatePlayer")
inline fun <reified T : Any> onOperate(
    noinline condition: Operate<T>.(Player) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Operate<T>.(player: Player, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }

@JvmName("onOperateNPC")
inline fun <reified T : Any> onOperate(
    noinline condition: Operate<T>.(NPC) -> Boolean = { true },
    priority: Priority = Priority.MEDIUM,
    noinline block: suspend Operate<T>.(npc: NPC, target: T) -> Unit
) = addEvent(condition, priority) { block.invoke(this, it, target) }