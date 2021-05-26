package world.gregs.voidps.bot

import org.koin.dsl.module
import world.gregs.voidps.bot.navigation.await
import world.gregs.voidps.engine.entity.character.player.Bot
import java.util.*
import kotlin.random.Random

val taskModule = module {
    single { TaskManager() }
}

class TaskManager {
    private val queue = LinkedList<Task>()

    fun register(task: Task) {
        queue.add(task)
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
    }
}