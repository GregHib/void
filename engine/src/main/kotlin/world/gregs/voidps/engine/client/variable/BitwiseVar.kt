package world.gregs.voidps.engine.client.variable

abstract class BitwiseVar<T> : Variable<Int> {
    abstract fun getValue(id: T): Int?
}