package org.redrune.network.model.packet

import io.netty.buffer.ByteBuf
import org.redrune.network.codec.Codec

/**
 * This class handles the decoding of a simple packet.
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class SimplePacketDecoder(codec: Codec) : PacketDecoder(codec) {

}