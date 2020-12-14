package rs.dusk.core.network.codec.packet.decode

import io.netty.buffer.ByteBuf
import rs.dusk.core.io.crypto.IsaacCipher

/**
 * This packet decoder decodes runescape packets which are built in this manner [opcode, length, buffer], with the opcode decryption requiring an [IsaacCipher]
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class RS2PacketDecoder(private val cipher : IsaacCipher) : SimplePacketDecoder() {
	
    override fun readOpcode(buf: ByteBuf): Int {
        return (buf.readUnsignedByte().toInt() - cipher.nextInt()) and 0xff
    }

}