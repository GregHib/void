package rs.dusk.core.network.model.packet

/**
 * An enumeration which contains the different types of packets.
 * @since February 18, 2020
 *
 * @author Graham
 * @author Tyluur <contact@kiaira.tech>
 */
enum class PacketType {

    /**
     * A packet with no header (no opcode, length, simply the buffer)
     */
    RAW,

    /**
     * A packet where the length is known by both the client and server already.
     */
    FIXED,

    /**
     * A packet where the length is sent to its destination with it as a byte.
     */
    BYTE,

    /**
     * A packet where the length is sent to its destination with it as a short.
     */
    SHORT;

    companion object {

        const val VARIABLE_LENGTH_BYTE = -1

        const val VARIABLE_LENGTH_SHORT = -2

        fun byLength(length: Int): PacketType {
            return when (length) {
                VARIABLE_LENGTH_BYTE -> BYTE
                VARIABLE_LENGTH_SHORT -> SHORT
                else -> FIXED
            }
        }
    }
}