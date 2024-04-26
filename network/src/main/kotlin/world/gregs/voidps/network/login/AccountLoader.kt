package world.gregs.voidps.network.login

import kotlinx.coroutines.channels.SendChannel
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.Instruction

/**
 * Loads and setups account from data on file
 */
interface AccountLoader {
    fun password(username: String): String?

    suspend fun load(client: Client, username: String, passwordHash: String, displayMode: Int): SendChannel<Instruction>?
}