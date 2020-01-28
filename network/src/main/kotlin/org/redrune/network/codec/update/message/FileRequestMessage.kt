package org.redrune.network.codec.update.message

import org.redrune.network.codec.handshake.request.HandshakeRequestType
import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:51 a.m.
 */
data class FileRequest(val indexId: Int, val archiveId: Int, val priority: Boolean) : Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:23 a.m.
 */
enum class FileRequestType(val opcode: Int, val priority: Boolean = false) {

    FILE_REQUEST(0),

    PRIORITY_FILE_REQUEST(1, priority = true),

    CLIENT_LOGGED_IN(2),

    CLIENT_LOGGED_OUT(3),

    ENCRYPTION(4),

    CONNECTION_INITIATED(6),

    CONNECTION_TERMINATED(7);

    companion object {
        val types = values().associateBy(FileRequestType::opcode)

        fun valueOf(opcode: Int): FileRequestType? {
            return types[opcode]
        }
    }
}