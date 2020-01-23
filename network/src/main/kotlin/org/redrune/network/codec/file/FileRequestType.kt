package org.redrune.network.codec.file

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:23 a.m.
 */
// TODO: document this
enum class FileRequestType(val opcode: Int, val priority: Boolean = false) {

    FILE_REQUEST(0),

    PRIORITY_FILE_REQUEST(1, priority = true),

    CLIENT_LOGGED_IN(2),

    CLIENT_LOGGED_OUT(3),

    ENCRYPTION(4),

    CONNECTION_INITIATED(6),

    CONNECTION_TERMINATED(7)
}