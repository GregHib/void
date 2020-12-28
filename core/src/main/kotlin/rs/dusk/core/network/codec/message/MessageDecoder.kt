package rs.dusk.core.network.codec.message

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.message.Message

abstract class MessageDecoder<M : Message>(val length: Int) {

    var handler: MessageHandler<M>? = null

    /**
     * The decoding of an incoming packet, which has been transformed into a packet reading object, is performed here
     * @param packet PacketReader The reader of the packet
     * @return T The message to return
     */
    abstract fun decode(packet: PacketReader): M
}