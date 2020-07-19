package rs.dusk.engine.task

class TaskIterator(private val list: MutableList<Task>) : MutableIterator<Task> {
    private var index = 0

    override fun hasNext() = index < list.size

    override fun next() = list[index++]

    override fun remove() {
        list.removeAt(
            if (index == 0) {
                index
            } else {
                --index
            }
        )
    }
}