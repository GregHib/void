package world.gregs.voidps.network.login.protocol.encode.zone

abstract class ZoneUpdate(
    val packetId: Int,
    val packetIndex: Int,
    val size: Int,
) {
    open val private: Boolean = false

    /**
     * Compare with username as index could have changed on logout.
     */
    open fun visible(owner: String): Boolean = false
}
