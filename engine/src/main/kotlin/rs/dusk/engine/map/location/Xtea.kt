package rs.dusk.engine.map.location

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 16, 2020
 */
inline class Xtea(val keys: IntArray) {
    companion object {
        operator fun invoke(init: (Int) -> Int) = Xtea(IntArray(4, init))
    }
}