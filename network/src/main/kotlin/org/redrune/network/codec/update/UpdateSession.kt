package org.redrune.network.codec.update

import io.netty.channel.Channel
import org.redrune.network.Session
import org.redrune.network.codec.CodecRepository
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 12:50 a.m.
 */
class UpdateSession(channel: Channel, codec: CodecRepository) : Session(channel, codec) {
}