package rs.dusk.engine.action

import org.koin.dsl.module
import rs.dusk.engine.event.then
import rs.dusk.engine.model.engine.Tick
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.player.Players
import java.util.*

class GlobalActions(private val players: Players, private val npcs: NPCs) {
    val active = mutableListOf<Action>()
    val inactive: Queue<Action> = LinkedList()

    fun run(action: suspend Action.() -> Unit) {
        val a = get()
        active.add(a)
        a.run(ActionType.Global, action)
    }

    fun get() = inactive.poll() ?: Action()

    val tick : Tick.(Tick) -> Unit = {
        players.forEach {
            val action = it.action
            if (action.suspension == Suspension.Tick) {
                action.resume()
            }
        }
        npcs.forEach {
            val action = it.action
            if (action.suspension == Suspension.Tick) {
                action.resume()
            }
        }
        active.forEach { action ->
            if (action.suspension == Suspension.Tick) {
                action.resume()
                if(action.continuation?.isCompleted == true) {
                    active.remove(action)
                    inactive.offer(action)
                }
            }
        }
    }

    init {
        Tick.then(tick)
    }

}

val globalActionModule = module {
    single(createdAtStart = true) { GlobalActions(get(), get()) }
}