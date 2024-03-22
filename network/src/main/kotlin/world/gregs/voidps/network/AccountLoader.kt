package world.gregs.voidps.network

import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Client

/**
 * Loads and setups account from data on file
 */
interface AccountLoader {
    fun password(username: String): String?

    suspend fun load(client: Client, username: String, passwordHash: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>?
}