package org.redrune.network.codec.update

import io.netty.channel.Channel
import org.redrune.cache.Cache
import org.redrune.network.Session

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 12:50 a.m.
 */
class UpdateSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Any) {
        if (msg is FileRequest) {
            val (index, archive, priority) = msg

            if (archive < 0) {
                return
            }
            if (index != 255) {
                if (Cache.indexes.size <= index || !Cache.indexes[index].archiveExists(archive)) {
                    return
                }
            } else if (archive != 255) {
                if (Cache.indexes.size <= archive) {
                    return
                }
            }
            send(msg)
        }
    }
}