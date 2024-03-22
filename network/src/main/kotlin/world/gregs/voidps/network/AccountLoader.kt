package world.gregs.voidps.network

import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Instruction

/**
 * Loads and setups account from data on file
 */
interface AccountLoader {
    fun assignIndex(username: String): Int?
    fun password(username: String): String?

    suspend fun load(client: Client, username: String, passwordHash: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>?
}