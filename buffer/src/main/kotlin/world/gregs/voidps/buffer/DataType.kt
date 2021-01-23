package world.gregs.voidps.buffer

/**
 * @author GregHib <greg@gregs.world>
 * @since February 18, 2020
 */
enum class DataType(val byteCount: Int) {
    BYTE(1),
    SHORT(2),
    MEDIUM(3),
    INT(4),
    LONG(8);
}