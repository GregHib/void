package world.gregs.voidps.network

sealed class ClientState {
    object Connected : ClientState()
    /**
     * Waiting for [NetworkQueue] to allow disconnection
     */
    object Disconnecting : ClientState()
    object Disconnected : ClientState()
}