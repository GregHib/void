package rs.dusk.core.network.codec.packet.access

import io.netty.buffer.ByteBuf
import rs.dusk.core.io.crypto.IsaacCipher

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since February 18, 2020
 */
class PacketBuilder(private val cipher : IsaacCipher? = null, private val sized : Boolean = cipher != null) {
	
	fun build(buffer : ByteBuf, build : (PacketWriter) -> Unit) {
		val writer = PacketWriter(buffer = buffer, cipher = cipher)
		build(writer)
		if (sized) {
			writer.writeSize()
		}
	}
	
}