package world.gregs.voidps.network

import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Client

/**
 * Loads and setups account from data on file
 */
interface AccountLoader {
    fun validate(username: String, password: String): Int

    fun encrypt(username: String, password: String): String

    suspend fun load(client: Client, username: String, passwordHash: String, index: Int, displayMode: Int): MutableSharedFlow<Instruction>?
}