package world.gregs.voidps.network

interface SessionManager {
    fun count(key: String): Int
    fun add(key: String): Int?
    fun remove(key: String)
    fun clear()
}