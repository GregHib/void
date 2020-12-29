package rs.dusk.core.network.codec.message

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.core.network.model.message.Message

interface MessageEncoder<M : Message> {

	/**
	 * Encodes a message into a packet builder
	 */
	fun encode(builder : PacketWriter, msg : M) : Any

}