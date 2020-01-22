package org.redrune.network.session

import io.netty.channel.Channel
import org.redrune.network.codec.message.Message

class FileSession(channel: Channel) : Session(channel) {
    override fun messageReceived(message: Any) {
    }

    override fun onInactive() {
    }
}