package world.gregs.voidps.bot

import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Bot
import world.gregs.voidps.utility.get
import java.util.*

val taskModule = module {
    single { TaskStore() }
}

class TaskStore {
    private val queue = LinkedList<suspend CoroutineScope.(Bot) -> Unit>()

    fun register(task: suspend CoroutineScope.(Bot) -> Unit) {
        queue.add(task)
    }

    fun obtain(): (suspend CoroutineScope.(Bot) -> Unit)? = queue.poll()
}

fun task(block: suspend CoroutineScope.(Bot) -> Unit) {
    get<TaskStore>().register(block)
}