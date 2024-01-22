package world.gregs.voidps.network.client

sealed class ClientState {
    object Connected : ClientState()
    /**
     * Waiting for [NetworkQueue] to allow disconnection
     */
    object Disconnecting : ClientState()
    object Disconnected : ClientState()
}