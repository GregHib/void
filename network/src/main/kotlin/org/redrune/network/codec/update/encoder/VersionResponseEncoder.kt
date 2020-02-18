package org.redrune.network.codec.update.encoder

import org.redrune.network.codec.update.message.UpdateResponseCode
import org.redrune.network.codec.update.message.impl.UpdateServiceVersionResponseMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-01
 */
class VersionResponseEncoder : MessageEncoder<UpdateServiceVersionResponseMessage>() {

    override fun encode(out: PacketBuilder, msg: UpdateServiceVersionResponseMessage) {
        out.writeByte(msg.responseCode)
        if (msg.responseCode == UpdateResponseCode.JS5_RESPONSE_OK) {
            NetworkConstants.GRAB_SERVER_KEYS.forEach {
                out.writeInt(it)
            }
        }
    }
}