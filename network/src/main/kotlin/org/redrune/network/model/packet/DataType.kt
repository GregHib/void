package org.redrune.network.model.packet

/**
 * The type of data that will be requested
 */
enum class DataType(val length: Int) {
    BYTE(1),
    SHORT(2),
    MEDIUM(3),
    INT(4),
    LONG(8);
}