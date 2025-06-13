package world.gregs.voidps.tools

class Pipeline<T> {
    interface Modifier<T> {
        fun modify(content: T): T = content
        fun process(content: T) {
        }
    }

    private val modifiers = mutableListOf<Modifier<T>>()

    fun add(modifier: Modifier<T>) {
        modifiers.add(modifier)
    }

    fun modify(content: T): T = modifiers.fold(content) { c, m -> m.modify(c) }

    fun process(content: T) {
        modifiers.forEach { m ->
            m.process(content)
        }
    }
}
