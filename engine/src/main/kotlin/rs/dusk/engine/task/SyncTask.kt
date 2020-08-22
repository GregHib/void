package rs.dusk.engine.task

import rs.dusk.utility.get

/**
 * Syncs task with the start of the current or next tick
 */
class SyncTask : CancelTask() {

    val subTasks = mutableListOf<(Long) -> Unit>()

    override fun run(tick: Long) {
        val iterator = subTasks.iterator()
        while (iterator.hasNext()) {
            val subTask = iterator.next()
            subTask.invoke(tick)
            iterator.remove()
        }
    }
}

@Suppress("unused")
fun TaskExecutor.sync(task: (Long) -> Unit) {
    val sync: SyncTask = get()
    sync.subTasks.add(task)
}