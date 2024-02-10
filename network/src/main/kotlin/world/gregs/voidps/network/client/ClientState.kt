package world.gregs.voidps.network.client

import world.gregs.voidps.network.NetworkQueue

sealed class ClientState {
    data object Connected : ClientState()
    /**
     * Waiting for [NetworkQueue] to allow disconnection
     */
    data object Disconnecting : ClientState()
    data object Disconnected : ClientState()
}