package world.gregs.voidps.bot

import world.gregs.voidps.bot.navigation.await
import java.util.*
import kotlin.random.Random

class TaskManager {
    private val queue = LinkedList<Task>()

    fun register(task: Task, test: Boolean = false) {
        if (!DEBUG || (DEBUG && test))
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
                repeat(Random.nextInt(10, 100)) {
                    await("tick")
                }
            },
            spaces = Int.MAX_VALUE
        )
        val DEBUG = false
    }
}