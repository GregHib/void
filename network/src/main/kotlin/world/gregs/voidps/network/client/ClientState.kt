package world.gregs.voidps.network.client

sealed class ClientState {
    data object Connected : ClientState()

    /**
     * Waiting for [ConnectionQueue] to allow disconnection
     */
    data object Disconnecting : ClientState()
    data object Disconnected : ClientState()
}
