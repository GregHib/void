package rs.dusk.tools

class Pipeline<T> {
    interface Modifier<T> {
        fun modify(content: T): T
    }

    private val modifiers = mutableListOf<Modifier<T>>()

    fun add(modifier: Modifier<T>) {
        modifiers.add(modifier)
    }

    fun modify(content: T): T {
        return modifiers.fold(content) { c, m -> m.modify(c) }
    }
}