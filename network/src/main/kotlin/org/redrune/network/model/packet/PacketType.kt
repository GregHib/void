package org.redrune.network.model.packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
enum class PacketType(val id: Int) {

    // TODO see if to include this
    RAW(-4),

    // TODO see how to merge this with bylength
    FIXED(0), BYTE(-1), SHORT(-2), INT(-3);

    companion object {
        fun byLength(length: Int) : PacketType {
            return when {
                length >= 0 -> FIXED
                length == -1 -> BYTE
                length == -2 -> SHORT
                length == -3 -> INT
                else -> throw IllegalStateException("Packet length $length was unexpected")
            }
        }
    }
}