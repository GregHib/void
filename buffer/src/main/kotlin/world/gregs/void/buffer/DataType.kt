package world.gregs.void.buffer

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since February 18, 2020
 */
enum class DataType(val byteCount: Int) {
    BYTE(1),
    SHORT(2),
    MEDIUM(3),
    INT(4),
    LONG(8);
}