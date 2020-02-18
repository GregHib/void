package org.redrune.network.model.packet

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
enum class PacketType(val id: Int) {

    FIXED(0),
    BYTE(-1),
    SHORT(-2),
    INT(-3),
    RAW(-4);

    companion object {

        val types = values().associateBy(PacketType::id)

        fun valueOf(length: Int): PacketType {
            if (length >= 0) return PacketType.FIXED
            return types[length] ?: throw IllegalStateException("Unable to identify packet type by [length=$length]")
        }

    }
}