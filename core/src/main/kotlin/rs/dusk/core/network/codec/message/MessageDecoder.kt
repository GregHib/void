package rs.dusk.core.network.codec.message

import io.netty.channel.ChannelHandler
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.message.Message
import java.util.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@ChannelHandler.Sharable
abstract class MessageDecoder<M : Message>(

    /**
     * The packet opcodes that this decoder is used for
     */
    var opcodes: IntArray? = null,

    /**
     * The expected length of the packet that this decoder is used for
     */
    var length: Int? = 0
) {

    /**
     * The decoding of an incoming packet, which has been transformed into a packet reading object, is performed here
     * @param packet PacketReader The reader of the packet
     * @return T The message to return
     */
    abstract fun decode(packet: PacketReader): M

    override fun toString(): String {
        return "MessageDecoder[opcodes=${Arrays.toString(opcodes)}, length=$length]"
    }
}