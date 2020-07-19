package rs.dusk.engine.task

import com.github.michaelbull.logging.InlineLogger
import org.koin.dsl.module
import java.util.*

val executorModule = module {
    single { StartTask() }
    single { TaskExecutor() }
}

class TaskExecutor : Runnable {

    private val tasks: MutableList<Task> = LinkedList()
    private val logger = InlineLogger()

    val empty: Boolean
        get() = tasks.isEmpty()
    var tick = 0L

    fun execute(task: Task) {
        tasks.add(task)
    }

    override fun run() {
        val iterator = TaskIterator(tasks)
        while (iterator.hasNext()) {
            val task = iterator.next()
            runTaskIfReady(task)
            removeTaskIfComplete(task, iterator)
        }
        tick++
    }

    private fun removeTaskIfComplete(task: Task, it: MutableIterator<Task>) {
        if (task.isTimeToRemove(tick)) {
            it.remove()
        }
    }

    private fun runTaskIfReady(task: Task) {
        if (task.isTimeToRun(tick)) {
            runTaskSafely(task)
        }
    }

    private fun runTaskSafely(task: Task) {
        try {
            task.run(tick)
        } catch (e: Throwable) {
            logger.error(e) { "Exception in engine task $task" }
            task.cancel()
        }
    }

    fun clear() {
        tasks.clear()
    }
}
