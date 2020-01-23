package org.redrune.network.session

import io.netty.channel.Channel
import org.redrune.network.codec.file.FileRequest
import org.redrune.network.codec.file.FileResponse

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 12:50 a.m.
 */
class FileSession(channel: Channel) : Session(channel) {
    override fun messageReceived(msg: Any) {
        if (msg is FileRequest) {
            send(FileResponse(msg.indexId, msg.archiveId, msg.priority))
        }
        println("message=$msg")
    }
}