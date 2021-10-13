package world.gregs.voidps.network

/**
 * Queue for accounts awaiting entry into the game
 */
interface NetworkQueue : Runnable {
    suspend fun await()
}