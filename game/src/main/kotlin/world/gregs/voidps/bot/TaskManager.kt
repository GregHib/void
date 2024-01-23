package world.gregs.voidps.bot

import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.type.random
import java.util.*

class TaskManager {
    private val queue = LinkedList<Task>()

    fun register(task: Task, test: Boolean = false) {
        if (DEBUG && !(DEBUG && test)) {
            return
        }
        queue.add(task)
    }

    fun get(name: String): Task? {
        return queue.firstOrNull { it.name == name }
    }

    fun assign(bot: Bot): Task {
        return queue
            .filter { !it.full() && it.requirements.all { req -> req(bot) } }
            .minByOrNull { it.distanceTo(bot.tile) } ?: idle
    }

    companion object {
        val idle = Task(
            name = "do nothing",
            block = {
                repeat(random.nextInt(10, 100)) {
                    await("tick")
                }
            },
            spaces = Int.MAX_VALUE
        )
        const val DEBUG = false
    }
}