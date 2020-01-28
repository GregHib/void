package org.redrune.network.codec.update.handler

import org.redrune.cache.Cache
import org.redrune.network.Session
import org.redrune.network.codec.update.UpdateMessageHandler
import org.redrune.network.codec.update.message.FileRequestMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:38 p.m.
 */
class FileRequestHandler : UpdateMessageHandler<FileRequestMessage>() {
    override fun handle(session: Session, msg: FileRequestMessage) {
        val (index, archive, priority) = msg
        if (!Cache.valid(index, archive)) return
        session.send(msg)
    }
}