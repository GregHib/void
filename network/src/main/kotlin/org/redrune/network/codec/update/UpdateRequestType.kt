package org.redrune.network.codec.update

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
enum class UpdateRequestType(val opcode: Int, val priority: Boolean = false) {

    FILE_REQUEST(0),

    PRIORITY_FILE_REQUEST(1, priority = true),

    CLIENT_LOGGED_IN(2),

    CLIENT_LOGGED_OUT(3),

    ENCRYPTION(4),

    CONNECTION_INITIATED(6),

    CONNECTION_TERMINATED(7);

    companion object {

        val types = values().associateBy(UpdateRequestType::opcode)

        fun valueOf(opcode: Int): UpdateRequestType? {
            return types[opcode]
        }
    }
}