package world.gregs.voidps.bot

import org.koin.dsl.module
import world.gregs.voidps.engine.entity.character.player.Bot
import java.util.*

val taskModule = module {
    single { TaskStore() }
}

class TaskStore {
    private val queue = LinkedList<Task>()

    fun register(task: Task) {
        queue.add(task)
    }

    fun obtain(bot: Bot): Task? {
        val it = queue.iterator()
        while (it.hasNext()) {
            val task = it.next()
            if (task.requirements.all { req -> req(bot) }) {
                it.remove()// TODO re-add, or don't remove?
                return task
            }
        }
        return null
    }
}