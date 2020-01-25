package org.redrune.network.codec.update.handler

import org.redrune.cache.Cache
import org.redrune.network.Session
import org.redrune.network.codec.update.UpdateMessageHandler
import org.redrune.network.codec.update.message.FileRequest

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 8:38 p.m.
 */
class FileRequestHandler : UpdateMessageHandler<FileRequest>() {
    override fun handle(session: Session, msg: FileRequest) {
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
        session.send(msg)
    }
}