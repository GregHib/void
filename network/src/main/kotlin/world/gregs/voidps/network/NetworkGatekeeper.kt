package world.gregs.voidps.network

/**
 * Keeps track of current connections
 */
interface NetworkGatekeeper {
    fun connected(name: String): Boolean
    fun connections(address: String): Int
    fun connect(name: String, address: String? = null): Int?
    fun disconnect(name: String, address: String, index: Int?)
}