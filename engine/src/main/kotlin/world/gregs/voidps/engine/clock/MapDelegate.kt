package world.gregs.voidps.engine.clock

interface MapDelegate {
    operator fun get(name: String): Int?
    operator fun set(name: String, value: Int)
    fun remove(name: String)
}