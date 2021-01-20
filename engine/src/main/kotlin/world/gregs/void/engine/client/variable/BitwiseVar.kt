package world.gregs.void.engine.client.variable

abstract class BitwiseVar<T> : Variable<Int> {
    abstract fun getValue(id: T): Int?
}