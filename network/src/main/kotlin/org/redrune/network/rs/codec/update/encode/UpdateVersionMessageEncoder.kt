package org.redrune.network.rs.codec.update.encode

import org.redrune.core.network.codec.packet.access.PacketWriter
import org.redrune.network.rs.codec.update.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.encode.message.UpdateVersionMessage
import org.redrune.utility.constants.network.FileServerResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateVersionMessageEncoder : UpdateMessageEncoder<UpdateVersionMessage>() {

    override fun encode(builder: PacketWriter, msg: UpdateVersionMessage) {
        builder.writeByte(msg.opcode)
        if (msg.opcode == FileServerResponseCodes.JS5_RESPONSE_OK) {
            GRAB_SERVER_KEYS.forEach {
                builder.writeInt(it)
            }
        }
    }

    companion object {

        /**
         * The keys sent during grab server decoding
         */
        private val GRAB_SERVER_KEYS = intArrayOf(
            1362,
            77448,
            44880,
            39771,
            24563,
            363672,
            44375,
            0,
            1614,
            0,
            5340,
            142976,
            741080,
            188204,
            358294,
            416732,
            828327,
            19517,
            22963,
            16769,
            1244,
            11976,
            10,
            15,
            119,
            817677,
            1624243
        )
    }
}