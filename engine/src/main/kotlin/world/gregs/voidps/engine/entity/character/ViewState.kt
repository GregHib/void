package world.gregs.voidps.engine.entity.character

class ViewState(capacity: Int) {

    private val state = IntArray(capacity)

    fun global(index: Int) = state[index - 1] == GLOBAL

    fun local(index: Int) = state[index - 1] == LOCAL

    fun adding(index: Int) = state[index - 1] == ADDING

    fun removing(index: Int) = state[index - 1] == REMOVING

    fun setGlobal(index: Int) {
        state[index - 1] = GLOBAL
    }

    fun setLocal(index: Int) {
        state[index - 1] = LOCAL
    }

    fun setAdding(index: Int) {
        state[index - 1] = ADDING
    }

    fun setRemoving(index: Int) {
        state[index - 1] = REMOVING
    }

    companion object {
        private const val GLOBAL = 0
        private const val LOCAL = 1
        private const val ADDING = 2
        private const val REMOVING = 3
    }
}