package world.gregs.voidps.bot

import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.type.random
import java.util.*

class TaskManager {
    private val queue = LinkedList<Task>()
    private var idle = Task(
        name = "do nothing",
        block = {
            repeat(random.nextInt(10, 100)) {
                bot.await("tick")
            }
        },
        spaces = Int.MAX_VALUE
    )

    fun register(task: Task, test: Boolean = false) {
        if (DEBUG && !(DEBUG && test)) {
            return
        }
        queue.add(task)
    }

    fun get(name: String): Task? {
        return queue.firstOrNull { it.name == name }
    }

    fun assign(bot: Bot, last: String?): Task {
        return queue
            .filter { it.name != last && !it.full() && it.requirements.all { req -> req(bot.player) } }
            .minByOrNull { it.distanceTo(bot.tile) } ?: idle
    }

    fun idle(task: Task) {
        idle = task
    }

    companion object {
        const val DEBUG = false
    }
}