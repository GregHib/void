package rs.dusk.engine.client.ui

class Interfaces(
    private val io: InterfaceIO,
    private val interfaces: Map<Int, Interface>,
    var resizable: Boolean = false
) {

    interface InterfaceIO {
        fun sendOpen(id: Int, parent: Int, index: Int)
        fun sendClose(id: Int)
    }

    open class InterfaceException : RuntimeException()

    class InvalidInterfaceException : InterfaceException()

    var openedList = mutableSetOf<Int>()

    fun open(id: Int): Boolean {
        if (openedList.add(id)) {
            io.sendOpen(id, getParent(id), getIndex(id))
            return true
        }
        return false
    }

    private fun getIndex(id: Int): Int = getFixedOrResizableIndex(id) ?: throw InvalidInterfaceException()

    private fun getFixedOrResizableIndex(id: Int) = if (resizable) {
        getResizableIndex(id)
    } else {
        getFixedIndex(id)
    }

    private fun getInterface(id: Int): Interface? = interfaces[id]

    private fun getFixedIndex(id: Int): Int? = getInterface(id)?.fixedIndex

    private fun getResizableIndex(id: Int): Int? = getInterface(id)?.resizableIndex

    private fun getParent(id: Int): Int = getFixedOrResizableParent(id) ?: throw InvalidInterfaceException()

    private fun getFixedOrResizableParent(id: Int) = if (resizable) {
        getResizableParent(id)
    } else {
        getFixedParent(id)
    }

    private fun getFixedParent(id: Int): Int? = getInterface(id)?.fixedParent

    private fun getResizableParent(id: Int): Int? = getInterface(id)?.resizableParent

    fun close(id: Int): Boolean {
        if (openedList.remove(id)) {
            io.sendClose(id)
            return true
        }
        return false
    }

    fun contains(id: Int): Boolean {
        return openedList.contains(id)
    }

    val isEmpty: Boolean
        get() = openedList.isEmpty()
}