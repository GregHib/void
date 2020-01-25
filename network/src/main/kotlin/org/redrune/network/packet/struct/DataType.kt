package org.redrune.network.packet.struct

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 2:35 a.m.
 */
enum class DataType(val bytes: Int) {
    BYTE(1), SHORT(2), MEDIUM(3), INT(4), LONG(8)
}
