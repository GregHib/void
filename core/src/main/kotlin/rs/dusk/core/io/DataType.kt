package rs.dusk.core.io

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
enum class DataType(val byteCount: Int) {
    BYTE(1),
    SHORT(2),
    MEDIUM(3),
    INT(4),
    LONG(8);
}