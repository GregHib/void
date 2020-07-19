package rs.dusk.engine.task

import rs.dusk.utility.get

class StartTask : CancelTask() {

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
fun TaskExecutor.start(task: (Long) -> Unit) {
    val start: StartTask = get()
    start.subTasks.add(task)
}