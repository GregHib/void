package rs.dusk.network.rs.codec.login.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
data class GameLoginMessage(
    val username: String,
    val password: String,
    val isaacKeys: IntArray,
    val mode: Int,
    val width: Int,
    val height: Int,
    val antialias: Int,
    val settings: String,
    val affiliate: Int,
    val session: Int,
    val os: Int,
    val is64Bit: Int,
    val versionType: Int,
    val vendorType: Int,
    val javaRelease: Int,
    val javaVersion: Int,
    val javaUpdate: Int,
    val isUnsigned: Int,
    val heapSize: Int,
    val processorCount: Int,
    val totalMemory: Int
) : Message {
    companion object : MessageCompanion<GameLoginMessage>()
}